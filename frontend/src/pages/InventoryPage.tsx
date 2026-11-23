import { useEffect, useRef, useState, type FormEvent } from 'react'
import { api } from '../api'
import { Icon } from '../components/Icons'
import { Modal } from '../components/Modal'
import { useToast } from '../components/Toast'
import {
  Badge,
  Button,
  Card,
  EmptyState,
  ErrorBlock,
  Field,
  FormError,
  Input,
  LoadingBlock,
  PageHeader,
  SearchBox,
  Tabs,
  Textarea,
  rowClass,
  tdClass,
  thClass,
} from '../components/ui'
import { money } from '../format'
import { errorMessage, useApi, useDebounced } from '../hooks'
import type { PartInput, SparePart } from '../types'

type Tab = 'PARTS' | 'SERVICES'

function PartFormModal({
  open,
  part,
  onClose,
  onSaved,
}: {
  open: boolean
  part: SparePart | null
  onClose: () => void
  onSaved: () => void
}) {
  const [partNumber, setPartNumber] = useState('')
  const [name, setName] = useState('')
  const [unitPrice, setUnitPrice] = useState('0')
  const [stockQty, setStockQty] = useState('0')
  const [reorderLevel, setReorderLevel] = useState('0')
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
    setPartNumber(part?.partNumber ?? '')
    setName(part?.name ?? '')
    setUnitPrice(String(part?.unitPrice ?? 0))
    setStockQty(String(part?.stockQty ?? 0))
    setReorderLevel(String(part?.reorderLevel ?? 0))
    setError(null)
    setSaving(false)
  }, [open, part])

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSaving(true)
    setError(null)
    const input: PartInput = {
      partNumber,
      name,
      unitPrice: Number(unitPrice),
      stockQty: Number(stockQty),
      reorderLevel: Number(reorderLevel),
    }
    try {
      if (part) {
        await api.parts.update(part.id, input)
      } else {
        await api.parts.create(input)
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
      title={part ? 'Edit spare part' : 'Add a spare part'}
      description="Stock can never go below zero. Low stock is flagged at or below the reorder level."
      footer={
        <>
          <Button variant="ghost" onClick={onClose}>
            Cancel
          </Button>
          <Button type="submit" form="part-form" disabled={saving}>
            {saving ? 'Saving…' : part ? 'Save changes' : 'Add part'}
          </Button>
        </>
      }
    >
      <form id="part-form" onSubmit={submit} className="space-y-4">
        {error && <FormError message={error} />}
        <div className="grid gap-4 sm:grid-cols-2">
          <Field label="Part number" required hint="Stored in uppercase, unique.">
            <Input
              value={partNumber}
              onChange={(event) => setPartNumber(event.target.value)}
              placeholder="BRK-01"
              className="font-mono uppercase"
              required
            />
          </Field>
          <Field label="Unit price" required>
            <Input
              type="number"
              min={0}
              step="0.01"
              value={unitPrice}
              onChange={(event) => setUnitPrice(event.target.value)}
              className="font-mono"
              required
            />
          </Field>
        </div>
        <Field label="Part name" required>
          <Input
            value={name}
            onChange={(event) => setName(event.target.value)}
            placeholder="Front brake pad set"
            required
          />
        </Field>
        <div className="grid gap-4 sm:grid-cols-2">
          <Field label="Stock quantity" required>
            <Input
              type="number"
              min={0}
              value={stockQty}
              onChange={(event) => setStockQty(event.target.value)}
              className="font-mono"
              required
            />
          </Field>
          <Field label="Reorder level" required hint="Warn when stock reaches this number.">
            <Input
              type="number"
              min={0}
              value={reorderLevel}
              onChange={(event) => setReorderLevel(event.target.value)}
              className="font-mono"
              required
            />
          </Field>
        </div>
      </form>
    </Modal>
  )
}

function ReceiveStockModal({
  part,
  onClose,
  onSaved,
}: {
  part: SparePart | null
  onClose: () => void
  onSaved: () => void
}) {
  const [quantity, setQuantity] = useState('10')
  const [error, setError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    if (part) {
      setQuantity('10')
      setError(null)
      setSaving(false)
    }
  }, [part])

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!part) return
    setSaving(true)
    setError(null)
    try {
      await api.parts.receive(part.id, Number(quantity))
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
      open={part !== null}
      onClose={onClose}
      title={part ? `Receive stock · ${part.name}` : 'Receive stock'}
      description={
        part
          ? `Currently ${part.stockQty} in stock. Received units are added to it.`
          : undefined
      }
      footer={
        <>
          <Button variant="ghost" onClick={onClose}>
            Cancel
          </Button>
          <Button type="submit" form="stock-form" disabled={saving}>
            {saving ? 'Adding…' : 'Add to stock'}
          </Button>
        </>
      }
    >
      <form id="stock-form" onSubmit={submit} className="space-y-4">
        {error && <FormError message={error} />}
        <Field label="Units received" required>
          <Input
            type="number"
            min={1}
            value={quantity}
            onChange={(event) => setQuantity(event.target.value)}
            className="font-mono"
            required
          />
        </Field>
      </form>
    </Modal>
  )
}

