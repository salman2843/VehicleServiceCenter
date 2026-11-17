# database

The MySQL scripts live here.

| File | Status | Purpose |
| --- | --- | --- |
| `schema.sql` | **written, not yet executed** | Creates the `vehicle_service_center` database and its 10 tables |
| `sample_data.sql` | not created yet | Demo customers, vehicles, parts and services |

MySQL is not installed on the development machine yet, so the scripts are kept
here as plain SQL. When MySQL is ready:

```bash
mysql -u root -p < database/schema.sql
```

Design details (tables, keys, rules) are in
[`../docs/database_design.md`](../docs/database_design.md); the ER-diagram
hand-off is in [`../docs/er_diagram.md`](../docs/er_diagram.md).
