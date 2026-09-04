import { useMemo, useRef, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { Play, Search } from 'lucide-react';
import { api } from '../api/client';
import { attachmentUrl } from '../lib/attachments';
import { useTestCaseSteps } from '../hooks/useQueries';
import { Card, LoadingState, ErrorState } from '../components/Primitives';
import { InfoPanel, WideInfoBlock } from '../components/InfoPanel';
import { StatCardButton, Pagination } from '../components/StatCardButton';
import { StatusBadge } from '../components/StatusBadge';
import { SlideshowModal } from '../components/SlideshowModal';
import { QuickNav } from '../components/QuickNav';
import { formatDuration } from '../lib/format';
import { countByStatus, deriveReference, firstIndexWithStatus } from '../lib/testCaseStats';

const PAGE_SIZE = 20;

export function TestCaseDetailPage() {
  const { id } = useParams();
  const testCaseId = Number(id);

  const { data: testCase, isLoading: loadingCase, isError: errorCase } = useQuery({
    queryKey: ['testCase', testCaseId],
    queryFn: () => api.testCases.getById(testCaseId),
    enabled: !!testCaseId,
  });
  const { data: suite } = useQuery({
    queryKey: ['suite', testCase?.testSuiteId],
    queryFn: () => api.suites.getById(testCase!.testSuiteId),
    enabled: !!testCase?.testSuiteId,
  });
  const { data: execution } = useQuery({
    queryKey: ['execution', suite?.testExecutionId],
    queryFn: () => api.executions.getById(suite!.testExecutionId),
    enabled: !!suite?.testExecutionId,
  });
  const { data: steps, isLoading: loadingSteps, isError: errorSteps } = useTestCaseSteps(testCaseId);

  const [query, setQuery] = useState('');
  const [page, setPage] = useState(1);
  const [slideshowOpen, setSlideshowOpen] = useState(false);
  const stepRefs = useRef<Record<number, HTMLDivElement | null>>({});

  const filteredSteps = useMemo(() => {
    const list = steps ?? [];
    if (!query.trim()) return list;
    const q = query.trim().toLowerCase();
    return list.filter((s) => s.description.toLowerCase().includes(q));
  }, [steps, query]);

  const totalPages = Math.max(1, Math.ceil(filteredSteps.length / PAGE_SIZE));
  const pageItems = filteredSteps.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);
  const counts = useMemo(() => countByStatus(steps ?? []), [steps]);

  function jumpToFirstFailed() {
    const idx = firstIndexWithStatus(steps ?? [], 'FAILED');
    if (idx === -1) return;
    const targetPage = Math.floor(idx / PAGE_SIZE) + 1;
    if (targetPage !== page) setPage(targetPage);
    const failedStep = (steps ?? [])[idx];
    // wait a tick for the page change to render before scrolling
    setTimeout(() => {
      stepRefs.current[failedStep.id]?.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }, 50);
  }

  if (loadingCase || loadingSteps) return <LoadingState label="Loading test case…" />;
  if (errorCase || errorSteps || !testCase) return <ErrorState message="Could not load this test case." />;

  const totalDurationMs = (steps ?? []).reduce((s, step) => s + (step.executionTime ?? 0), 0);

  return (
    <div className="mx-auto max-w-[1000px] px-8 py-6">
      <div className="mb-1 flex items-center justify-between text-xs text-[var(--color-ink-muted)]">
        <div>
          {suite ? (
            <Link to={`/executions/${suite.testExecutionId}`} className="hover:underline">
              {suite.testExecutionName ?? 'Execution'}
            </Link>
          ) : (
            <span>Execution</span>
          )}{' '}
          / {testCase.name}
        </div>
        <QuickNav />
      </div>
      <div className="mb-5 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <h1 className="text-xl font-semibold tracking-tight text-slate-900">{testCase.name}</h1>
          <StatusBadge status={testCase.executionStatus} />
        </div>
        <button
          onClick={() => setSlideshowOpen(true)}
          className="flex items-center gap-1.5 rounded-lg border border-[var(--color-border)] bg-white px-3 py-1.5 text-xs font-medium text-slate-700 hover:bg-slate-50"
        >
          <Play className="h-3.5 w-3.5" />
          View slideshow
        </button>
      </div>

      {execution && (
        <div className="mb-4 grid grid-cols-2 gap-4">
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
              { label: 'Execution Started', value: testCase.executionStartTime ? new Date(testCase.executionStartTime).toLocaleString() : '—' },
              { label: 'Execution Ended', value: testCase.executionEndTime ? new Date(testCase.executionEndTime).toLocaleString() : '—' },
              { label: 'Execution Time', value: formatDuration(testCase.executionTime ?? totalDurationMs) },
              { label: 'Test Suite', value: testCase.testSuiteName ?? '—' },
            ]}
          />
        </div>
      )}

      <WideInfoBlock
        className="mb-4"
        rows={[
          { label: 'Test Case Name', value: testCase.name },
          { label: 'Reference', value: deriveReference(testCase) },
        ]}
      />

      <div className="mb-6 grid grid-cols-4 gap-3">
        <StatCardButton label="Steps Passed" value={counts.passed} tone="text-[var(--color-status-passed)]" />
        <StatCardButton label="Steps Failed" value={counts.failed} tone="text-[var(--color-status-failed)]" onClick={counts.failed > 0 ? jumpToFirstFailed : undefined} />
        <StatCardButton label="Steps Warning" value={counts.warning} tone="text-[var(--color-status-warning)]" />
        <StatCardButton label="Steps In Progress" value={counts.progress} tone="text-[var(--color-status-progress)]" />
      </div>

      <div className="mb-3 flex items-center justify-between">
        <h2 className="text-sm font-semibold text-slate-900">Steps</h2>
        <div className="relative">
          <Search className="pointer-events-none absolute left-3 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-[var(--color-ink-muted)]" />
          <input
            value={query}
            onChange={(e) => { setQuery(e.target.value); setPage(1); }}
            placeholder="Search steps"
            className="w-56 rounded-lg border border-[var(--color-border)] bg-white py-1.5 pl-8 pr-3 text-xs outline-none focus:border-slate-400"
          />
        </div>
      </div>

      {pageItems.length === 0 ? (
        <Card className="py-10 text-center text-sm text-[var(--color-ink-muted)]">No steps match your search.</Card>
      ) : (
        <div className="space-y-2">
          {pageItems.map((step, idx) => {
            const isFailed = step.executionStatus === 'FAILED';
            const thumb = step.attachments?.[0] ? attachmentUrl(step.attachments[0].attachmentRelativePath) : undefined;
            return (
              <div
                key={step.id}
                ref={(el) => { stepRefs.current[step.id] = el; }}
                className={`rounded-xl border bg-white p-4 ${isFailed ? 'border-red-400 border-2' : 'border-[var(--color-border)]'}`}
              >
                <div className="flex items-start gap-3">
                  <span className="mt-0.5 flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-[var(--color-surface-muted)] text-xs font-semibold text-[var(--color-ink-muted)]">
                    {(page - 1) * PAGE_SIZE + idx + 1}
                  </span>
                  <div className="min-w-0 flex-1">
                    <p className="text-sm font-medium text-slate-900">{step.description}</p>
                    <p className="mt-0.5 text-xs text-[var(--color-ink-muted)]">
                      {step.operation ?? ''}{step.objectName ? ` · ${step.objectName}` : ''}
                    </p>
                    <p className={`mt-1 text-xs ${isFailed ? 'text-red-600' : 'text-[var(--color-ink-muted)]'}`}>
                      {step.result || '—'}
                    </p>
                  </div>
                  <div className="flex shrink-0 flex-col items-end gap-1">
                    <StatusBadge status={step.executionStatus} />
                    <span className="text-xs text-[var(--color-ink-muted)]">{formatDuration(step.executionTime)}</span>
                  </div>
                  {thumb ? (
                    <img src={thumb} alt="" className="h-12 w-12 shrink-0 rounded-md border border-[var(--color-border)] object-cover" />
                  ) : (
                    <div className="h-12 w-12 shrink-0 rounded-md border border-[var(--color-border)] bg-[var(--color-surface-muted)]" />
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}

      <div className="mt-3 flex items-center justify-between">
        <span className="text-xs text-[var(--color-ink-muted)]">
          Showing {pageItems.length} of {filteredSteps.length} steps
        </span>
        <Pagination page={page} totalPages={totalPages} onChange={setPage} />
      </div>

      {slideshowOpen && (
        <SlideshowModal testCaseId={testCase.id} testCaseName={testCase.name} onClose={() => setSlideshowOpen(false)} />
      )}
    </div>
  );
}
