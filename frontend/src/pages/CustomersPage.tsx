import { useEffect, useRef, useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api'
import { Icon } from '../components/Icons'
import { Drawer, Modal } from '../components/Modal'
import { useToast } from '../components/Toast'
import {
  Button,
  Card,
  EmptyState,
  ErrorBlock,
  Facts,
  Field,
  FormError,
  Input,
  LoadingBlock,
  PageHeader,
  SearchBox,
  Textarea,
  rowClass,
  tdClass,
  thClass,
} from '../components/ui'
import { VehicleFormModal } from '../components/VehicleForm'
import { formatDate } from '../format'
import { errorMessage, useApi, useDebounced } from '../hooks'
import type { Customer, CustomerDetail } from '../types'

function CustomerFormModal({
  open,
  customer,
  onClose,
  onSaved,
}: {
  open: boolean
  customer: Customer | null
  onClose: () => void
  onSaved: () => void
}) {
  const [name, setName] = useState('')
  const [phone, setPhone] = useState('')
  const [email, setEmail] = useState('')
  const [address, setAddress] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)
  const initialised = useRef(false)

  useEffect(() => {
    if (!open) {
      initialised.current = false
      return
    }
    if (initialised.current) return
    initialised.current = true
    setName(customer?.name ?? '')
    setPhone(customer?.phone ?? '')
    setEmail(customer?.email ?? '')
    setAddress(customer?.address ?? '')
    setError(null)
    setSaving(false)
  }, [open, customer])

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSaving(true)
    setError(null)
    try {
      const input = { name, phone, email, address }
      if (customer) {
        await api.customers.update(customer.id, input)
      } else {
        await api.customers.create(input)
      }
      onSaved()
      onClose()
    } catch (cause) {
      setError(errorMessage(cause))
    } finally {
      setSaving(false)
    }
  }

  return (
    <Modal
      open={open}
      onClose={onClose}
      title={customer ? 'Edit customer' : 'Register a customer'}
      description={
        customer
          ? 'Changes apply to future job cards; old bills keep their saved prices.'
          : 'One phone number per customer, so the same person is never registered twice.'
      }
      footer={
        <>
          <Button variant="ghost" onClick={onClose}>
            Cancel
          </Button>
          <Button type="submit" form="customer-form" disabled={saving}>
            {saving ? 'Saving…' : customer ? 'Save changes' : 'Register customer'}
          </Button>
        </>
      }
    >
      <form id="customer-form" onSubmit={submit} className="space-y-4">
        {error && <FormError message={error} />}
        <div className="grid gap-4 sm:grid-cols-2">
          <Field label="Full name" required>
            <Input
              value={name}
              onChange={(event) => setName(event.target.value)}
              placeholder="Asha Rao"
              autoFocus
              required
            />
          </Field>
          <Field label="Phone" required hint="7 to 15 digits">
            <Input
              value={phone}
              onChange={(event) => setPhone(event.target.value)}
              placeholder="+91 98765 43210"
              className="font-mono"
              required
            />
          </Field>
        </div>
        <Field label="E-mail" hint="Optional">
          <Input
            type="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            placeholder="asha@example.com"
          />
        </Field>
        <Field label="Address" hint="Optional, up to 255 characters">
          <Textarea
            value={address}
            onChange={(event) => setAddress(event.target.value)}
            placeholder="14, 5th Cross, Indiranagar, Bengaluru"
          />
        </Field>
      </form>
    </Modal>
  )
}

