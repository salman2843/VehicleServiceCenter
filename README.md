# Vehicle Service Center Management System

A Java Swing desktop application for a small vehicle workshop: customers,
vehicles, job cards, mechanics, spare-part inventory, invoices and payments.
Built with Java 21, Swing, Maven, JUnit 5 and MySQL via JDBC.

This is an MCA Java / OOP project; the code is kept readable so every team
member can explain it.

## Current status

**Version 0.3.0 — backend ready (database + DAO + service layers).**

| Area | State |
| --- | --- |
| Swing window + navigation | **Done** — header, 7 screens in a side menu, status bar, File/View/Help menus |
| Feature screens | **Placeholder** — each lists what it will do and which service it will call |
| Model classes | **Done** — 12 entity classes + 3 enums |
| Shared helpers | **Done** — `InputValidator`, `Money`, `AppInfo`, `DBConnection` |
| Database | **Done and executed** — `database/schema.sql` created all 10 tables on MySQL 8.4.13 |
| DAO layer | **Done** — 10 DAO classes, `PreparedStatement` only |
| Service layer | **Done** — customers, vehicles, mechanics, catalogue, inventory, job cards, billing |
| Tests | **31 passing** (`mvn test`), including an end-to-end run against MySQL |
| Screens wired to the database | **Not yet** — this is the next step |

The next step is the UI: replace the placeholder screens one at a time, starting
with Customers (search, register, open vehicles), then Job cards and Invoices.

## Requirements

| Tool  | Version | Needed for |
| ----- | ------- | ---------- |
| JDK   | 21      | Everything |
| Maven | 3.8+    | `mvn` commands (not installed yet — use the VS Code **Run** button meanwhile) |
| MySQL | **8.4 LTS** (8.0+ works) | Saving data. Installed and running as service `MySQL84` on the development machine |

## Run it in VS Code

1. Install JDK 21 and VS Code. Confirm with `java -version` in a terminal.
2. Install the **Extension Pack for Java** (Microsoft) in VS Code.
3. `File -> Open Folder` and choose this project folder (the one with `pom.xml`).
4. Wait until the "Java: importing…" progress in the bottom-right disappears.
5. Open `src/main/java/com/vsc/Main.java` and click **Run** above `main`, or
   press `F5` to debug.
6. The *Vehicle Service Center 0.3.0* window opens on the Dashboard. Click the
   screens in the left menu (Customers, Vehicles, Job cards, Inventory,
   Invoices, Mechanics) to move around.

If **Run** does not appear: `Ctrl+Shift+P` -> `Java: Clean Java Language Server
Workspace`, then reopen the file.

## What is in the window today

```
+------------------------------------------------------+
| Vehicle Service Center 0.3.0        File View Help   |
+--------------+---------------------------------------+
| Dashboard    |                                       |
| Customers    |   <selected screen>                   |
| Vehicles     |                                       |
| Job cards    |   e.g. "Job cards" with the list of   |
| Inventory    |        features planned for it        |
| Invoices     |                                       |
| Mechanics    |                                       |
+--------------+---------------------------------------+
| Application running - backend ready, screens not ... |
+------------------------------------------------------+
```

Each screen under `src/main/java/com/vsc/ui/` is its own class, so a team
member can own one screen without touching the others. They currently extend
`PlaceholderPanel`; when a screen is wired to the service layer, that content is
replaced by the real table or form.

## Run it from a terminal

```bash
mvn clean compile        # compile everything
mvn test                 # run the JUnit 5 tests
```

`mvn exec:java` is not configured yet; it will be added together with the
database connection step.


## Understanding the build output (`target/` folder)

After you build or run the project (Maven, or the VS Code **Run** button), a
`target/` folder appears in the project root. **It is generated, not source code**
— you can delete it at any time and rebuild.

```
target/
+-- classes/                 your src/main/java compiled to .class files
|      com/vsc/Main.class
|      com/vsc/model/Customer.class   (one per .java file)
|      application.properties.example (copied from resources)
+-- test-classes/            src/test/java compiled (e.g. AppSmokeTest.class)
+-- surefire-reports/        test results ("Tests run: 31, Failures: 0")
+-- maven-status/            Maven's bookkeeping of what was compiled
+-- generated-sources/       empty for us; plugins would generate code here
```

Things every member should know:

- `.class` files are **bytecode**, not readable text. Never edit anything inside
  `target/`; edit `src/` instead.
- `target/` is listed in `.gitignore`, so it is **never pushed to GitHub**. Each
  member builds their own.
- `mvn clean` deletes the whole folder. `mvn clean test` = delete, rebuild,
  re-run tests.
- If the app behaves strangely after a change, delete `target/` (or run
  `mvn clean compile`) and run again — an old `.class` file can otherwise linger.
- Using VS Code's **Run** button instead of Maven may create a `bin/` folder
  with the same compiled output. Both `bin/` and `target/` are ignored by Git.

What to share with the team: only `src/`, `pom.xml`, `database/*.sql`, `docs/`
and the Markdown files. Everything else is either local config or build output.

## Backend: database, DAO and service layers

### One-time setup on your machine

1. Run the schema once (see [`database/README.md`](database/README.md)):
   `mysql -u root -p < database/schema.sql`
   Optionally load the demo rows: `mysql -u root -p < database/sample_data.sql`
