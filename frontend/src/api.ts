import type {
  Customer,
  CustomerDetail,
  CustomerInput,
  Dashboard,
  Health,
  InvoiceDetail,
  InvoiceSummary,
  Job,
  JobDetail,
  JobStatus,
  Mechanic,
  MechanicInput,
  PartInput,
  PaymentMethod,
  ServiceInput,
  ServiceItem,
  SparePart,
  Vehicle,
  VehicleDetail,
  VehicleInput,
} from './types'

const BASE = import.meta.env.VITE_API_BASE ?? '/api'

export class ApiError extends Error {
  status: number
  field: string | null

  constructor(message: string, status: number, field: string | null = null) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.field = field
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  let response: Response
  try {
    response = await fetch(BASE + path, {
      ...init,
      headers: {
        'Content-Type': 'application/json',
        ...(init?.headers ?? {}),
      },
    })
  } catch {
    throw new ApiError(
      'Cannot reach the server. Start the Java API with "mvn exec:java" on port 8080.',
      0,
    )
  }

  if (response.status === 204) {
    return undefined as T
  }

  const text = await response.text()
  let payload: unknown = null
  if (text) {
    try {
      payload = JSON.parse(text)
    } catch {
      payload = null
    }
  }

  if (!response.ok) {
    const body = payload as { error?: string; field?: string | null } | null
    throw new ApiError(
      body?.error ?? `Request failed with status ${response.status}.`,
      response.status,
      body?.field ?? null,
    )
  }

  return payload as T
}

const json = (value: unknown): RequestInit => ({
  method: 'POST',
  body: JSON.stringify(value),
})

const put = (value: unknown): RequestInit => ({
  method: 'PUT',
  body: JSON.stringify(value),
})

export const api = {
  health: () => request<Health>('/health'),

  dashboard: () => request<Dashboard>('/dashboard'),

  customers: {
    list: (q = '') => request<Customer[]>(`/customers?q=${encodeURIComponent(q)}`),
    get: (id: number) => request<CustomerDetail>(`/customers/${id}`),
    create: (input: CustomerInput) => request<Customer>('/customers', json(input)),
    update: (id: number, input: CustomerInput) =>
      request<Customer>(`/customers/${id}`, put(input)),
  },

  vehicles: {
    list: (q = '') => request<Vehicle[]>(`/vehicles?q=${encodeURIComponent(q)}`),
    get: (id: number) => request<VehicleDetail>(`/vehicles/${id}`),
    create: (input: VehicleInput) => request<Vehicle>('/vehicles', json(input)),
    byCustomer: (customerId: number) =>
      request<Vehicle[]>(`/vehicles?customerId=${customerId}`),
  },

  mechanics: {
    list: () => request<Mechanic[]>('/mechanics'),
    active: () => request<Mechanic[]>('/mechanics?active=true'),
    create: (input: MechanicInput) => request<Mechanic>('/mechanics', json(input)),
    update: (id: number, input: MechanicInput) =>
      request<Mechanic>(`/mechanics/${id}`, put(input)),
  },

  services: {
    list: () => request<ServiceItem[]>('/services'),
    create: (input: ServiceInput) => request<ServiceItem>('/services', json(input)),
  },

  parts: {
    list: () => request<SparePart[]>('/parts'),
    create: (input: PartInput) => request<SparePart>('/parts', json(input)),
    update: (id: number, input: PartInput) =>
      request<SparePart>(`/parts/${id}`, put(input)),
    receive: (id: number, quantity: number) =>
      request<SparePart>(`/parts/${id}/receive`, json({ quantity })),
  },

  jobs: {
    list: (params: { status?: JobStatus; q?: string } = {}) => {
      const query = new URLSearchParams()
      if (params.status) query.set('status', params.status)
      if (params.q) query.set('q', params.q)
      const suffix = query.toString()
      return request<Job[]>(`/jobs${suffix ? `?${suffix}` : ''}`)
    },
    get: (id: number) => request<JobDetail>(`/jobs/${id}`),
    open: (vehicleId: number, complaint: string) =>
      request<JobDetail>('/jobs', json({ vehicleId, complaint })),
    assign: (id: number, mechanicId: number) =>
      request<JobDetail>(`/jobs/${id}/mechanic`, json({ mechanicId })),
    advance: (id: number) => request<JobDetail>(`/jobs/${id}/status`, json({})),
    addService: (id: number, serviceId: number, agreedPrice?: number) =>
      request<JobDetail>(
        `/jobs/${id}/services`,
        json(agreedPrice === undefined ? { serviceId } : { serviceId, agreedPrice }),
      ),
    addPart: (id: number, partId: number, quantity: number) =>
      request<JobDetail>(`/jobs/${id}/parts`, json({ partId, quantity })),
  },

  invoices: {
    list: (params: { status?: string; q?: string } = {}) => {
      const query = new URLSearchParams()
      if (params.status) query.set('status', params.status)
      if (params.q) query.set('q', params.q)
      const suffix = query.toString()
      return request<InvoiceSummary[]>(`/invoices${suffix ? `?${suffix}` : ''}`)
    },
    get: (id: number) => request<InvoiceDetail>(`/invoices/${id}`),
    create: (jobCardId: number, discount: number, taxPercent = 0) =>
      request<InvoiceDetail>('/invoices', json({ jobCardId, discount, taxPercent })),
    pay: (id: number, amount: number, method: PaymentMethod, reference: string) =>
      request<InvoiceDetail>(`/invoices/${id}/payments`, json({ amount, method, reference })),
  },
}
