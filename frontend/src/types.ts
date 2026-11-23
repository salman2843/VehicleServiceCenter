export type JobStatus = 'RECEIVED' | 'IN_PROGRESS' | 'COMPLETED' | 'DELIVERED'
export type PaymentStatus = 'UNPAID' | 'PARTIALLY_PAID' | 'PAID'
export type PaymentMethod = 'CASH' | 'UPI' | 'CARD'
export type VehicleType = 'CAR' | 'MOTORCYCLE'

export interface Customer {
  id: number
  name: string
  phone: string
  email: string | null
  address: string | null
  createdOn: string
  vehicleCount: number
}

export interface CustomerInput {
  name: string
  phone: string
  email: string
  address: string
}

export interface Vehicle {
  id: number
  customerId: number
  registrationNo: string
  vehicleType: VehicleType
  make: string
  modelName: string
  year: number
  numberOfDoors?: number | null
  engineCapacityCc?: number | null
  customerName: string | null
}

export interface CustomerDetail extends Customer {
  vehicles: Vehicle[]
}

export interface VehicleInput {
  customerId: number
  vehicleType: VehicleType
  registrationNo: string
  make: string
  modelName: string
  year: number
  numberOfDoors?: number
  engineCapacityCc?: number
}

export interface VehicleDetail extends Vehicle {
  jobs: Job[]
}

export interface Mechanic {
  id: number
  name: string
  phone: string | null
  specialization: string | null
  active: boolean
}

export interface MechanicInput {
  name: string
  phone: string
  specialization: string
  active: boolean
}

export interface ServiceItem {
  id: number
  name: string
  details: string | null
  basePrice: number
}

export interface ServiceInput {
  name: string
  details: string
  basePrice: number
}

export interface SparePart {
  id: number
  partNumber: string
  name: string
  unitPrice: number
  stockQty: number
  reorderLevel: number
  lowStock: boolean
}

export interface PartInput {
  partNumber: string
  name: string
  unitPrice: number
  stockQty: number
  reorderLevel: number
}

export interface Job {
  id: number
  vehicleId: number
  mechanicId: number
  complaint: string
  status: JobStatus
  nextStatus: JobStatus | null
  receivedOn: string
  completedOn: string | null
  registrationNo: string | null
  vehicleLabel: string | null
  vehicleType: VehicleType | null
  customerId: number | null
  customerName: string | null
  customerPhone: string | null
  mechanicName: string | null
}

export interface JobServiceLine {
  id: number
  serviceId: number
  serviceName: string
  agreedPrice: number
  lineTotal: number
}

export interface JobPartLine {
  id: number
  partId: number
  partNumber: string
  partName: string
  quantity: number
  unitPrice: number
  lineTotal: number
}

export interface JobDetail extends Job {
  services: JobServiceLine[]
  parts: JobPartLine[]
  laborTotal: number
  partsTotal: number
  jobTotal: number
  invoice: InvoiceSummary | null
}

export interface InvoiceSummary {
  id: number
  jobCardId: number
  laborTotal: number
  partsTotal: number
  discount: number
  taxAmount: number
  total: number
  paid: number
  outstanding: number
  paymentStatus: PaymentStatus
  createdOn: string
  registrationNo: string | null
  customerName: string | null
  jobStatus: JobStatus | null
}

export interface Payment {
  id: number
  invoiceId: number
  amount: number
  method: PaymentMethod
  reference: string | null
  paidOn: string
}

export interface InvoiceDetail extends InvoiceSummary {
  payments: Payment[]
  job: Omit<JobDetail, 'invoice'>
}

export interface Dashboard {
  counts: {
    customers: number
    vehicles: number
    jobs: number
    openJobs: number
    invoices: number
    unpaidInvoices: number
    lowStockParts: number
    mechanics: number
  }
  money: {
    invoiced: number
    collected: number
    outstanding: number
  }
  jobsByStatus: { status: JobStatus; count: number }[]
  recentJobs: Job[]
  outstandingInvoices: InvoiceSummary[]
  lowStock: SparePart[]
}

export interface Health {
  status: string
  app: string
  version: string
  stage: string
  database: string
}