function ServiceFormModal({
  open,
  onClose,
  onSaved,
}: {
  open: boolean
  onClose: () => void
  onSaved: () => void
}) {
  const [name, setName] = useState('')
  const [details, setDetails] = useState('')
  const [basePrice, setBasePrice] = useState('0')
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
    setName('')
    setDetails('')
    setBasePrice('0')
    setError(null)
    setSaving(false)
  }, [open])

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSaving(true)
    setError(null)
    try {
      await api.services.create({ name, details, basePrice: Number(basePrice) })
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
      title="Add a catalogue service"
      description="The catalogue price is copied to a job when the service is added; old bills never change."
      footer={
        <>
          <Button variant="ghost" onClick={onClose}>
            Cancel
          </Button>
          <Button type="submit" form="service-form" disabled={saving}>
            {saving ? 'Saving…' : 'Add service'}
          </Button>
        </>
      }
    >
      <form id="service-form" onSubmit={submit} className="space-y-4">
        {error && <FormError message={error} />}
        <Field label="Service name" required>
          <Input
            value={name}
            onChange={(event) => setName(event.target.value)}
            placeholder="Wheel alignment"
            required
          />
        </Field>
        <Field label="Details" hint="Optional, up to 255 characters.">
          <Textarea
            value={details}
            onChange={(event) => setDetails(event.target.value)}
            placeholder="Four-wheel alignment and balancing check."
          />
        </Field>
        <Field label="Base price" required>
          <Input
            type="number"
            min={0}
            step="0.01"
            value={basePrice}
            onChange={(event) => setBasePrice(event.target.value)}
            className="font-mono"
            required
          />
        </Field>
      </form>
    </Modal>
  )
}

