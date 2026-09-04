import { NavLink, Outlet } from 'react-router-dom';
import { LayoutDashboard, ListChecks, Server, Layers } from 'lucide-react';
import clsx from 'clsx';

const NAV_ITEMS = [
  { to: '/', label: 'Dashboard', icon: LayoutDashboard, end: true },
  { to: '/executions', label: 'Executions', icon: ListChecks, end: false },
  { to: '/applications', label: 'Applications', icon: Layers, end: false },
  { to: '/environments', label: 'Environments', icon: Server, end: false },
];

export function AppShell() {
  return (
    <div className="flex h-full min-h-screen">
      <aside className="w-60 shrink-0 border-r border-[var(--color-border)] bg-white px-4 py-6">
        <div className="mb-8 flex items-center gap-2 px-2">
          <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-slate-900 text-sm font-bold text-white">
            QA
          </div>
          <span className="text-base font-semibold tracking-tight">QAtlas</span>
        </div>

        <nav className="flex flex-col gap-1">
          {NAV_ITEMS.map(({ to, label, icon: Icon, end }) => (
            <NavLink
              key={to}
              to={to}
              end={end}
              className={({ isActive }) =>
                clsx(
                  'flex items-center gap-2.5 rounded-lg px-3 py-2 text-sm font-medium transition-colors',
                  isActive
                    ? 'bg-slate-900 text-white'
                    : 'text-[var(--color-ink-muted)] hover:bg-slate-100 hover:text-[var(--color-ink)]'
                )
              }
            >
              <Icon className="h-4 w-4" />
              {label}
            </NavLink>
          ))}
        </nav>
      </aside>

      <main className="flex-1 overflow-x-hidden px-8 py-6">
        <Outlet />
      </main>
    </div>
  );
}
