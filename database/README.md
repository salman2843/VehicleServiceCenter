# database

The MySQL scripts and the quick "how do I look at the data?" guide live here.

| File | Status | Purpose |
| --- | --- | --- |
| `schema.sql` | **executed successfully** on MySQL 8.4.13 | Creates the `vehicle_service_center` database and its 10 tables |
| `sample_data.sql` | **executed successfully** | Demo rows: 5 customers, 5 vehicles, 3 mechanics, 5 services, 5 spare parts, 4 job cards (one per status), 2 invoices with payments |

`mysql.exe` is **not on PATH** on this machine. Full path:
`C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe`

---

## 1. Open the database

### A. Start menu (easiest)

Start menu → **MySQL 8.4 Command Line Client** (Unicode) → enter the `root`
password → you get the `mysql>` prompt.

### B. PowerShell

```powershell
# interactive
& "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe" -u root -p

# one-off query (-p asks for the password)
& "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe" -u root -p -D vehicle_service_center -e "SELECT id, name FROM customers;"
```

PowerShell does not understand `<`, so load a script with `Get-Content` or
`source`:

```powershell
& "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe" -u root -p -e "source database/schema.sql"
```

Make `mysql` available in every terminal (run once, then open a new terminal):

```powershell
setx PATH "$env:Path;C:\Program Files\MySQL\MySQL Server 8.4\bin"
```

### C. MySQL Workbench (GUI, not installed yet)

Install it from <https://dev.mysql.com/downloads/workbench/>, then:

1. **+** next to *MySQL Connections* → Name `VSC local`, Hostname `127.0.0.1`,
   Port `3306`, Username `root`, **Store in Vault** → password → **Test Connection**.
2. Open the connection → **Navigator → Schemas → refresh ↻ →
   `vehicle_service_center` → Tables**.
3. Hover a table → click the table icon (*Select Rows – Limit 1000*) to see the
   grid; **Apply** writes edits back.
4. **SQL+** = new query tab, **Ctrl+Enter** = run.
5. Right-click the schema → **Schema Inspector** (keys/indexes) or
   **Reverse Engineer** (EER diagram, handy for `docs/er_diagram.md`).

---

## 2. First commands in the shell

```sql
SHOW DATABASES;
USE vehicle_service_center;
SHOW TABLES;                 -- expect 10 tables
DESCRIBE spare_parts;        -- columns, types, NULL, keys
SELECT * FROM customers;
exit
```

---

## 3. Run the scripts (new machine or clean reset)

```powershell
# create the database and 10 tables
& "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe" -u root -p -e "source database/schema.sql"

# optional: demo rows for screenshots and the report
& "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe" -u root -p -e "source database/sample_data.sql"
```

`sample_data.sql` writes explicit ids, so running it twice stops at the first
duplicate key and changes nothing.

Full reset:

```sql
DROP DATABASE vehicle_service_center;
```

then run `schema.sql` and `sample_data.sql` again, in that order.

---

## 4. Queries worth remembering

**Job board (the workflow view):**

```sql
SELECT j.id AS job, c.name AS customer, v.registration_no AS vehicle,
       j.status, m.name AS mechanic
FROM job_cards j
JOIN vehicles  v ON v.id = j.vehicle_id
JOIN customers c ON c.id = v.customer_id
LEFT JOIN mechanics m ON m.id = j.mechanic_id
ORDER BY j.id;
```

**Money: total, paid, outstanding:**

```sql
SELECT i.id AS invoice, c.name AS customer, i.total, i.payment_status,
       COALESCE(SUM(p.amount),0) AS paid,
       i.total - COALESCE(SUM(p.amount),0) AS outstanding
FROM invoices i
JOIN job_cards j ON j.id = i.job_card_id
JOIN vehicles  v ON v.id = j.vehicle_id
JOIN customers c ON c.id = v.customer_id
LEFT JOIN payments p ON p.invoice_id = i.id
GROUP BY i.id, c.name, i.total, i.payment_status;
```

**Lines of one job (what the invoice is built from):**

```sql
SELECT js.job_card_id, s.name AS service, js.agreed_price
FROM job_services js JOIN services s ON s.id = js.service_id WHERE js.job_card_id = 1
UNION ALL
SELECT jp.job_card_id, CONCAT(p.name, ' x', jp.quantity), jp.quantity * jp.unit_price
FROM job_parts jp JOIN spare_parts p ON p.id = jp.part_id WHERE jp.job_card_id = 1;
```

**Low stock (same rule as `SparePart.isLowStock()`):**

```sql
SELECT part_number, name, stock_qty, reorder_level
FROM spare_parts WHERE stock_qty <= reorder_level;
```

**Service history of a vehicle:**

```sql
SELECT j.id, j.status, j.received_on, j.complaint
FROM job_cards j JOIN vehicles v ON v.id = j.vehicle_id
WHERE v.registration_no = 'KA01AB1234'
ORDER BY j.received_on DESC;
```

---

## 5. App connection settings

The application reads `src/main/resources/application.properties`
(git-ignored, one copy per developer, never committed). Copy the example first:

```powershell
Copy-Item src/main/resources/application.properties.example src/main/resources/application.properties
```

then set your own `db.password`. Nothing is hard-coded in Java:

```properties
db.url=jdbc:mysql://localhost:3306/vehicle_service_center?sslMode=DISABLED&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.username=root
db.password=your-password-here
```

---

## 6. Troubleshooting

| Message | Cause / fix |
| --- | --- |
| `Access denied for user 'root'` | Wrong password. Reset it in MySQL Configurator if lost. |
| `Unknown database 'vehicle_service_center'` | `schema.sql` has not been run yet. |
| `mysql is not recognized` | Use the full path or the `setx PATH` command above. |
| `The '<' operator is reserved` | PowerShell: use `Get-Content file.sql \| mysql ...` or `-e "source ..."`. |
| Table already exists | The schema was already loaded. Do not re-run it; use `IF NOT EXISTS` scripts or a full reset. |

Design details (tables, keys, rules) are in
[`../docs/database_design.md`](../docs/database_design.md); the ER-diagram
hand-off is in [`../docs/er_diagram.md`](../docs/er_diagram.md).
