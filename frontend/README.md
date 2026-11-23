# Vehicle Service Center — React UI

The frontend for the Vehicle Service Center. React 19 + TypeScript + Tailwind CSS 4,
built with Vite. It talks to the Java HTTP API (see the root `README.md`); it never
connects to MySQL.

## Develop

Start the Java API first, in the repository root:

```bash
mvn exec:java
```

Then, in this folder:

```bash
npm install
npm run dev
```

Open http://localhost:5173. Vite proxies `/api` to `http://localhost:8080`, so the
UI uses the real database.

## Build

```bash
npm run build
```

The output goes to `frontend/dist`, which the Java server serves on
http://localhost:8080. `dist/` is git-ignored, so build it once per machine.

## Layout

| Path | Purpose |
| --- | --- |
| `src/main.tsx` | Router, toast provider, app mount |
| `src/App.tsx` | Route table |
| `src/pages/` | One file per screen (overview, customers, vehicles, jobs, invoices, inventory, mechanics) |
| `src/components/` | Layout, shared UI primitives, modals, toasts, icons |
| `src/api.ts` | Typed HTTP client; the only place that calls `fetch` |
| `src/types.ts` | JSON shapes returned by the API |
| `src/hooks.ts` | `useApi`, `useDebounced`, error helpers |
| `src/format.ts` | Money (`en-IN`, INR), dates, status labels |
| `src/index.css` | Tailwind theme tokens (clean-workshop palette) |

`PRODUCT.md` and `DESIGN.md` in the repository root describe the product context
and the design system.

## Checks

```bash
npm run build   # TypeScript + production build
npm run lint    # oxlint
```
