import { useLocation, useNavigate } from 'react-router-dom';
import { LayoutGrid, FolderKanban, PlayCircle } from 'lucide-react';
import { useApplications, useExecutions } from '../hooks/useQueries';
import { getCurrentProjectId } from '../lib/currentProject';

interface QuickNavTarget {
  label: string;
  icon: typeof LayoutGrid;
  path: string | null;
}

export function QuickNav() {
  const location = useLocation();
  const navigate = useNavigate();
  const { data: applications } = useApplications();
  const { data: executions } = useExecutions();

  // "current" project: whatever was last visited/switched to, falling back to
  // the first project in the list per the agreed default
  const currentProjectId = getCurrentProjectId() ?? applications?.[0]?.id ?? null;

  const projectExecutions = (executions ?? []).filter((e) => e.applicationId === currentProjectId);
  const latestExecution = projectExecutions.slice().sort(
    (a, b) => new Date(b.startTime).getTime() - new Date(a.startTime).getTime()
  )[0];

  const targets: QuickNavTarget[] = [
    { label: 'Dashboard', icon: LayoutGrid, path: '/' },
    { label: 'Project Details', icon: FolderKanban, path: currentProjectId ? `/projects/${currentProjectId}` : null },
    { label: 'Execution Details', icon: PlayCircle, path: latestExecution ? `/executions/${latestExecution.id}` : null },
  ];

  function go(path: string | null) {
    if (!path) return;
    if (location.pathname === path) {
      window.location.reload();
    } else {
      navigate(path);
    }
  }

  return (
    <div className="flex items-center gap-1 rounded-lg border border-[var(--color-border)] bg-white p-0.5">
      {targets.map((t) => {
        const Icon = t.icon;
        const isActive = location.pathname === t.path;
        const disabled = !t.path;
        return (
          <button
            key={t.label}
            onClick={() => go(t.path)}
            disabled={disabled}
            title={disabled ? `${t.label}: no data yet` : isActive ? `Refresh ${t.label}` : `Go to ${t.label}`}
            className={`flex items-center gap-1.5 rounded-md px-2.5 py-1.5 text-xs font-medium ${
              isActive ? 'bg-slate-900 text-white' : 'text-[var(--color-ink-muted)] hover:bg-slate-100'
            } ${disabled ? 'cursor-not-allowed opacity-40' : ''}`}
          >
            <Icon className="h-3.5 w-3.5" />
            {t.label}
          </button>
        );
      })}
    </div>
  );
}