2. Copy `src/main/resources/application.properties.example` to
   `application.properties` and put your own MySQL password in it. This file is
   git-ignored, so nobody's password reaches GitHub.
3. Run `mvn test`. The integration test drives the whole workflow against your
   database and deletes its own rows afterwards. **If MySQL is not running it is
   skipped, not failed**, so the app and the test suite still work without a
   database.

### Who does what

| Layer | Classes | Responsibility |
| --- | --- | --- |
| `dao` | `CustomerDao`, `VehicleDao`, `MechanicDao`, `ServiceItemDao`, `SparePartDao`, `JobCardDao`, `JobServiceDao`, `JobPartDao`, `InvoiceDao`, `PaymentDao` | SQL only: `PreparedStatement`, `ResultSet` to object mapping |
| `service` | `CustomerService`, `VehicleService`, `MechanicService`, `ServiceCatalogService`, `InventoryService`, `JobCardService`, `BillingService` | Validation, transactions and business rules |
| `util` | `DBConnection` | Reads `application.properties`, opens a `Connection`, hides the password in error messages |
| `ui` | the panels | Next step: call a service and show the result / an error dialog |

DAOs never create connections; the service passes one in, which is what makes a
transaction possible.

### Rules that are actually enforced by code

- **Status** moves one step only: `RECEIVED → IN_PROGRESS → COMPLETED → DELIVERED`
  (`JobStatus.canMoveTo`, `JobCardService.updateStatus`).
- **Stock** can never go negative: checked in `InventoryService.ensureAvailable`,
  and the `UPDATE ... WHERE stock_qty >= ?` in `SparePartDao.deductStock` checks
  it again so two users cannot take the same last unit.
- **Adding a part** writes the stock deduction and the job line in one
  transaction (`JobCardService.addPart`): both succeed or neither does.
- **Invoice** = labour + parts − discount (+ tax), from the prices saved on the
  job lines (`BillingService.calculate`, unit tested).
- **Payment status** is derived, never typed in: `UNPAID`, `PARTIALLY_PAID`,
  `PAID` (`BillingService.deriveStatus`).
- An invoice needs a `COMPLETED` job, one invoice per job, and a `DELIVERED` job
  cannot be edited any more.

### How a screen will call it

```java
Customer saved = new CustomerService().register(new Customer(name, phone));
JobCard job    = new JobCardService().openJob(vehicleId, complaint);
Invoice bill   = new BillingService().generateInvoice(job.getId(), discount);
```

The panel wraps those calls in `try/catch` for `ValidationException`,
`InsufficientStockException`, `InvalidStatusTransitionException` and
`SQLException`, and shows a friendly `JOptionPane`. No SQL in the UI.

## Database design

The database is created and running on the development machine (MySQL 8.4.13).
`database/schema.sql` is the source of truth; anyone can recreate it from that
file.

| Document | What it contains |
| --- | --- |
| [`docs/database_design.md`](docs/database_design.md) | **Start here.** All 10 tables, columns, keys, foreign keys, cardinality, business rules and the mapping to the Java model |
| [`database/schema.sql`](database/schema.sql) | The executable MySQL 8 DDL that implements that design (already run) |
| [`docs/er_diagram.md`](docs/er_diagram.md) | ER-diagram hand-off sheet: what to draw, the 9 relationships, tools, and a checklist for the team member who draws it |

Quick summary of the model:

```
customers ──< vehicles ──< job_cards ──< job_services >── services
                              │
                              ├──< job_parts >── spare_parts
                              ├──  invoices ──< payments
                              └──  mechanics (assigned mechanic)
```

Create the database on a new machine with:

```bash
mysql -u root -p < database/schema.sql
```

## Project layout

```
database/                 schema.sql (executed) + sample_data.sql (demo rows loaded)
docs/                     database_design.md, er_diagram.md, project_proposal.md, screenshots/
src/main/java/com/vsc/
    Main.java             entry point (launches the Swing window)
    model/                entities: Customer, Vehicle/Car/Motorcycle, JobCard, JobServiceLine, ...
    dao/                  10 DAO classes: SQL and row mapping only
    service/              7 service classes: validation, transactions, business rules
    ui/                   MainFrame, one panel per screen, UiTheme, PlaceholderPanel
    util/                 DBConnection, AppInfo, InputValidator, Money
    exception/            custom checked exceptions
src/main/resources/
    application.properties.example    template to copy (the real one is git-ignored)
src/test/java/com/vsc/    JUnit 5: model rules, billing, stock, validation, DB integration
target/                   build output, generated, git-ignored (see section above)
```

Layer rule: `ui -> service -> dao -> MySQL`. The UI never contains SQL, and
business rules never live in a button listener.

## Contributing

- Work on a feature branch: `feature/<short-name>` or `fix/<short-name>`.
- Money is always `BigDecimal`, never `double`.
- Run `mvn test` before opening a pull request (31 tests; the MySQL one skips
  itself when no database is running).
- Never commit `application.properties` (copy
  `src/main/resources/application.properties.example`).
- **Update this README after every major change**: tick the `Current status`
  table, add the new screen to the layout, and bump `AppInfo.VERSION`. A member
  who opens the repository should always see what works today without asking.
- One screen = one class in `com.vsc.ui`. Do not put two screens in one file.
- Add a JUnit test with every new rule (calculation, stock, status, validation).
- AI agents must read `AGENTS.md` first; it describes the current state of the
  code and the rules to follow.
