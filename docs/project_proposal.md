# Project proposal

The signed proposal document is a Word file kept **locally only**
(`proposal/Vehicle_Service_Center_Project_propposal.docx`); the `proposal/`
folder is git-ignored, so teammates will not receive it with a clone. Attach it
separately if the guide needs to see it.

## Summary

A desktop application for a small vehicle workshop that keeps customers,
vehicles, job cards, mechanics, spare-part stock, invoices and payments in one
connected workflow:

1. Register a customer and their vehicle.
2. Create a job card with the complaint.
3. Add services and parts, assign a mechanic (stock is deducted).
4. Track the job: `RECEIVED -> IN_PROGRESS -> COMPLETED -> DELIVERED`.
5. Generate the invoice from the job's saved lines.
6. Record payments (cash / UPI / card, partial payments allowed).
7. Retrieve a vehicle's service history later.

## Scope of version 1

Customers, vehicles, job cards, services, inventory, invoicing, payments and
history. Online payments, GPS, multi-branch and accounting exports are out of
scope.
