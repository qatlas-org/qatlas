import { useApplications } from '../hooks/useQueries';
import { Card, EmptyState, ErrorState, LoadingState, PageHeader } from '../components/Primitives';

export function ApplicationsPage() {
  const { data: applications, isLoading, isError } = useApplications();

  if (isLoading) return <LoadingState label="Loading applications…" />;
  if (isError) return <ErrorState message="Could not reach the QAtlas backend." />;

  return (
    <div>
      <PageHeader title="Applications" subtitle="Applications registered for test reporting" />
      <Card>
        {!applications || applications.length === 0 ? (
          <EmptyState message="No applications registered yet." />
        ) : (
          <ul className="divide-y divide-[var(--color-border)]">
            {applications.map((app) => (
              <li key={app.id} className="px-5 py-3.5">
                <div className="text-sm font-medium text-slate-900">{app.name}</div>
                {app.description && <div className="mt-0.5 text-xs text-[var(--color-ink-muted)]">{app.description}</div>}
              </li>
            ))}
          </ul>
        )}
      </Card>
    </div>
  );
}
