import { useEffect, useRef, useState, type FormEvent } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { api } from '../api'
import { Icon } from '../components/Icons'
import { Modal } from '../components/Modal'
import { useToast } from '../components/Toast'
import {
  Button,
  Card,
  EmptyState,
  ErrorBlock,
  Field,
  FormError,
  LoadingBlock,
  PageHeader,
  SearchBox,
  Select,
  StatusBadge,
  Tabs,
  Textarea,
  rowClass,
  tdClass,
  thClass,
} from '../components/ui'
import { formatDate, statusLabel } from '../format'
import { errorMessage, useApi, useDebounced } from '../hooks'
import type { JobStatus, Vehicle } from '../types'

type Tab = 'ALL' | JobStatus

const tabs: { value: Tab; label: string }[] = [
  { value: 'ALL', label: 'All' },
  { value: 'RECEIVED', label: 'Received' },
  { value: 'IN_PROGRESS', label: 'In progress' },
  { value: 'COMPLETED', label: 'Completed' },
  { value: 'DELIVERED', label: 'Delivered' },
]

function NewJobModal({
  open,
  onClose,
  vehicles,
  defaultVehicleId,
}: {
  open: boolean
  onClose: () => void
  vehicles: Vehicle[]
  defaultVehicleId?: number
}) {
  const toast = useToast()
  const navigate = useNavigate()
  const [vehicleId, setVehicleId] = useState(0)
  const [complaint, setComplaint] = useState('')
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
    setVehicleId(defaultVehicleId ?? vehicles[0]?.id ?? 0)
    setComplaint('')
    setError(null)
    setSaving(false)
  }, [open, defaultVehicleId, vehicles])

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSaving(true)
    setError(null)
    try {
      const job = await api.jobs.open(vehicleId, complaint)
      toast(`Job card #${job.id} opened.`)
      onClose()
      navigate(`/jobs/${job.id}`)
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
      title="Open a job card"
      description="Record what the customer reported. Services and parts are added on the job screen."
      footer={
        <>
          <Button variant="ghost" onClick={onClose}>
            Cancel
          </Button>
          <Button type="submit" form="job-form" disabled={saving || !vehicleId}>
            {saving ? 'Opening…' : 'Open job card'}
          </Button>
        </>
      }
    >
      <form id="job-form" onSubmit={submit} className="space-y-4">
        {error && <FormError message={error} />}
        {vehicles.length === 0 ? (
          <div className="rounded-lg border border-line bg-muted/50 px-4 py-5 text-[12.5px] text-ink-soft">
            No vehicle is on file yet. Add a customer and their vehicle first.
          </div>
        ) : (
          <>
            <Field label="Vehicle" required>
              <Select
                value={vehicleId}
                onChange={(event) => setVehicleId(Number(event.target.value))}
                required
              >
                <option value={0} disabled>
                  Choose a vehicle
                </option>
                {vehicles.map((vehicle) => (
                  <option key={vehicle.id} value={vehicle.id}>
                    {vehicle.registrationNo} · {vehicle.make} {vehicle.modelName} ·{' '}
                    {vehicle.customerName ?? 'Unknown owner'}
                  </option>
                ))}
              </Select>
            </Field>
            <Field
              label="Customer complaint"
              required
              hint="What the driver reported, in their words. Up to 2000 characters."
            >
              <Textarea
                value={complaint}
                onChange={(event) => setComplaint(event.target.value)}
                placeholder="Brake pedal feels soft and the car pulls to the left when braking."
                required
                autoFocus
              />
            </Field>
          </>
        )}
      </form>
    </Modal>
  )
}

