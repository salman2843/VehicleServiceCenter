import { Link, useNavigate } from 'react-router-dom'
import { api } from '../api'
import { Icon } from '../components/Icons'
import {
  Button,
  Card,
  CardHeader,
  EmptyState,
  ErrorBlock,
  LoadingBlock,
  PageHeader,
  Stat,
  StatusBadge,
  rowClass,
  tdClass,
  thClass,
} from '../components/ui'
import { formatDate, money, statusLabel } from '../format'
import { useApi } from '../hooks'

export default function DashboardPage() {
  const navigate = useNavigate()
  const { data, error, loading, reload } = useApi('dashboard', api.dashboard)

  const collectionRate =
    data && data.money.invoiced > 0
      ? Math.min(100, Math.round((data.money.collected / data.money.invoiced) * 100))
      : 0

  return (
    <>
      <PageHeader
        eyebrow="Overview"
        title="Workshop overview"
        description="What is in the workshop today, what has been billed, and what needs a decision."
        actions={
          <Button variant="secondary" onClick={reload} disabled={loading}>
            <Icon name="refresh" className="h-4 w-4" />
            Refresh
          </Button>
        }
      />

      {error && (
        <Card>
          <ErrorBlock message={error} onRetry={reload} />
        </Card>
      )}

      {!data && !error && (
        <Card>
          <LoadingBlock label="Reading the workshop ledger…" />
        </Card>
      )}

      {data && (
        <div className="space-y-4">
          <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
            <Stat
              label="Open jobs"
              value={data.counts.openJobs}
              hint={`${data.counts.jobs} job cards in total`}
              icon="clipboard"
            />
            <Stat
              label="Vehicles on file"
              value={data.counts.vehicles}
              hint={`${data.counts.customers} registered customers`}
              icon="car"
            />
            <Stat
              label="Unpaid invoices"
              value={data.counts.unpaidInvoices}
              hint={`${money(data.money.outstanding)} still to collect`}
              icon="receipt"
            />
            <Stat
              label="Low stock parts"
              value={data.counts.lowStockParts}
              hint="At or below the reorder level"
              icon="box"
            />
          </div>

          <div className="grid gap-4 xl:grid-cols-[1.7fr_1fr]">
            <div className="space-y-4">
              <Card>
                <CardHeader
                  title="Job pipeline"
                  description="Every job card by its current stage. Only one step forward is allowed."
                  action={
                    <Link
                      to="/jobs"
                      className="text-[12px] font-semibold text-brand hover:text-brand-dark"
                    >
                      All job cards
                    </Link>
                  }
                />
                <div className="grid grid-cols-2 gap-3 p-5 sm:grid-cols-4">
                  {data.jobsByStatus.map((row) => (
                    <Link
                      key={row.status}
                      to={`/jobs?status=${row.status}`}
                      className="rounded-lg border border-line px-3 py-3 transition-colors hover:border-brand/35 hover:bg-brand-soft/40"
                    >
                      <p className="text-[10.5px] font-semibold uppercase tracking-[0.1em] text-ink-soft">
                        {statusLabel(row.status)}
                      </p>
                      <p className="tnum mt-1 font-mono text-[23px] leading-none text-ink">
                        {row.count}
                      </p>
                      <div className="mt-2.5 h-1 overflow-hidden rounded-full bg-muted">
                        <div
                          className="h-full rounded-full bg-brand/70"
                          style={{
                            width: `${data.counts.jobs ? (row.count / data.counts.jobs) * 100 : 0}%`,
                          }}
                        />
                      </div>
                    </Link>
                  ))}
                </div>
              </Card>

              <Card>
                <CardHeader
                  title="Recent job cards"
                  description="The six most recent visits, newest first."
                  action={
                    <Link
                      to="/jobs"
                      className="text-[12px] font-semibold text-brand hover:text-brand-dark"
                    >
                      Open list
                    </Link>
                  }
                />
                {data.recentJobs.length === 0 ? (
                  <EmptyState
                    icon="clipboard"
                    title="No job cards yet"
                    description="Open the first job card when a vehicle arrives at the workshop."
                    action={
                      <Link to="/jobs?new=1">
                        <Button size="sm">New job card</Button>
                      </Link>
                    }
                  />
                ) : (
                  <div className="overflow-x-auto">
                    <table className="w-full border-collapse text-left">
                      <thead>
                        <tr>
                          <th className={thClass}>Job</th>
                          <th className={thClass}>Vehicle</th>
                          <th className={thClass}>Customer</th>
                          <th className={thClass}>Received</th>
                          <th className={thClass}>Status</th>
                        </tr>
                      </thead>
                      <tbody>
                        {data.recentJobs.map((job) => (
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
                              <span className="block text-[11.5px] text-ink-soft">
                                {job.vehicleLabel}
                              </span>
                            </td>
                            <td className={tdClass}>{job.customerName ?? '—'}</td>
                            <td className={`${tdClass} text-ink-soft`}>
                              {formatDate(job.receivedOn)}
                            </td>
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
            </div>

            <div className="space-y-4">
              <Card className="p-5">
                <p className="text-[10.5px] font-semibold uppercase tracking-[0.14em] text-ink-soft">
                  Collected
                </p>
                <p className="tnum mt-2 font-mono text-[30px] leading-none text-ink">
                  {money(data.money.collected)}
                </p>
                <p className="mt-1.5 text-[12px] text-ink-soft">
                  of {money(data.money.invoiced)} invoiced
                </p>
                <div className="mt-4 h-2 overflow-hidden rounded-full bg-muted">
                  <div
                    className="h-full rounded-full bg-brand transition-[width] duration-500"
                    style={{ width: `${collectionRate}%` }}
                  />
                </div>
                <div className="mt-2 flex items-center justify-between text-[11.5px]">
                  <span className="text-ink-soft">{collectionRate}% collected</span>
                  <span className="tnum font-mono text-amber">
                    {money(data.money.outstanding)} open
                  </span>
                </div>
              </Card>

              <Card>
                <CardHeader title="Needs attention" description="Low stock and unpaid bills." />
                <div className="divide-y divide-line/70">
                  {data.lowStock.length === 0 && data.outstandingInvoices.length === 0 && (
                    <div className="px-5 py-6 text-[12.5px] text-ink-soft">
                      Nothing urgent. Stock and payments are up to date.
                    </div>
                  )}

                  {data.lowStock.map((part) => (
                    <Link
                      key={`part-${part.id}`}
                      to="/inventory"
                      className="flex items-center justify-between gap-3 px-5 py-3 transition-colors hover:bg-muted/45"
                    >
                      <div className="min-w-0">
                        <p className="truncate text-[12.5px] font-medium text-ink">{part.name}</p>
                        <p className="font-mono text-[11px] text-ink-soft">{part.partNumber}</p>
                      </div>
                      <span className="shrink-0 rounded-full bg-amber-soft px-2 py-0.5 font-mono text-[10.5px] text-amber">
                        {part.stockQty} / {part.reorderLevel}
                      </span>
                    </Link>
                  ))}

                  {data.outstandingInvoices.map((invoice) => (
                    <Link
                      key={`invoice-${invoice.id}`}
                      to={`/invoices/${invoice.id}`}
                      className="flex items-center justify-between gap-3 px-5 py-3 transition-colors hover:bg-muted/45"
                    >
                      <div className="min-w-0">
                        <p className="truncate text-[12.5px] font-medium text-ink">
                          Invoice #{invoice.id} · {invoice.customerName ?? 'Customer'}
                        </p>
                        <p className="font-mono text-[11px] text-ink-soft">
                          {invoice.registrationNo ?? '—'}
                        </p>
                      </div>
                      <span className="tnum shrink-0 font-mono text-[11.5px] text-amber">
                        {money(invoice.outstanding)}
                      </span>
                    </Link>
                  ))}
                </div>
              </Card>
            </div>
          </div>
        </div>
      )}
    </>
  )
}
