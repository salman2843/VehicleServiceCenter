import { useEffect, useState, type FormEvent } from 'react'
import { Link, useParams } from 'react-router-dom'
import { api } from '../api'
import { Icon } from '../components/Icons'
import { useToast } from '../components/Toast'
import {
  Button,
  Card,
  CardHeader,
  EmptyState,
  ErrorBlock,
  Facts,
  Field,
  FormError,
  Input,
  LoadingBlock,
  PageHeader,
  Select,
  StatusBadge,
  tdClass,
  thClass,
} from '../components/ui'
import { formatDate, formatDateTime, money } from '../format'
import { errorMessage, useApi } from '../hooks'
import type { InvoiceDetail, PaymentMethod } from '../types'

export default function InvoiceDetailPage() {
  const { id } = useParams()
  const invoiceId = Number(id)
  const toast = useToast()

  const invoice = useApi<InvoiceDetail>(`invoice:${invoiceId}`, () => api.invoices.get(invoiceId))

  const [amount, setAmount] = useState('')
  const [method, setMethod] = useState<PaymentMethod>('CASH')
  const [reference, setReference] = useState('')
  const [busy, setBusy] = useState(false)
  const [formError, setFormError] = useState<string | null>(null)

  useEffect(() => {
    if (invoice.data) {
      setAmount(invoice.data.outstanding > 0 ? String(invoice.data.outstanding) : '')
    }
  }, [invoice.data?.outstanding])

  const data = invoice.data

  async function takePayment(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!data) return
    setBusy(true)
    setFormError(null)
    try {
      const updated = await api.invoices.pay(data.id, Number(amount), method, reference)
      invoice.setData(updated)
      setReference('')
      toast('Payment recorded.')
    } catch (cause) {
      const message = errorMessage(cause)
      setFormError(message)
      toast(message, 'error')
    } finally {
      setBusy(false)
    }
  }

  if (invoice.error) {
    return (
      <>
        <Link
          to="/invoices"
          className="mb-4 inline-flex items-center gap-1.5 text-[12.5px] font-medium text-ink-soft transition-colors hover:text-ink"
        >
          <Icon name="arrowLeft" className="h-3.5 w-3.5" />
          All invoices
        </Link>
        <Card>
          <ErrorBlock message={invoice.error} onRetry={invoice.reload} />
        </Card>
      </>
    )
  }

  if (!data) {
    return (
      <Card>
        <LoadingBlock label="Opening the invoice…" />
      </Card>
    )
  }

  const subtotal = data.laborTotal + data.partsTotal

  return (
    <>
      <Link
        to="/invoices"
        className="mb-4 inline-flex items-center gap-1.5 text-[12.5px] font-medium text-ink-soft transition-colors hover:text-ink"
      >
        <Icon name="arrowLeft" className="h-3.5 w-3.5" />
        All invoices
      </Link>

      <PageHeader
        eyebrow={`Invoice #${data.id}`}
        title={money(data.total)}
        description={
          <>
            For job #{data.jobCardId} · {data.registrationNo ?? 'Vehicle'} ·{' '}
            {data.customerName ?? 'Customer'} · Issued {formatDate(data.createdOn)}
          </>
        }
        actions={<StatusBadge status={data.paymentStatus} />}
      />

      <div className="grid gap-4 xl:grid-cols-[1.7fr_1fr]">
        <div className="space-y-4">
          <Card>
            <CardHeader title="Bill" description="Prices saved with the job, not today's catalogue." />
            <div className="overflow-x-auto">
              <table className="w-full border-collapse text-left">
                <thead>
                  <tr>
                    <th className={thClass}>Description</th>
                    <th className={`${thClass} text-right`}>Qty</th>
                    <th className={`${thClass} text-right`}>Amount</th>
                  </tr>
                </thead>
                <tbody>
                  {data.job.services.map((line) => (
                    <tr key={`service-${line.id}`} className="border-t border-line/80">
                      <td className={tdClass}>
                        {line.serviceName}
                        <span className="ml-2 text-[11px] uppercase tracking-wide text-ink-soft">
                          Labour
                        </span>
                      </td>
                      <td className={`${tdClass} tnum text-right font-mono text-ink-soft`}>1</td>
                      <td className={`${tdClass} tnum text-right font-mono`}>
                        {money(line.lineTotal)}
                      </td>
                    </tr>
                  ))}
                  {data.job.parts.map((line) => (
                    <tr key={`part-${line.id}`} className="border-t border-line/80">
                      <td className={tdClass}>
                        {line.partName}
                        <span className="ml-2 font-mono text-[11px] text-ink-soft">
                          {line.partNumber}
                        </span>
                      </td>
                      <td className={`${tdClass} tnum text-right font-mono`}>{line.quantity}</td>
                      <td className={`${tdClass} tnum text-right font-mono`}>
                        {money(line.lineTotal)}
                      </td>
                    </tr>
                  ))}
                  {data.job.services.length === 0 && data.job.parts.length === 0 && (
                    <tr>
                      <td colSpan={3}>
                        <EmptyState
                          icon="receipt"
                          title="No billable lines"
                          description="This job had no services or parts saved when the invoice was generated."
                        />
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>

            <dl className="space-y-2 border-t border-line bg-muted/25 px-5 py-4 text-[12.5px]">
              <div className="flex justify-between">
                <dt className="text-ink-soft">Labour</dt>
                <dd className="tnum font-mono">{money(data.laborTotal)}</dd>
              </div>
              <div className="flex justify-between">
                <dt className="text-ink-soft">Parts</dt>
                <dd className="tnum font-mono">{money(data.partsTotal)}</dd>
              </div>
              <div className="flex justify-between border-t border-line/80 pt-2">
                <dt className="text-ink-soft">Subtotal</dt>
                <dd className="tnum font-mono">{money(subtotal)}</dd>
              </div>
              {data.discount > 0 && (
                <div className="flex justify-between">
                  <dt className="text-ink-soft">Discount</dt>
                  <dd className="tnum font-mono text-danger">− {money(data.discount)}</dd>
                </div>
              )}
              {data.taxAmount > 0 && (
                <div className="flex justify-between">
                  <dt className="text-ink-soft">Tax</dt>
                  <dd className="tnum font-mono">{money(data.taxAmount)}</dd>
                </div>
              )}
              <div className="flex items-baseline justify-between border-t border-line pt-2">
                <dt className="text-[13px] font-semibold text-ink">Total</dt>
                <dd className="tnum font-mono text-[17px] font-semibold">
                  {money(data.total)}
                </dd>
              </div>
              <div className="flex justify-between">
                <dt className="text-ink-soft">Paid</dt>
                <dd className="tnum font-mono text-brand">{money(data.paid)}</dd>
              </div>
              <div className="flex justify-between">
                <dt className="font-medium text-ink">Outstanding</dt>
                <dd
                  className={`tnum font-mono font-semibold ${
                    data.outstanding > 0 ? 'text-amber' : 'text-brand'
                  }`}
                >
                  {money(data.outstanding)}
                </dd>
              </div>
            </dl>
          </Card>

          <Card>
            <CardHeader
              title="Payments"
              description={
                data.payments.length === 0
                  ? 'No payment recorded yet.'
                  : `${data.payments.length} payment${data.payments.length === 1 ? '' : 's'} recorded.`
              }
            />
            {data.payments.length === 0 ? (
              <EmptyState
                icon="receipt"
                title="Nothing paid yet"
                description="Record a full or part payment from the panel on the right."
              />
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full border-collapse text-left">
                  <thead>
                    <tr>
                      <th className={thClass}>Date</th>
                      <th className={thClass}>Method</th>
                      <th className={thClass}>Reference</th>
                      <th className={`${thClass} text-right`}>Amount</th>
                    </tr>
                  </thead>
                  <tbody>
                    {data.payments.map((payment) => (
                      <tr key={payment.id} className="border-t border-line/80">
                        <td className={`${tdClass} text-ink-soft`}>
                          {formatDateTime(payment.paidOn)}
                        </td>
                        <td className={tdClass}>
                          <span className="font-mono text-[12px]">{payment.method}</span>
                        </td>
                        <td className={`${tdClass} font-mono text-[12px] text-ink-soft`}>
                          {payment.reference || '—'}
                        </td>
                        <td className={`${tdClass} tnum text-right font-mono`}>
                          {money(payment.amount)}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </Card>
        </div>

        <div className="space-y-4">
          <Card>
            <CardHeader title="Bill to" />
            <div className="px-5 py-2">
              <Facts
                items={[
                  { label: 'Customer', value: data.customerName ?? '—' },
                  {
                    label: 'Vehicle',
                    value: <span className="font-mono">{data.registrationNo ?? '—'}</span>,
                  },
                  {
                    label: 'Job card',
                    value: (
                      <Link to={`/jobs/${data.jobCardId}`} className="text-brand hover:underline">
                        #{data.jobCardId}
                      </Link>
                    ),
                  },
                  { label: 'Issued', value: formatDate(data.createdOn) },
                ]}
              />
            </div>
          </Card>

          <Card>
            <CardHeader
              title="Record a payment"
              description={
                data.outstanding > 0
                  ? `${money(data.outstanding)} still outstanding.`
                  : 'This invoice is settled in full.'
              }
            />
            {data.outstanding > 0 ? (
              <form onSubmit={takePayment} className="space-y-3 px-5 py-4">
                {formError && <FormError message={formError} />}
                <Field label="Amount" required hint="Cannot exceed the outstanding amount.">
                  <Input
                    type="number"
                    min={0.01}
                    step="0.01"
                    max={data.outstanding}
                    value={amount}
                    onChange={(event) => setAmount(event.target.value)}
                    className="font-mono"
                    required
                  />
                </Field>
                <Field label="Method" required>
                  <Select
                    value={method}
                    onChange={(event) => setMethod(event.target.value as PaymentMethod)}
                  >
                    <option value="CASH">Cash</option>
                    <option value="UPI">UPI</option>
                    <option value="CARD">Card</option>
                  </Select>
                </Field>
                <Field label="Reference" hint="UPI transaction id or card approval code.">
                  <Input
                    value={reference}
                    onChange={(event) => setReference(event.target.value)}
                    placeholder="Optional"
                    className="font-mono"
                  />
                </Field>
                <Button type="submit" className="w-full" disabled={busy}>
                  {busy ? 'Recording…' : 'Record payment'}
                </Button>
              </form>
            ) : (
              <div className="px-5 py-5">
                <div className="flex items-center gap-2 rounded-lg bg-brand-soft px-3.5 py-3 text-[12.5px] font-medium text-brand-dark">
                  <Icon name="check" className="h-4 w-4" />
                  Paid in full. Nothing left to collect.
                </div>
              </div>
            )}
          </Card>
        </div>
      </div>
    </>
  )
}
