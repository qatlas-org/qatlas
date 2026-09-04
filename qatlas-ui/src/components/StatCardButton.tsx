import type { ReactNode } from 'react';

export function StatCardButton({
  label, value, tone, onClick,
}: { label: string; value: number | string; tone?: string; onClick?: () => void }) {
  const Tag = onClick ? 'button' : 'div';
  return (
    <Tag
      onClick={onClick}
      className={`rounded-xl border border-[var(--color-border)] bg-[var(--color-surface-muted)] px-4 py-3 text-left ${
        onClick ? 'cursor-pointer hover:bg-slate-100' : ''
      }`}
    >
      <div className="text-xs font-medium text-[var(--color-ink-muted)]">{label}</div>
      <div className={`mt-1 text-xl font-semibold ${tone ?? 'text-slate-900'}`}>{value}</div>
    </Tag>
  );
}

export function Pagination({
  page, totalPages, onChange,
}: { page: number; totalPages: number; onChange: (page: number) => void }) {
  if (totalPages <= 1) {
    return (
      <div className="flex items-center gap-2 text-xs text-[var(--color-ink-muted)]">
        <button disabled className="rounded-md border border-[var(--color-border)] px-2 py-1 opacity-40">Prev</button>
        <span>Page 1 of 1</span>
        <button disabled className="rounded-md border border-[var(--color-border)] px-2 py-1 opacity-40">Next</button>
      </div>
    );
  }
  const pageNumbers = Array.from({ length: Math.min(totalPages, 5) }, (_, i) => {
    const start = Math.max(1, Math.min(page - 2, totalPages - 4));
    return start + i;
  }).filter((n) => n >= 1 && n <= totalPages);

  return (
    <div className="flex items-center gap-1 text-xs">
      <button
        disabled={page <= 1}
        onClick={() => onChange(page - 1)}
        className="rounded-md border border-[var(--color-border)] px-2.5 py-1 text-[var(--color-ink-muted)] disabled:opacity-40"
      >
        Prev
      </button>
      {pageNumbers.map((n) => (
        <button
          key={n}
          onClick={() => onChange(n)}
          className={`rounded-md border px-2.5 py-1 ${
            n === page ? 'border-slate-900 bg-slate-900 text-white' : 'border-[var(--color-border)] text-[var(--color-ink-muted)]'
          }`}
        >
          {n}
        </button>
      ))}
      <button
        disabled={page >= totalPages}
        onClick={() => onChange(page + 1)}
        className="rounded-md border border-[var(--color-border)] px-2.5 py-1 text-[var(--color-ink-muted)] disabled:opacity-40"
      >
        Next
      </button>
    </div>
  );
}

export function SectionHeader({ title, actions }: { title: string; actions?: ReactNode }) {
  return (
    <div className="mb-3 flex items-center justify-between">
      <h2 className="text-sm font-semibold text-slate-900">{title}</h2>
      {actions}
    </div>
  );
}
