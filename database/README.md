# database

The MySQL scripts live here.

| File | Status | Purpose |
| --- | --- | --- |
| `schema.sql` | **executed successfully** on MySQL 8.4.13 | Creates the `vehicle_service_center` database and its 10 tables |
| `sample_data.sql` | not created yet | Demo customers, vehicles, parts and services |

## Run the schema

```bash
# MySQL 8.4: `mysql.exe` is not on PATH, so use the full path or add
# "C:\Program Files\MySQL\MySQL Server 8.4\bin" to PATH.
mysql -u root -p < database/schema.sql
```

Check it worked:

```sql
USE vehicle_service_center;
SHOW TABLES;          -- expect 10 tables
```

The schema uses `CREATE TABLE` without `IF NOT EXISTS`, so a second run stops at
the first existing table. That is intentional: it stops you from wiping data by
accident. Drop the database first only when you really want a fresh start:

```sql
DROP DATABASE vehicle_service_center;
```

## App connection settings

The application reads `src/main/resources/application.properties`
(git-ignored, one copy per developer). Copy the example first:

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

then set your own `db.password`. Nothing is hard-coded in Java.

Design details (tables, keys, rules) are in
[`../docs/database_design.md`](../docs/database_design.md); the ER-diagram
hand-off is in [`../docs/er_diagram.md`](../docs/er_diagram.md).
