import { useEffect, useState, type FormEvent } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { api } from '../api'
import { Icon } from '../components/Icons'
import { Modal } from '../components/Modal'
import { useToast } from '../components/Toast'
import {
  Badge,
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
import { formatDate, formatDateTime, money, statusLabel } from '../format'
import { errorMessage, useApi } from '../hooks'
import type { JobDetail, JobStatus } from '../types'

const steps: JobStatus[] = ['RECEIVED', 'IN_PROGRESS', 'COMPLETED', 'DELIVERED']

export default function JobDetailPage() {
  const { id } = useParams()
  const jobId = Number(id)
  const navigate = useNavigate()
  const toast = useToast()

  const job = useApi<JobDetail>(`job:${jobId}`, () => api.jobs.get(jobId))
  const services = useApi('catalogue-services', () => api.services.list())
  const parts = useApi('catalogue-parts', () => api.parts.list())
  const mechanics = useApi('mechanics-active', () => api.mechanics.active())

  const [mechanicId, setMechanicId] = useState(0)
  const [serviceId, setServiceId] = useState(0)
  const [agreedPrice, setAgreedPrice] = useState('')
  const [partId, setPartId] = useState(0)
  const [quantity, setQuantity] = useState('1')
  const [discount, setDiscount] = useState('0')
  const [busy, setBusy] = useState<string | null>(null)
  const [formError, setFormError] = useState<string | null>(null)
  const [confirmDeliver, setConfirmDeliver] = useState(false)

  useEffect(() => {
    if (job.data) setMechanicId(job.data.mechanicId)
  }, [job.data?.mechanicId])

  const data = job.data

  async function run(label: string, action: () => Promise<JobDetail>) {
    setBusy(label)
    setFormError(null)
    try {
      const updated = await action()
      job.setData(updated)
      return updated
    } catch (cause) {
      const message = errorMessage(cause)
      setFormError(message)
      toast(message, 'error')
      return null
    } finally {
      setBusy(null)
    }
  }

  async function advance() {
    if (!data) return
    setConfirmDeliver(false)
    const updated = await run('status', () => api.jobs.advance(data.id))
    if (updated) toast(`Job moved to ${statusLabel(updated.status).toLowerCase()}.`)
  }

  async function assignMechanic(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!data || !mechanicId) return
    const updated = await run('mechanic', () => api.jobs.assign(data.id, mechanicId))
    if (updated) toast('Mechanic assigned.')
  }

  async function addService(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!data || !serviceId) return
    const updated = await run('service', () =>
      api.jobs.addService(data.id, serviceId, agreedPrice.trim() === '' ? undefined : Number(agreedPrice)),
    )
    if (updated) {
      setServiceId(0)
      setAgreedPrice('')
      toast('Service added to the job.')
    }
  }

  async function addPart(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!data || !partId) return
    const updated = await run('part', () => api.jobs.addPart(data.id, partId, Number(quantity)))
    if (updated) {
      setPartId(0)
      setQuantity('1')
      parts.reload()
      toast('Part added and stock deducted.')
    }
  }

  async function generateInvoice(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!data) return
    setBusy('invoice')
    setFormError(null)
    try {
      const invoice = await api.invoices.create(data.id, Number(discount || 0))
      toast(`Invoice #${invoice.id} generated.`)
      navigate(`/invoices/${invoice.id}`)
    } catch (cause) {
      const message = errorMessage(cause)
      setFormError(message)
      toast(message, 'error')
    } finally {
      setBusy(null)
    }
  }

  if (job.error) {
    return (
      <>
        <Link
          to="/jobs"
          className="mb-4 inline-flex items-center gap-1.5 text-[12.5px] font-medium text-ink-soft transition-colors hover:text-ink"
        >
          <Icon name="arrowLeft" className="h-3.5 w-3.5" />
          All job cards
        </Link>
        <Card>
          <ErrorBlock message={job.error} onRetry={job.reload} />
        </Card>
      </>
    )
  }

  if (!data) {
    return (
      <Card>
        <LoadingBlock label="Opening the job card…" />
      </Card>
    )
  }

  const locked = data.status === 'DELIVERED'
  const currentIndex = steps.indexOf(data.status)
  const nextStatus = data.nextStatus

  return (
    <>
      <Link
        to="/jobs"
        className="mb-4 inline-flex items-center gap-1.5 text-[12.5px] font-medium text-ink-soft transition-colors hover:text-ink"
      >
        <Icon name="arrowLeft" className="h-3.5 w-3.5" />
        All job cards
      </Link>

      <PageHeader
        eyebrow={`Job card #${data.id}`}
        title={`${data.registrationNo ?? 'Vehicle'} · ${data.vehicleLabel ?? ''}`}
        description={
          <>
            {data.customerName ?? 'Unknown customer'}
            {data.customerPhone ? ` · ${data.customerPhone}` : ''} · Received{' '}
            {formatDate(data.receivedOn)}
          </>
        }
        actions={
          <>
            <StatusBadge status={data.status} />
            {nextStatus ? (
              <Button
                onClick={() =>
                  nextStatus === 'DELIVERED' ? setConfirmDeliver(true) : advance()
                }
                disabled={busy !== null}
              >
                {busy === 'status'
                  ? 'Updating…'
                  : `Mark ${statusLabel(nextStatus).toLowerCase()}`}
              </Button>
            ) : (
              <Button variant="secondary" disabled>
                Delivered
              </Button>
            )}
          </>
        }
      />

      {formError && <FormError message={formError} />}

      <div className={`grid gap-4 xl:grid-cols-[1.7fr_1fr] ${formError ? 'mt-4' : ''}`}>
        <div className="space-y-4">
          <Card>
            <CardHeader title="Complaint" description="Recorded when the vehicle arrived." />
            <p className="whitespace-pre-line px-5 py-4 text-[13.5px] leading-relaxed text-ink">
              {data.complaint}
            </p>
          </Card>

          <Card>
            <CardHeader
              title="Services"
              description="Labour lines with the price agreed for this job."
              action={
                <span className="tnum font-mono text-[12.5px] text-ink-soft">
                  {money(data.laborTotal)}
                </span>
              }
            />
            {data.services.length === 0 ? (
              <EmptyState
                icon="wrench"
                title="No services added"
                description="Pick a service from the catalogue below."
              />
            ) : (
              <table className="w-full border-collapse text-left">
                <thead>
                  <tr>
                    <th className={thClass}>Service</th>
                    <th className={`${thClass} text-right`}>Agreed price</th>
                  </tr>
                </thead>
                <tbody>
                  {data.services.map((line) => (
                    <tr key={line.id} className="border-t border-line/80">
                      <td className={tdClass}>{line.serviceName}</td>
                      <td className={`${tdClass} tnum text-right font-mono`}>
                        {money(line.agreedPrice)}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}

            {!locked && (
              <form
                onSubmit={addService}
                className="grid gap-3 border-t border-line bg-muted/25 px-5 py-4 sm:grid-cols-[1fr_150px_auto] sm:items-end"
              >
                <Field label="Service">
                  <Select
                    value={serviceId}
                    onChange={(event) => setServiceId(Number(event.target.value))}
                  >
                    <option value={0}>Choose a service</option>
                    {(services.data ?? []).map((service) => (
                      <option key={service.id} value={service.id}>
                        {service.name} · {money(service.basePrice)}
                      </option>
                    ))}
                  </Select>
                </Field>
                <Field label="Agreed price" hint="Blank uses catalogue">
                  <Input
                    type="number"
                    min={0}
                    step="0.01"
                    value={agreedPrice}
                    onChange={(event) => setAgreedPrice(event.target.value)}
                    placeholder="Catalogue"
                    className="font-mono"
                  />
                </Field>
                <Button type="submit" className="h-[38px]" disabled={!serviceId || busy !== null}>
                  Add
                </Button>
              </form>
            )}
          </Card>

          <Card>
            <CardHeader
              title="Spare parts"
              description="Adding a part deducts its stock in the same transaction."
              action={
                <span className="tnum font-mono text-[12.5px] text-ink-soft">
                  {money(data.partsTotal)}
                </span>
              }
            />
            {data.parts.length === 0 ? (
              <EmptyState
                icon="box"
                title="No parts used"
                description="Pick a part below; the store stock updates immediately."
              />
            ) : (
              <table className="w-full border-collapse text-left">
                <thead>
                  <tr>
                    <th className={thClass}>Part</th>
                    <th className={`${thClass} text-right`}>Qty</th>
                    <th className={`${thClass} text-right`}>Unit price</th>
                    <th className={`${thClass} text-right`}>Line total</th>
                  </tr>
                </thead>
                <tbody>
                  {data.parts.map((line) => (
                    <tr key={line.id} className="border-t border-line/80">
                      <td className={tdClass}>
                        {line.partName}
                        <span className="ml-2 font-mono text-[11px] text-ink-soft">
                          {line.partNumber}
                        </span>
                      </td>
                      <td className={`${tdClass} tnum text-right font-mono`}>{line.quantity}</td>
                      <td className={`${tdClass} tnum text-right font-mono`}>
                        {money(line.unitPrice)}
                      </td>
                      <td className={`${tdClass} tnum text-right font-mono`}>
                        {money(line.lineTotal)}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}

            <div className="flex flex-wrap items-center justify-end gap-x-7 gap-y-1.5 border-t border-line bg-muted/25 px-5 py-3 text-[12px] text-ink-soft">
              <span>
                Labour{' '}
                <span className="tnum ml-1 font-mono text-ink">{money(data.laborTotal)}</span>
              </span>
              <span>
                Parts{' '}
                <span className="tnum ml-1 font-mono text-ink">{money(data.partsTotal)}</span>
              </span>
              <span>
                Job total{' '}
                <span className="tnum ml-1 font-mono text-[13px] font-semibold text-ink">
                  {money(data.jobTotal)}
                </span>
              </span>
            </div>

            {!locked && (
              <form
                onSubmit={addPart}
                className="grid gap-3 border-t border-line px-5 py-4 sm:grid-cols-[1fr_110px_auto] sm:items-end"
              >
                <Field label="Spare part">
                  <Select
                    value={partId}
                    onChange={(event) => setPartId(Number(event.target.value))}
                  >
                    <option value={0}>Choose a part</option>
                    {(parts.data ?? []).map((part) => (
                      <option key={part.id} value={part.id} disabled={part.stockQty === 0}>
                        {part.name} · {money(part.unitPrice)} · {part.stockQty} in stock
                      </option>
                    ))}
                  </Select>
                </Field>
                <Field label="Quantity">
                  <Input
                    type="number"
                    min={1}
                    value={quantity}
                    onChange={(event) => setQuantity(event.target.value)}
                    className="font-mono"
                  />
                </Field>
                <Button type="submit" className="h-[38px]" disabled={!partId || busy !== null}>
                  Add
                </Button>
              </form>
            )}
          </Card>
        </div>

        <div className="space-y-4">
          <Card>
            <CardHeader title="Workshop stage" />
            <ol className="space-y-3 px-5 py-4">
              {steps.map((step, index) => {
                const done = index < currentIndex
                const current = index === currentIndex
                return (
                  <li key={step} className="flex items-start gap-3">
                    <span
                      className={`mt-0.5 grid h-5 w-5 shrink-0 place-items-center rounded-full border text-[10px] font-semibold ${
                        done
                          ? 'border-brand bg-brand text-white'
                          : current
                            ? 'border-brand bg-brand-soft text-brand'
                            : 'border-line bg-surface text-ink-soft/50'
                      }`}
                    >
                      {done ? <Icon name="check" className="h-3 w-3" /> : index + 1}
                    </span>
                    <div className="min-w-0">
                      <p
                        className={`text-[12.5px] ${
                          current
                            ? 'font-semibold text-ink'
                            : done
                              ? 'text-ink-soft'
                              : 'text-ink-soft/60'
                        }`}
                      >
                        {statusLabel(step)}
                      </p>
                      {step === 'RECEIVED' && (
                        <p className="font-mono text-[10.5px] text-ink-soft/75">
                          {formatDateTime(data.receivedOn)}
                        </p>
                      )}
                      {step === 'COMPLETED' && data.completedOn && (
                        <p className="font-mono text-[10.5px] text-ink-soft/75">
                          {formatDateTime(data.completedOn)}
                        </p>
                      )}
                    </div>
                  </li>
                )
              })}
            </ol>
            <p className="border-t border-line px-5 py-3 text-[11.5px] leading-relaxed text-ink-soft">
              Status only moves one step forward. A delivered job becomes history.
            </p>
          </Card>

          <Card>
            <CardHeader title="Invoice" />
            {data.invoice ? (
              <div className="space-y-3 px-5 py-4">
                <div className="flex items-center justify-between gap-3">
                  <span className="font-mono text-[13px] font-semibold">
                    Invoice #{data.invoice.id}
                  </span>
                  <StatusBadge status={data.invoice.paymentStatus} />
                </div>
                <div className="flex items-baseline justify-between">
                  <span className="text-[12px] text-ink-soft">Total</span>
                  <span className="tnum font-mono text-[17px] font-semibold">
                    {money(data.invoice.total)}
                  </span>
                </div>
                {data.invoice.outstanding > 0 && (
                  <div className="flex items-baseline justify-between">
                    <span className="text-[12px] text-ink-soft">Outstanding</span>
                    <span className="tnum font-mono text-[13px] text-amber">
                      {money(data.invoice.outstanding)}
                    </span>
                  </div>
                )}
                <Link to={`/invoices/${data.invoice.id}`} className="block">
                  <Button variant="secondary" className="w-full">
                    Open invoice
                  </Button>
                </Link>
              </div>
            ) : data.status === 'COMPLETED' || data.status === 'DELIVERED' ? (
              <form onSubmit={generateInvoice} className="space-y-3 px-5 py-4">
                <Field label="Discount" hint="Cannot be more than the job total.">
                  <Input
                    type="number"
                    min={0}
                    step="0.01"
                    value={discount}
                    onChange={(event) => setDiscount(event.target.value)}
                    className="font-mono"
                  />
                </Field>
                <div className="flex items-baseline justify-between text-[12.5px]">
                  <span className="text-ink-soft">Job total</span>
                  <span className="tnum font-mono font-semibold">{money(data.jobTotal)}</span>
                </div>
                <Button type="submit" className="w-full" disabled={busy !== null}>
                  {busy === 'invoice' ? 'Generating…' : 'Generate invoice'}
                </Button>
              </form>
            ) : (
              <p className="px-5 py-4 text-[12.5px] leading-relaxed text-ink-soft">
                An invoice can be generated once the job is marked completed.
              </p>
            )}
          </Card>

          <Card>
            <CardHeader title="Mechanic" />
            <form onSubmit={assignMechanic} className="space-y-3 px-5 py-4">
              <Select
                value={mechanicId}
                onChange={(event) => setMechanicId(Number(event.target.value))}
                aria-label="Assigned mechanic"
              >
                <option value={0}>Unassigned</option>
                {(mechanics.data ?? []).map((mechanic) => (
                  <option key={mechanic.id} value={mechanic.id}>
                    {mechanic.name}
                    {mechanic.specialization ? ` · ${mechanic.specialization}` : ''}
                  </option>
                ))}
              </Select>
              <Button
                type="submit"
                variant="secondary"
                className="w-full"
                disabled={!mechanicId || busy !== null || mechanicId === data.mechanicId}
              >
                {busy === 'mechanic' ? 'Saving…' : 'Save mechanic'}
              </Button>
              {!data.mechanicId && (
                <p className="text-[11.5px] text-ink-soft">No mechanic assigned yet.</p>
              )}
            </form>
          </Card>

          <Card>
            <CardHeader title="Details" />
            <div className="px-5 py-2">
              <Facts
                items={[
                  {
                    label: 'Vehicle',
                    value: <span className="font-mono">{data.registrationNo ?? '—'}</span>,
                  },
                  { label: 'Type', value: data.vehicleType === 'CAR' ? 'Car' : 'Motorcycle' },
                  { label: 'Received', value: formatDateTime(data.receivedOn) },
                  { label: 'Completed', value: formatDateTime(data.completedOn) },
                  {
                    label: 'Money lines',
                    value: locked ? (
                      <Badge tone="neutral">Locked</Badge>
                    ) : (
                      <Badge tone="brand">Editable</Badge>
                    ),
                  },
                ]}
              />
            </div>
          </Card>
        </div>
      </div>

      <Modal
        open={confirmDeliver}
        onClose={() => setConfirmDeliver(false)}
        title="Mark this job as delivered?"
        description="Delivering closes the job: services and parts can no longer be added."
        footer={
          <>
            <Button variant="ghost" onClick={() => setConfirmDeliver(false)}>
              Not yet
            </Button>
            <Button onClick={advance} disabled={busy !== null}>
              {busy === 'status' ? 'Delivering…' : 'Yes, mark delivered'}
            </Button>
          </>
        }
      >
        <p className="text-[13px] leading-relaxed text-ink-soft">
          Job #{data.id} for {data.registrationNo} will move to Delivered. The invoice stays
          payable if money is still outstanding.
        </p>
      </Modal>
    </>
  )
}
