# Database design

**Status: designed, not yet executed.** MySQL is not installed on the
development machine yet, so `database/schema.sql` has been written but never
run. Expect one small "fix the script" round after the first execution.

| File | Purpose |
| --- | --- |
| `database/schema.sql` | Creates the database and all tables (MySQL 8.0+, InnoDB, utf8mb4) |
| `database/sample_data.sql` | Demo data — to be added with the database step |
| `docs/database_design.md` | This document: tables, keys, rules |
| `docs/er_diagram.md` | ER diagram hand-off (to be drawn by a team member) |

How to create it later:

```bash
mysql -u root -p < database/schema.sql
```

---

## 1. Overview

One database, `vehicle_service_center`, with ten tables:

```
customers ──< vehicles ──< job_cards ──< job_services >── services
                              │
                              ├──< job_parts >── spare_parts
                              │
                              ├──  invoices ──< payments
                              │
                              └──  mechanics (assigned mechanic)
```

`A ──< B` means one A has many B; `>──` means B points at A.

---

## 2. Tables

### 2.1 `customers`
| Column | Type | Notes |
| --- | --- | --- |
| `id` | `INT AUTO_INCREMENT` | Primary key |
| `name` | `VARCHAR(80) NOT NULL` | 2–80 characters (checked in Java) |
| `phone` | `VARCHAR(15) NOT NULL` | Indexed for the "find customer" search |
| `email` | `VARCHAR(120)` | Optional |
| `address` | `VARCHAR(255)` | Optional |
| `created_on` | `DATETIME` | Defaults to now |

### 2.2 `vehicles`
| Column | Type | Notes |
| --- | --- | --- |
| `id` | `INT AUTO_INCREMENT` | Primary key |
| `customer_id` | `INT NOT NULL` | FK → `customers.id`, `ON DELETE RESTRICT` |
| `registration_no` | `VARCHAR(16) NOT NULL` | **UNIQUE** — no two vehicles share a plate |
| `vehicle_type` | `ENUM('CAR','MOTORCYCLE')` | Decides which Java subclass to build |
| `make`, `model_name` | `VARCHAR(50) NOT NULL` | e.g. Maruti / Swift |
| `model_year` | `INT` | Optional |
| `number_of_doors` | `INT` | **Car only**, `NULL` for motorcycles |
| `engine_capacity_cc` | `INT` | **Motorcycle only**, `NULL` for cars |

**Why one table and not two?** `Vehicle` is an abstract Java class with `Car`
and `Motorcycle` subclasses. Storing both in one table (single-table
inheritance) keeps the job-card workflow simple: every job points at
`vehicles.id`, no matter the vehicle kind. The DAO reads `vehicle_type` and
returns `new Car(...)` or `new Motorcycle(...)`.

### 2.3 `mechanics`
| Column | Type | Notes |
| --- | --- | --- |
| `id` | `INT AUTO_INCREMENT` | Primary key |
| `name` | `VARCHAR(80) NOT NULL` | |
| `phone`, `specialization` | `VARCHAR` | Optional |
| `active` | `TINYINT(1)` | Inactive mechanics are hidden from the assign dropdown |

### 2.4 `services` (catalogue)
| Column | Type | Notes |
| --- | --- | --- |
| `id` | `INT AUTO_INCREMENT` | Primary key |
| `name` | `VARCHAR(100) NOT NULL` | e.g. "Oil change" |
| `details` | `VARCHAR(255)` | Optional description |
| `base_price` | `DECIMAL(10,2) NOT NULL` | Must be ≥ 0 |

### 2.5 `spare_parts` (inventory)
| Column | Type | Notes |
| --- | --- | --- |
| `id` | `INT AUTO_INCREMENT` | Primary key |
| `part_number` | `VARCHAR(30) NOT NULL` | **UNIQUE** |
| `name` | `VARCHAR(100) NOT NULL` | |
| `unit_price` | `DECIMAL(10,2) NOT NULL` | ≥ 0 |
| `stock_qty` | `INT NOT NULL DEFAULT 0` | **`CHECK (stock_qty >= 0)`** |
| `reorder_level` | `INT NOT NULL DEFAULT 0` | Low-stock warning when `stock_qty <= reorder_level` |

### 2.6 `job_cards`
| Column | Type | Notes |
| --- | --- | --- |
| `id` | `INT AUTO_INCREMENT` | Primary key |
| `vehicle_id` | `INT NOT NULL` | FK → `vehicles.id` |
| `mechanic_id` | `INT NULL` | FK → `mechanics.id`, `ON DELETE SET NULL` (job survives if a mechanic leaves) |
| `complaint` | `TEXT NOT NULL` | What the customer reported |
| `status` | `ENUM('RECEIVED','IN_PROGRESS','COMPLETED','DELIVERED')` | Defaults to `RECEIVED` |
| `received_on` | `DATETIME` | Defaults to now |
| `completed_on` | `DATETIME NULL` | Set when the job reaches `COMPLETED` |

The enum values match `com.vsc.model.JobStatus` exactly. Forward-only movement
is enforced in the **service layer**, not in SQL, so the rule can be unit
tested and the error message can be friendly.

### 2.7 `job_services` (job ↔ service)
| Column | Type | Notes |
| --- | --- | --- |
| `id` | `INT AUTO_INCREMENT` | Primary key |
| `job_card_id` | `INT NOT NULL` | FK → `job_cards.id`, `ON DELETE CASCADE` |
| `service_id` | `INT NOT NULL` | FK → `services.id`, `ON DELETE RESTRICT` |
| `agreed_price` | `DECIMAL(10,2) NOT NULL` | **Price snapshot** taken from `services.base_price` when added |

