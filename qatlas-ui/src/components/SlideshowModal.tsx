import { useState } from 'react';
import { X, ChevronLeft, ChevronRight } from 'lucide-react';
import { useTestCaseSteps } from '../hooks/useQueries';
import { attachmentUrl } from '../lib/attachments';
import { LoadingState } from './Primitives';

export function SlideshowModal({ testCaseId, testCaseName, onClose }: {
  testCaseId: number; testCaseName: string; onClose: () => void;
}) {
  const { data: steps, isLoading } = useTestCaseSteps(testCaseId);
  const [index, setIndex] = useState(0);

  // only steps that actually have an attachment are worth showing in the slideshow
  const withAttachments = (steps ?? []).filter((s) => s.attachments && s.attachments.length > 0);
  const current = withAttachments[index];
  const shot = current?.attachments?.[0] ? attachmentUrl(current.attachments[0].attachmentRelativePath) : undefined;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-6" onClick={onClose}>
      <div
        className="w-full max-w-3xl rounded-2xl border border-[var(--color-border)] bg-white shadow-2xl"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-start justify-between px-6 pt-5">
          <div>
            <p className="text-xs font-medium text-[var(--color-ink-muted)]">
              {withAttachments.length > 0 ? `Step ${index + 1} of ${withAttachments.length}` : 'No screenshots'}
            </p>
            <p className="mt-0.5 text-xs text-[var(--color-ink-muted)]">{testCaseName}</p>
          </div>
          <button onClick={onClose} className="rounded-lg bg-slate-100 p-1.5 hover:bg-slate-200">
            <X className="h-4 w-4 text-slate-700" />
          </button>
        </div>

        {isLoading ? (
          <LoadingState label="Loading screenshots…" />
        ) : withAttachments.length === 0 ? (
          <p className="px-6 py-16 text-center text-sm text-[var(--color-ink-muted)]">
            This test case has no step screenshots.
          </p>
        ) : (
          <>
            <div className="relative mx-6 mt-4 h-80 overflow-hidden rounded-xl border border-[var(--color-border)] bg-[var(--color-surface-muted)]">
              {shot ? (
                <img src={shot} alt={current.description} className="h-full w-full object-contain" />
              ) : (
                <div className="flex h-full items-center justify-center text-xs text-[var(--color-ink-muted)]">No preview</div>
              )}
              {withAttachments.length > 1 && (
                <>
                  <button
                    onClick={() => setIndex((i) => Math.max(0, i - 1))}
                    disabled={index === 0}
                    className="absolute left-3 top-1/2 -translate-y-1/2 rounded-full bg-white p-2 shadow disabled:opacity-30"
                  >
                    <ChevronLeft className="h-4 w-4" />
                  </button>
                  <button
                    onClick={() => setIndex((i) => Math.min(withAttachments.length - 1, i + 1))}
                    disabled={index === withAttachments.length - 1}
                    className="absolute right-3 top-1/2 -translate-y-1/2 rounded-full bg-white p-2 shadow disabled:opacity-30"
                  >
                    <ChevronRight className="h-4 w-4" />
                  </button>
                </>
              )}
            </div>

            <div className="space-y-3 px-6 py-4">
              <div>
                <p className="text-[10px] font-semibold uppercase tracking-wide text-[var(--color-ink-muted)]">Step</p>
                <p className="text-sm text-slate-900">{current.description}</p>
              </div>
              <div>
                <p className="text-[10px] font-semibold uppercase tracking-wide text-[var(--color-ink-muted)]">Actual</p>
                <p className={`text-sm ${current.executionStatus === 'FAILED' ? 'text-red-600' : 'text-green-700'}`}>
                  {current.result || '—'}
                </p>
                {/* Note: the backend's TestStep model has no separate "expected result" field —
                   only the actual observed result is captured, so we can't show an Expected row
                   without a backend change. Flagging rather than fabricating a value. */}
              </div>
            </div>

            <div className="flex justify-center gap-2 border-t border-[var(--color-border)] px-6 py-3">
              {withAttachments.map((_, i) => (
                <button
                  key={i}
                  onClick={() => setIndex(i)}
                  className={`h-2 w-2 rounded-full ${i === index ? 'bg-blue-600' : 'bg-slate-200'}`}
                />
              ))}
            </div>
          </>
        )}
      </div>
    </div>
  );
}
