import { Link, useParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { api } from '../api/client';
import { useSuiteTestCases } from '../hooks/useQueries';
import { Card, EmptyState, ErrorState, LoadingState, PageHeader } from '../components/Primitives';
import { StatusBadge } from '../components/StatusBadge';
import { formatDuration } from '../lib/format';

export function SuiteDetailPage() {
  const { id } = useParams();
  const suiteId = Number(id);

  const { data: suite, isLoading: loadingSuite, isError: errorSuite } = useQuery({
    queryKey: ['suite', suiteId],
    queryFn: () => api.suites.getById(suiteId),
    enabled: !!suiteId,
  });
  const { data: testCases, isLoading: loadingCases, isError: errorCases } = useSuiteTestCases(suiteId);

  if (loadingSuite || loadingCases) return <LoadingState label="Loading suite…" />;
  if (errorSuite || errorCases || !suite) return <ErrorState message="Could not load this test suite." />;

  return (
    <div>
      <nav className="mb-3 text-xs text-[var(--color-ink-muted)]">
        <Link to={`/executions/${suite.testExecutionId}`} className="hover:underline">
          {suite.testExecutionName ?? 'Execution'}
        </Link>{' '}
        / <span className="text-[var(--color-ink)]">{suite.testSuiteName}</span>
      </nav>

      <PageHeader
        title={suite.testSuiteName}
        subtitle={`${suite.executedTestCaseCount ?? 0} of ${suite.plannedTestCaseCount} test cases executed`}
      />

      <Card>
        <div className="border-b border-[var(--color-border)] px-5 py-3">
          <h2 className="text-sm font-semibold">Test cases</h2>
        </div>
        {!testCases || testCases.length === 0 ? (
          <EmptyState message="This suite has no test cases yet." />
        ) : (
          <table className="w-full text-sm">
            <thead>
              <tr className="text-left text-xs uppercase tracking-wide text-[var(--color-ink-muted)]">
                <th className="px-5 py-2.5 font-medium">Test case</th>
                <th className="px-5 py-2.5 font-medium">Steps</th>
                <th className="px-5 py-2.5 font-medium">Duration</th>
                <th className="px-5 py-2.5 font-medium">Status</th>
              </tr>
            </thead>
            <tbody>
              {testCases.map((tc) => (
                <tr key={tc.id} className="border-t border-[var(--color-border)] hover:bg-slate-50">
                  <td className="px-5 py-3">
                    <Link to={`/test-cases/${tc.id}`} className="font-medium text-slate-900 hover:underline">
                      {tc.name}
                    </Link>
                  </td>
                  <td className="px-5 py-3 text-[var(--color-ink-muted)]">
                    {tc.passedTestStepCount ?? 0}/{tc.totalTestStepCount ?? 0}
                  </td>
                  <td className="px-5 py-3 text-[var(--color-ink-muted)]">{formatDuration(tc.executionTime)}</td>
                  <td className="px-5 py-3">
                    <StatusBadge status={tc.executionStatus} />
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
