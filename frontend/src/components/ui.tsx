import type {
  ButtonHTMLAttributes,
  InputHTMLAttributes,
  ReactNode,
  SelectHTMLAttributes,
  TextareaHTMLAttributes,
} from 'react'
import { statusLabel } from '../format'
import { Icon, type IconName } from './Icons'

type ButtonVariant = 'primary' | 'secondary' | 'ghost' | 'danger'
type ButtonSize = 'sm' | 'md'

const buttonVariants: Record<ButtonVariant, string> = {
  primary:
    'border border-transparent bg-brand text-white hover:bg-brand-dark active:bg-brand-dark',
  secondary:
    'border border-line bg-surface text-ink hover:border-ink-soft/35 hover:bg-muted',
  ghost: 'border border-transparent text-ink-soft hover:bg-muted hover:text-ink',
  danger: 'border border-transparent bg-danger text-white hover:bg-[#8e3024]',
}

const buttonSizes: Record<ButtonSize, string> = {
  sm: 'h-8 px-3 text-[12px]',
  md: 'h-9 px-4 text-[13px]',
}

export function Button({
  variant = 'primary',
  size = 'md',
  className = '',
  type = 'button',
  children,
  ...props
}: ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: ButtonVariant
  size?: ButtonSize
}) {
  return (
    <button
      type={type}
      className={`inline-flex shrink-0 items-center justify-center gap-1.5 rounded-lg font-semibold transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand/25 focus-visible:ring-offset-2 focus-visible:ring-offset-paper disabled:cursor-not-allowed disabled:opacity-50 ${buttonVariants[variant]} ${buttonSizes[size]} ${className}`}
      {...props}
    >
      {children}
    </button>
  )
}

type Tone = 'brand' | 'amber' | 'slate' | 'danger' | 'neutral'

const badgeTones: Record<Tone, string> = {
  brand: 'bg-brand-soft text-brand-dark',
  amber: 'bg-amber-soft text-amber',
  slate: 'bg-slate-soft text-slate',
  danger: 'bg-danger-soft text-danger',
  neutral: 'bg-muted text-ink-soft',
}

export function Badge({
  tone = 'neutral',
  children,
  className = '',
}: {
  tone?: Tone
  children: ReactNode
  className?: string
}) {
  return (
    <span
      className={`inline-flex h-[22px] items-center whitespace-nowrap rounded-full px-2.5 text-[11px] font-semibold ${badgeTones[tone]} ${className}`}
    >
      {children}
    </span>
  )
}

const statusTones: Record<string, Tone> = {
  RECEIVED: 'slate',
  IN_PROGRESS: 'amber',
  COMPLETED: 'brand',
  DELIVERED: 'neutral',
  UNPAID: 'amber',
  PARTIALLY_PAID: 'slate',
  PAID: 'brand',
}

export function StatusBadge({ status }: { status: string }) {
  return <Badge tone={statusTones[status] ?? 'neutral'}>{statusLabel(status)}</Badge>
}

export function Card({
  className = '',
  children,
}: {
  className?: string
  children: ReactNode
}) {
  return (
    <section className={`rounded-[10px] border border-line bg-surface ${className}`}>
      {children}
    </section>
  )
}

export function CardHeader({
  title,
  description,
  action,
}: {
  title: ReactNode
  description?: ReactNode
  action?: ReactNode
}) {
  return (
    <div className="flex flex-wrap items-start justify-between gap-3 border-b border-line px-5 py-4">
      <div className="min-w-0">
        <h2 className="text-[14.5px] font-semibold tracking-[-0.01em] text-ink">{title}</h2>
        {description && (
          <p className="mt-0.5 text-[12.5px] leading-relaxed text-ink-soft">{description}</p>
        )}
      </div>
      {action}
    </div>
  )
}

export function PageHeader({
  eyebrow,
  title,
  description,
  actions,
}: {
  eyebrow?: string
  title: string
  description?: ReactNode
  actions?: ReactNode
}) {
  return (
    <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
      <div className="min-w-0">
        {eyebrow && (
          <p className="mb-1 font-mono text-[10.5px] uppercase tracking-[0.18em] text-ink-soft">
            {eyebrow}
          </p>
        )}
        <h1 className="text-[26px] font-semibold leading-tight tracking-[-0.025em] text-ink sm:text-[29px]">
          {title}
        </h1>
        {description && (
          <p className="mt-1.5 max-w-2xl text-[13.5px] leading-relaxed text-ink-soft">
            {description}
          </p>
        )}
      </div>
      {actions && <div className="flex flex-wrap items-center gap-2">{actions}</div>}
    </div>
  )
}

