import { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { Search, Download, FileDown, CircleSlash, X as XIcon } from 'lucide-react';
import { useAuth } from '../hooks/useAuth';
import { api } from '../api/client';
import { Card, EmptyState } from './Primitives';
import { formatDateTime } from '../lib/format';
import { Pagination } from './StatCardButton';
import { KebabMenu } from './KebabMenu';
import type { TestExecution } from '../types/domain';

const PAGE_SIZE = 8;

export function ExecutionHistoryTable({ executions }: { executions: TestExecution[] }) {
  const { isAdmin } = useAuth();
  const queryClient = useQueryClient();
  const [query, setQuery] = useState('');
  const [selected, setSelected] = useState<Set<number>>(new Set());
  const [page, setPage] = useState(1);

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase();
    if (!q) return executions;
    return executions.filter((e) => e.name.toLowerCase().includes(q));
  }, [executions, query]);

  const totalPages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const pageItems = filtered.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);
  const allOnPageSelected = pageItems.length > 0 && pageItems.every((e) => selected.has(e.id));

  const archiveMutation = useMutation({
    mutationFn: ({ ids, screenshotsOnly }: { ids: number[]; screenshotsOnly: boolean }) =>
      api.executions.archive(ids, screenshotsOnly),
    onSuccess: () => {
      setSelected(new Set());
      queryClient.invalidateQueries({ queryKey: ['executions'] });
    },
  });

  function toggleRow(id: number) {
    setSelected((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  }

  function toggleAllOnPage() {
    setSelected((prev) => {
      const next = new Set(prev);
      if (allOnPageSelected) pageItems.forEach((e) => next.delete(e.id));
      else pageItems.forEach((e) => next.add(e.id));
      return next;
    });
  }

  function handleDelete(screenshotsOnly: boolean) {
    if (selected.size === 0) return;
    const verb = screenshotsOnly ? 'delete the screenshots for' : 'permanently delete';
    if (!confirm(`Are you sure you want to ${verb} ${selected.size} execution(s)? This cannot be undone.`)) return;
    archiveMutation.mutate({ ids: Array.from(selected), screenshotsOnly });
  }

  return (
    <div>
      <div className="mb-3 flex items-center justify-between">
        <div className="relative">
          <Search className="pointer-events-none absolute left-3 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-[var(--color-ink-muted)]" />
          <input
            value={query}
            onChange={(e) => { setQuery(e.target.value); setPage(1); }}
            placeholder="Search run name"
            className="w-64 rounded-lg border border-[var(--color-border)] bg-white py-1.5 pl-8 pr-3 text-xs outline-none focus:border-slate-400"
          />
        </div>
        <KebabMenu
          items={[
            { label: 'Export as CSV (all rows)', onClick: () => alert('CSV export — wire to a real export endpoint once available') },
            { label: 'Export as Excel (all rows)', onClick: () => alert('Excel export — wire to a real export endpoint once available') },
            'divider',
            { label: 'Screenshot current view (image)', onClick: () => alert('Client-side screenshot — implement with html-to-image or similar') },
          ]}
        />
      </div>

      {!isAdmin && (
        <p className="mb-2 text-xs text-[var(--color-ink-muted)]">
          <Link to="/login" className="underline">Sign in as admin</Link> to delete executions or screenshots.
        </p>
      )}

      {isAdmin && selected.size > 0 && (
        <div className="mb-2 flex items-center justify-between rounded-lg border border-amber-200 bg-amber-50 px-3 py-1.5">
          <span className="text-xs font-medium text-amber-800">{selected.size} selected</span>
          <div className="flex gap-1.5">
            <button title="Download attachments" className="rounded-md border border-[var(--color-border)] bg-white p-1.5 hover:bg-slate-50">
              <Download className="h-3.5 w-3.5 text-slate-700" />
            </button>
            <button title="Download static report" className="rounded-md border border-[var(--color-border)] bg-white p-1.5 hover:bg-slate-50">
              <FileDown className="h-3.5 w-3.5 text-slate-700" />
            </button>
            <button
              title="Delete attachments"
              onClick={() => handleDelete(true)}
              disabled={archiveMutation.isPending}
              className="rounded-md border border-[var(--color-border)] bg-white p-1.5 hover:bg-slate-50 disabled:opacity-40"
            >
              <CircleSlash className="h-3.5 w-3.5 text-amber-600" />
            </button>
            <button
              title="Delete selected"
              onClick={() => handleDelete(false)}
              disabled={archiveMutation.isPending}
              className="rounded-md border border-[var(--color-border)] bg-white p-1.5 hover:bg-slate-50 disabled:opacity-40"
            >
              <XIcon className="h-3.5 w-3.5 text-red-600" />
            </button>
          </div>
        </div>
      )}

      <Card className="overflow-hidden">
        <div className="grid grid-cols-[24px_1fr_1.6fr_1fr_1fr_60px_60px_60px_60px_60px_70px] items-center gap-2 border-b border-[var(--color-border)] bg-slate-50 px-4 py-2 text-[10px] font-medium uppercase tracking-wide text-[var(--color-ink-muted)]">
          {isAdmin ? (
            <input type="checkbox" checked={allOnPageSelected} onChange={toggleAllOnPage} />
          ) : <span />}
          <span>Release</span>
          <span>Run name</span>
          <span>Environment</span>
          <span>Ran on</span>
          <span>Target.</span>
          <span>Exec.</span>
          <span>Pass</span>
          <span>Fail</span>
          <span>Skip</span>
          <span>Prog.</span>
        </div>

        {pageItems.length === 0 ? (
          <EmptyState message={query ? 'No runs match your search.' : 'No executions recorded yet.'} />
        ) : (
          pageItems.map((e) => (
            <div
              key={e.id}
              className="grid grid-cols-[24px_1fr_1.6fr_1fr_1fr_60px_60px_60px_60px_60px_70px] items-center gap-2 border-b border-[var(--color-border)] px-4 py-2.5 text-xs hover:bg-slate-50"
            >
              {isAdmin ? (
                <input type="checkbox" checked={selected.has(e.id)} onChange={() => toggleRow(e.id)} />
              ) : <span />}
              <span className="truncate text-[var(--color-ink-muted)]">{e.applicationVersion ?? '—'}</span>
              <Link to={`/executions/${e.id}`} className="truncate font-medium text-slate-900 hover:underline">
                {e.name}
              </Link>
              <span className="truncate text-[var(--color-ink-muted)]">{e.environmentName ?? '—'}</span>
              <span className="truncate text-[var(--color-ink-muted)]">{formatDateTime(e.startTime)}</span>
              <span>{e.targetedTestCaseCount ?? '—'}</span>
              <span>{e.executedTestCaseCount ?? '—'}</span>
              <span className="text-[var(--color-status-passed)]">{e.passedTestCaseCount ?? 0}</span>
              <span className="text-[var(--color-status-failed)]">{e.failedTestCaseCount ?? 0}</span>
              <span className="text-[var(--color-status-warning)]">{e.skippedTestCaseCount ?? 0}</span>
              <span className="text-[var(--color-status-progress)]">{e.inProgressTestCaseCount ?? 0}</span>
            </div>
          ))
        )}
      </Card>

      <div className="mt-2 flex items-center justify-between">
        <span className="text-xs text-[var(--color-ink-muted)]">
          Showing {pageItems.length} of {filtered.length} runs
        </span>
        <Pagination page={page} totalPages={totalPages} onChange={setPage} />
      </div>
    </div>
  );
}
