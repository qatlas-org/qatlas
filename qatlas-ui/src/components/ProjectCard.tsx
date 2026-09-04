import { Link } from 'react-router-dom';
import type { ProjectCardData, ProjectStatus } from '../lib/portfolio';
import { formatDateTime } from '../lib/format';

const STATUS_STYLES: Record<ProjectStatus, { bg: string; fg: string; border: string }> = {
  FAILED: { bg: 'bg-red-50', fg: 'text-red-700', border: 'border-red-300' },
  RUNNING: { bg: 'bg-blue-50', fg: 'text-blue-700', border: 'border-[var(--color-border)]' },
  WARNING: { bg: 'bg-amber-50', fg: 'text-amber-700', border: 'border-[var(--color-border)]' },
  PASSED: { bg: 'bg-green-50', fg: 'text-green-700', border: 'border-[var(--color-border)]' },
};

const SEGMENT_COLOR: Record<ProjectStatus, string> = {
  PASSED: 'var(--color-status-passed)',
  FAILED: 'var(--color-status-failed)',
  WARNING: 'var(--color-status-warning)',
  RUNNING: 'var(--color-status-progress)',
};

function relativeTime(iso?: string): string {
  if (!iso) return '—';
  const diffMs = Date.now() - new Date(iso).getTime();
  const mins = Math.round(diffMs / 60000);
  if (mins < 1) return 'just now';
  if (mins < 60) return `${mins} min ago`;
  const hours = Math.round(mins / 60);
  if (hours < 24) return `${hours} hr ago`;
  const days = Math.round(hours / 24);
  return `${days} day${days === 1 ? '' : 's'} ago`;
}

export function ProjectCard({ data }: { data: ProjectCardData }) {
  const { application, latest, status, statusLabel, totalExecutions, segments } = data;
  const style = STATUS_STYLES[status];
  const total = segments.reduce((s, seg) => s + seg.value, 0);
  const executedBy = latest?.executedBy || latest?.systemName;

  return (
    <Link
      to={`/projects/${application.id}`}
      className={`block rounded-xl border bg-white p-4 transition-shadow hover:shadow-sm ${style.border}`}
      title={application.description}
    >
      <div className="flex items-start justify-between gap-2">
        <div className="min-w-0">
          <p className="truncate text-sm font-semibold text-slate-900">{application.name}</p>
          <p className="mt-0.5 truncate text-xs text-[var(--color-ink-muted)]">
            {executedBy ? `Executed by ${executedBy}` : 'No executions yet'}
          </p>
        </div>
        <span
          className={`shrink-0 rounded-full px-2.5 py-0.5 text-[11px] font-medium whitespace-nowrap ${style.bg} ${style.fg}`}
        >
          {statusLabel}
        </span>
      </div>

      {total > 0 && (
        <div className="mt-4 flex h-1.5 w-full overflow-hidden rounded-full bg-slate-100">
          {segments.map((seg, i) => (
            <div
              key={i}
              style={{ width: `${(seg.value / total) * 100}%`, backgroundColor: SEGMENT_COLOR[seg.status] }}
            />
          ))}
        </div>
      )}

      <div className="mt-2 flex items-center justify-between text-[11px] text-[var(--color-ink-muted)]">
        <span>{latest ? relativeTime(latest.startTime) : '—'}</span>
        <span>{totalExecutions.toLocaleString()} total execution{totalExecutions === 1 ? '' : 's'}</span>
      </div>
    </Link>
  );
}

// exported for potential reuse / debugging in dev tools
export { formatDateTime };