export default function JobsPage() {
  const [params, setParams] = useSearchParams()
  const navigate = useNavigate()
  const [query, setQuery] = useState('')
  const debouncedQuery = useDebounced(query)

  const jobs = useApi('jobs-all', () => api.jobs.list())
  const vehicles = useApi('vehicles-all', () => api.vehicles.list())

  const rawStatus = params.get('status')
  const tab: Tab = tabs.some((item) => item.value === rawStatus) ? (rawStatus as Tab) : 'ALL'
  const newOpen = params.get('new') === '1'
  const defaultVehicleId = Number(params.get('vehicleId') ?? 0) || undefined

  function setTab(next: Tab) {
    const updated = new URLSearchParams(params)
    if (next === 'ALL') {
      updated.delete('status')
    } else {
      updated.set('status', next)
    }
    setParams(updated, { replace: true })
  }

  function closeNew() {
    const updated = new URLSearchParams(params)
    updated.delete('new')
    updated.delete('vehicleId')
    setParams(updated, { replace: true })
  }

  function openNew() {
    const updated = new URLSearchParams(params)
    updated.set('new', '1')
    setParams(updated, { replace: true })
  }

  const all = jobs.data ?? []
  const counts = {
    ALL: all.length,
    RECEIVED: all.filter((job) => job.status === 'RECEIVED').length,
    IN_PROGRESS: all.filter((job) => job.status === 'IN_PROGRESS').length,
    COMPLETED: all.filter((job) => job.status === 'COMPLETED').length,
    DELIVERED: all.filter((job) => job.status === 'DELIVERED').length,
  }

  const needle = debouncedQuery.trim().toLowerCase()
  const rows = all.filter((job) => {
    if (tab !== 'ALL' && job.status !== tab) return false
    if (!needle) return true
    return (
      String(job.id) === needle ||
      (job.registrationNo ?? '').toLowerCase().includes(needle) ||
      (job.customerName ?? '').toLowerCase().includes(needle) ||
      (job.complaint ?? '').toLowerCase().includes(needle) ||
      (job.mechanicName ?? '').toLowerCase().includes(needle)
    )
  })

  return (
    <>
      <PageHeader
        eyebrow="Job cards"
        title="Job cards"
        description="Each visit from the moment the vehicle arrives to the day it is delivered."
        actions={
          <Button onClick={openNew} disabled={!vehicles.data?.length}>
            <Icon name="plus" className="h-4 w-4" />
            New job card
          </Button>
        }
      />

      <Card>
        <div className="space-y-3 border-b border-line px-4 py-3">
          <div className="flex flex-wrap items-center gap-3">
            <SearchBox
              value={query}
              onChange={setQuery}
              placeholder="Search job, plate, customer or complaint"
              className="w-full sm:w-96"
            />
            <p className="ml-auto text-[12px] text-ink-soft">
              {jobs.data ? `${rows.length} shown` : '—'}
            </p>
          </div>
          <Tabs
            value={tab}
            onChange={setTab}
            items={tabs.map((item) => ({ ...item, count: counts[item.value] }))}
          />
        </div>

        {jobs.error && <ErrorBlock message={jobs.error} onRetry={jobs.reload} />}
        {jobs.loading && !jobs.data && <LoadingBlock />}

        {jobs.data && rows.length === 0 && (
          <EmptyState
            icon="clipboard"
            title={
              needle
                ? 'No job card matches that search'
                : tab === 'ALL'
                  ? 'No job cards yet'
                  : `No ${statusLabel(tab).toLowerCase()} jobs`
            }
            description={
              tab === 'ALL'
                ? 'Open a job card when a vehicle arrives at the workshop.'
                : 'Switch to another stage or open a new job card.'
            }
            action={
              tab === 'ALL' && (
                <Button size="sm" onClick={openNew} disabled={!vehicles.data?.length}>
                  New job card
                </Button>
              )
            }
          />
        )}

        {rows.length > 0 && (
          <div className="overflow-x-auto">
            <table className="w-full border-collapse text-left">
              <thead>
                <tr>
                  <th className={thClass}>Job</th>
                  <th className={thClass}>Vehicle</th>
                  <th className={thClass}>Customer</th>
                  <th className={thClass}>Complaint</th>
                  <th className={thClass}>Mechanic</th>
                  <th className={thClass}>Received</th>
                  <th className={thClass}>Status</th>
                </tr>
              </thead>
              <tbody>
                {rows.map((job) => (
                  <tr
                    key={job.id}
                    className={`${rowClass} cursor-pointer`}
                    onClick={() => navigate(`/jobs/${job.id}`)}
                  >
                    <td className={`${tdClass} font-mono text-[12.5px] text-ink-soft`}>
                      #{job.id}
                    </td>
                    <td className={tdClass}>
                      <span className="font-mono text-[12.5px] font-semibold">
                        {job.registrationNo ?? '—'}
                      </span>
                      <span className="block text-[11.5px] text-ink-soft">{job.vehicleLabel}</span>
                    </td>
                    <td className={tdClass}>{job.customerName ?? '—'}</td>
                    <td className={`${tdClass} max-w-[280px]`}>
                      <span className="line-clamp-2 text-[12.5px] text-ink-soft">
                        {job.complaint}
                      </span>
                    </td>
                    <td className={tdClass}>{job.mechanicName ?? 'Unassigned'}</td>
                    <td className={`${tdClass} text-ink-soft`}>{formatDate(job.receivedOn)}</td>
                    <td className={tdClass}>
                      <StatusBadge status={job.status} />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>

      <NewJobModal
        open={newOpen}
        onClose={closeNew}
        vehicles={vehicles.data ?? []}
        defaultVehicleId={defaultVehicleId}
      />
    </>
  )
}
