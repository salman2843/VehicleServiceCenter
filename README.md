# Vehicle Service Center Management System

A workshop desk for a small vehicle service centre: customers, vehicles, job
cards, mechanics, spare-part inventory, invoices and payments.

- **Frontend:** React 19 + TypeScript + Tailwind CSS 4 (Vite), in `frontend/`.
- **Backend:** Java 21 HTTP API on the JDK's built-in `com.sun.net.httpserver`,
  in `com.vsc.api`. No new runtime dependencies.
- **Storage:** MySQL 8 through JDBC, behind the existing DAO and service layers.

The React UI never touches MySQL. It calls the JSON API, the API calls the
service layer, and the services call the DAOs. This keeps the business rules
(status transitions, stock, invoice calculation, payments) in one place.

This is an MCA Java / OOP project; the code is kept readable so every team
member can explain it.

## Current status

**Version 1.0.0 — complete application.** React UI, Java HTTP API, MySQL
storage, 30 passing tests.

| Area | State |
| --- | --- |
| React UI | **Done** — 9 screens: overview, customers, vehicles, job cards + detail, invoices + detail, inventory, mechanics |
| Design system | **Done** — `frontend/src/index.css` tokens, shared primitives in `components/ui.tsx`; context in `PRODUCT.md` and `DESIGN.md` |
| HTTP API | **Done** — 30 endpoints in `com.vsc.api`, CORS, JSON errors, static hosting of `frontend/dist` |
| Model classes | **Done** — 12 entity classes + 3 enums |
| Shared helpers | **Done** — `InputValidator`, `Money`, `AppInfo`, `DBConnection` |
| Database | **Done and executed** — `database/schema.sql` created all 10 tables on MySQL 8.4.13 |
| DAO layer | **Done** — 10 DAO classes, `PreparedStatement` only |
| Service layer | **Done** — customers, vehicles, mechanics, catalogue, inventory, job cards, billing |
| Tests | **30 passing** (`mvn test`), including an end-to-end run against MySQL |

## Requirements

| Tool  | Version | Needed for |
| ----- | ------- | ---------- |
| JDK   | 21      | Backend and tests |
| Maven | 3.8+    | `mvn` commands |
| MySQL | **8.4 LTS** (8.0+ works) | Saving data |
| Node  | 20+ and npm | Building or developing the React UI |

## One-time setup

1. Create the database and tables (see [`database/README.md`](database/README.md)):

   ```bash
   mysql -u root -p < database/schema.sql
   ```

   Optionally load the demo rows:

   ```bash
   mysql -u root -p < database/sample_data.sql
   ```

2. Copy `src/main/resources/application.properties.example` to
   `application.properties` and put your own MySQL password in it. This file is
   git-ignored, so nobody's password reaches GitHub.

3. Install the frontend dependencies:

   ```bash
   cd frontend
   npm install
   ```

## Run the whole application

Build the UI once, then start the Java server. It serves both the API and the
built React app from one port:

```bash
cd frontend
npm run build
cd ..

mvn clean compile
mvn exec:java
```

Open **http://localhost:8080**. The browser opens automatically unless you pass
`--no-open`. Use another port with `-Dvsc.port=9090`.

`mvn exec:java` needs the `exec-maven-plugin`, which is already configured in
`pom.xml`. If `frontend/dist` has not been built yet, the server still starts and
shows a short page explaining how to build the UI.

### Develop the UI with hot reload

Run the API and Vite in two terminals:

```bash
# terminal 1 - Java API on :8080
mvn exec:java

# terminal 2 - React dev server on :5173
cd frontend
npm run dev
```

