import type { ReactNode } from 'react';

export interface InfoRow {
  label: string;
  value: ReactNode;
}

export function InfoPanel({ title, rows, className = '' }: { title: string; rows: InfoRow[]; className?: string }) {
  return (
    <div className={`rounded-xl border border-[var(--color-border)] bg-white ${className}`}>
      <div className="px-5 py-3 text-sm font-semibold text-slate-900">{title}</div>
      <dl className="divide-y divide-[var(--color-border)]">
        {rows.map((row, i) => (
          <div key={i} className="flex items-center justify-between px-5 py-2.5 text-sm">
            <dt className="text-[var(--color-ink-muted)]">{row.label}</dt>
            <dd className="font-medium text-slate-900">{row.value}</dd>
          </div>
        ))}
      </dl>
    </div>
  );
}

/** Full-width block for long values (e.g. test case names) that don't fit
 * comfortably in a two-column InfoPanel row. */
export function WideInfoBlock({ rows, className = '' }: { rows: InfoRow[]; className?: string }) {
  return (
    <div className={`rounded-xl border border-[var(--color-border)] bg-white divide-y divide-[var(--color-border)] ${className}`}>
      {rows.map((row, i) => (
        <div key={i} className="px-5 py-3">
          <div className="text-xs text-[var(--color-ink-muted)]">{row.label}</div>
          <div className="mt-1 text-sm font-medium text-slate-900">{row.value}</div>
        </div>
      ))}
    </div>
  );
}