export function Stat({
  label,
  value,
  hint,
  icon,
  emphasis = false,
}: {
  label: string
  value: ReactNode
  hint?: ReactNode
  icon?: IconName
  emphasis?: boolean
}) {
  return (
    <Card className="p-5">
      <div className="flex items-center justify-between gap-3">
        <p className="text-[10.5px] font-semibold uppercase tracking-[0.14em] text-ink-soft">
          {label}
        </p>
        {icon && <Icon name={icon} className="h-4 w-4 text-ink-soft/50" />}
      </div>
      <p
        className={`tnum mt-3 font-mono text-[28px] leading-none ${emphasis ? 'text-brand' : 'text-ink'}`}
      >
        {value}
      </p>
      {hint && <p className="mt-2 text-[12px] leading-snug text-ink-soft">{hint}</p>}
    </Card>
  )
}

export const inputClass =
  'h-[38px] w-full rounded-lg border border-line bg-surface px-3 text-[13px] text-ink placeholder:text-ink-soft/55 focus:border-brand focus:outline-none focus:ring-2 focus:ring-brand/15 disabled:cursor-not-allowed disabled:bg-muted disabled:text-ink-soft aria-invalid:border-danger'

export function Input({ className = '', ...props }: InputHTMLAttributes<HTMLInputElement>) {
  return <input className={`${inputClass} ${className}`} {...props} />
}

export function Select({
  className = '',
  children,
  ...props
}: SelectHTMLAttributes<HTMLSelectElement>) {
  return (
    <div className="relative">
      <select className={`${inputClass} appearance-none pr-9 ${className}`} {...props}>
        {children}
      </select>
      <Icon
        name="chevronDown"
        className="pointer-events-none absolute right-3 top-1/2 h-4 w-4 -translate-y-1/2 text-ink-soft/55"
      />
    </div>
  )
}

export function Textarea({
  className = '',
  ...props
}: TextareaHTMLAttributes<HTMLTextAreaElement>) {
  return (
    <textarea
      className={`min-h-[92px] w-full resize-y rounded-lg border border-line bg-surface px-3 py-2 text-[13px] leading-relaxed text-ink placeholder:text-ink-soft/55 focus:border-brand focus:outline-none focus:ring-2 focus:ring-brand/15 aria-invalid:border-danger ${className}`}
      {...props}
    />
  )
}

export function Field({
  label,
  hint,
  error,
  required,
  children,
  className = '',
}: {
  label: string
  hint?: ReactNode
  error?: string | null
  required?: boolean
  children: ReactNode
  className?: string
}) {
  return (
    <label className={`block ${className}`}>
      <span className="mb-1.5 flex items-center gap-1 text-[12px] font-medium text-ink-soft">
        {label}
        {required && <span className="text-danger">*</span>}
      </span>
      {children}
      {error ? (
        <span className="mt-1 block text-[11.5px] text-danger">{error}</span>
      ) : hint ? (
        <span className="mt-1 block text-[11.5px] leading-snug text-ink-soft/85">{hint}</span>
      ) : null}
    </label>
  )
}

export function SearchBox({
  value,
  onChange,
  placeholder = 'Search',
  className = '',
}: {
  value: string
  onChange: (value: string) => void
  placeholder?: string
  className?: string
}) {
  return (
    <div className={`relative ${className}`}>
      <Icon
        name="search"
        className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-ink-soft/55"
      />
      <input
        type="search"
        value={value}
        onChange={(event) => onChange(event.target.value)}
        placeholder={placeholder}
        className={`${inputClass} pl-9`}
      />
    </div>
  )
}

