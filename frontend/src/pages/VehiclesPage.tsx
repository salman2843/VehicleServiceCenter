import { useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api'
import { Icon } from '../components/Icons'
import { Drawer } from '../components/Modal'
import { useToast } from '../components/Toast'
import {
  Badge,
  Button,
  Card,
  EmptyState,
  ErrorBlock,
  Facts,
  LoadingBlock,
  PageHeader,
  SearchBox,
  StatusBadge,
  Tabs,
  rowClass,
  tdClass,
  thClass,
} from '../components/ui'
import { VehicleFormModal } from '../components/VehicleForm'
import { formatDate, vehicleLabel } from '../format'
import { useApi, useDebounced } from '../hooks'
import type { VehicleDetail } from '../types'

type TypeFilter = 'ALL' | 'CAR' | 'MOTORCYCLE'

export default function VehiclesPage() {
  const toast = useToast()
  const [query, setQuery] = useState('')
  const debouncedQuery = useDebounced(query)
  const [typeFilter, setTypeFilter] = useState<TypeFilter>('ALL')
  const [formOpen, setFormOpen] = useState(false)
  const [detailId, setDetailId] = useState<number | null>(null)

  const vehicles = useApi('vehicles', () => api.vehicles.list())
  const customers = useApi('customers-all', () => api.customers.list())
  const detail = useApi<VehicleDetail | null>(
    detailId ? `vehicle:${detailId}` : 'vehicle:none',
    () => (detailId ? api.vehicles.get(detailId) : Promise.resolve(null)),
  )

  const needle = debouncedQuery.trim().toLowerCase()
  const rows = (vehicles.data ?? []).filter((vehicle) => {
    if (typeFilter !== 'ALL' && vehicle.vehicleType !== typeFilter) return false
    if (!needle) return true
    return (
      vehicle.registrationNo.toLowerCase().includes(needle) ||
      vehicle.make.toLowerCase().includes(needle) ||
      vehicle.modelName.toLowerCase().includes(needle) ||
      (vehicle.customerName ?? '').toLowerCase().includes(needle)
    )
  })

  const carCount = (vehicles.data ?? []).filter((vehicle) => vehicle.vehicleType === 'CAR').length
  const bikeCount = (vehicles.data ?? []).length - carCount

  return (
    <>
      <PageHeader
        eyebrow="Vehicles"
        title="Vehicles"
        description="Every vehicle on file, with its owner and service history."
        actions={
          <Button onClick={() => setFormOpen(true)} disabled={!customers.data?.length}>
            <Icon name="plus" className="h-4 w-4" />
            Add vehicle
          </Button>
        }
      />

      <Card>
        <div className="flex flex-wrap items-center gap-3 border-b border-line px-4 py-3">
          <SearchBox
            value={query}
            onChange={setQuery}
            placeholder="Search plate, make or owner"
            className="w-full sm:w-80"
          />
          <Tabs
            value={typeFilter}
            onChange={setTypeFilter}
            items={[
              { value: 'ALL', label: 'All', count: vehicles.data?.length },
              { value: 'CAR', label: 'Cars', count: carCount },
              { value: 'MOTORCYCLE', label: 'Motorcycles', count: bikeCount },
            ]}
          />
        </div>

        {vehicles.error && <ErrorBlock message={vehicles.error} onRetry={vehicles.reload} />}
        {vehicles.loading && !vehicles.data && <LoadingBlock />}

        {vehicles.data && rows.length === 0 && (
          <EmptyState
            icon="car"
            title={needle ? 'No vehicle matches that search' : 'No vehicles yet'}
            description={
              customers.data?.length
                ? 'Add a vehicle to a registered customer to open job cards for it.'
                : 'Register a customer first, then add their vehicle.'
            }
            action={
              customers.data?.length ? (
                <Button size="sm" onClick={() => setFormOpen(true)}>
                  Add vehicle
                </Button>
              ) : (
                <Link to="/customers">
                  <Button size="sm">Go to customers</Button>
                </Link>
              )
            }
          />
        )}

        {rows.length > 0 && (
          <div className="overflow-x-auto">
            <table className="w-full border-collapse text-left">
              <thead>
                <tr>
                  <th className={thClass}>Registration</th>
                  <th className={thClass}>Vehicle</th>
                  <th className={thClass}>Owner</th>
                  <th className={thClass}>Year</th>
                  <th className={thClass}>Type</th>
                  <th className={thClass}>
                    <span className="sr-only">Open</span>
                  </th>
                </tr>
              </thead>
              <tbody>
                {rows.map((vehicle) => (
                  <tr
                    key={vehicle.id}
                    className={`${rowClass} cursor-pointer`}
                    onClick={() => setDetailId(vehicle.id)}
                  >
                    <td className={`${tdClass} font-mono text-[12.5px] font-semibold`}>
                      {vehicle.registrationNo}
                    </td>
                    <td className={tdClass}>
                      {vehicleLabel(vehicle)}
                      <span className="block text-[11.5px] text-ink-soft">
                        {vehicle.vehicleType === 'CAR'
                          ? `${vehicle.numberOfDoors ?? 4} doors`
                          : `${vehicle.engineCapacityCc ?? 0} cc`}
                      </span>
                    </td>
                    <td className={tdClass}>{vehicle.customerName ?? '—'}</td>
                    <td className={`${tdClass} font-mono text-[12.5px] text-ink-soft`}>
                      {vehicle.year}
                    </td>
                    <td className={tdClass}>
                      <Badge tone={vehicle.vehicleType === 'CAR' ? 'slate' : 'neutral'}>
                        {vehicle.vehicleType === 'CAR' ? 'Car' : 'Motorcycle'}
                      </Badge>
                    </td>
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

      <VehicleFormModal
        open={formOpen}
        onClose={() => setFormOpen(false)}
        customers={customers.data ?? []}
        onSaved={() => {
          toast('Vehicle added.')
          vehicles.reload()
        }}
      />

      <Drawer
        open={detailId !== null}
        onClose={() => setDetailId(null)}
        eyebrow="Vehicle"
        title={detail.data?.registrationNo ?? 'Loading…'}
        footer={
          detail.data && (
            <Link to={`/jobs?new=1&vehicleId=${detail.data.id}`}>
              <Button>Open job card</Button>
            </Link>
          )
        }
      >
        {detail.loading && !detail.data && <LoadingBlock />}
        {detail.error && <ErrorBlock message={detail.error} onRetry={detail.reload} />}

        {detail.data && (
          <div className="space-y-6">
            <div>
              <p className="text-[17px] font-semibold tracking-[-0.01em] text-ink">
                {detail.data.make} {detail.data.modelName}
              </p>
              <p className="mt-0.5 text-[12.5px] text-ink-soft">
                {detail.data.vehicleType === 'CAR' ? 'Car' : 'Motorcycle'} ·{' '}
                {detail.data.year}
              </p>
            </div>

            <Facts
              items={[
                { label: 'Owner', value: detail.data.customerName ?? '—' },
                {
                  label: 'Type',
                  value: detail.data.vehicleType === 'CAR' ? 'Car' : 'Motorcycle',
                },
                detail.data.vehicleType === 'CAR'
                  ? { label: 'Doors', value: detail.data.numberOfDoors ?? 4 }
                  : { label: 'Engine', value: `${detail.data.engineCapacityCc ?? 0} cc` },
                { label: 'Registration', value: <span className="font-mono">{detail.data.registrationNo}</span> },
              ]}
            />

            <section>
              <h3 className="text-[13px] font-semibold text-ink">Service history</h3>
              <p className="text-[11.5px] text-ink-soft">
                {detail.data.jobs.length === 0
                  ? 'No job cards for this vehicle yet.'
                  : `${detail.data.jobs.length} job card${detail.data.jobs.length === 1 ? '' : 's'}`}
              </p>

              {detail.data.jobs.length > 0 && (
                <ul className="mt-3 space-y-2">
                  {detail.data.jobs.map((job) => (
                    <li key={job.id}>
                      <Link
                        to={`/jobs/${job.id}`}
                        className="flex items-start justify-between gap-3 rounded-lg border border-line px-3.5 py-3 transition-colors hover:border-brand/35 hover:bg-brand-soft/35"
                      >
                        <div className="min-w-0">
                          <p className="font-mono text-[12px] font-semibold text-ink">
                            Job #{job.id}
                          </p>
                          <p className="mt-0.5 line-clamp-2 text-[12px] text-ink-soft">
                            {job.complaint}
                          </p>
                          <p className="mt-1 font-mono text-[10.5px] text-ink-soft/80">
                            {formatDate(job.receivedOn)}
                          </p>
                        </div>
                        <StatusBadge status={job.status} />
                      </Link>
                    </li>
                  ))}
                </ul>
              )}
            </section>
          </div>
        )}
      </Drawer>
    </>
  )
}
