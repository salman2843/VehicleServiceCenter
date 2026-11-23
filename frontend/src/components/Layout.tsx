import { useEffect, useState } from 'react'
import { Link, NavLink, Outlet, useLocation } from 'react-router-dom'
import { api } from '../api'
import { formatToday } from '../format'
import { useApi } from '../hooks'
import type { Health } from '../types'
import { Icon, type IconName } from './Icons'

interface NavItem {
  to: string
  label: string
  icon: IconName
  end?: boolean
}

const navItems: NavItem[] = [
  { to: '/', label: 'Overview', icon: 'dashboard', end: true },
  { to: '/customers', label: 'Customers', icon: 'users' },
  { to: '/vehicles', label: 'Vehicles', icon: 'car' },
  { to: '/jobs', label: 'Job cards', icon: 'clipboard' },
  { to: '/invoices', label: 'Invoices', icon: 'receipt' },
  { to: '/inventory', label: 'Inventory', icon: 'box' },
  { to: '/mechanics', label: 'Mechanics', icon: 'wrench' },
]

const sectionNames: [string, string][] = [
  ['/customers', 'Customers'],
  ['/vehicles', 'Vehicles'],
  ['/jobs', 'Job cards'],
  ['/invoices', 'Invoices'],
  ['/inventory', 'Inventory'],
  ['/mechanics', 'Mechanics'],
]

function sectionFor(pathname: string): string {
  const match = sectionNames.find(([prefix]) => pathname.startsWith(prefix))
  return match ? match[1] : 'Overview'
}

function Brand({ onClick }: { onClick?: () => void }) {
  return (
    <Link to="/" onClick={onClick} className="flex items-center gap-3 px-5 pb-5 pt-6">
      <span className="block h-10 w-[3px] rounded-full bg-brand" aria-hidden="true" />
      <span>
        <span className="block font-serif text-[16px] leading-tight tracking-[-0.01em] text-ink">
          Vehicle Service
        </span>
        <span className="mt-0.5 block text-[10px] font-semibold uppercase tracking-[0.2em] text-ink-soft">
          Center
        </span>
      </span>
    </Link>
  )
}

function Sidebar({
  health,
  onNavigate,
}: {
  health: { data: Health | null; error: string | null }
  onNavigate?: () => void
}) {
  const online = Boolean(health.data)
  const offline = Boolean(health.error) && !health.data

  return (
    <>
      <Brand onClick={onNavigate} />
      <nav className="flex-1 overflow-y-auto px-3 pb-4">
        <p className="px-3 pb-2 pt-2 text-[9.5px] font-semibold uppercase tracking-[0.2em] text-ink-soft/65">
          Workshop
        </p>
        <div className="space-y-0.5">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              onClick={onNavigate}
              className={({ isActive }) =>
                `group flex items-center gap-3 rounded-lg px-3 py-2 text-[13px] font-medium transition-colors ${
                  isActive
                    ? 'bg-brand-soft text-brand-dark'
                    : 'text-ink-soft hover:bg-muted hover:text-ink'
                }`
              }
            >
              {({ isActive }) => (
                <>
                  <Icon
                    name={item.icon}
                    className={`h-[17px] w-[17px] ${
                      isActive ? 'text-brand' : 'text-ink-soft/65 group-hover:text-ink'
                    }`}
                  />
                  <span>{item.label}</span>
                </>
              )}
            </NavLink>
          ))}
        </div>
      </nav>
      <div className="border-t border-line px-5 py-4">
        <div className="flex items-center gap-2">
          <span
            className={`h-1.5 w-1.5 rounded-full ${
              online ? 'bg-brand' : offline ? 'bg-danger' : 'bg-ink-soft/40'
            }`}
            aria-hidden="true"
          />
          <span className="text-[10px] font-semibold uppercase tracking-[0.14em] text-ink-soft">
            {online ? 'API online' : offline ? 'API offline' : 'Checking'}
          </span>
        </div>
        <p
          className="mt-1.5 truncate font-mono text-[10.5px] text-ink-soft/70"
          title={health.data ? `${health.data.app} — ${health.data.stage}` : undefined}
        >
          {health.data ? `v${health.data.version} · ${health.data.database}` : 'no server'}
        </p>
      </div>
    </>
  )
}

export default function Layout() {
  const [navOpen, setNavOpen] = useState(false)
  const { pathname } = useLocation()
  const health = useApi('health', api.health)

  useEffect(() => {
    setNavOpen(false)
  }, [pathname])

  return (
    <div className="min-h-screen lg:grid lg:grid-cols-[236px_1fr]">
      <aside className="sticky top-0 hidden h-screen flex-col border-r border-line bg-surface lg:flex">
        <Sidebar health={health} />
      </aside>

      {navOpen && (
        <div className="fixed inset-0 z-50 lg:hidden">
          <div
            className="animate-fade absolute inset-0 bg-ink/35"
            onClick={() => setNavOpen(false)}
            aria-hidden="true"
          />
          <aside className="animate-fade absolute inset-y-0 left-0 flex w-[264px] flex-col border-r border-line bg-surface">
            <Sidebar health={health} onNavigate={() => setNavOpen(false)} />
          </aside>
        </div>
      )}

      <div className="flex min-w-0 flex-col">
        <header className="sticky top-0 z-30 flex h-14 items-center justify-between gap-3 border-b border-line bg-paper/85 px-4 backdrop-blur-md lg:px-10">
          <div className="flex min-w-0 items-center gap-2.5">
            <button
              type="button"
              onClick={() => setNavOpen(true)}
              aria-label="Open navigation"
              className="grid h-8 w-8 place-items-center rounded-lg border border-line bg-surface text-ink-soft transition-colors hover:text-ink lg:hidden"
            >
              <Icon name="menu" className="h-4 w-4" />
            </button>
            <p className="truncate text-[10.5px] font-semibold uppercase tracking-[0.18em] text-ink-soft">
              {sectionFor(pathname)}
            </p>
          </div>
          <div className="flex items-center gap-3">
            <span className="hidden font-mono text-[11.5px] text-ink-soft sm:block">
              {formatToday()}
            </span>
            <Link
              to="/jobs?new=1"
              className="inline-flex h-8 items-center gap-1.5 rounded-lg bg-brand px-3 text-[12.5px] font-semibold text-white transition-colors hover:bg-brand-dark"
            >
              <Icon name="plus" className="h-3.5 w-3.5" />
              New job
            </Link>
          </div>
        </header>

        <main className="flex-1 px-4 pb-16 pt-6 lg:px-10 lg:pt-8">
          <div className="mx-auto w-full max-w-[1400px]">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  )
}