export default function CustomersPage() {
  const toast = useToast()
  const [query, setQuery] = useState('')
  const debouncedQuery = useDebounced(query)
  const [formOpen, setFormOpen] = useState(false)
  const [editing, setEditing] = useState<Customer | null>(null)
  const [detailId, setDetailId] = useState<number | null>(null)
  const [vehicleOpen, setVehicleOpen] = useState(false)

  const customers = useApi(`customers:${debouncedQuery}`, () => api.customers.list(debouncedQuery))
  const detail = useApi<CustomerDetail | null>(
    detailId ? `customer:${detailId}` : 'customer:none',
    () => (detailId ? api.customers.get(detailId) : Promise.resolve(null)),
  )

  function openRegister() {
    setEditing(null)
    setFormOpen(true)
  }

  function openEdit(customer: Customer) {
    setEditing(customer)
    setFormOpen(true)
  }

  function afterSaved(message: string) {
    toast(message)
    customers.reload()
    detail.reload()
  }

  return (
    <>
      <PageHeader
        eyebrow="Customers"
        title="Customers"
        description="Everyone who has brought a vehicle in. Search by name or phone before registering someone new."
        actions={
          <Button onClick={openRegister}>
            <Icon name="plus" className="h-4 w-4" />
            Register customer
          </Button>
        }
      />

      <Card>
        <div className="flex flex-wrap items-center gap-3 border-b border-line px-4 py-3">
          <SearchBox
            value={query}
            onChange={setQuery}
            placeholder="Search name or phone"
            className="w-full sm:w-72"
          />
          <p className="ml-auto text-[12px] text-ink-soft">
            {customers.data ? `${customers.data.length} customer${customers.data.length === 1 ? '' : 's'}` : '—'}
          </p>
        </div>

        {customers.error && <ErrorBlock message={customers.error} onRetry={customers.reload} />}
        {customers.loading && !customers.data && <LoadingBlock />}

        {customers.data && customers.data.length === 0 && (
          <EmptyState
            icon="users"
            title={query ? 'No customer matches that search' : 'No customers yet'}
            description={
              query
                ? 'Try a shorter name or the last digits of the phone number.'
                : 'Register the first customer, then add their vehicle.'
            }
            action={
              !query && (
                <Button size="sm" onClick={openRegister}>
                  Register customer
                </Button>
              )
            }
          />
        )}

        {customers.data && customers.data.length > 0 && (
          <div className="overflow-x-auto">
            <table className="w-full border-collapse text-left">
              <thead>
                <tr>
                  <th className={thClass}>Customer</th>
                  <th className={thClass}>Phone</th>
                  <th className={thClass}>E-mail</th>
                  <th className={thClass}>Vehicles</th>
                  <th className={thClass}>Since</th>
                  <th className={thClass}>
                    <span className="sr-only">Open</span>
                  </th>
                </tr>
              </thead>
              <tbody>
                {customers.data.map((customer) => (
                  <tr
                    key={customer.id}
                    className={`${rowClass} cursor-pointer`}
                    onClick={() => setDetailId(customer.id)}
                  >
                    <td className={tdClass}>
                      <span className="font-medium text-ink">{customer.name}</span>
                      <span className="ml-2 font-mono text-[11px] text-ink-soft">
                        #{customer.id}
                      </span>
                    </td>
                    <td className={`${tdClass} font-mono text-[12.5px]`}>{customer.phone}</td>
                    <td className={`${tdClass} text-ink-soft`}>{customer.email || '—'}</td>
                    <td className={tdClass}>
                      <span className="tnum font-mono text-[12.5px]">{customer.vehicleCount}</span>
                    </td>
                    <td className={`${tdClass} text-ink-soft`}>{formatDate(customer.createdOn)}</td>
                    <td className={`${tdClass} text-right`}>
                      <Icon name="external" className="ml-auto h-4 w-4 text-ink-soft/50" />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>

      <CustomerFormModal
        open={formOpen}
        customer={editing}
        onClose={() => setFormOpen(false)}
        onSaved={() => afterSaved(editing ? 'Customer updated.' : 'Customer registered.')}
      />

      <Drawer
        open={detailId !== null}
        onClose={() => setDetailId(null)}
        eyebrow="Customer"
        title={detail.data?.name ?? 'Loading…'}
        footer={
          detail.data && (
            <>
              <Button variant="ghost" onClick={() => setDetailId(null)}>
                Close
              </Button>
              <Button variant="secondary" onClick={() => openEdit(detail.data!)}>
                Edit customer
              </Button>
            </>
          )
        }
      >
        {detail.loading && !detail.data && <LoadingBlock />}
        {detail.error && <ErrorBlock message={detail.error} onRetry={detail.reload} />}

        {detail.data && (
          <div className="space-y-6">
            <Facts
              items={[
                { label: 'Phone', value: <span className="font-mono">{detail.data.phone}</span> },
                { label: 'E-mail', value: detail.data.email || '—' },
                { label: 'Address', value: detail.data.address || '—' },
                { label: 'Customer since', value: formatDate(detail.data.createdOn) },
              ]}
            />

            <section>
              <div className="mb-3 flex items-center justify-between gap-3">
                <div>
                  <h3 className="text-[13px] font-semibold text-ink">Vehicles</h3>
                  <p className="text-[11.5px] text-ink-soft">
                    {detail.data.vehicles.length === 0
                      ? 'No vehicle added yet.'
                      : `${detail.data.vehicles.length} on file`}
                  </p>
                </div>
                <Button size="sm" variant="secondary" onClick={() => setVehicleOpen(true)}>
                  <Icon name="plus" className="h-3.5 w-3.5" />
                  Add vehicle
                </Button>
              </div>

              {detail.data.vehicles.length === 0 ? (
                <div className="rounded-lg border border-dashed border-line px-4 py-5 text-center text-[12.5px] text-ink-soft">
                  Add the vehicle so a job card can be opened for it.
                </div>
              ) : (
                <ul className="space-y-2">
                  {detail.data.vehicles.map((vehicle) => (
                    <li
                      key={vehicle.id}
                      className="rounded-lg border border-line px-3.5 py-3"
                    >
                      <div className="flex flex-wrap items-start justify-between gap-3">
                        <div className="min-w-0">
                          <p className="font-mono text-[12.5px] font-semibold text-ink">
                            {vehicle.registrationNo}
                          </p>
                          <p className="mt-0.5 text-[12px] text-ink-soft">
                            {vehicle.make} {vehicle.modelName} · {vehicle.year} ·{' '}
                            {vehicle.vehicleType === 'CAR' ? 'Car' : 'Motorcycle'}
                          </p>
                        </div>
                        <Link to={`/jobs?new=1&vehicleId=${vehicle.id}`}>
                          <Button size="sm" variant="secondary">
                            Open job
                          </Button>
                        </Link>
                      </div>
                    </li>
                  ))}
                </ul>
              )}
            </section>
          </div>
        )}
      </Drawer>

      <VehicleFormModal
        open={vehicleOpen}
        onClose={() => setVehicleOpen(false)}
        customers={customers.data ?? []}
        defaultCustomerId={detailId ?? undefined}
        onSaved={() => afterSaved('Vehicle added.')}
      />
    </>
  )
}
