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
  rowClass,
  tdClass,
  thClass,
} from '../components/ui'
import { errorMessage, useApi, useDebounced } from '../hooks'
import type { Mechanic, MechanicInput } from '../types'

function MechanicFormModal({
  open,
  mechanic,
  onClose,
  onSaved,
}: {
  open: boolean
  mechanic: Mechanic | null
  onClose: () => void
  onSaved: () => void
}) {
  const [name, setName] = useState('')
  const [phone, setPhone] = useState('')
  const [specialization, setSpecialization] = useState('')
  const [active, setActive] = useState(true)
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
    setName(mechanic?.name ?? '')
    setPhone(mechanic?.phone ?? '')
    setSpecialization(mechanic?.specialization ?? '')
    setActive(mechanic?.active ?? true)
    setError(null)
    setSaving(false)
  }, [open, mechanic])

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSaving(true)
    setError(null)
    const input: MechanicInput = { name, phone, specialization, active }
    try {
      if (mechanic) {
        await api.mechanics.update(mechanic.id, input)
      } else {
        await api.mechanics.create(input)
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
      title={mechanic ? 'Edit mechanic' : 'Add a mechanic'}
      description="Only active mechanics can be assigned to a job card."
      footer={
        <>
          <Button variant="ghost" onClick={onClose}>
            Cancel
          </Button>
          <Button type="submit" form="mechanic-form" disabled={saving}>
            {saving ? 'Saving…' : mechanic ? 'Save changes' : 'Add mechanic'}
          </Button>
        </>
      }
    >
      <form id="mechanic-form" onSubmit={submit} className="space-y-4">
        {error && <FormError message={error} />}
        <div className="grid gap-4 sm:grid-cols-2">
          <Field label="Name" required>
            <Input
              value={name}
              onChange={(event) => setName(event.target.value)}
              placeholder="Ravi Kumar"
              autoFocus
              required
            />
          </Field>
          <Field label="Phone" hint="Optional, 7 to 15 digits.">
            <Input
              value={phone}
              onChange={(event) => setPhone(event.target.value)}
              placeholder="+91 90000 00000"
              className="font-mono"
            />
          </Field>
        </div>
        <Field label="Specialization" hint="Optional, e.g. engine, electrical, bodywork.">
          <Input
            value={specialization}
            onChange={(event) => setSpecialization(event.target.value)}
            placeholder="Engine and transmission"
          />
        </Field>
        <label className="flex items-center gap-2.5 text-[13px] text-ink">
          <input
            type="checkbox"
            checked={active}
            onChange={(event) => setActive(event.target.checked)}
            className="h-4 w-4 rounded border-line accent-brand"
          />
          Available for new jobs
        </label>
      </form>
    </Modal>
  )
}

export default function MechanicsPage() {
  const toast = useToast()
  const [query, setQuery] = useState('')
  const debouncedQuery = useDebounced(query)
  const [formOpen, setFormOpen] = useState(false)
  const [editing, setEditing] = useState<Mechanic | null>(null)

  const mechanics = useApi('mechanics', () => api.mechanics.list())

  const needle = debouncedQuery.trim().toLowerCase()
  const rows = (mechanics.data ?? []).filter((mechanic) => {
    if (!needle) return true
    return (
      mechanic.name.toLowerCase().includes(needle) ||
      (mechanic.phone ?? '').toLowerCase().includes(needle) ||
      (mechanic.specialization ?? '').toLowerCase().includes(needle)
    )
  })

  const activeCount = (mechanics.data ?? []).filter((mechanic) => mechanic.active).length

  return (
    <>
      <PageHeader
        eyebrow="Mechanics"
        title="Mechanics"
        description={`${activeCount} available for new jobs. Inactive mechanics stay in history but cannot be assigned.`}
        actions={
          <Button
            onClick={() => {
              setEditing(null)
              setFormOpen(true)
            }}
          >
            <Icon name="plus" className="h-4 w-4" />
            Add mechanic
          </Button>
        }
      />

      <Card>
        <div className="flex flex-wrap items-center gap-3 border-b border-line px-4 py-3">
          <SearchBox
            value={query}
            onChange={setQuery}
            placeholder="Search name, phone or skill"
            className="w-full sm:w-80"
          />
          <p className="ml-auto text-[12px] text-ink-soft">
            {mechanics.data ? `${rows.length} shown` : '—'}
          </p>
        </div>

        {mechanics.error && <ErrorBlock message={mechanics.error} onRetry={mechanics.reload} />}
        {mechanics.loading && !mechanics.data && <LoadingBlock />}

        {mechanics.data && rows.length === 0 && (
          <EmptyState
            icon="wrench"
            title={needle ? 'No mechanic matches that search' : 'No mechanics yet'}
            description="Add the technicians who work on job cards."
            action={
              !needle && (
                <Button
                  size="sm"
                  onClick={() => {
                    setEditing(null)
                    setFormOpen(true)
                  }}
                >
                  Add mechanic
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
                  <th className={thClass}>Name</th>
                  <th className={thClass}>Phone</th>
                  <th className={thClass}>Specialization</th>
                  <th className={thClass}>Status</th>
                  <th className={thClass}>
                    <span className="sr-only">Edit</span>
                  </th>
                </tr>
              </thead>
              <tbody>
                {rows.map((mechanic) => (
                  <tr key={mechanic.id} className={rowClass}>
                    <td className={`${tdClass} font-medium`}>{mechanic.name}</td>
                    <td className={`${tdClass} font-mono text-[12.5px] text-ink-soft`}>
                      {mechanic.phone || '—'}
                    </td>
                    <td className={`${tdClass} text-ink-soft`}>
                      {mechanic.specialization || '—'}
                    </td>
                    <td className={tdClass}>
                      <Badge tone={mechanic.active ? 'brand' : 'neutral'}>
                        {mechanic.active ? 'Active' : 'Inactive'}
                      </Badge>
                    </td>
                    <td className={`${tdClass} text-right`}>
                      <Button
                        size="sm"
                        variant="ghost"
                        onClick={() => {
                          setEditing(mechanic)
                          setFormOpen(true)
                        }}
                      >
                        Edit
                      </Button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>

      <MechanicFormModal
        open={formOpen}
        mechanic={editing}
        onClose={() => setFormOpen(false)}
        onSaved={() => {
          toast(editing ? 'Mechanic updated.' : 'Mechanic added.')
          mechanics.reload()
        }}
      />
    </>
  )
}
