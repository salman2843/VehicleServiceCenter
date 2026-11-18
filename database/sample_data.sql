-- =====================================================================
-- Vehicle Service Center Management System - sample data
-- Target: MySQL 8.0+ (run after database/schema.sql)
--
-- Purpose: demo rows so a teammate can open the tables and see a realistic
-- workshop, ready for screenshots and the project report.
--
-- Run once:
--     mysql -u root -p < database/sample_data.sql
--
-- Notes:
--  * Ids are written explicitly so joins and screenshots are reproducible.
--  * Running it twice stops at the first duplicate key and changes nothing,
--    which is the safe behaviour for a demo file.
--  * To start completely fresh: DROP DATABASE vehicle_service_center;
--    then run schema.sql and this file again.
-- =====================================================================

USE vehicle_service_center;

-- ---------------------------------------------------------------------
-- customers
-- ---------------------------------------------------------------------
INSERT INTO customers (id, name, phone, email, address) VALUES
    (1, 'Asha Rao',     '9876543210', 'asha@example.com',  'Jayanagar, Bengaluru'),
    (2, 'Imran Khan',   '9812345678', 'imran@example.com', 'Andheri East, Mumbai'),
    (3, 'Priya Menon',  '9900112233', 'priya@example.com', 'Kakkanad, Kochi'),
    (4, 'Ravi Shankar', '9765432109', 'ravi@example.com',  'Kothrud, Pune'),
    (5, 'Neha Gupta',   '9700111222', 'neha@example.com',  'Dwarka, New Delhi');

-- ---------------------------------------------------------------------
-- vehicles (vehicle_type decides the Java subclass: Car / Motorcycle)
-- ---------------------------------------------------------------------
INSERT INTO vehicles (id, customer_id, registration_no, vehicle_type, make, model_name,
                      model_year, number_of_doors, engine_capacity_cc) VALUES
    (1, 1, 'KA01AB1234', 'CAR',        'Maruti',         'Swift',            2021, 5,    NULL),
    (2, 2, 'MH02CD5678', 'CAR',        'Hyundai',        'Creta',            2022, 5,    NULL),
    (3, 3, 'KL07EF9012', 'MOTORCYCLE', 'Royal Enfield',  'Classic 350',      2020, NULL, 350),
    (4, 4, 'MH12GH3456', 'CAR',        'Tata',           'Nexon',            2023, 5,    NULL),
    (5, 5, 'DL08IJ7890', 'MOTORCYCLE', 'Honda',          'Activa 6G',        2021, NULL, 110);

-- ---------------------------------------------------------------------
-- mechanics (one inactive, to show the active filter)
-- ---------------------------------------------------------------------
INSERT INTO mechanics (id, name, phone, specialization, active) VALUES
    (1, 'Ravi Kumar',   '9000000001', 'Engine and transmission', 1),
    (2, 'Suresh Patil', '9000000002', 'Electrical',              1),
    (3, 'Meena Joshi',  '9000000003', 'Bodywork and painting',   0);

-- ---------------------------------------------------------------------
-- service catalogue (labour charges)
-- ---------------------------------------------------------------------
INSERT INTO services (id, name, details, base_price) VALUES
    (1, 'General service',        'Inspection, cleaning and basic adjustments', 850.00),
    (2, 'Oil change',             'Engine oil and filter change',               450.00),
    (3, 'Wheel alignment',        'Front and rear alignment',                   600.00),
    (4, 'Brake pad replacement',  'Front brake pads, includes labour',          700.00),
    (5, 'AC service',             'Gas top-up and cooling check',              1200.00);

-- ---------------------------------------------------------------------
-- spare parts (AIR-004 is low, CHN-005 is out of stock)
-- ---------------------------------------------------------------------
INSERT INTO spare_parts (id, part_number, name, unit_price, stock_qty, reorder_level) VALUES
    (1, 'FLT-001', 'Oil filter',          250.00, 19, 5),
    (2, 'BRK-002', 'Brake pad set',       450.00, 11, 4),
    (3, 'SPK-003', 'Spark plug',          180.00, 30, 10),
    (4, 'AIR-004', 'Air filter',          320.00,  6, 6),
    (5, 'CHN-005', 'Chain lubricant',     150.00,  0, 3);

-- ---------------------------------------------------------------------
-- job cards in every status, so each screen has something to show
-- ---------------------------------------------------------------------
INSERT INTO job_cards (id, vehicle_id, mechanic_id, complaint, status, received_on, completed_on) VALUES
    (1, 1, 1,    'Engine noise at cold start, oil change requested', 'COMPLETED',
        NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 1 DAY),
    (2, 3, 2,    'Headlight not working', 'IN_PROGRESS',
        NOW() - INTERVAL 1 DAY, NULL),
    (3, 2, NULL, 'AC not cooling properly', 'RECEIVED',
        NOW() - INTERVAL 4 HOUR, NULL),
    (4, 4, 1,    'Brake pads worn out, grinding sound', 'DELIVERED',
        NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 9 DAY);

-- ---------------------------------------------------------------------
-- lines of those job cards (agreed_price / unit_price are frozen prices)
-- ---------------------------------------------------------------------
INSERT INTO job_services (job_card_id, service_id, agreed_price) VALUES
    (1, 1, 850.00),   -- General service
    (1, 2, 450.00),   -- Oil change
    (4, 4, 700.00);   -- Brake pad replacement

INSERT INTO job_parts (job_card_id, part_id, quantity, unit_price) VALUES
    (1, 1, 1, 250.00),   -- Oil filter
    (4, 2, 1, 450.00);   -- Brake pad set

-- ---------------------------------------------------------------------
-- invoices: job 1 partly paid, job 4 fully paid
--   1: 1300 labour + 250 parts - 50 discount = 1500, paid 1000
--   4:  700 labour + 450 parts              = 1150, paid 1150
-- ---------------------------------------------------------------------
INSERT INTO invoices (id, job_card_id, labor_total, parts_total, discount,
                      tax_amount, total, payment_status, created_on) VALUES
    (1, 1, 1300.00, 250.00, 50.00, 0.00, 1500.00, 'PARTIALLY_PAID',
        NOW() - INTERVAL 1 DAY),
    (2, 4,  700.00, 450.00,  0.00, 0.00, 1150.00, 'PAID',
        NOW() - INTERVAL 9 DAY);

INSERT INTO payments (invoice_id, amount, method, reference, paid_on) VALUES
    (1, 1000.00, 'CASH', NULL,            NOW() - INTERVAL 1 DAY),
    (2, 1150.00, 'UPI',  'UPI-8820011',   NOW() - INTERVAL 9 DAY);

-- ---------------------------------------------------------------------
-- quick check after loading
-- ---------------------------------------------------------------------
SELECT 'customers' AS table_name, COUNT(*) AS rows_loaded FROM customers
UNION ALL SELECT 'vehicles',    COUNT(*) FROM vehicles
UNION ALL SELECT 'mechanics',   COUNT(*) FROM mechanics
UNION ALL SELECT 'services',    COUNT(*) FROM services
UNION ALL SELECT 'spare_parts', COUNT(*) FROM spare_parts
UNION ALL SELECT 'job_cards',   COUNT(*) FROM job_cards
UNION ALL SELECT 'job_services',COUNT(*) FROM job_services
UNION ALL SELECT 'job_parts',   COUNT(*) FROM job_parts
UNION ALL SELECT 'invoices',    COUNT(*) FROM invoices
UNION ALL SELECT 'payments',    COUNT(*) FROM payments;
