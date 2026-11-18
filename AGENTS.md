# AGENTS.md — Vehicle Service Center Management System

This file tells AI coding agents (Codex, etc.) and developers how this project is organized and how to work in it. Read it fully before changing any code.

## 1. Project Summary

A Java Swing desktop application that helps a small vehicle workshop manage customers, vehicles, service visits (job cards), mechanics, spare-part inventory, invoices, and payments in one connected workflow.

This is an MCA Java / OOP project. Code must stay readable and explainable, because everyone working on it has to be able to understand and present it.

## 1.1 Current state of the project (last updated: 2026-11-18)

Version **0.3.0** — *Backend ready*. Read this table before planning any change;
it says what already exists, so an agent does not rebuild or overwrite it.

| Area | State | Where |
| ---- | ----- | ----- |
| Entry point | done | `com.vsc.Main` → creates `ui.MainFrame` on the EDT |
| Main window | done | `ui/MainFrame` — header, left navigation, `CardLayout` screens, status bar, File/View/Help menus |
| Screens | placeholder only — **next step** | `ui/DashboardPanel` + `CustomerPanel`, `VehiclePanel`, `JobCardPanel`, `InventoryPanel`, `InvoicePanel`, `MechanicPanel` (all extend `ui.PlaceholderPanel`) |
| Styling | done | `ui.UiTheme` (colours, label factories, `card()`), `util.AppInfo` (name/version/stage) |
| Model | done | `Customer`, `Vehicle` (abstract) + `Car`/`Motorcycle`, `Mechanic`, `ServiceItem`, `SparePart`, `JobCard`, `Invoice`, `Payment`, `JobServiceLine`, `JobPartLine`, `Billable`, enums `JobStatus` (has `next()`/`canMoveTo()`), `PaymentMethod`, `PaymentStatus` |
| Shared helpers | done | `InputValidator`, `Money` (`BigDecimal`), `AppInfo`, `DBConnection` |
| Exceptions | done | `ValidationException`, `InvalidStatusTransitionException`, `InsufficientStockException` |
| Database | **executed** on MySQL 8.4.13 | `database/schema.sql` (10 tables), `docs/database_design.md`, ER brief in `docs/er_diagram.md` |
| `dao` package | done | 10 DAOs: `CustomerDao`, `VehicleDao`, `MechanicDao`, `ServiceItemDao`, `SparePartDao`, `JobCardDao`, `JobServiceDao`, `JobPartDao`, `InvoiceDao`, `PaymentDao` |
| `service` package | done | `CustomerService`, `VehicleService`, `MechanicService`, `ServiceCatalogService`, `InventoryService`, `JobCardService`, `BillingService` |
| `pom.xml` | `mysql-connector-j` 9.7.0 added (approved with the backend step) | no `exec-maven-plugin` yet |
| Tests | **31 passing** | `AppSmokeTest`, `UiScreensTest`, `JobStatusTest`, `BillingCalculationTest`, `StockRuleTest`, `ServiceValidationTest`, `dao.WorkflowIntegrationTest` |

Known environment facts on this machine:

- JDK 21 is installed; **Maven is still not on `PATH`** (a portable Maven is used
  for verification) and VS Code's Java extension runs the app.
- **MySQL 8.4.13 is installed and running** as Windows service `MySQL84`; the
  database `vehicle_service_center` exists with all 10 tables. `mysql.exe` is not
  on `PATH` (`C:\Program Files\MySQL\MySQL Server 8.4\bin`).
- Credentials live only in `src/main/resources/application.properties`
  (git-ignored). Never hard-code or commit them.

Rules that follow from this state:

1. The UI must keep working **without a database**: no startup connection in
   `Main`; a screen opens its connection only when the user acts.
2. Replace a placeholder screen by editing its own class; do not put two screens
   in one file and do not bypass `MainFrame.addScreen(...)` when adding one.
3. Update this section, `README.md` "Current status", and `AppInfo.VERSION`
   whenever a major piece lands (a real screen, a new rule, a schema change).
4. **Screens call services, never DAOs or `Connection`.** DAO methods take the
   `Connection` from the service so multi-step rules stay in one transaction.
5. `dao.WorkflowIntegrationTest` is the reference for the whole workflow; keep it
   passing (it skips itself when MySQL is unavailable) and extend it when a new
   rule is added.

## 2. Tech Stack

| Area            | Choice                                             |
| --------------- | -------------------------------------------------- |
| Language        | Java 21 (JDK 21)                                   |
| UI              | Java Swing (AWT/Swing, layout managers, listeners) |
| Database        | MySQL accessed through JDBC                        |
| Build           | Maven                                              |
| Tests           | JUnit 5                                            |
| Version control | Git                                                |
| IDE             | IntelliJ IDEA                                      |

Do not introduce other frameworks (Spring, Hibernate/JPA, JavaFX, Lombok, web frontends) unless the team explicitly agrees.

