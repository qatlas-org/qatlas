import { useMemo, useRef, useState } from 'react';
import type { PointerEvent as ReactPointerEvent } from 'react';
import { Link, useParams } from 'react-router-dom';
import { PieChart, Pie, Cell, BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts';
import { Play, MessageSquare, Search } from 'lucide-react';
import { useExecution, useExecutionTestCases } from '../hooks/useQueries';
import { LoadingState, ErrorState, Card } from '../components/Primitives';
import { InfoPanel } from '../components/InfoPanel';
import { StatCardButton, SectionHeader, Pagination } from '../components/StatCardButton';
import { KebabMenu, ExportScope } from '../components/KebabMenu';
import { StatusBadge } from '../components/StatusBadge';
import { SlideshowModal } from '../components/SlideshowModal';
import { QuickNav } from '../components/QuickNav';
import { formatDateTime, formatDuration } from '../lib/format';
import { deriveReference } from '../lib/testCaseStats';
import type { ExecutionStatus } from '../types/domain';

const CHART_COLORS: Record<string, string> = {
  PASSED: 'var(--color-status-passed)',
  FAILED: 'var(--color-status-failed)',
  WARNING: 'var(--color-status-warning)',
  PROGRESS: 'var(--color-status-progress)',
};

const PAGE_SIZE = 8;

type ColumnKey =
    | 'suite'
    | 'testCase'
    | 'reference'
    | 'duration'
    | 'status'
    | 'actions';

const COLUMN_ORDER: ColumnKey[] = [
  'suite',
  'testCase',
  'reference',
  'duration',
  'status',
  'actions',
];

const DEFAULT_COLUMN_WIDTHS: Record<ColumnKey, number> = {
  suite: 14,
  testCase: 28,
  reference: 30,
  duration: 8,
  status: 11,
  actions: 9,
};

const MIN_COLUMN_WIDTHS: Record<ColumnKey, number> = {
  suite: 6,
  testCase: 10,
  reference: 10,
  duration: 5,
  status: 8,
  actions: 7,
};

export function ExecutionDetailPage() {
  const { id } = useParams();
  const executionId = Number(id);
  const { data: execution, isLoading: loadingExec, isError: errorExec } = useExecution(executionId);
  const { data: testCases, isLoading: loadingCases, isError: errorCases } = useExecutionTestCases(executionId);

  const [statusFilter, setStatusFilter] = useState<ExecutionStatus | 'ALL'>('ALL');
  const [query, setQuery] = useState('');
  const [page, setPage] = useState(1);
  const [slideshowFor, setSlideshowFor] = useState<{ id: number; name: string } | null>(null);
  const [columnWidths, setColumnWidths] =
      useState<Record<ColumnKey, number>>(DEFAULT_COLUMN_WIDTHS);
  const testCasesTableRef = useRef<HTMLDivElement>(null);
  const [commentIds, setCommentIds] = useState<Set<number>>(() => {
    const raw = localStorage.getItem('qatlas_test_case_comments');
    return raw ? new Set(JSON.parse(raw)) : new Set();
  });

  function toggleComment(testCaseId: number) {
    const existing = localStorage.getItem(`qatlas_comment_${testCaseId}`);
    const next = prompt('Comment on this test case:', existing ?? '');
    if (next === null) return;
    if (next.trim() === '') {
      localStorage.removeItem(`qatlas_comment_${testCaseId}`);
      setCommentIds((prev) => {
        const s = new Set(prev); s.delete(testCaseId);
        localStorage.setItem('qatlas_test_case_comments', JSON.stringify([...s]));
        return s;
      });
    } else {
      localStorage.setItem(`qatlas_comment_${testCaseId}`, next);
      setCommentIds((prev) => {
        const s = new Set(prev); s.add(testCaseId);
        localStorage.setItem('qatlas_test_case_comments', JSON.stringify([...s]));
        return s;
      });
    }
  }

  const filteredCases = useMemo(() => {
    let list = testCases ?? [];
    if (statusFilter !== 'ALL') list = list.filter((tc) => tc.executionStatus === statusFilter);
    if (query.trim()) {
      const q = query.trim().toLowerCase();
      list = list.filter(
          (tc) => tc.name.toLowerCase().includes(q) || (tc.testSuiteName ?? '').toLowerCase().includes(q)
      );
    }
    return list;
  }, [testCases, statusFilter, query]);

  const totalPages = Math.max(1, Math.ceil(filteredCases.length / PAGE_SIZE));
  const pageItems = filteredCases.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);

  if (loadingExec || loadingCases) return <LoadingState label="Loading execution…" />;
  if (errorExec || errorCases || !execution) return <ErrorState message="Could not load this execution." />;

  const donutData = [
    { name: 'Passed', value: execution.passedTestCaseCount ?? 0, key: 'PASSED' },
    { name: 'Failed', value: execution.failedTestCaseCount ?? 0, key: 'FAILED' },
    { name: 'Skipped', value: execution.skippedTestCaseCount ?? 0, key: 'WARNING' },
    { name: 'In Progress', value: execution.inProgressTestCaseCount ?? 0, key: 'PROGRESS' },
  ].filter((d) => d.value > 0);

  const slowest = (testCases ?? [])
      .filter((tc) => tc.executionTime != null)
      .sort((a, b) => (b.executionTime ?? 0) - (a.executionTime ?? 0))
      .slice(0, 15)
      .map((tc) => ({ name: tc.name, seconds: (tc.executionTime ?? 0) / 1000, passed: tc.executionStatus === 'PASSED' }));

  function jumpToFailed() {
    const failedIndex = filteredCases.findIndex((tc) => tc.executionStatus === 'FAILED');
    if (failedIndex === -1) {
      setStatusFilter('FAILED');
      setPage(1);
    } else {
      setPage(Math.floor(failedIndex / PAGE_SIZE) + 1);
    }
  }

  const gridTemplateColumns = COLUMN_ORDER
      .map((key) => `${columnWidths[key]}%`)
      .join(' ');

  function resizeColumn(key: ColumnKey, requestedWidth: number) {
    setColumnWidths((current) => {
      const otherKeys = COLUMN_ORDER.filter((column) => column !== key);

      const minimumOthers = otherKeys.reduce(
          (sum, column) => sum + MIN_COLUMN_WIDTHS[column],
          0
      );

      const targetWidth = Math.max(
          MIN_COLUMN_WIDTHS[key],
          Math.min(requestedWidth, 100 - minimumOthers)
      );

      const remainingWidth = 100 - targetWidth;
      const flexibleCapacity = otherKeys.reduce(
          (sum, column) =>
              sum + Math.max(0, current[column] - MIN_COLUMN_WIDTHS[column]),
          0
      );

      const remainingAfterMinimums =
          remainingWidth -
          otherKeys.reduce(
              (sum, column) => sum + MIN_COLUMN_WIDTHS[column],
              0
          );

      const next = { ...current, [key]: targetWidth };

      otherKeys.forEach((column) => {
        const currentFlexible = Math.max(
            0,
            current[column] - MIN_COLUMN_WIDTHS[column]
        );

        const share =
            flexibleCapacity > 0
                ? currentFlexible / flexibleCapacity
                : 1 / otherKeys.length;

        next[column] =
            MIN_COLUMN_WIDTHS[column] +
            Math.max(0, remainingAfterMinimums) * share;
      });

      return next;
    });
  }

  function startColumnResize(
      event: ReactPointerEvent<HTMLButtonElement>,
      key: ColumnKey
  ) {
    event.preventDefault();

    const tableWidth =
        testCasesTableRef.current?.getBoundingClientRect().width ?? 0;
    if (tableWidth <= 0) return;

    const startX = event.clientX;
    const startWidth = columnWidths[key];

    function onPointerMove(moveEvent: PointerEvent) {
      const deltaPercent =
          ((moveEvent.clientX - startX) / tableWidth) * 100;

      resizeColumn(key, startWidth + deltaPercent);
    }

    function onPointerUp() {
      window.removeEventListener('pointermove', onPointerMove);
      window.removeEventListener('pointerup', onPointerUp);
    }

    window.addEventListener('pointermove', onPointerMove);
    window.addEventListener('pointerup', onPointerUp);
  }

  const headerCellClass = 'relative min-w-0 truncate px-2 pr-3';

  function resizeHandle(key: ColumnKey) {
    return (
        <button
            type="button"
            aria-label={`Resize ${key} column`}
            title="Drag to resize column"
            onPointerDown={(event) => startColumnResize(event, key)}
            className="absolute right-0 top-0 h-full w-2 cursor-col-resize touch-none select-none border-0 bg-transparent p-0 hover:bg-slate-300/60"
        />
    );
  }

  return (
      <div className="mx-auto max-w-[1280px] px-8 py-6">
        <div className="mb-1 flex items-center justify-between text-xs text-[var(--color-ink-muted)]">
          <div>
            <Link to={`/projects/${execution.applicationId}`} className="hover:underline">
              {execution.applicationName ?? 'Project'}
            </Link>{' '}
            / {execution.name}
          </div>
          <QuickNav />
        </div>
        <div className="mb-4 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <h1 className="text-xl font-semibold tracking-tight text-slate-900">{execution.name}</h1>
            <StatusBadge status={(execution.failedTestCaseCount ?? 0) > 0 ? 'FAILED' : execution.endTime ? 'PASSED' : 'PROGRESS'} />
          </div>
          <KebabMenu
              items={[
                { label: 'Copy to clipboard', onClick: () => alert('Implement with Clipboard API + html-to-image') },
                { label: 'Export as JPEG', onClick: () => alert('Implement with html-to-image') },
                { label: 'Export as PDF', onClick: () => alert('Implement with a PDF export lib') },
                { label: 'Export as Excel', onClick: () => alert('Implement with a spreadsheet export lib') },
              ]}
          />
        </div>

        <ExportScope>
          <div className="grid grid-cols-2 gap-4">
            <InfoPanel
                title="System details"
                rows={[
                  { label: 'Executed by', value: execution.executedBy ?? execution.systemName ?? '—' },
                  { label: 'Project Name', value: execution.applicationName ?? '—' },
                  { label: 'Environment', value: execution.environmentName ?? '—' },
                  { label: 'Browser', value: execution.browser },
                ]}
            />
            <InfoPanel
                title="Execution details"
                rows={[
                  { label: 'Run', value: execution.name },
                  { label: 'Execution Started', value: formatDateTime(execution.startTime) },
                  { label: 'Execution Ended', value: execution.endTime ? formatDateTime(execution.endTime) : '—' },
                  {
                    label: 'Execution Time',
                    value: execution.endTime
                        ? formatDuration(new Date(execution.endTime).getTime() - new Date(execution.startTime).getTime())
                        : '—',
                  },
                ]}
            />
          </div>

          <div className="my-4 grid grid-cols-6 gap-3">
            <StatCardButton label="Targeted" value={execution.targetedTestCaseCount ?? 0} />
            <StatCardButton label="Executed" value={execution.executedTestCaseCount ?? 0} />
            <StatCardButton label="Passed" value={execution.passedTestCaseCount ?? 0} tone="text-[var(--color-status-passed)]" onClick={() => { setStatusFilter('PASSED'); setPage(1); }} />
            <StatCardButton label="Failed" value={execution.failedTestCaseCount ?? 0} tone="text-[var(--color-status-failed)]" onClick={jumpToFailed} />
            <StatCardButton label="Skipped" value={execution.skippedTestCaseCount ?? 0} tone="text-[var(--color-status-warning)]" onClick={() => { setStatusFilter('SKIPPED'); setPage(1); }} />
            <StatCardButton label="In Progress" value={execution.inProgressTestCaseCount ?? 0} tone="text-[var(--color-status-progress)]" onClick={() => { setStatusFilter('PROGRESS'); setPage(1); }} />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <Card className="p-4">
              <p className="mb-2 text-xs font-medium text-[var(--color-ink-muted)]">Test Scenarios Results</p>
              {donutData.length === 0 ? (
                  <p className="py-16 text-center text-xs text-[var(--color-ink-muted)]">No data yet</p>
              ) : (
                  <ResponsiveContainer width="100%" height={200}>
                    <PieChart>
                      <Pie data={donutData} dataKey="value" nameKey="name" innerRadius={50} outerRadius={80}>
                        {donutData.map((d) => <Cell key={d.key} fill={CHART_COLORS[d.key]} />)}
                      </Pie>
                      <Tooltip />
                    </PieChart>
                  </ResponsiveContainer>
              )}
            </Card>
            <Card className="p-4">
              <p className="mb-2 text-xs font-medium text-[var(--color-ink-muted)]">Slowest Test Scenarios (seconds)</p>
              {slowest.length === 0 ? (
                  <p className="py-16 text-center text-xs text-[var(--color-ink-muted)]">No data yet</p>
              ) : (
                  <ResponsiveContainer width="100%" height={200}>
                    <BarChart data={slowest}>
                      <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" vertical={false} />
                      <XAxis dataKey="name" tick={false} />
                      <YAxis tick={{ fontSize: 10 }} />
                      <Tooltip />
                      <Bar dataKey="seconds" radius={[3, 3, 0, 0]}>
                        {slowest.map((d, i) => (
                            <Cell key={i} fill={d.passed ? 'var(--color-status-passed)' : 'var(--color-status-failed)'} />
                        ))}
                      </Bar>
                    </BarChart>
                  </ResponsiveContainer>
              )}
            </Card>
          </div>
        </ExportScope>

        {/* Test Cases */}
        <div className="mt-6">
          <SectionHeader
              title="Test Cases"
              actions={
                <div className="flex items-center gap-2">
                  <div className="relative">
                    <Search className="pointer-events-none absolute left-3 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-[var(--color-ink-muted)]" />
                    <input
                        value={query}
                        onChange={(e) => { setQuery(e.target.value); setPage(1); }}
                        placeholder="Search test case / suite"
                        className="w-56 rounded-lg border border-[var(--color-border)] bg-white py-1.5 pl-8 pr-3 text-xs outline-none focus:border-slate-400"
                    />
                  </div>
                  <select
                      value={statusFilter}
                      onChange={(e) => { setStatusFilter(e.target.value as ExecutionStatus | 'ALL'); setPage(1); }}
                      className="rounded-lg border border-[var(--color-border)] bg-white px-2.5 py-1.5 text-xs font-medium text-slate-700"
                  >
                    <option value="ALL">All statuses</option>
                    <option value="PASSED">Passed</option>
                    <option value="FAILED">Failed</option>
                    <option value="SKIPPED">Skipped</option>
                    <option value="PROGRESS">In Progress</option>
                    <option value="WARNING">Warning</option>
                  </select>
                  <KebabMenu
                      items={[
                        { label: 'Export as CSV (all rows)', onClick: () => alert('Wire to a real export endpoint') },
                        { label: 'Export as Excel (all rows)', onClick: () => alert('Wire to a real export endpoint') },
                        'divider',
                        { label: 'Screenshot current view (image)', onClick: () => alert('Implement with html-to-image') },
                      ]}
                  />
                </div>
              }
          />

          <div ref={testCasesTableRef} className="w-full min-w-0">
            <Card className="w-full overflow-hidden">
              <div
                  className="grid w-full border-b border-[var(--color-border)] bg-slate-50 px-2 py-2 text-[10px] font-medium uppercase tracking-wide text-[var(--color-ink-muted)]"
                  style={{ gridTemplateColumns }}
              >
              <span className={headerCellClass}>
                Test Suite
                {resizeHandle('suite')}
              </span>
                <span className={headerCellClass}>
                Test Case
                  {resizeHandle('testCase')}
              </span>
                <span className={headerCellClass}>
                Reference
                  {resizeHandle('reference')}
              </span>
                <span className={headerCellClass}>
                Duration
                  {resizeHandle('duration')}
              </span>
                <span className={headerCellClass}>
                Status
                  {resizeHandle('status')}
              </span>
                <span className="min-w-0 truncate px-2">Actions</span>
              </div>

              {pageItems.length === 0 ? (
                  <p className="px-4 py-10 text-center text-sm text-[var(--color-ink-muted)]">
                    No test cases match the current filter.
                  </p>
              ) : (
                  pageItems.map((tc) => {
                    const reference = deriveReference(tc);
                    const suiteName = tc.testSuiteName ?? '—';
                    const duration = formatDuration(tc.executionTime);

                    return (
                        <div
                            key={tc.id}
                            id={`test-case-row-${tc.id}`}
                            className="grid w-full items-center border-b border-[var(--color-border)] px-2 py-2.5 text-xs hover:bg-slate-50"
                            style={{ gridTemplateColumns }}
                        >
                    <span
                        className="min-w-0 truncate px-2 text-[var(--color-ink-muted)]"
                        title={suiteName}
                    >
                      {suiteName}
                    </span>

                          <div className="min-w-0 px-2">
                            <Link
                                to={`/test-cases/${tc.id}`}
                                title={tc.name}
                                className="block min-w-0 truncate font-medium text-blue-700 hover:underline"
                            >
                              {tc.name}
                            </Link>
                          </div>

                          <span
                              className="min-w-0 truncate px-2 text-[var(--color-ink-muted)]"
                              title={reference}
                          >
                      {reference}
                    </span>

                          <span
                              className="min-w-0 truncate px-2 text-[var(--color-ink-muted)]"
                              title={duration}
                          >
                      {duration}
                    </span>

                          <div className="min-w-0 overflow-hidden px-2">
                            <StatusBadge status={tc.executionStatus} />
                          </div>

                          <div className="flex min-w-0 gap-1.5 overflow-hidden px-2">
                            <button
                                title="View attachments as slideshow"
                                onClick={() =>
                                    setSlideshowFor({ id: tc.id, name: tc.name })
                                }
                                className="shrink-0 rounded-md border border-[var(--color-border)] bg-white p-1.5 hover:bg-slate-50"
                            >
                              <Play className="h-3 w-3 text-slate-700" />
                            </button>
                            <button
                                title={
                                  commentIds.has(tc.id)
                                      ? 'Edit comment'
                                      : 'Add comment'
                                }
                                onClick={() => toggleComment(tc.id)}
                                className={`shrink-0 rounded-md border p-1.5 hover:bg-slate-50 ${
                                    commentIds.has(tc.id)
                                        ? 'border-blue-300 bg-blue-50'
                                        : 'border-[var(--color-border)] bg-white'
                                }`}
                            >
                              <MessageSquare
                                  className={`h-3 w-3 ${
                                      commentIds.has(tc.id)
                                          ? 'text-blue-600'
                                          : 'text-slate-700'
                                  }`}
                              />
                            </button>
                          </div>
                        </div>
                    );
                  })
              )}
            </Card>
          </div>

          <div className="mt-2 flex items-center justify-between">
          <span className="text-xs text-[var(--color-ink-muted)]">
            Showing {pageItems.length} of {filteredCases.length} test cases
          </span>
            <Pagination page={page} totalPages={totalPages} onChange={setPage} />
          </div>
        </div>

        {slideshowFor && (
            <SlideshowModal testCaseId={slideshowFor.id} testCaseName={slideshowFor.name} onClose={() => setSlideshowFor(null)} />
        )}
      </div>
  );
}
