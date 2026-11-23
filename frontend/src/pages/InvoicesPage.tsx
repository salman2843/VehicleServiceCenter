import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../api'
import {
  Card,
  EmptyState,
  ErrorBlock,
  LoadingBlock,
  PageHeader,
  SearchBox,
  StatusBadge,
  Tabs,
  rowClass,
  tdClass,
  thClass,
} from '../components/ui'
import { formatDate, money, statusLabel } from '../format'
import { useApi, useDebounced } from '../hooks'

type Tab = 'ALL' | 'UNPAID' | 'PARTIALLY_PAID' | 'PAID'

const tabs: { value: Tab; label: string }[] = [
  { value: 'ALL', label: 'All' },
  { value: 'UNPAID', label: 'Unpaid' },
  { value: 'PARTIALLY_PAID', label: 'Part paid' },
  { value: 'PAID', label: 'Paid' },
]

export default function InvoicesPage() {
  const navigate = useNavigate()
  const [query, setQuery] = useState('')
  const debouncedQuery = useDebounced(query)
  const [tab, setTab] = useState<Tab>('ALL')

  const invoices = useApi('invoices', () => api.invoices.list())

  const all = invoices.data ?? []
  const counts = {
    ALL: all.length,
    UNPAID: all.filter((invoice) => invoice.paymentStatus === 'UNPAID').length,
    PARTIALLY_PAID: all.filter((invoice) => invoice.paymentStatus === 'PARTIALLY_PAID').length,
    PAID: all.filter((invoice) => invoice.paymentStatus === 'PAID').length,
  }

  const needle = debouncedQuery.trim().toLowerCase()
  const rows = all.filter((invoice) => {
    if (tab !== 'ALL' && invoice.paymentStatus !== tab) return false
    if (!needle) return true
    return (
      String(invoice.id) === needle ||
      String(invoice.jobCardId) === needle ||
      (invoice.registrationNo ?? '').toLowerCase().includes(needle) ||
      (invoice.customerName ?? '').toLowerCase().includes(needle)
    )
  })

  const outstanding = all.reduce((sum, invoice) => sum + invoice.outstanding, 0)

  return (
    <>
      <PageHeader
        eyebrow="Invoices"
        title="Invoices"
        description={`${all.length} invoice${all.length === 1 ? '' : 's'} issued · ${money(outstanding)} still outstanding.`}
      />

      <Card>
        <div className="space-y-3 border-b border-line px-4 py-3">
          <div className="flex flex-wrap items-center gap-3">
            <SearchBox
              value={query}
              onChange={setQuery}
              placeholder="Search invoice, job, plate or customer"
              className="w-full sm:w-96"
            />
            <p className="ml-auto text-[12px] text-ink-soft">
              {invoices.data ? `${rows.length} shown` : '—'}
            </p>
          </div>
          <Tabs
            value={tab}
            onChange={setTab}
            items={tabs.map((item) => ({ ...item, count: counts[item.value] }))}
          />
        </div>

        {invoices.error && <ErrorBlock message={invoices.error} onRetry={invoices.reload} />}
        {invoices.loading && !invoices.data && <LoadingBlock />}

        {invoices.data && rows.length === 0 && (
          <EmptyState
            icon="receipt"
            title={
              needle
                ? 'No invoice matches that search'
                : tab === 'ALL'
                  ? 'No invoices yet'
                  : `No ${statusLabel(tab).toLowerCase()} invoices`
            }
            description={
              tab === 'ALL'
                ? 'Invoices are generated from a completed job card.'
                : 'Switch to another payment state.'
            }
          />
        )}

        {rows.length > 0 && (
          <div className="overflow-x-auto">
            <table className="w-full border-collapse text-left">
              <thead>
                <tr>
                  <th className={thClass}>Invoice</th>
                  <th className={thClass}>Job</th>
                  <th className={thClass}>Vehicle</th>
                  <th className={thClass}>Customer</th>
                  <th className={`${thClass} text-right`}>Total</th>
                  <th className={`${thClass} text-right`}>Paid</th>
                  <th className={`${thClass} text-right`}>Outstanding</th>
                  <th className={thClass}>Status</th>
                  <th className={thClass}>Issued</th>
                </tr>
              </thead>
              <tbody>
                {rows.map((invoice) => (
                  <tr
                    key={invoice.id}
                    className={`${rowClass} cursor-pointer`}
                    onClick={() => navigate(`/invoices/${invoice.id}`)}
                  >
                    <td className={`${tdClass} font-mono text-[12.5px] font-semibold`}>
                      #{invoice.id}
                    </td>
                    <td className={`${tdClass} font-mono text-[12px] text-ink-soft`}>
                      #{invoice.jobCardId}
                    </td>
                    <td className={`${tdClass} font-mono text-[12.5px]`}>
                      {invoice.registrationNo ?? '—'}
                    </td>
                    <td className={tdClass}>{invoice.customerName ?? '—'}</td>
                    <td className={`${tdClass} tnum text-right font-mono`}>
                      {money(invoice.total)}
                    </td>
                    <td className={`${tdClass} tnum text-right font-mono text-ink-soft`}>
                      {money(invoice.paid)}
                    </td>
                    <td
                      className={`${tdClass} tnum text-right font-mono ${
                        invoice.outstanding > 0 ? 'text-amber' : 'text-ink-soft'
                      }`}
                    >
                      {money(invoice.outstanding)}
                    </td>
                    <td className={tdClass}>
                      <StatusBadge status={invoice.paymentStatus} />
                    </td>
                    <td className={`${tdClass} text-ink-soft`}>{formatDate(invoice.createdOn)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>
    </>
  )
}
