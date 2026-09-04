export type DateRangeKey = '30D' | '3M' | '6M' | '1Y';

const DAYS_BY_RANGE: Record<DateRangeKey, number> = {
  '30D': 30,
  '3M': 90,
  '6M': 180,
  '1Y': 365,
};

export const RANGE_OPTIONS: DateRangeKey[] = ['30D', '3M', '6M', '1Y'];

export const RANGE_LABELS: Record<DateRangeKey, string> = {
  '30D': 'last 30 days',
  '3M': 'last 3 months',
  '6M': 'last 6 months',
  '1Y': 'last 12 months',
};

export function rangeStartDate(range: DateRangeKey, now: Date = new Date()): Date {
  const start = new Date(now);
  start.setDate(start.getDate() - DAYS_BY_RANGE[range]);
  start.setHours(0, 0, 0, 0);
  return start;
}

export function dayKey(date: Date): string {
  return date.toISOString().slice(0, 10); // YYYY-MM-DD
}

/** Generates every day key between start and end (inclusive), so charts show gaps as zero rather than skipping days. */
export function dayKeysBetween(start: Date, end: Date): string[] {
  const keys: string[] = [];
  const cursor = new Date(start);
  cursor.setHours(0, 0, 0, 0);
  const endDay = new Date(end);
  endDay.setHours(0, 0, 0, 0);
  while (cursor <= endDay) {
    keys.push(dayKey(cursor));
    cursor.setDate(cursor.getDate() + 1);
  }
  return keys;
}
