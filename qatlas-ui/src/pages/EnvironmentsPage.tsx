import { useEnvironments } from '../hooks/useQueries';
import { Card, EmptyState, ErrorState, LoadingState, PageHeader } from '../components/Primitives';

export function EnvironmentsPage() {
  const { data: environments, isLoading, isError } = useEnvironments();

  if (isLoading) return <LoadingState label="Loading environments…" />;
  if (isError) return <ErrorState message="Could not reach the QAtlas backend." />;

  return (
    <div>
      <PageHeader title="Environments" subtitle="Environments test executions can run against" />
      <Card>
        {!environments || environments.length === 0 ? (
          <EmptyState message="No environments registered yet." />
        ) : (
          <ul className="divide-y divide-[var(--color-border)]">
            {environments.map((env) => (
              <li key={env.id} className="px-5 py-3.5">
                <div className="text-sm font-medium text-slate-900">{env.name}</div>
                {env.description && <div className="mt-0.5 text-xs text-[var(--color-ink-muted)]">{env.description}</div>}
              </li>
            ))}
          </ul>
        )}
      </Card>
    </div>
  );
}