## 3. Core Workflow the App Supports

1. Register or find a customer and their vehicle.
2. Create a job card with the complaint and requested work.
3. Add services, assign a mechanic, record parts used (stock is deducted).
4. Track status: `RECEIVED → IN_PROGRESS → COMPLETED → DELIVERED`.
5. Generate an invoice from the job's saved service and part lines.
6. Record payments (cash / UPI / card); partial payments are allowed.
7. Keep job and invoice history so a vehicle's service history can be retrieved later.

Every feature should fit into this flow. Do not build disconnected screens.

## 4. Folder Structure

```
VehicleServiceCenter/
├── pom.xml
├── README.md
├── AGENTS.md
├── .gitignore
├── database/
│   ├── README.md
│   ├── schema.sql                            (executed on MySQL 8.4.13)
│   └── sample_data.sql                       (to be added)
├── docs/
│   ├── project_proposal.md
│   ├── database_design.md
│   ├── er_diagram.md                         (hand-off brief for a team member)
│   └── screenshots/
├── target/                                   (build output, generated, git-ignored)
└── src/
    ├── main/
    │   ├── java/com/vsc/
    │   │   ├── Main.java
    │   │   ├── model/
    │   │   ├── dao/                          10 DAOs (SQL only, Connection passed in)
    │   │   ├── service/                      7 services (validation + transactions)
    │   │   ├── ui/                           MainFrame, UiTheme, PlaceholderPanel + one panel per screen
    │   │   ├── util/                         DBConnection, AppInfo, InputValidator, Money
    │   │   └── exception/
    │   └── resources/
    │       ├── application.properties.example
    │       └── application.properties        (local only, git-ignored)
    └── test/java/com/vsc/                    model/service rule tests + dao.WorkflowIntegrationTest
```

### Package responsibilities

| Package     | Responsibility                                                                                                                                                               | Must NOT contain            |
| ----------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------- |
| `model`     | Entities: `Customer`, `Vehicle` (abstract), `Car`, `Motorcycle`, `Mechanic`, `ServiceItem`, `SparePart`, `JobCard`, `Invoice`, `Payment`, plus interfaces such as `Billable` | SQL, Swing code             |
| `dao`       | All SQL and JDBC: CRUD, ResultSet-to-object mapping. Use `PreparedStatement` only                                                                                            | Business rules, Swing code  |
| `service`   | Business rules: status transitions, stock validation, invoice calculation, payment handling, transactions                                                                    | Swing code, raw SQL strings |
| `ui`        | Swing frames, panels, dialogs, tables, event listeners, navigation                                                                                                           | SQL, business calculations  |
| `util`      | Shared helpers: `DBConnection`, `InputValidator`, formatting helpers                                                                                                         | Feature-specific logic      |
| `exception` | Custom exceptions such as `InsufficientStockException`, `InvalidStatusTransitionException`, `ValidationException`                                                            | —                           |

### Layer flow (strict)

```
Swing panel (ui)  →  service class  →  DAO class  →  MySQL
```

- UI calls services only, never DAOs or SQL directly.
- Services call DAOs and apply business rules.
- DAOs talk to the database only.
- Models are plain data/behavior objects shared by all layers.
- Never put SQL or invoice/stock calculations inside a button's event handler.

## 5. Database Rules

Schema lives in `database/schema.sql`; sample data in `database/sample_data.sql`. Any schema change must update `schema.sql` and `docs/database_design.md` in the same commit.

Core tables: `customers`, `vehicles`, `mechanics`, `services`, `spare_parts`, `job_cards`, `job_services`, `job_parts`, `invoices`, `payments`, and optionally `users`.

Relationships:

- One customer → many vehicles.
- One vehicle → many job cards over time.
- A job card → many services (`job_services`) and many parts (`job_parts`), and one assigned mechanic.
- One invoice (per job) → many payments (supports partial payment).

Rules:

- Use `DECIMAL` (and `BigDecimal` in Java) for all money. Never `float`/`double`.
- Use `PreparedStatement` for every query. No string-concatenated SQL.
- Use foreign keys for referential integrity.
- Store the agreed price on `job_services.agreed_price` and `job_parts.unit_price` so later catalog price changes never rewrite old bills.
- Use a JDBC transaction (`setAutoCommit(false)`, commit/rollback) whenever stock deduction and job/billing changes must succeed or fail together.
- Database credentials come from `application.properties` (local, git-ignored). Never hard-code or commit credentials.

## 6. Business Rules

- Job status moves forward only: `RECEIVED → IN_PROGRESS → COMPLETED → DELIVERED`. Reject invalid transitions with a custom exception.
- Stock cannot go negative. If requested quantity exceeds stock, throw `InsufficientStockException`.
- Show a low-stock warning when `stock_qty <= reorder_level`.
- Invoice: labor total + parts total = subtotal; subtotal − discount (+ tax if configured) = total.
- Payment status is derived from payments: `UNPAID`, `PARTIALLY_PAID`, `PAID`.
- Payment methods: `CASH`, `UPI`, `CARD`.
- A job should be `COMPLETED` before an invoice is generated, and `DELIVERED` only after the vehicle is handed over.

