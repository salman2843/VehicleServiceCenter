/** Display helpers: money, dates and vehicle labels. */

const inr = new Intl.NumberFormat('en-IN', {
  style: 'currency',
  currency: 'INR',
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
})

const inrCompact = new Intl.NumberFormat('en-IN', {
  style: 'currency',
  currency: 'INR',
  maximumFractionDigits: 0,
})

export function money(value: number | string | null | undefined): string {
  const amount = Number(value ?? 0)
  return inr.format(Number.isFinite(amount) ? amount : 0)
}

export function moneyShort(value: number | string | null | undefined): string {
  const amount = Number(value ?? 0)
  return inrCompact.format(Number.isFinite(amount) ? amount : 0)
}

function parseDate(value: string | null | undefined): Date | null {
  if (!value) return null
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? null : date
}

const dateShort = new Intl.DateTimeFormat('en-IN', {
  day: '2-digit',
  month: 'short',
  year: 'numeric',
})

const dateTime = new Intl.DateTimeFormat('en-IN', {
  day: '2-digit',
  month: 'short',
  year: 'numeric',
  hour: '2-digit',
  minute: '2-digit',
})

export function formatDate(value: string | null | undefined): string {
  const date = parseDate(value)
  return date ? dateShort.format(date) : '—'
}

export function formatDateTime(value: string | null | undefined): string {
  const date = parseDate(value)
  return date ? dateTime.format(date) : '—'
}

export function formatToday(): string {
  return new Intl.DateTimeFormat('en-IN', {
    weekday: 'short',
    day: '2-digit',
    month: 'short',
  }).format(new Date())
}

export function vehicleLabel(vehicle: {
  make: string
  modelName: string
  vehicleType?: string | null
}): string {
  return `${vehicle.make} ${vehicle.modelName}`
}

export function statusLabel(status: string): string {
  switch (status) {
    case 'RECEIVED':
      return 'Received'
    case 'IN_PROGRESS':
      return 'In progress'
    case 'COMPLETED':
      return 'Completed'
    case 'DELIVERED':
      return 'Delivered'
    case 'UNPAID':
      return 'Unpaid'
    case 'PARTIALLY_PAID':
      return 'Part paid'
    case 'PAID':
      return 'Paid'
    default:
      return status
  }
}
