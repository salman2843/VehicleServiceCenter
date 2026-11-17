# Vehicle Service Center Management System

A Java Swing desktop application for a small vehicle workshop: customers,
vehicles, job cards, mechanics, spare-part inventory, invoices and payments.
Built with Java 21, Swing, Maven, JUnit 5 and (later) MySQL via JDBC.

This is an MCA Java / OOP project; the code is kept readable so every team
member can explain it.

## Current status

**Version 0.2.0 — starter UI.**

| Area | State |
| --- | --- |
| Swing window + navigation | **Done** — header, 7 screens in a side menu, status bar, File/View/Help menus |
| Feature screens | **Placeholder** — each lists what it will do and which service it will call |
| Model classes | **Done** — all 11 entities + 3 enums |
| Shared helpers | **Done** — `InputValidator`, `Money`, `AppInfo` |
| Database | **Designed only** — `database/schema.sql` written, never executed (no MySQL yet) |
| DAO layer | **Not started** |
| Service layer | **Not started** |
| Tests | 8 passing (`mvn test`) |

The next steps are: install MySQL and run the schema, add the JDBC connection,
then the DAO and service layers, then replace the placeholder screens.

## Requirements

| Tool  | Version | Needed for |
| ----- | ------- | ---------- |
| JDK   | 21      | Everything |
| Maven | 3.8+    | `mvn` commands (not installed yet — use the VS Code **Run** button meanwhile) |
| MySQL | 8.0+    | Running `schema.sql` and saving data (not installed yet) |

## Run it in VS Code

1. Install JDK 21 and VS Code. Confirm with `java -version` in a terminal.
2. Install the **Extension Pack for Java** (Microsoft) in VS Code.
3. `File -> Open Folder` and choose this project folder (the one with `pom.xml`).
4. Wait until the "Java: importing…" progress in the bottom-right disappears.
5. Open `src/main/java/com/vsc/Main.java` and click **Run** above `main`, or
   press `F5` to debug.
6. The *Vehicle Service Center 0.2.0* window opens on the Dashboard. Click the
   screens in the left menu (Customers, Vehicles, Job cards, Inventory,
   Invoices, Mechanics) to move around.

If **Run** does not appear: `Ctrl+Shift+P` -> `Java: Clean Java Language Server
Workspace`, then reopen the file.

## What is in the window today

```
+------------------------------------------------------+
| Vehicle Service Center 0.2.0        File View Help   |
+--------------+---------------------------------------+
| Dashboard    |                                       |
| Customers    |   <selected screen>                   |
| Vehicles     |                                       |
| Job cards    |   e.g. "Job cards" with the list of   |
| Inventory    |        features planned for it        |
| Invoices     |                                       |
| Mechanics    |                                       |
+--------------+---------------------------------------+
| Application running - no database connected yet      |
+------------------------------------------------------+
```

Each screen under `src/main/java/com/vsc/ui/` is its own class, so a team
member can own one screen without touching the others. They currently extend
`PlaceholderPanel`; when the service layer exists, that content is replaced by
the real table or form.

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
+-- surefire-reports/        test results ("Tests run: 8, Failures: 0")
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

## Database design

The database is designed but **not created yet** — no MySQL installation is
needed to read this part.

| Document | What it contains |
| --- | --- |
| [`docs/database_design.md`](docs/database_design.md) | **Start here.** All 10 tables, columns, keys, foreign keys, cardinality, business rules and the mapping to the Java model |
| [`database/schema.sql`](database/schema.sql) | The executable MySQL 8 DDL that implements that design |
| [`docs/er_diagram.md`](docs/er_diagram.md) | ER-diagram hand-off sheet: what to draw, the 9 relationships, tools, and a checklist for the team member who draws it |

Quick summary of the model:

```
customers ──< vehicles ──< job_cards ──< job_services >── services
                              │
                              ├──< job_parts >── spare_parts
                              ├──  invoices ──< payments
                              └──  mechanics (assigned mechanic)
```

When MySQL is installed, create the database with:

```bash
mysql -u root -p < database/schema.sql
```

## Project layout

```
database/                 schema.sql (designed, not yet run) + sample_data.sql (later)
docs/                     database_design.md, er_diagram.md, project_proposal.md, screenshots/
src/main/java/com/vsc/
    Main.java             entry point (launches the Swing window)
    model/                entities: Customer, Vehicle/Car/Motorcycle, JobCard, ...
    dao/                  JDBC code (empty for now)
    service/              business rules and transactions (empty for now)
    ui/                   MainFrame, one panel per screen, UiTheme, PlaceholderPanel
    util/                 AppInfo, InputValidator, Money, DBConnection (later)
    exception/            custom checked exceptions
src/test/java/com/vsc/    JUnit 5 tests (model + screens)
target/                   build output, generated, git-ignored (see section above)
```

Layer rule: `ui -> service -> dao -> MySQL`. The UI never contains SQL, and
business rules never live in a button listener.

## Contributing

- Work on a feature branch: `feature/<short-name>` or `fix/<short-name>`.
- Money is always `BigDecimal`, never `double`.
- Run `mvn test` before opening a pull request.
- Never commit `application.properties` (copy
  `src/main/resources/application.properties.example`).
- **Update this README after every major change**: tick the `Current status`
  table, add the new screen to the layout, and bump `AppInfo.VERSION`. A member
  who opens the repository should always see what works today without asking.
- One screen = one class in `com.vsc.ui`. Do not put two screens in one file.
- AI agents must read `AGENTS.md` first; it describes the current state of the
  code and the rules to follow.
