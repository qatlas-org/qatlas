import { useState, useRef, useEffect, type ReactNode } from 'react';
import { MoreHorizontal } from 'lucide-react';

export interface MenuItem {
  label: string;
  onClick: () => void;
  danger?: boolean;
}

/** Generic kebab dropdown, used for the "Export scope" menu (Copy to clipboard /
 * Export as JPEG / PDF / Excel) and the table export menu (CSV / Excel / Screenshot). */
export function KebabMenu({ items, icon }: { items: (MenuItem | 'divider')[]; icon?: ReactNode }) {
  const [open, setOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    function onClickOutside(e: MouseEvent) {
      if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false);
    }
    document.addEventListener('mousedown', onClickOutside);
    return () => document.removeEventListener('mousedown', onClickOutside);
  }, []);

  return (
    <div className="relative" ref={ref}>
      <button
        onClick={() => setOpen((o) => !o)}
        className="flex h-8 w-8 items-center justify-center rounded-lg border border-[var(--color-border)] bg-white text-[var(--color-ink-muted)] hover:bg-slate-50"
      >
        {icon ?? <MoreHorizontal className="h-4 w-4" />}
      </button>
      {open && (
        <div className="absolute right-0 z-20 mt-1 w-56 rounded-lg border border-[var(--color-border)] bg-white py-1 shadow-lg">
          {items.map((item, i) =>
            item === 'divider' ? (
              <div key={i} className="my-1 border-t border-[var(--color-border)]" />
            ) : (
              <button
                key={i}
                onClick={() => { item.onClick(); setOpen(false); }}
                className={`block w-full px-4 py-2 text-left text-sm hover:bg-slate-50 ${
                  item.danger ? 'text-red-600' : 'text-slate-700'
                }`}
              >
                {item.label}
              </button>
            )
          )}
        </div>
      )}
    </div>
  );
}

/** Wraps a section in a dashed outline with an "Export scope" tag, matching
 * the Figma convention for marking what a snapshot export captures. */
export function ExportScope({ children }: { children: ReactNode }) {
  return (
    <div className="relative rounded-xl border border-dashed border-blue-400 p-3 pt-5">
      <span className="absolute -top-2.5 left-3 bg-white px-1.5 text-[10px] font-medium text-blue-600">
        Export scope
      </span>
      {children}
    </div>
  );
}