export default function InventoryPage() {
  const toast = useToast()
  const [tab, setTab] = useState<Tab>('PARTS')
  const [query, setQuery] = useState('')
  const debouncedQuery = useDebounced(query)
  const [partModalOpen, setPartModalOpen] = useState(false)
  const [editingPart, setEditingPart] = useState<SparePart | null>(null)
  const [receivingPart, setReceivingPart] = useState<SparePart | null>(null)
  const [serviceModalOpen, setServiceModalOpen] = useState(false)

  const parts = useApi('inventory-parts', () => api.parts.list())
  const services = useApi('inventory-services', () => api.services.list())

  const needle = debouncedQuery.trim().toLowerCase()
  const partRows = (parts.data ?? []).filter((part) => {
    if (!needle) return true
    return (
      part.partNumber.toLowerCase().includes(needle) ||
      part.name.toLowerCase().includes(needle)
    )
  })
  const serviceRows = (services.data ?? []).filter((service) => {
    if (!needle) return true
    return (
      service.name.toLowerCase().includes(needle) ||
      (service.details ?? '').toLowerCase().includes(needle)
    )
  })

  const lowStockCount = (parts.data ?? []).filter((part) => part.lowStock).length

  return (
    <>
      <PageHeader
        eyebrow="Inventory"
        title="Parts and services"
        description="What the store holds and what the workshop charges for labour."
        actions={
          tab === 'PARTS' ? (
            <Button
              onClick={() => {
                setEditingPart(null)
                setPartModalOpen(true)
              }}
            >
              <Icon name="plus" className="h-4 w-4" />
              Add part
            </Button>
          ) : (
            <Button onClick={() => setServiceModalOpen(true)}>
              <Icon name="plus" className="h-4 w-4" />
              Add service
            </Button>
          )
        }
      />

      {lowStockCount > 0 && (
        <div className="mb-4 flex items-start gap-2.5 rounded-lg border border-amber/25 bg-amber-soft px-4 py-3 text-[12.5px] leading-relaxed text-amber">
          <Icon name="alert" className="mt-0.5 h-4 w-4 shrink-0" />
          <span>
            {lowStockCount} part{lowStockCount === 1 ? '' : 's'} at or below the reorder level.
            Receive stock before the next job needs them.
          </span>
        </div>
      )}

      <Card>
        <div className="space-y-3 border-b border-line px-4 py-3">
          <div className="flex flex-wrap items-center gap-3">
            <SearchBox
              value={query}
              onChange={setQuery}
              placeholder={tab === 'PARTS' ? 'Search part number or name' : 'Search service'}
              className="w-full sm:w-80"
            />
            <Tabs
              value={tab}
              onChange={setTab}
              items={[
                { value: 'PARTS', label: 'Spare parts', count: parts.data?.length },
                { value: 'SERVICES', label: 'Service catalogue', count: services.data?.length },
              ]}
            />
          </div>
        </div>

        {tab === 'PARTS' && (
          <>
            {parts.error && <ErrorBlock message={parts.error} onRetry={parts.reload} />}
            {parts.loading && !parts.data && <LoadingBlock />}
            {parts.data && partRows.length === 0 && (
              <EmptyState
                icon="box"
                title={needle ? 'No part matches that search' : 'No spare parts yet'}
                description="Add the parts the workshop keeps on the shelf."
                action={
                  !needle && (
                    <Button
                      size="sm"
                      onClick={() => {
                        setEditingPart(null)
                        setPartModalOpen(true)
                      }}
                    >
                      Add part
                    </Button>
                  )
                }
              />
            )}
            {partRows.length > 0 && (
              <div className="overflow-x-auto">
                <table className="w-full border-collapse text-left">
                  <thead>
                    <tr>
                      <th className={thClass}>Part</th>
                      <th className={`${thClass} text-right`}>Unit price</th>
                      <th className={`${thClass} text-right`}>Stock</th>
                      <th className={`${thClass} text-right`}>Reorder at</th>
                      <th className={thClass}>Status</th>
                      <th className={thClass}>
                        <span className="sr-only">Actions</span>
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    {partRows.map((part) => (
                      <tr key={part.id} className={rowClass}>
                        <td className={tdClass}>
                          <span className="font-medium text-ink">{part.name}</span>
                          <span className="ml-2 font-mono text-[11px] text-ink-soft">
                            {part.partNumber}
                          </span>
                        </td>
                        <td className={`${tdClass} tnum text-right font-mono`}>
                          {money(part.unitPrice)}
                        </td>
                        <td
                          className={`${tdClass} tnum text-right font-mono ${
                            part.lowStock ? 'font-semibold text-amber' : ''
                          }`}
                        >
                          {part.stockQty}
                        </td>
                        <td className={`${tdClass} tnum text-right font-mono text-ink-soft`}>
                          {part.reorderLevel}
                        </td>
                        <td className={tdClass}>
                          {part.stockQty === 0 ? (
                            <Badge tone="danger">Out of stock</Badge>
                          ) : part.lowStock ? (
                            <Badge tone="amber">Low stock</Badge>
                          ) : (
                            <Badge tone="brand">In stock</Badge>
                          )}
                        </td>
                        <td className={`${tdClass} text-right`}>
                          <div className="flex justify-end gap-1.5">
                            <Button
                              size="sm"
                              variant="secondary"
                              onClick={() => setReceivingPart(part)}
                            >
                              Receive
                            </Button>
                            <Button
                              size="sm"
                              variant="ghost"
                              onClick={() => {
                                setEditingPart(part)
                                setPartModalOpen(true)
                              }}
                            >
                              Edit
                            </Button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </>
        )}

        {tab === 'SERVICES' && (
          <>
            {services.error && <ErrorBlock message={services.error} onRetry={services.reload} />}
            {services.loading && !services.data && <LoadingBlock />}
            {services.data && serviceRows.length === 0 && (
              <EmptyState
                icon="wrench"
                title={needle ? 'No service matches that search' : 'No catalogue services yet'}
                description="Add the labour items the workshop sells, e.g. oil change or wheel alignment."
                action={
                  !needle && (
                    <Button size="sm" onClick={() => setServiceModalOpen(true)}>
                      Add service
                    </Button>
                  )
                }
              />
            )}
            {serviceRows.length > 0 && (
              <div className="overflow-x-auto">
                <table className="w-full border-collapse text-left">
                  <thead>
                    <tr>
                      <th className={thClass}>Service</th>
                      <th className={thClass}>Details</th>
                      <th className={`${thClass} text-right`}>Catalogue price</th>
                    </tr>
                  </thead>
                  <tbody>
                    {serviceRows.map((service) => (
                      <tr key={service.id} className={rowClass}>
                        <td className={`${tdClass} font-medium`}>{service.name}</td>
                        <td className={`${tdClass} text-ink-soft`}>{service.details || '—'}</td>
                        <td className={`${tdClass} tnum text-right font-mono`}>
                          {money(service.basePrice)}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </>
        )}
      </Card>

      <PartFormModal
        open={partModalOpen}
        part={editingPart}
        onClose={() => setPartModalOpen(false)}
        onSaved={() => {
          toast(editingPart ? 'Spare part updated.' : 'Spare part added.')
          parts.reload()
        }}
      />

      <ReceiveStockModal
        part={receivingPart}
        onClose={() => setReceivingPart(null)}
        onSaved={() => {
          toast('Stock received.')
          parts.reload()
        }}
      />

      <ServiceFormModal
        open={serviceModalOpen}
        onClose={() => setServiceModalOpen(false)}
        onSaved={() => {
          toast('Service added to the catalogue.')
          services.reload()
        }}
      />
    </>
  )
}
