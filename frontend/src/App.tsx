import { Link, Route, Routes } from 'react-router-dom'
import Layout from './components/Layout'
import { Button } from './components/ui'
import CustomersPage from './pages/CustomersPage'
import DashboardPage from './pages/DashboardPage'
import InventoryPage from './pages/InventoryPage'
import InvoiceDetailPage from './pages/InvoiceDetailPage'
import InvoicesPage from './pages/InvoicesPage'
import JobDetailPage from './pages/JobDetailPage'
import JobsPage from './pages/JobsPage'
import MechanicsPage from './pages/MechanicsPage'
import VehiclesPage from './pages/VehiclesPage'

function NotFound() {
  return (
    <div className="mx-auto max-w-md py-20 text-center">
      <p className="font-mono text-[11px] uppercase tracking-[0.2em] text-ink-soft">404</p>
      <h1 className="mt-2 text-[26px] font-semibold tracking-[-0.02em]">This page does not exist</h1>
      <p className="mt-2 text-[13.5px] leading-relaxed text-ink-soft">
        The address may be old, or the screen may have moved.
      </p>
      <Link to="/" className="mt-5 inline-block">
        <Button>Back to overview</Button>
      </Link>
    </div>
  )
}

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<DashboardPage />} />
        <Route path="customers" element={<CustomersPage />} />
        <Route path="vehicles" element={<VehiclesPage />} />
        <Route path="jobs" element={<JobsPage />} />
        <Route path="jobs/:id" element={<JobDetailPage />} />
        <Route path="invoices" element={<InvoicesPage />} />
        <Route path="invoices/:id" element={<InvoiceDetailPage />} />
        <Route path="inventory" element={<InventoryPage />} />
        <Route path="mechanics" element={<MechanicsPage />} />
        <Route path="*" element={<NotFound />} />
      </Route>
    </Routes>
  )
}
