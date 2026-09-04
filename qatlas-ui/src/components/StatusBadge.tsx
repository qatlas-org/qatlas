import type { ExecutionStatus } from '../types/domain';
import { CheckCircle2, XCircle, AlertTriangle, Loader2, CircleSlash } from 'lucide-react';

const STYLES: Record<ExecutionStatus, { label: string; classes: string; Icon: typeof CheckCircle2 }> = {
  PASSED: { label: 'Passed', classes: 'bg-green-50 text-green-700 border-green-200', Icon: CheckCircle2 },
  FAILED: { label: 'Failed', classes: 'bg-red-50 text-red-700 border-red-200', Icon: XCircle },
  WARNING: { label: 'Warning', classes: 'bg-amber-50 text-amber-700 border-amber-200', Icon: AlertTriangle },
  PROGRESS: { label: 'In Progress', classes: 'bg-blue-50 text-blue-700 border-blue-200', Icon: Loader2 },
  SKIPPED: { label: 'Skipped', classes: 'bg-slate-100 text-slate-500 border-slate-200', Icon: CircleSlash },
};

export function StatusBadge({ status }: { status: ExecutionStatus }) {
  const s = STYLES[status] ?? STYLES.PROGRESS;
  const { Icon } = s;
  return (
    <span
      className={`inline-flex items-center gap-1.5 rounded-full border px-2.5 py-1 text-xs font-medium ${s.classes}`}
    >
      <Icon className={`h-3.5 w-3.5 ${status === 'PROGRESS' ? 'animate-spin' : ''}`} />
      {s.label}
    </span>
  );
}
