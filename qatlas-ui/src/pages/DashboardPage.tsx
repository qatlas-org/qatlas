import { useMemo } from 'react';
import { Link } from 'react-router-dom';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts';
import { useExecutions } from '../hooks/useQueries';
import { Card, EmptyState, ErrorState, LoadingState, PageHeader, StatCard } from '../components/Primitives';
import { StatusBadge } from '../components/StatusBadge';
import { formatDateTime } from '../lib/format';

export function DashboardPage() {
  const { data: executions, isLoading, isError } = useExecutions();

  const totals = useMemo(() => {
    if (!executions) return null;
    return executions.reduce(
      (acc, e) => ({
        executions: acc.executions + 1,
        passed: acc.passed + (e.passedTestCaseCount ?? 0),
        failed: acc.failed + (e.failedTestCaseCount ?? 0),
        inProgress: acc.inProgress + (e.inProgressTestCaseCount ?? 0),
      }),
      { executions: 0, passed: 0, failed: 0, inProgress: 0 }
    );
  }, [executions]);

  const chartData = useMemo(
    () =>
      (executions ?? [])
        .slice(0, 10)
        .reverse()
        .map((e) => ({
          name: e.name.length > 16 ? `${e.name.slice(0, 16)}…` : e.name,
          Passed: e.passedTestCaseCount ?? 0,
          Failed: e.failedTestCaseCount ?? 0,
        })),
    [executions]
  );

  if (isLoading) return <LoadingState label="Loading dashboard…" />;
  if (isError) return <ErrorState message="Could not reach the QAtlas backend." />;

  return (
    <div>
      <PageHeader title="Dashboard" subtitle="Overview of recent test execution activity" />

      <div className="mb-6 grid grid-cols-4 gap-4">
        <StatCard label="Total Executions" value={totals?.executions ?? 0} />
        <StatCard label="Passed Cases" value={totals?.passed ?? 0} tone="text-[var(--color-status-passed)]" />
        <StatCard label="Failed Cases" value={totals?.failed ?? 0} tone="text-[var(--color-status-failed)]" />
        <StatCard label="In Progress" value={totals?.inProgress ?? 0} tone="text-[var(--color-status-progress)]" />
      </div>

      <Card className="mb-6 p-5">
        <h2 className="mb-4 text-sm font-semibold text-[var(--color-ink)]">Recent executions — pass/fail</h2>
        {chartData.length === 0 ? (
          <EmptyState />
        ) : (
          <ResponsiveContainer width="100%" height={260}>
            <BarChart data={chartData}>
              <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" vertical={false} />
              <XAxis dataKey="name" tick={{ fontSize: 12 }} />
              <YAxis tick={{ fontSize: 12 }} allowDecimals={false} />
              <Tooltip />
              <Bar dataKey="Passed" fill="#16a34a" radius={[4, 4, 0, 0]} />
              <Bar dataKey="Failed" fill="#dc2626" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        )}
      </Card>

      <Card>
        <div className="border-b border-[var(--color-border)] px-5 py-3">
          <h2 className="text-sm font-semibold text-[var(--color-ink)]">Latest executions</h2>
        </div>
        {!executions || executions.length === 0 ? (
          <EmptyState message="No test executions have been recorded yet." />
        ) : (
          <table className="w-full text-sm">
            <thead>
              <tr className="text-left text-xs uppercase tracking-wide text-[var(--color-ink-muted)]">
                <th className="px-5 py-2.5 font-medium">Execution</th>
                <th className="px-5 py-2.5 font-medium">Application</th>
                <th className="px-5 py-2.5 font-medium">Started</th>
                <th className="px-5 py-2.5 font-medium">Result</th>
              </tr>
            </thead>
            <tbody>
              {executions.slice(0, 8).map((e) => (
                <tr key={e.id} className="border-t border-[var(--color-border)] hover:bg-slate-50">
                  <td className="px-5 py-3">
                    <Link to={`/executions/${e.id}`} className="font-medium text-slate-900 hover:underline">
                      {e.name}
                    </Link>
                  </td>
                  <td className="px-5 py-3 text-[var(--color-ink-muted)]">{e.applicationName ?? '—'}</td>
                  <td className="px-5 py-3 text-[var(--color-ink-muted)]">{formatDateTime(e.startTime)}</td>
                  <td className="px-5 py-3">
                    <StatusBadge status={(e.failedTestCaseCount ?? 0) > 0 ? 'FAILED' : 'PASSED'} />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </Card>
    </div>
  );
}
