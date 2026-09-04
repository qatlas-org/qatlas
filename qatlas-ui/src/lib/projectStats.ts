import type { TestExecution } from '../types/domain';
import { dayKey, dayKeysBetween, rangeStartDate, type DateRangeKey } from './dateRange';

export interface DailyOutcomeBucket {
  date: string;
  outcomes: ('pass' | 'fail' | 'running')[];
}

/** Buckets a project's executions by day, each bucket holding an ordered list
 * (chronological) of per-execution outcomes — powers the stacked "executions
 * per day" chart where each segment is one real execution. */
export function bucketExecutionsByDay(executions: TestExecution[], range: DateRangeKey): DailyOutcomeBucket[] {
  const start = rangeStartDate(range);
  const now = new Date();
  const sorted = executions
    .filter((e) => new Date(e.startTime) >= start)
    .slice()
    .sort((a, b) => new Date(a.startTime).getTime() - new Date(b.startTime).getTime());

  const byDay = new Map<string, DailyOutcomeBucket>();
  for (const key of dayKeysBetween(start, now)) {
    byDay.set(key, { date: key, outcomes: [] });
  }
  for (const e of sorted) {
    const key = dayKey(new Date(e.startTime));
    const bucket = byDay.get(key);
    if (!bucket) continue;
    let outcome: 'pass' | 'fail' | 'running';
    if (!e.endTime) outcome = 'running';
    else if ((e.failedTestCaseCount ?? 0) > 0) outcome = 'fail';
    else outcome = 'pass';
    bucket.outcomes.push(outcome);
  }
  return Array.from(byDay.values());
}

export interface PassRatePoint {
  date: string;
  rate: number | null;
}

export function computePassRateTrend(executions: TestExecution[], range: DateRangeKey): PassRatePoint[] {
  const start = rangeStartDate(range);
  const now = new Date();
  const byDay = new Map<string, { passed: number; executed: number }>();
  for (const key of dayKeysBetween(start, now)) byDay.set(key, { passed: 0, executed: 0 });

  for (const e of executions) {
    if (new Date(e.startTime) < start) continue;
    const key = dayKey(new Date(e.startTime));
    const bucket = byDay.get(key);
    if (!bucket) continue;
    bucket.passed += e.passedTestCaseCount ?? 0;
    bucket.executed += e.executedTestCaseCount ?? 0;
  }

  return Array.from(byDay.entries()).map(([date, b]) => ({
    date,
    rate: b.executed > 0 ? Math.round((b.passed / b.executed) * 100) : null,
  }));
}

export function currentlyRunning(executions: TestExecution[]): TestExecution[] {
  return executions.filter((e) => !e.endTime);
}