export function Tabs<T extends string>({
  items,
  value,
  onChange,
}: {
  items: { value: T; label: string; count?: number }[]
  value: T
  onChange: (value: T) => void
}) {
  return (
    <div className="flex flex-wrap items-center gap-1.5" role="tablist">
      {items.map((item) => {
        const active = item.value === value
        return (
          <button
            key={item.value}
            type="button"
            role="tab"
            aria-selected={active}
            onClick={() => onChange(item.value)}
            className={`inline-flex h-8 items-center gap-1.5 rounded-full border px-3 text-[12.5px] font-medium transition-colors ${
              active
                ? 'border-brand bg-brand text-white'
                : 'border-line bg-surface text-ink-soft hover:border-ink-soft/30 hover:text-ink'
            }`}
          >
            {item.label}
            {item.count !== undefined && (
              <span
                className={`tnum font-mono text-[10.5px] ${
                  active ? 'text-white/70' : 'text-ink-soft/65'
                }`}
              >
                {item.count}
              </span>
            )}
          </button>
        )
      })}
    </div>
  )
}

export const thClass =
  'whitespace-nowrap bg-muted/70 px-4 py-2.5 text-left text-[10.5px] font-semibold uppercase tracking-[0.11em] text-ink-soft'
export const tdClass = 'px-4 py-3 align-middle text-[13px]'
export const rowClass =
  'border-t border-line/80 transition-colors hover:bg-muted/45'

export function EmptyState({
  icon = 'info',
  title,
  description,
  action,
}: {
  icon?: IconName
  title: string
  description?: ReactNode
  action?: ReactNode
}) {
  return (
    <div className="flex flex-col items-center justify-center px-6 py-14 text-center">
      <span className="grid h-10 w-10 place-items-center rounded-full bg-muted text-ink-soft">
        <Icon name={icon} className="h-[18px] w-[18px]" />
      </span>
      <p className="mt-3 text-[14px] font-semibold text-ink">{title}</p>
      {description && (
        <p className="mt-1 max-w-sm text-[12.5px] leading-relaxed text-ink-soft">{description}</p>
      )}
      {action && <div className="mt-4">{action}</div>}
    </div>
  )
}

export function Spinner({ className = 'h-4 w-4' }: { className?: string }) {
  return (
    <svg className={`animate-spin ${className}`} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <circle cx="12" cy="12" r="9" stroke="currentColor" strokeOpacity="0.2" strokeWidth="3" />
      <path
        d="M21 12a9 9 0 0 0-9-9"
        stroke="currentColor"
        strokeWidth="3"
        strokeLinecap="round"
      />
    </svg>
  )
}

export function LoadingBlock({ label = 'Loading…' }: { label?: string }) {
  return (
    <div className="flex items-center gap-2.5 px-5 py-12 text-[13px] text-ink-soft">
      <Spinner />
      {label}
    </div>
  )
}

export function ErrorBlock({
  message,
  onRetry,
}: {
  message: string
  onRetry?: () => void
}) {
  return (
    <div className="m-5 rounded-lg border border-danger/20 bg-danger-soft px-4 py-3">
      <div className="flex items-start gap-2.5">
        <Icon name="alert" className="mt-0.5 h-4 w-4 shrink-0 text-danger" />
        <div className="min-w-0 flex-1">
          <p className="text-[13px] font-semibold text-danger">Could not load this screen</p>
          <p className="mt-0.5 text-[12.5px] leading-relaxed text-danger/85">{message}</p>
        </div>
        {onRetry && (
          <Button variant="secondary" size="sm" onClick={onRetry}>
            Retry
          </Button>
        )}
      </div>
    </div>
  )
}

export function FormError({ message }: { message: string | null }) {
  if (!message) return null
  return (
    <div
      role="alert"
      className="flex items-start gap-2 rounded-lg border border-danger/20 bg-danger-soft px-3 py-2 text-[12.5px] leading-snug text-danger"
    >
      <Icon name="alert" className="mt-0.5 h-3.5 w-3.5 shrink-0" />
      <span>{message}</span>
    </div>
  )
}

export function Facts({
  items,
}: {
  items: { label: string; value: ReactNode }[]
}) {
  return (
    <dl className="divide-y divide-line/70">
      {items.map((item) => (
        <div key={item.label} className="flex items-start justify-between gap-4 py-2.5">
          <dt className="text-[12px] text-ink-soft">{item.label}</dt>
          <dd className="text-right text-[12.5px] font-medium text-ink">{item.value}</dd>
        </div>
      ))}
    </dl>
  )
}
