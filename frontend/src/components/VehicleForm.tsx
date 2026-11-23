import { useEffect, useRef, useState, type FormEvent } from 'react'
import { api } from '../api'
import { errorMessage } from '../hooks'
import type { Customer, Vehicle, VehicleType } from '../types'
import { Icon } from './Icons'
import { Modal } from './Modal'
import { Button, Field, FormError, Input, Select } from './ui'

const CURRENT_YEAR = new Date().getFullYear()

export function VehicleFormModal({
  open,
  onClose,
  onSaved,
  customers,
  defaultCustomerId,
}: {
  open: boolean
  onClose: () => void
  onSaved: (vehicle: Vehicle) => void
  customers: Customer[]
  defaultCustomerId?: number
}) {
  const [customerId, setCustomerId] = useState(0)
  const [type, setType] = useState<VehicleType>('CAR')
  const [registrationNo, setRegistrationNo] = useState('')
  const [make, setMake] = useState('')
  const [modelName, setModelName] = useState('')
  const [year, setYear] = useState(String(CURRENT_YEAR))
  const [doors, setDoors] = useState('4')
  const [engineCc, setEngineCc] = useState('150')
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
    setCustomerId(defaultCustomerId ?? customers[0]?.id ?? 0)
    setType('CAR')
    setRegistrationNo('')
    setMake('')
    setModelName('')
    setYear(String(CURRENT_YEAR))
    setDoors('4')
    setEngineCc('150')
    setError(null)
    setSaving(false)
  }, [open, defaultCustomerId, customers])

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSaving(true)
    setError(null)
    try {
      const vehicle = await api.vehicles.create({
        customerId,
        vehicleType: type,
        registrationNo,
        make,
        modelName,
        year: Number(year),
        ...(type === 'CAR'
          ? { numberOfDoors: Number(doors) }
          : { engineCapacityCc: Number(engineCc) }),
      })
      onSaved(vehicle)
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
      title="Add a vehicle"
      description="The registration number is stored in one shape, so plates never duplicate."
      footer={
        <>
          <Button variant="ghost" onClick={onClose}>
            Cancel
          </Button>
          <Button type="submit" form="vehicle-form" disabled={saving || !customerId}>
            {saving ? 'Saving…' : 'Save vehicle'}
          </Button>
        </>
      }
    >
      <form id="vehicle-form" onSubmit={submit} className="space-y-4">
        {error && <FormError message={error} />}

        <Field label="Owner" required>
          <Select
            value={customerId}
            onChange={(event) => setCustomerId(Number(event.target.value))}
            required
          >
            <option value={0} disabled>
              Choose a customer
            </option>
            {customers.map((customer) => (
              <option key={customer.id} value={customer.id}>
                {customer.name} · {customer.phone}
              </option>
            ))}
          </Select>
        </Field>

        <div>
          <span className="mb-1.5 block text-[12px] font-medium text-ink-soft">Vehicle type</span>
          <div className="grid grid-cols-2 gap-2">
            {(['CAR', 'MOTORCYCLE'] as VehicleType[]).map((option) => (
              <button
                key={option}
                type="button"
                onClick={() => setType(option)}
                className={`flex h-[46px] items-center justify-center gap-2 rounded-lg border text-[13px] font-medium transition-colors ${
                  type === option
                    ? 'border-brand bg-brand-soft text-brand-dark'
                    : 'border-line bg-surface text-ink-soft hover:border-ink-soft/30'
                }`}
              >
                <Icon name={option === 'CAR' ? 'car' : 'bike'} className="h-[18px] w-[18px]" />
                {option === 'CAR' ? 'Car' : 'Motorcycle'}
              </button>
            ))}
          </div>
        </div>

        <div className="grid gap-4 sm:grid-cols-2">
          <Field label="Registration number" required hint="e.g. KA01AB1234">
            <Input
              value={registrationNo}
              onChange={(event) => setRegistrationNo(event.target.value)}
              placeholder="KA01AB1234"
              className="font-mono uppercase"
              required
            />
          </Field>
          <Field label="Model year" required>
            <Input
              type="number"
              value={year}
              onChange={(event) => setYear(event.target.value)}
              min={1980}
              max={CURRENT_YEAR + 1}
              className="font-mono"
              required
            />
          </Field>
          <Field label="Make" required>
            <Input
              value={make}
              onChange={(event) => setMake(event.target.value)}
              placeholder="Maruti"
              required
            />
          </Field>
          <Field label="Model" required>
            <Input
              value={modelName}
              onChange={(event) => setModelName(event.target.value)}
              placeholder="Swift"
              required
            />
          </Field>
          {type === 'CAR' ? (
            <Field label="Number of doors" required hint="2 to 7">
              <Input
                type="number"
                value={doors}
                onChange={(event) => setDoors(event.target.value)}
                min={2}
                max={7}
                className="font-mono"
                required
              />
            </Field>
          ) : (
            <Field label="Engine capacity (cc)" required>
              <Input
                type="number"
                value={engineCc}
                onChange={(event) => setEngineCc(event.target.value)}
                min={1}
                className="font-mono"
                required
              />
            </Field>
          )}
        </div>
      </form>
    </Modal>
  )
}
