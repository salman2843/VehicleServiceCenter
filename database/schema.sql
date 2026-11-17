-- =====================================================================
-- Vehicle Service Center Management System - database schema
-- Target: MySQL 8.0+ (InnoDB, utf8mb4)
--
-- Status: written without a local MySQL installation, so it has NOT been
-- executed yet. Run it once MySQL is installed:
--     mysql -u root -p < database/schema.sql
-- Any error found on the first run should be fixed here and mirrored in
-- docs/database_design.md in the same commit (see AGENTS.md, Database Rules).
-- =====================================================================

CREATE DATABASE IF NOT EXISTS vehicle_service_center
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE vehicle_service_center;

-- ---------------------------------------------------------------------
-- customers: a person who owns vehicles
-- ---------------------------------------------------------------------
CREATE TABLE customers (
    id          INT             NOT NULL AUTO_INCREMENT,
    name        VARCHAR(80)     NOT NULL,
    phone       VARCHAR(15)     NOT NULL,
    email       VARCHAR(120)    NULL,
    address     VARCHAR(255)    NULL,
    created_on  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_customers_phone (phone)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- vehicles: one row per vehicle.
-- Car and Motorcycle are Java subclasses, stored in this single table:
-- vehicle_type says which subclass to build, and the two nullable
-- columns hold the subclass-specific fields.
-- ---------------------------------------------------------------------
CREATE TABLE vehicles (
    id                   INT             NOT NULL AUTO_INCREMENT,
    customer_id          INT             NOT NULL,
    registration_no      VARCHAR(16)     NOT NULL,
    vehicle_type         ENUM('CAR', 'MOTORCYCLE') NOT NULL,
    make                 VARCHAR(50)     NOT NULL,
    model_name           VARCHAR(50)     NOT NULL,
    model_year           INT             NULL,
    number_of_doors      INT             NULL,   -- CAR only
    engine_capacity_cc   INT             NULL,   -- MOTORCYCLE only
    PRIMARY KEY (id),
    UNIQUE KEY uq_vehicles_registration (registration_no),
    KEY idx_vehicles_customer (customer_id),
    CONSTRAINT fk_vehicles_customer
        FOREIGN KEY (customer_id) REFERENCES customers (id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- mechanics: technicians who can be assigned to a job card
-- ---------------------------------------------------------------------
CREATE TABLE mechanics (
    id              INT             NOT NULL AUTO_INCREMENT,
    name            VARCHAR(80)     NOT NULL,
    phone           VARCHAR(15)     NULL,
    specialization  VARCHAR(80)     NULL,
    active          TINYINT(1)      NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- services: the workshop's service catalogue (labour charges)
-- ---------------------------------------------------------------------
CREATE TABLE services (
    id          INT             NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100)    NOT NULL,
    details     VARCHAR(255)    NULL,
    base_price  DECIMAL(10, 2)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT chk_services_base_price CHECK (base_price >= 0)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- spare_parts: inventory. stock_qty can never go negative.
-- ---------------------------------------------------------------------
CREATE TABLE spare_parts (
    id             INT             NOT NULL AUTO_INCREMENT,
    part_number    VARCHAR(30)     NOT NULL,
    name           VARCHAR(100)    NOT NULL,
    unit_price     DECIMAL(10, 2)  NOT NULL,
    stock_qty      INT             NOT NULL DEFAULT 0,
    reorder_level  INT             NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uq_spare_parts_number (part_number),
    CONSTRAINT chk_spare_parts_price CHECK (unit_price >= 0),
    CONSTRAINT chk_spare_parts_stock CHECK (stock_qty >= 0),
    CONSTRAINT chk_spare_parts_reorder CHECK (reorder_level >= 0)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- job_cards: one service visit for one vehicle
-- status moves forward only: RECEIVED -> IN_PROGRESS -> COMPLETED -> DELIVERED
-- ---------------------------------------------------------------------
CREATE TABLE job_cards (
    id           INT          NOT NULL AUTO_INCREMENT,
    vehicle_id   INT          NOT NULL,
    mechanic_id  INT          NULL,        -- assigned later, may be unassigned
    complaint    TEXT         NOT NULL,
    status       ENUM('RECEIVED', 'IN_PROGRESS', 'COMPLETED', 'DELIVERED')
                              NOT NULL DEFAULT 'RECEIVED',
    received_on  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_on DATETIME     NULL,
    PRIMARY KEY (id),
    KEY idx_job_cards_vehicle (vehicle_id),
    KEY idx_job_cards_mechanic (mechanic_id),
    KEY idx_job_cards_status (status),
    CONSTRAINT fk_job_cards_vehicle
        FOREIGN KEY (vehicle_id) REFERENCES vehicles (id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_job_cards_mechanic
        FOREIGN KEY (mechanic_id) REFERENCES mechanics (id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- job_services: services added to a job card.
-- agreed_price freezes the price at the time of the job, so changing the
-- catalogue later never rewrites an old bill.
-- ---------------------------------------------------------------------
CREATE TABLE job_services (
    id            INT             NOT NULL AUTO_INCREMENT,
    job_card_id   INT             NOT NULL,
    service_id    INT             NOT NULL,
    agreed_price  DECIMAL(10, 2)  NOT NULL,
    PRIMARY KEY (id),
    KEY idx_job_services_job (job_card_id),
    KEY idx_job_services_service (service_id),
    CONSTRAINT fk_job_services_job
        FOREIGN KEY (job_card_id) REFERENCES job_cards (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_job_services_service
        FOREIGN KEY (service_id) REFERENCES services (id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_job_services_price CHECK (agreed_price >= 0)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- job_parts: spare parts used on a job card (stock is deducted for these)
-- unit_price is the snapshot price, for the same reason as agreed_price.
-- ---------------------------------------------------------------------
CREATE TABLE job_parts (
    id           INT             NOT NULL AUTO_INCREMENT,
    job_card_id  INT             NOT NULL,
    part_id      INT             NOT NULL,
    quantity     INT             NOT NULL,
    unit_price   DECIMAL(10, 2)  NOT NULL,
    PRIMARY KEY (id),
    KEY idx_job_parts_job (job_card_id),
    KEY idx_job_parts_part (part_id),
    CONSTRAINT fk_job_parts_job
        FOREIGN KEY (job_card_id) REFERENCES job_cards (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_job_parts_part
        FOREIGN KEY (part_id) REFERENCES spare_parts (id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_job_parts_quantity CHECK (quantity > 0),
    CONSTRAINT chk_job_parts_price CHECK (unit_price >= 0)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- invoices: exactly one invoice per job card (UNIQUE on job_card_id).
-- payment_status is derived from the payments table and stored for fast
-- listing; the service layer is the only code allowed to change it.
-- ---------------------------------------------------------------------
CREATE TABLE invoices (
    id              INT             NOT NULL AUTO_INCREMENT,
    job_card_id     INT             NOT NULL,
    labor_total     DECIMAL(10, 2)  NOT NULL DEFAULT 0.00,
    parts_total     DECIMAL(10, 2)  NOT NULL DEFAULT 0.00,
    discount        DECIMAL(10, 2)  NOT NULL DEFAULT 0.00,
    tax_amount      DECIMAL(10, 2)  NOT NULL DEFAULT 0.00,
    total           DECIMAL(10, 2)  NOT NULL DEFAULT 0.00,
    payment_status  ENUM('UNPAID', 'PARTIALLY_PAID', 'PAID')
                                    NOT NULL DEFAULT 'UNPAID',
    created_on      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_invoices_job_card (job_card_id),
    CONSTRAINT fk_invoices_job_card
        FOREIGN KEY (job_card_id) REFERENCES job_cards (id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_invoices_amounts CHECK (
        labor_total >= 0 AND parts_total >= 0 AND discount >= 0
        AND tax_amount >= 0 AND total >= 0)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- payments: several rows per invoice are allowed (partial payments).
-- ---------------------------------------------------------------------
CREATE TABLE payments (
    id          INT             NOT NULL AUTO_INCREMENT,
    invoice_id  INT             NOT NULL,
    amount      DECIMAL(10, 2)  NOT NULL,
    method      ENUM('CASH', 'UPI', 'CARD') NOT NULL,
    reference   VARCHAR(60)     NULL,   -- UPI transaction id / card approval code
    paid_on     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_payments_invoice (invoice_id),
    CONSTRAINT fk_payments_invoice
        FOREIGN KEY (invoice_id) REFERENCES invoices (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_payments_amount CHECK (amount > 0)
) ENGINE = InnoDB;