### 2.8 `job_parts` (job ↔ part, with quantity)
| Column | Type | Notes |
| --- | --- | --- |
| `id` | `INT AUTO_INCREMENT` | Primary key |
| `job_card_id` | `INT NOT NULL` | FK → `job_cards.id`, `ON DELETE CASCADE` |
| `part_id` | `INT NOT NULL` | FK → `spare_parts.id`, `ON DELETE RESTRICT` |
| `quantity` | `INT NOT NULL` | `CHECK (quantity > 0)` |
| `unit_price` | `DECIMAL(10,2) NOT NULL` | **Price snapshot**, same reason as `agreed_price` |

Adding a part here is the moment stock is deducted. Stock deduction and the
insert must share one JDBC transaction (`setAutoCommit(false)`).

### 2.9 `invoices`
| Column | Type | Notes |
| --- | --- | --- |
| `id` | `INT AUTO_INCREMENT` | Primary key |
| `job_card_id` | `INT NOT NULL` | FK → `job_cards.id`, **UNIQUE** = one invoice per job |
| `labor_total` | `DECIMAL(10,2)` | Sum of `job_services.agreed_price` |
| `parts_total` | `DECIMAL(10,2)` | Sum of `job_parts.quantity * unit_price` |
| `discount` | `DECIMAL(10,2)` | 0 when none |
| `tax_amount` | `DECIMAL(10,2)` | 0 unless tax is configured |
| `total` | `DECIMAL(10,2)` | `labor + parts - discount + tax` |
| `payment_status` | `ENUM('UNPAID','PARTIALLY_PAID','PAID')` | Derived from `payments` |
| `created_on` | `DATETIME` | Defaults to now |

Totals are calculated in the **service layer** with `BigDecimal` and then stored,
so an old invoice never changes when catalogue prices change.

### 2.10 `payments`
| Column | Type | Notes |
| --- | --- | --- |
| `id` | `INT AUTO_INCREMENT` | Primary key |
| `invoice_id` | `INT NOT NULL` | FK → `invoices.id`, `ON DELETE CASCADE` |
| `amount` | `DECIMAL(10,2) NOT NULL` | `CHECK (amount > 0)` |
| `method` | `ENUM('CASH','UPI','CARD')` | Matches `com.vsc.model.PaymentMethod` |
| `reference` | `VARCHAR(60)` | UPI transaction id / card approval code, optional |
| `paid_on` | `DATETIME` | Defaults to now |

Rules applied in the service layer:
- sum of payments cannot exceed `invoices.total`;
- 0 paid → `UNPAID`, less than total → `PARTIALLY_PAID`, equal → `PAID`.

---

## 3. Relationships (cardinality)

| Parent | Child | Cardinality | Delete rule |
| --- | --- | --- | --- |
| `customers` | `vehicles` | 1 → many | RESTRICT |
| `vehicles` | `job_cards` | 1 → many | RESTRICT |
| `mechanics` | `job_cards` | 1 → many (optional) | SET NULL |
| `job_cards` | `job_services` | 1 → many | CASCADE |
| `job_cards` | `job_parts` | 1 → many | CASCADE |
| `job_cards` | `invoices` | 1 → 1 (UNIQUE) | RESTRICT |
| `invoices` | `payments` | 1 → many | CASCADE |
| `services` | `job_services` | 1 → many | RESTRICT |
| `spare_parts` | `job_parts` | 1 → many | RESTRICT |

---

## 4. Conventions used everywhere

- **Money** is `DECIMAL(10,2)` in SQL and `BigDecimal` in Java — never
  `FLOAT`/`DOUBLE`.
- **Names** are `snake_case` in SQL, matching camelCase Java fields
  (`registration_no` ↔ `registrationNo`).
- **Primary keys** are surrogate `INT AUTO_INCREMENT` ids.
- **Foreign keys** are real constraints, so broken links cannot be inserted.
- **History is frozen**: `agreed_price` / `unit_price` store the price that was
  charged, not a pointer to today's catalogue price.
- **Charset** is `utf8mb4` so names in Indian languages and emoji-safe text work.
- **Deletes** are restricted for referenced master data (a customer with
  vehicles cannot be deleted); job lines disappear with their job card.

---

## 5. Mapping to the Java model

| Table | Java class (`com.vsc.model`) |
| --- | --- |
| `customers` | `Customer` |
| `vehicles` | `Vehicle` / `Car` / `Motorcycle` |
| `mechanics` | `Mechanic` |
| `services` | `ServiceItem` (implements `Billable`) |
| `spare_parts` | `SparePart` (implements `Billable`) |
| `job_cards` | `JobCard` + `JobStatus` |
| `job_services`, `job_parts` | job line rows (no model class; read into invoice lines) |
| `invoices` | `Invoice` + `PaymentStatus` |
| `payments` | `Payment` + `PaymentMethod` |

---

## 6. Not done yet

- [ ] Run `schema.sql` on a real MySQL 8 installation and fix any errors.
- [ ] Add `database/sample_data.sql` (a few customers, vehicles, parts, services).
- [ ] Add `mysql-connector-j` to `pom.xml` and implement `util/DBConnection`.
- [ ] Replace the "not designed yet" note in this document's status line once
      the script has been executed successfully.
