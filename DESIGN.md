# Design system

## World

**Clean workshop.** A calm, well-lit service desk. Off-white paper surfaces, a
deep workshop green used only for action and confirmation, humanist sans for
reading, monospace for anything the receptionist might compare or type twice
(registrations, amounts, job numbers). Soft cards on a warm background, generous
whitespace, hairline rules instead of heavy shadows. Conventional patterns used
well, not reinvented.

## Colour tokens

| Token | Value | Use |
| --- | --- | --- |
| `paper` | `#F7F5EF` | page background |
| `surface` | `#FFFFFF` | cards, tables, forms |
| `muted` | `#F1EEE5` | table headers, quiet fills |
| `ink` | `#1C211D` | primary text |
| `ink-soft` | `#5A625B` | secondary text, labels |
| `line` | `#E2DED2` | borders, dividers |
| `brand` | `#1F5138` | primary buttons, active nav, links |
| `brand-dark` | `#163D2A` | hover |
| `brand-soft` | `#E7F0E9` | selected rows, success tint |
| `amber` | `#B45309` | in-progress, low stock |
| `danger` | `#A63A2B` | errors, overdue |
| `slate` | `#475569` | received / neutral status |

Status mapping: received = slate, in progress = amber, completed = brand,
delivered = ink-soft, unpaid = amber, partially paid = slate, paid = brand.

## Type

- Sans: `"Segoe UI Variable Text", "Segoe UI", system-ui, sans-serif`.
- Mono: `"Cascadia Mono", "Consolas", ui-monospace, monospace` for plates,
  amounts, dates, job and invoice numbers.
- Serif: `"Iowan Old Style", "Palatino Linotype", Georgia, serif` for the brand
  wordmark only.
- Scale: page title 28/34 semibold, section title 16/24 semibold, body 14/22,
  label 12/16 medium uppercase tracking-wide, table 13/20, KPI 32/36 mono.

## Layout

- Fixed 236px sidebar on desktop, collapsible under 1024px.
- Content column max 1400px, 24px padding on mobile, 40px on desktop.
- Cards: 1px `line` border, 10px radius, no shadow; 20–24px inner padding.
- Tables: full-bleed inside cards, 12px cell padding, sticky header on scroll.
- Detail screens: two columns (main 2fr, rail 1fr) collapsing to one.

## Components

- Buttons: 36px high, 8px radius, 13px semibold. Primary = brand fill, secondary
  = surface + line border, ghost = transparent, danger = danger fill.
- Inputs: 38px high, 8px radius, surface fill, line border, brand focus ring.
- Badges: 22px high pill, tinted background, no border.
- Drawer: 520px right panel for customer/vehicle detail; modal 560px for forms.
- Empty states: one line of what is missing plus the action that fixes it.

## Motion

150–200ms ease-out for hover and drawer; no page-transition animation. Respect
`prefers-reduced-motion`.

## Voice

Plain, specific, short. "Register customer", "Mark completed", "Take payment".
Errors say what to fix, not that something "failed". Money always reads `₹1,250.00`.
