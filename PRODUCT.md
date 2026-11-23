# Vehicle Service Center

## What it is

A workshop management application for a small independent vehicle service
centre. One receptionist registers customers and their vehicles, opens a job
card for each visit, adds labour and spare parts, moves the job through the
workshop, generates an invoice and records payments.

## Who it is for

A single receptionist / workshop manager sitting at a desk at the front of the
shop, often with a customer standing in front of them. They use it many times a
day, in short bursts, and need to find a vehicle, open a job or take a payment
in seconds. The mechanic never uses the app.

## Jobs to be done

1. Find a customer by name or phone, or register a new one.
2. Add or look up a vehicle by registration number.
3. Open a job card for a complaint and assign a mechanic.
4. Add catalogue services and spare parts; stock is deducted automatically.
5. Move the job forward: received, in progress, completed, delivered.
6. Generate the invoice from the saved job lines and record full or part payments.
7. See what needs attention today: open jobs, unpaid invoices, low stock.

## Platform

Web application. React + Tailwind frontend, Java HTTP API, MySQL storage.

## Mode

Operate. The visitor completes a task. Scanability, consistency, predictable
patterns and a real usage scene outrank expression; craft shows in the details.

## Constraints

- Amounts are Indian rupees, always two decimals, never floating point.
- No login or user roles in this version.
- No deleting customers, vehicles, jobs or invoices.
- Job status only moves forward, one step at a time.
- A job must be completed before it can be invoiced.
- Payments cannot exceed the outstanding amount.

## Success

The receptionist can finish the whole flow without leaving the app, without a
manual, and without wondering whether something saved. Tables read at a glance,
forms explain what went wrong, and money is never ambiguous.

## Out of scope

Online payments, appointments, reports export, PDF invoices, multi-branch,
audit log, login.
