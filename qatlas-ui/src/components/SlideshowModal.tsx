import { useEffect, useMemo, useState } from 'react';
import {
  ChevronLeft,
  ChevronRight,
  ExternalLink,
  File,
  FileCode2,
  FileJson,
  FileText,
  X,
} from 'lucide-react';

import { useTestCaseSteps } from '../hooks/useQueries';
import {
  attachmentDisplayType,
  attachmentFileName,
  attachmentUrl,
  isImageAttachment,
} from '../lib/attachments';
import { LoadingState } from './Primitives';

interface SlideshowModalProps {
  testCaseId: number;
  testCaseName: string;
  onClose: () => void;

  /**
   * Allows the Test Case page to open the viewer
   * directly on a specific attachment.
   */
  initialAttachmentPath?: string;
}

export function SlideshowModal({
                                 testCaseId,
                                 testCaseName,
                                 onClose,
                                 initialAttachmentPath,
                               }: SlideshowModalProps) {
  const { data: steps, isLoading } = useTestCaseSteps(testCaseId);

  /**
   * Flatten every attachment from every test step.
   *
   * Example:
   *
   * Step 1
   *   screenshot-1.png
   *   response.json
   *
   * Step 2
   *   screenshot-2.png
   *
   * becomes 3 individual viewer items.
   */
  const items = useMemo(() => {
    return (steps ?? []).flatMap((step) =>
        (step.attachments ?? []).map((attachment) => ({
          step,
          attachment,
        }))
    );
  }, [steps]);

  /**
   * Find the attachment that was clicked in TestCaseDetailPage.
   */
  const initialIndex = useMemo(() => {
    if (!initialAttachmentPath) {
      return 0;
    }

    const foundIndex = items.findIndex(
        ({ attachment }) =>
            attachment.attachmentRelativePath === initialAttachmentPath
    );

    return foundIndex >= 0 ? foundIndex : 0;
  }, [items, initialAttachmentPath]);

  const [index, setIndex] = useState(0);

  /**
   * React Query loads attachments asynchronously.
   * Once items arrive, move directly to the requested attachment.
   */
  useEffect(() => {
    setIndex(initialIndex);
  }, [initialIndex]);

  /**
   * Protect against the index being outside the available items.
   */
  const safeIndex =
      items.length === 0
          ? 0
          : Math.min(Math.max(index, 0), items.length - 1);

  const current = items[safeIndex];

  const attachment = current?.attachment;
  const step = current?.step;

  const url = attachmentUrl(attachment?.attachmentRelativePath);
  const fileName = attachmentFileName(attachment);
  const fileType = attachmentDisplayType(attachment);
  const image = isImageAttachment(attachment);

  function openFile() {
    if (!url) {
      return;
    }

    window.open(url, '_blank', 'noopener,noreferrer');
  }

  function previous() {
    setIndex((currentIndex) => Math.max(0, currentIndex - 1));
  }

  function next() {
    setIndex((currentIndex) =>
        Math.min(items.length - 1, currentIndex + 1)
    );
  }

  function fileIcon() {
    switch (fileType) {
      case 'JSON':
        return <FileJson className="h-14 w-14 text-slate-500" />;

      case 'XML':
      case 'HTML':
      case 'HTM':
        return <FileCode2 className="h-14 w-14 text-slate-500" />;

      case 'TXT':
      case 'LOG':
      case 'CSV':
        return <FileText className="h-14 w-14 text-slate-500" />;

      default:
        return <File className="h-14 w-14 text-slate-500" />;
    }
  }

  return (
      <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-6"
          onClick={onClose}
      >
        <div
            className="w-full max-w-4xl rounded-2xl border border-[var(--color-border)] bg-white shadow-2xl"
            onClick={(event) => event.stopPropagation()}
        >
          {/* Header */}
          <div className="flex items-start justify-between px-6 pt-5">
            <div>
              <p className="text-xs font-medium text-[var(--color-ink-muted)]">
                {items.length > 0
                    ? `Attachment ${safeIndex + 1} of ${items.length}`
                    : 'No attachments'}
              </p>

              <p className="mt-0.5 text-xs text-[var(--color-ink-muted)]">
                {testCaseName}
              </p>
            </div>

            <button
                type="button"
                onClick={onClose}
                className="rounded-lg bg-slate-100 p-1.5 hover:bg-slate-200"
                aria-label="Close attachment viewer"
            >
              <X className="h-4 w-4 text-slate-700" />
            </button>
          </div>

          {isLoading ? (
              <LoadingState label="Loading attachments…" />
          ) : items.length === 0 ? (
              <p className="px-6 py-16 text-center text-sm text-[var(--color-ink-muted)]">
                This test case has no attachments.
              </p>
          ) : (
              <>
                {/* Viewer */}
                <div className="relative mx-6 mt-4 flex h-[430px] items-center justify-center overflow-hidden rounded-xl border border-[var(--color-border)] bg-[var(--color-surface-muted)]">

                  {/* Image attachment */}
                  {image && url ? (
                      <button
                          type="button"
                          onClick={openFile}
                          className="flex h-full w-full cursor-zoom-in items-center justify-center"
                          title="Open original image"
                      >
                        <img
                            src={url}
                            alt={fileName}
                            className="max-h-full max-w-full object-contain"
                        />
                      </button>
                  ) : (
                      /* Non-image attachment */
                      <div className="flex max-w-md flex-col items-center rounded-xl border border-[var(--color-border)] bg-white px-10 py-8 text-center shadow-sm">
                        {fileIcon()}

                        <p className="mt-4 max-w-full break-all text-sm font-semibold text-slate-900">
                          {fileName}
                        </p>

                        <span className="mt-1 rounded bg-slate-100 px-2 py-1 text-[10px] font-semibold text-slate-600">
                    {fileType}
                  </span>

                        {url && (
                            <button
                                type="button"
                                onClick={openFile}
                                className="mt-5 flex items-center gap-2 rounded-lg border border-[var(--color-border)] bg-white px-4 py-2 text-xs font-medium text-slate-700 hover:bg-slate-50"
                            >
                              <ExternalLink className="h-3.5 w-3.5" />
                              Open file
                            </button>
                        )}
                      </div>
                  )}

                  {/* Previous */}
                  {items.length > 1 && (
                      <button
                          type="button"
                          onClick={previous}
                          disabled={safeIndex === 0}
                          className="absolute left-3 top-1/2 -translate-y-1/2 rounded-full bg-white p-2 shadow disabled:cursor-not-allowed disabled:opacity-30"
                          aria-label="Previous attachment"
                      >
                        <ChevronLeft className="h-4 w-4" />
                      </button>
                  )}

                  {/* Next */}
                  {items.length > 1 && (
                      <button
                          type="button"
                          onClick={next}
                          disabled={safeIndex === items.length - 1}
                          className="absolute right-3 top-1/2 -translate-y-1/2 rounded-full bg-white p-2 shadow disabled:cursor-not-allowed disabled:opacity-30"
                          aria-label="Next attachment"
                      >
                        <ChevronRight className="h-4 w-4" />
                      </button>
                  )}
                </div>

                {/* Attachment details */}
                <div className="space-y-3 px-6 py-4">
                  <div>
                    <p className="text-[10px] font-semibold uppercase tracking-wide text-[var(--color-ink-muted)]">
                      File
                    </p>

                    <p className="break-all text-sm text-slate-900">
                      {fileName}
                    </p>
                  </div>

                  <div>
                    <p className="text-[10px] font-semibold uppercase tracking-wide text-[var(--color-ink-muted)]">
                      Step
                    </p>

                    <p className="text-sm text-slate-900">
                      {step?.description ?? '—'}
                    </p>
                  </div>

                  <div>
                    <p className="text-[10px] font-semibold uppercase tracking-wide text-[var(--color-ink-muted)]">
                      Actual
                    </p>

                    <p
                        className={`text-sm ${
                            step?.executionStatus === 'FAILED'
                                ? 'text-red-600'
                                : step?.executionStatus === 'WARNING'
                                    ? 'text-amber-600'
                                    : 'text-green-700'
                        }`}
                    >
                      {step?.result || '—'}
                    </p>
                  </div>
                </div>

                {/* Attachment navigation indicators */}
                <div className="flex max-h-12 flex-wrap justify-center gap-2 overflow-y-auto border-t border-[var(--color-border)] px-6 py-3">
                  {items.map(({ attachment }, itemIndex) => (
                      <button
                          type="button"
                          key={`${attachment.id ?? 'attachment'}-${itemIndex}`}
                          onClick={() => setIndex(itemIndex)}
                          title={attachmentFileName(attachment)}
                          className={`h-2 w-2 rounded-full transition ${
                              itemIndex === safeIndex
                                  ? 'bg-blue-600'
                                  : 'bg-slate-200 hover:bg-slate-400'
                          }`}
                          aria-label={`Open attachment ${itemIndex + 1}`}
                      />
                  ))}
                </div>
              </>
          )}
        </div>
      </div>
  );
}