Vite proxies `/api` to `http://localhost:8080`, so the UI works against the real
database. Open the address Vite prints (usually **http://localhost:5173**).

### Run the tests

```bash
mvn test
```

`dao.WorkflowIntegrationTest` needs MySQL; without a reachable server it is
**skipped** (JUnit assumption), not failed, so the suite still passes on a
teammate's machine.

## What the screens do

| Screen | Route | What it does |
| --- | --- | --- |
| Overview | `/` | Counts, job pipeline by status, recent jobs, money collected vs invoiced, low stock and unpaid bills |
| Customers | `/customers` | Search by name/phone, register, edit, open a customer drawer with their vehicles |
| Vehicles | `/vehicles` | Search, filter cars/motorcycles, add a vehicle, open service history |
| Job cards | `/jobs` | Filter by stage, search, open a job card |
| Job detail | `/jobs/:id` | Complaint, services, parts (stock deducted), mechanic, one-step status advance, invoice generation |
| Invoices | `/invoices` | Filter by payment state, search, totals and outstanding amounts |
| Invoice detail | `/invoices/:id` | Bill lines from the saved job prices, discount, payments, record a payment |
| Inventory | `/inventory` | Spare parts (stock, low-stock warning, receive stock, edit) and the service catalogue |
| Mechanics | `/mechanics` | Add and edit mechanics; only active mechanics can be assigned |

## How the layers connect

```
React page (frontend/src/pages)
   -> api client (frontend/src/api.ts)  -- HTTP/JSON -->
      ApiServer / Router (com.vsc.api)
         -> service class (com.vsc.service)
            -> DAO (com.vsc.dao)
               -> MySQL
```

- The UI never contains SQL or money/stock calculations; it formats values for
  display with `Intl.NumberFormat` (`en-IN`, INR).
- Services own validation, transactions and business rules.
- DAOs take the `Connection` from the service so multi-step rules stay in one
  transaction.
- Amounts cross the wire as JSON numbers produced from `BigDecimal`; the Java
  side never uses `float`/`double` for money.

### API endpoints

| Area | Endpoints |
| --- | --- |
| Health | `GET /api/health` |
| Dashboard | `GET /api/dashboard` |
| Customers | `GET/POST /api/customers`, `GET/PUT /api/customers/{id}` |
| Vehicles | `GET/POST /api/vehicles`, `GET /api/vehicles/{id}`, `GET /api/vehicles/{id}/jobs` |
| Mechanics | `GET/POST /api/mechanics`, `PUT /api/mechanics/{id}` |
| Catalogue | `GET/POST /api/services` |
| Inventory | `GET/POST /api/parts`, `PUT /api/parts/{id}`, `POST /api/parts/{id}/receive` |
| Jobs | `GET/POST /api/jobs`, `GET /api/jobs/{id}`, `POST /api/jobs/{id}/mechanic`, `POST /api/jobs/{id}/status`, `POST /api/jobs/{id}/services`, `POST /api/jobs/{id}/parts` |
| Invoices | `GET/POST /api/invoices`, `GET /api/invoices/{id}`, `POST /api/invoices/{id}/payments` |

List endpoints accept `?q=` where it makes sense (`customers`, `vehicles`,
`jobs`, `invoices`); jobs also accept `?status=`, invoices `?status=`, vehicles
`?customerId=`, mechanics `?active=true`.

### Rules enforced by the backend

- **Status** moves one step only: `RECEIVED → IN_PROGRESS → COMPLETED → DELIVERED`
  (`JobStatus.canMoveTo`, `JobCardService.updateStatus`).
- **Stock** can never go negative: checked in `InventoryService.ensureAvailable`,
  and the `UPDATE ... WHERE stock_qty >= ?` in `SparePartDao.deductStock` checks
  it again so two users cannot take the same last unit.
- **Adding a part** writes the stock deduction and the job line in one
  transaction (`JobCardService.addPart`): both succeed or neither does.
- **Invoice** = labour + parts − discount (+ tax), from the prices saved on the
  job lines (`BillingService.calculate`, unit tested). The UI uses a discount and
  no tax by default.
- **Payment status** is derived, never typed in: `UNPAID`, `PARTIALLY_PAID`,
  `PAID` (`BillingService.deriveStatus`).
- An invoice needs a `COMPLETED` job, one invoice per job, and a `DELIVERED` job
  cannot be edited any more.

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

## Project layout

```
frontend/                 React + TypeScript + Tailwind UI
    src/pages/            one file per screen (9 screens)
    src/components/       Layout, ui primitives, modals, toasts, icons
    src/api.ts            typed HTTP client
    src/types.ts          JSON shapes returned by the API
    src/hooks.ts          useApi / useDebounced helpers
    src/index.css         Tailwind theme tokens
    dist/                 production build, served by the Java server (git-ignored)
database/                 schema.sql (executed) + sample_data.sql (demo rows)
docs/                     database_design.md, er_diagram.md, project_proposal.md
src/main/java/com/vsc/
    Main.java             entry point (starts the HTTP API and opens the browser)
    api/                  HTTP server, router, JSON, one handler per resource
    model/                entities: Customer, Vehicle/Car/Motorcycle, JobCard, ...
    dao/                  10 DAO classes: SQL and row mapping only
    service/              7 service classes: validation, transactions, business rules
    util/                 DBConnection, AppInfo, InputValidator, Money
    exception/            custom checked exceptions
src/main/resources/
    application.properties.example    template to copy (the real one is git-ignored)
src/test/java/com/vsc/    JUnit 5: model rules, billing, stock, validation, DB integration
target/                   Maven build output, generated, git-ignored
```

Layer rule: `React -> HTTP API -> service -> dao -> MySQL`. The UI never contains
SQL, and business rules never live in a button handler.

## Contributing

- Work on a feature branch: `feature/<short-name>` or `fix/<short-name>`.
- Money is always `BigDecimal`, never `double`.
- Run `mvn test` and `cd frontend && npm run build` before opening a pull request
  (30 tests; the MySQL one skips itself when no database is running).
- Never commit `application.properties` (copy
  `src/main/resources/application.properties.example`) or `frontend/dist`.
- **Update this README after every major change**: tick the `Current status`
  table, add the new screen to the route table, and bump `AppInfo.VERSION`. A
  member who opens the repository should always see what works today without
  asking.
- One screen = one file in `frontend/src/pages`. Do not put two screens in one
  file.
- Add a JUnit test with every new rule (calculation, stock, status, validation).
- AI agents must read `AGENTS.md` first; it describes the current state of the
  code and the rules to follow.