## 7. Java / OOP Conventions (syllabus-relevant, keep them natural)

- `Vehicle` is an abstract base class; `Car` and `Motorcycle` extend it and override `getVehicleType()`.
- `Billable` interface defines how an item contributes a charge; chargeable items implement it.
- Use constructor overloading where it makes sense (required vs. optional fields).
- Use custom exceptions with `try/catch/finally` where appropriate.
- Use `SwingWorker` (or a background thread) only where it truly keeps the UI responsive, e.g. loading reports. Do not add threads just to claim coverage.
- Use Swing layout managers (`BorderLayout`, `GridBagLayout`, etc.), `JTable` with table models, and `ActionListener`s for events.

## 8. Code Style

- Package root: `com.vsc`. Classes `PascalCase`, methods/variables `camelCase`, constants `UPPER_SNAKE_CASE`, DB tables/columns `snake_case`.
- One public class per file. Keep methods short and single-purpose.
- Add brief Javadoc on public classes and non-obvious methods; comment the "why", not the obvious.
- Validate user input in `InputValidator` / service layer, and show friendly dialogs for errors. Never show raw stack traces to the user.
- Close JDBC resources with try-with-resources.
- No unused code, no commented-out blocks, no dead imports.

## 9. Working Rules for AI Agents

1. Read this file and the existing code in the relevant package before editing.
2. Make small, focused changes: one feature or fix at a time.
3. Respect the layer flow in section 4. If a change would break it, fix the design instead.
4. Do not rename, move, or delete existing packages/classes or change shared files (`pom.xml`, `schema.sql`, `DBConnection`, `Main`) unless the task requires it. When you do, say so clearly.
5. Do not add new dependencies without asking.
6. Update `schema.sql` and `docs/database_design.md` whenever tables change.
7. Add or update JUnit tests for service-layer logic (invoice calculation, stock checks, status transitions).
8. Before finishing: build with `mvn clean compile` and run `mvn test`. Report anything you could not verify.
9. Explain what you changed in plain language so the developer can understand and present it.
10. Never commit secrets, local config, build output, or IDE files.
11. After any major change, refresh the project documentation in the same change:
    - `README.md` → "Current status" table, the window/screens description, and the layout tree.
    - `AGENTS.md` → section 1.1 state table.
    - `util.AppInfo.VERSION` / `BUILD_STAGE`.
    The README is what teammates and teachers read first, so it must never describe an older version than the code.
12. The application must stay launchable without a database: no startup query in `Main`. MySQL is reached only when a screen calls a service.

## 10. Build and Run

```bash
mvn clean compile        # compile
mvn test                 # run tests
mvn exec:java            # run the app (main class: com.vsc.Main)
```

Current machine note: **Maven is not on `PATH`** yet, so `mvn` commands do not
run here. Use VS Code's **Run** button on `src/main/java/com/vsc/Main.java`
(the Java extension compiles to `bin/` or `target/` for you). Once Maven is
installed, the three commands above work as written. `mvn exec:java` needs the
`exec-maven-plugin`, which is not in `pom.xml` yet — ask before adding it.

`mvn test` runs 31 tests. `dao.WorkflowIntegrationTest` needs MySQL;
without a reachable server it is **skipped** (JUnit assumption), not failed, so
the suite still passes on a teammate's machine.

`target/` is Maven's generated build output (compiled `.class` files, test
reports). It is git-ignored: never edit it, never commit it, and delete it
freely with `mvn clean`.

Local setup:

1. Install JDK 21, Maven, and MySQL.
2. Create the database and tables: run `database/schema.sql`, then optionally `database/sample_data.sql`.
3. Copy `src/main/resources/application.properties.example` to `application.properties` and fill in your MySQL URL, username, and password.

## 11. Git Rules

- Work on a feature branch, not directly on `main`: `feature/<short-name>`, `fix/<short-name>`.
- Make small commits with clear messages, e.g. `Add customer DAO and service`.
- Pull/rebase from `main` before merging, and make sure the project compiles and tests pass.
- Resolve conflicts carefully, especially in `schema.sql` and `pom.xml`.
- Review every AI-generated diff before committing.

## 12. Out of Scope for Version 1

Online payments, GPS tracking, complex accounting, multi-branch support. Optional later: login roles (Admin/Receptionist), dashboard, search/filter, reports, appointment scheduling, service reminders, audit log, PDF invoice export.

## 13. Definition of Done for the First Version

A receptionist can register a customer and vehicle, create a job card, add services and parts, assign a mechanic, complete the job, generate a correct invoice, record payment, and later retrieve the service history. Data persists in MySQL, invalid input is handled safely, and the project can be explained end to end.
