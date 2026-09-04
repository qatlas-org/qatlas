import { useMemo, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import { useVirtualizer } from '@tanstack/react-virtual';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { Search, Trash2, ImageOff } from 'lucide-react';
import { useExecutions } from '../hooks/useQueries';
import { useAuth } from '../hooks/useAuth';
import { api } from '../api/client';
import { Card, EmptyState, ErrorState, LoadingState, PageHeader } from '../components/Primitives';
import { StatusBadge } from '../components/StatusBadge';
import { formatDateTime } from '../lib/format';

const ROW_HEIGHT = 56;

export function ExecutionsPage() {
  const { data: executions, isLoading, isError } = useExecutions();
  const { isAdmin } = useAuth();
  const queryClient = useQueryClient();
  const [query, setQuery] = useState('');
  const [selected, setSelected] = useState<Set<number>>(new Set());
  const scrollRef = useRef<HTMLDivElement>(null);

  const filtered = useMemo(() => {
    if (!executions) return [];
    const q = query.trim().toLowerCase();
    if (!q) return executions;
    return executions.filter(
      (e) =>
        e.name.toLowerCase().includes(q) ||
        (e.applicationName ?? '').toLowerCase().includes(q) ||
        (e.environmentName ?? '').toLowerCase().includes(q)
    );
  }, [executions, query]);

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

  function handleDelete(screenshotsOnly: boolean) {
    if (selected.size === 0) return;
    const verb = screenshotsOnly ? 'delete the screenshots for' : 'permanently delete';
    if (!confirm(`Are you sure you want to ${verb} ${selected.size} execution(s)? This cannot be undone.`)) {
      return;
    }
    archiveMutation.mutate({ ids: Array.from(selected), screenshotsOnly });
  }

  // Virtualized: with thousands of executions in prod, only ~15 rows are ever
  // mounted in the DOM at once regardless of dataset size — this is what
  // keeps this screen fast and low-memory as data grows.
  const rowVirtualizer = useVirtualizer({
    count: filtered.length,
    getScrollElement: () => scrollRef.current,
    estimateSize: () => ROW_HEIGHT,
    overscan: 8,
  });

  if (isLoading) return <LoadingState label="Loading executions…" />;
  if (isError) return <ErrorState message="Could not reach the QAtlas backend." />;

  const gridCols = isAdmin ? 'grid-cols-[28px_2fr_1.2fr_1fr_1.4fr_1fr]' : 'grid-cols-[2fr_1.2fr_1fr_1.4fr_1fr]';

  return (
    <div>
      <PageHeader
        title="Test Executions"
        subtitle={`${filtered.length.toLocaleString()} of ${executions?.length.toLocaleString() ?? 0} executions`}
        actions={
          <div className="relative">
            <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-[var(--color-ink-muted)]" />
            <input
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Search executions…"
              className="w-72 rounded-lg border border-[var(--color-border)] bg-white py-2 pl-9 pr-3 text-sm outline-none focus:border-slate-400"
            />
          </div>
        }
      />

      {!isAdmin && (
        <p className="mb-3 text-xs text-[var(--color-ink-muted)]">
          <Link to="/login" className="underline">
            Sign in as admin
          </Link>{' '}
          to delete executions or screenshots.
        </p>
      )}

      {isAdmin && selected.size > 0 && (
        <div className="mb-3 flex items-center justify-between rounded-lg border border-amber-200 bg-amber-50 px-4 py-2">
          <span className="text-xs font-medium text-amber-800">{selected.size} selected</span>
          <div className="flex gap-2">
            <button
              onClick={() => handleDelete(true)}
              disabled={archiveMutation.isPending}
              className="flex items-center gap-1.5 rounded-md border border-amber-300 bg-white px-3 py-1.5 text-xs font-medium text-amber-800 hover:bg-amber-100 disabled:opacity-50"
            >
              <ImageOff className="h-3.5 w-3.5" />
              Delete screenshots only
            </button>
            <button
              onClick={() => handleDelete(false)}
              disabled={archiveMutation.isPending}
              className="flex items-center gap-1.5 rounded-md border border-red-300 bg-white px-3 py-1.5 text-xs font-medium text-red-700 hover:bg-red-50 disabled:opacity-50"
            >
              <Trash2 className="h-3.5 w-3.5" />
              Delete selected
            </button>
          </div>
        </div>
      )}

      {archiveMutation.isError && (
        <p className="mb-3 text-xs text-red-600">
          Delete failed — check that you're still signed in and try again.
        </p>
      )}

      <Card className="overflow-hidden">
        <div className={`grid ${gridCols} gap-3 border-b border-[var(--color-border)] bg-slate-50 px-5 py-2.5 text-xs font-medium uppercase tracking-wide text-[var(--color-ink-muted)]`}>
          {isAdmin && <span />}
          <span>Execution</span>
          <span>Application</span>
          <span>Environment</span>
          <span>Started</span>
          <span>Result</span>
        </div>

        {filtered.length === 0 ? (
          <EmptyState message={query ? 'No executions match your search.' : 'No test executions recorded yet.'} />
        ) : (
          <div ref={scrollRef} className="max-h-[600px] overflow-auto">
            <div style={{ height: rowVirtualizer.getTotalSize(), position: 'relative', width: '100%' }}>
              {rowVirtualizer.getVirtualItems().map((virtualRow) => {
                const e = filtered[virtualRow.index];
                return (
                  <div
                    key={e.id}
                    className={`absolute left-0 top-0 grid w-full ${gridCols} items-center gap-3 border-b border-[var(--color-border)] px-5 hover:bg-slate-50`}
                    style={{ height: ROW_HEIGHT, transform: `translateY(${virtualRow.start}px)` }}
                  >
                    {isAdmin && (
                      <input
                        type="checkbox"
                        checked={selected.has(e.id)}
                        onChange={() => toggleRow(e.id)}
                        onClick={(evt) => evt.stopPropagation()}
                      />
                    )}
                    <Link to={`/executions/${e.id}`} className="truncate text-sm font-medium text-slate-900 hover:underline">
                      {e.name}
                    </Link>
                    <span className="truncate text-sm text-[var(--color-ink-muted)]">{e.applicationName ?? '—'}</span>
                    <span className="truncate text-sm text-[var(--color-ink-muted)]">{e.environmentName ?? '—'}</span>
                    <span className="truncate text-sm text-[var(--color-ink-muted)]">{formatDateTime(e.startTime)}</span>
                    <StatusBadge status={(e.failedTestCaseCount ?? 0) > 0 ? 'FAILED' : 'PASSED'} />
                  </div>
                );
              })}
            </div>
          </div>
        )}
      </Card>
    </div>
  );
}
