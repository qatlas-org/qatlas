import type { ReactNode } from 'react';
import { Loader2, AlertCircle } from 'lucide-react';

export function PageHeader({ title, subtitle, actions }: { title: string; subtitle?: string; actions?: ReactNode }) {
  return (
    <div className="mb-6 flex items-start justify-between">
      <div>
        <h1 className="text-2xl font-semibold tracking-tight text-[var(--color-ink)]">{title}</h1>
        {subtitle && <p className="mt-1 text-sm text-[var(--color-ink-muted)]">{subtitle}</p>}
      </div>
      {actions}
    </div>
  );
}

export function Card({ children, className = '' }: { children: ReactNode; className?: string }) {
  return (
    <div className={`rounded-xl border border-[var(--color-border)] bg-white shadow-sm ${className}`}>
      {children}
    </div>
  );
}

export function LoadingState({ label = 'Loading…' }: { label?: string }) {
  return (
    <div className="flex items-center justify-center gap-2 py-16 text-sm text-[var(--color-ink-muted)]">
      <Loader2 className="h-4 w-4 animate-spin" />
      {label}
    </div>
  );
}

export function ErrorState({ message = 'Something went wrong loading this data.' }: { message?: string }) {
  return (
    <div className="flex items-center justify-center gap-2 rounded-xl border border-red-200 bg-red-50 py-10 text-sm text-red-700">
      <AlertCircle className="h-4 w-4" />
      {message}
    </div>
  );
}

export function EmptyState({ message = 'Nothing to show here yet.' }: { message?: string }) {
  return (
    <div className="flex items-center justify-center py-16 text-sm text-[var(--color-ink-muted)]">{message}</div>
  );
}

export function StatCard({ label, value, tone }: { label: string; value: string | number; tone?: string }) {
  return (
    <Card className="px-5 py-4">
      <div className="text-xs font-medium uppercase tracking-wide text-[var(--color-ink-muted)]">{label}</div>
      <div className={`mt-1 text-2xl font-semibold ${tone ?? 'text-[var(--color-ink)]'}`}>{value}</div>
    </Card>
  );
}
