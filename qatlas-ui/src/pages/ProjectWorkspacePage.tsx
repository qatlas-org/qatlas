import { useEffect, useMemo, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import {
  PieChart, Pie, Cell, BarChart, Bar, LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid,
} from 'recharts';
import { useApplications, useExecutions, useExecutionTestCases } from '../hooks/useQueries';
import { useAuth } from '../hooks/useAuth';
import { LoadingState, ErrorState, Card } from '../components/Primitives';
import { InfoPanel } from '../components/InfoPanel';
import { StatCardButton, SectionHeader } from '../components/StatCardButton';
import { KebabMenu, ExportScope } from '../components/KebabMenu';
import { ExecutionHistoryTable } from '../components/ExecutionHistoryTable';
import { formatDateTime, formatDuration } from '../lib/format';
import { bucketExecutionsByDay, computePassRateTrend, currentlyRunning } from '../lib/projectStats';
import type { DateRangeKey } from '../lib/dateRange';
import type { TestExecution } from '../types/domain';
import { setCurrentProjectId } from '../lib/currentProject';
import { QuickNav } from '../components/QuickNav';

type Variant = 'A' | 'B';
const VARIANT_KEY = 'qatlas_workspace_variant';

function getStoredVariant(): Variant {
  return (localStorage.getItem(VARIANT_KEY) as Variant) ?? 'A';
}

export function ProjectWorkspacePage() {
  const { applicationId } = useParams();
  const appId = Number(applicationId);
  const navigate = useNavigate();
  const { isAdmin, username, logout } = useAuth();

  const [variant, setVariant] = useState<Variant>(getStoredVariant);
  function changeVariant(v: Variant) {
    setVariant(v);
    localStorage.setItem(VARIANT_KEY, v);
  }

  const { data: applications, isLoading: loadingApps } = useApplications();
  const { data: allExecutions, isLoading: loadingExecs, isError } = useExecutions();

  useEffect(() => {
    if (appId) setCurrentProjectId(appId);
  }, [appId]);

  const application = applications?.find((a) => a.id === appId);
  const projectExecutions = useMemo(
    () => (allExecutions ?? []).filter((e) => e.applicationId === appId),
    [allExecutions, appId]
  );
  const latest = projectExecutions.slice().sort(
    (a, b) => new Date(b.startTime).getTime() - new Date(a.startTime).getTime()
  )[0];

  if (loadingApps || loadingExecs) return <LoadingState label="Loading project…" />;
  if (isError || !application) return <ErrorState message="Could not load this project." />;

  return (
    <div className="mx-auto max-w-[1280px] px-8 py-6">
      {/* Header */}
      <div className="mb-1 flex items-center justify-between text-xs text-[var(--color-ink-muted)]">
        <div>
          <Link to="/" className="hover:underline">Projects</Link> / {application.name}
        </div>
        <QuickNav />
      </div>
      <div className="mb-4 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <h1 className="text-xl font-semibold tracking-tight text-slate-900">{application.name}</h1>
          <select
            value={appId}
            onChange={(e) => navigate(`/projects/${e.target.value}`)}
            className="rounded-lg border border-[var(--color-border)] bg-white px-2.5 py-1 text-xs font-medium text-slate-700"
          >
            {applications?.map((a) => (
              <option key={a.id} value={a.id}>{a.name}</option>
            ))}
          </select>
          <div className="flex rounded-lg border border-[var(--color-border)] bg-white p-0.5 text-xs">
            {(['A', 'B'] as Variant[]).map((v) => (
              <button
                key={v}
                onClick={() => changeVariant(v)}
                className={`rounded-md px-2.5 py-1 font-medium ${
                  variant === v ? 'bg-slate-900 text-white' : 'text-[var(--color-ink-muted)]'
                }`}
              >
                {v}
              </button>
            ))}
          </div>
        </div>
        <div className="flex items-center gap-2">
          <KebabMenu
            items={[
              { label: 'Copy to clipboard', onClick: () => alert('Implement with Clipboard API + html-to-image') },
              { label: 'Export as JPEG', onClick: () => alert('Implement with html-to-image') },
              { label: 'Export as PDF', onClick: () => alert('Implement with a PDF export lib') },
              { label: 'Export as Excel', onClick: () => alert('Implement with a spreadsheet export lib') },
            ]}
          />
          {isAdmin ? (
            <div className="flex items-center gap-2">
              <span className="text-xs text-[var(--color-ink-muted)]">Signed in as {username}</span>
              <button onClick={logout} className="rounded-lg border border-[var(--color-border)] bg-white px-3 py-1.5 text-xs font-medium text-slate-700 hover:bg-slate-50">
                Sign out
              </button>
            </div>
          ) : (
            <Link to="/login" className="rounded-lg border border-[var(--color-border)] bg-white px-3 py-1.5 text-xs font-medium text-slate-700 hover:bg-slate-50">
              Sign in
            </Link>
          )}
        </div>
      </div>

      {variant === 'A' ? (
        <VariantA latest={latest} projectExecutions={projectExecutions} />
      ) : (
        <VariantB application={application} projectExecutions={projectExecutions} />
      )}
    </div>
  );
}

const CHART_COLORS = {
  PASSED: 'var(--color-status-passed)',
  FAILED: 'var(--color-status-failed)',
  WARNING: 'var(--color-status-warning)',
  PROGRESS: 'var(--color-status-progress)',
};

function VariantA({ latest, projectExecutions }: { latest?: TestExecution; projectExecutions: TestExecution[] }) {
  const navigate = useNavigate();
  const { data: testCases } = useExecutionTestCases(latest?.id ?? 0);

  if (!latest) {
    return <Card className="py-16 text-center text-sm text-[var(--color-ink-muted)]">No executions recorded for this project yet.</Card>;
  }

  const donutData = [
    { name: 'Passed', value: latest.passedTestCaseCount ?? 0, key: 'PASSED' as const },
    { name: 'Failed', value: latest.failedTestCaseCount ?? 0, key: 'FAILED' as const },
    { name: 'Skipped', value: latest.skippedTestCaseCount ?? 0, key: 'WARNING' as const },
    { name: 'In Progress', value: latest.inProgressTestCaseCount ?? 0, key: 'PROGRESS' as const },
  ].filter((d) => d.value > 0);

  const slowest = (testCases ?? [])
    .filter((tc) => tc.executionTime != null)
    .sort((a, b) => (b.executionTime ?? 0) - (a.executionTime ?? 0))
    .slice(0, 15)
    .map((tc) => ({ name: tc.name, seconds: (tc.executionTime ?? 0) / 1000, passed: tc.executionStatus === 'PASSED' }));

  const executionId = latest.id;
  function goToExecution() {
    navigate(`/executions/${executionId}`);
  }

  return (
    <div>
      <ExportScope>
        <div className="grid grid-cols-2 gap-4">
          <InfoPanel
            title="System details"
            rows={[
              { label: 'Executed by', value: latest.executedBy ?? latest.systemName ?? '—' },
              { label: 'Project Name', value: latest.applicationName ?? '—' },
              { label: 'Environment', value: latest.environmentName ?? '—' },
              { label: 'Browser', value: latest.browser },
            ]}
          />
          <InfoPanel
            title="Execution details"
            rows={[
              { label: 'Run', value: latest.name },
              { label: 'Execution Started', value: formatDateTime(latest.startTime) },
              { label: 'Execution Ended', value: latest.endTime ? formatDateTime(latest.endTime) : '—' },
              {
                label: 'Execution Time',
                value: latest.endTime
                  ? formatDuration(new Date(latest.endTime).getTime() - new Date(latest.startTime).getTime())
                  : '—',
              },
            ]}
          />
        </div>

        <div className="my-4 grid grid-cols-6 gap-3">
          <StatCardButton label="Targeted" value={latest.targetedTestCaseCount ?? 0} onClick={goToExecution} />
          <StatCardButton label="Executed" value={latest.executedTestCaseCount ?? 0} onClick={goToExecution} />
          <StatCardButton label="Passed" value={latest.passedTestCaseCount ?? 0} tone="text-[var(--color-status-passed)]" onClick={goToExecution} />
          <StatCardButton label="Failed" value={latest.failedTestCaseCount ?? 0} tone="text-[var(--color-status-failed)]" onClick={goToExecution} />
          <StatCardButton label="Skipped" value={latest.skippedTestCaseCount ?? 0} tone="text-[var(--color-status-warning)]" onClick={goToExecution} />
          <StatCardButton label="In Progress" value={latest.inProgressTestCaseCount ?? 0} tone="text-[var(--color-status-progress)]" onClick={goToExecution} />
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

      <div className="mt-6">
        <SectionHeader title="Execution history" />
        <ExecutionHistoryTable executions={projectExecutions} />
      </div>
    </div>
  );
}

function VariantB({ application, projectExecutions }: { application: { id: number; name: string }; projectExecutions: TestExecution[] }) {
  const range: DateRangeKey = '30D';
  const dayBuckets = useMemo(() => bucketExecutionsByDay(projectExecutions, range), [projectExecutions]);
  const passRateTrend = useMemo(() => computePassRateTrend(projectExecutions, range), [projectExecutions]);
  const running = useMemo(() => currentlyRunning(projectExecutions), [projectExecutions]);

  const barData = dayBuckets.map((b) => ({ date: b.date, count: b.outcomes.length }));

  return (
    <div>
      <ExportScope>
        <SectionHeader title="Executions overview" actions={<span className="text-xs text-[var(--color-ink-muted)]">Last 30 days</span>} />
        <div className="mb-6 grid grid-cols-2 gap-4">
          <Card className="p-4">
            <p className="mb-2 text-xs font-medium text-[var(--color-ink-muted)]">Executions per day</p>
            <ResponsiveContainer width="100%" height={180}>
              <BarChart data={barData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" vertical={false} />
                <XAxis dataKey="date" tick={{ fontSize: 9 }} tickFormatter={(d) => d.slice(5)} />
                <YAxis tick={{ fontSize: 10 }} allowDecimals={false} />
                <Tooltip />
                <Bar dataKey="count" fill="var(--color-chart-neutral)" radius={[3, 3, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </Card>
          <Card className="p-4">
            <p className="mb-2 text-xs font-medium text-[var(--color-ink-muted)]">Pass rate trend</p>
            <ResponsiveContainer width="100%" height={180}>
              <LineChart data={passRateTrend}>
                <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" vertical={false} />
                <XAxis dataKey="date" tick={{ fontSize: 9 }} tickFormatter={(d) => d.slice(5)} />
                <YAxis tick={{ fontSize: 10 }} domain={[0, 100]} />
                <Tooltip />
                <Line type="monotone" dataKey="rate" stroke="var(--color-status-passed)" strokeWidth={2} dot={false} connectNulls />
              </LineChart>
            </ResponsiveContainer>
          </Card>
        </div>

        <SectionHeader title="Currently running" />
        {running.length === 0 ? (
          <Card className="py-8 text-center text-sm text-[var(--color-ink-muted)]">No executions currently running for {application.name}.</Card>
        ) : (
          <div className="mb-2 space-y-2">
            {running.map((e) => (
              <Link
                key={e.id}
                to={`/executions/${e.id}`}
                className="flex items-center justify-between rounded-xl border border-blue-300 bg-white px-4 py-3 hover:bg-slate-50"
              >
                <div>
                  <p className="text-sm font-medium text-blue-700">{e.name}</p>
                  <p className="mt-0.5 text-xs text-[var(--color-ink-muted)]">
                    {e.environmentName ?? '—'} · started {formatDateTime(e.startTime)} · {e.executedTestCaseCount ?? 0} of {e.targetedTestCaseCount ?? 0} executed
                  </p>
                </div>
                <span className="rounded-full bg-blue-50 px-3 py-1 text-xs font-medium text-blue-700">In Progress</span>
              </Link>
            ))}
          </div>
        )}
      </ExportScope>

      <div className="mt-6">
        <SectionHeader title="All executions" />
        <ExecutionHistoryTable executions={projectExecutions} />
      </div>
    </div>
  );
}
