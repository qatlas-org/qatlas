import type { Application, TestExecution } from '../types/domain';
import { dayKey, dayKeysBetween, rangeStartDate, type DateRangeKey } from './dateRange';

export type ProjectStatus = 'FAILED' | 'RUNNING' | 'WARNING' | 'PASSED';

export interface ProjectCardData {
  application: Application;
  latest: TestExecution | null;
  status: ProjectStatus;
  statusLabel: string;
  totalExecutions: number;
  segments: { status: ProjectStatus; value: number }[];
}

function statusOf(exec: TestExecution): ProjectStatus {
  if (!exec.endTime) return 'RUNNING';
  if ((exec.failedTestCaseCount ?? 0) > 0) return 'FAILED';
  if ((exec.skippedTestCaseCount ?? 0) > 0) return 'WARNING';
  return 'PASSED';
}

function statusLabelOf(exec: TestExecution, status: ProjectStatus): string {
  if (status === 'RUNNING') return 'Running';
  if (status === 'FAILED') return `${exec.failedTestCaseCount} failed`;
  if (status === 'WARNING') return `${exec.skippedTestCaseCount} skipped`;
  return 'All passed';
}

/** Builds one card per application from its most recent execution, plus a lifetime execution count. */
export function buildProjectCards(applications: Application[], executions: TestExecution[]): ProjectCardData[] {
  const byApp = new Map<number, TestExecution[]>();
  for (const exec of executions) {
    const list = byApp.get(exec.applicationId) ?? [];
    list.push(exec);
    byApp.set(exec.applicationId, list);
  }

  return applications.map((app) => {
    const appExecs = (byApp.get(app.id) ?? []).slice().sort(
      (a, b) => new Date(b.startTime).getTime() - new Date(a.startTime).getTime()
    );
    const latest = appExecs[0] ?? null;
    if (!latest) {
      return {
        application: app,
        latest: null,
        status: 'PASSED',
        statusLabel: 'No executions yet',
        totalExecutions: 0,
        segments: [],
      };
    }
    const status = statusOf(latest);
    const passed = latest.passedTestCaseCount ?? 0;
    const failed = latest.failedTestCaseCount ?? 0;
    const skipped = latest.skippedTestCaseCount ?? 0;
    const inProgress = latest.inProgressTestCaseCount ?? 0;
    const rawSegments: { status: ProjectStatus; value: number }[] = [
      { status: 'PASSED' as const, value: passed },
      { status: 'FAILED' as const, value: failed },
      { status: 'WARNING' as const, value: skipped },
      { status: 'RUNNING' as const, value: inProgress },
    ];
    const segments = rawSegments.filter((s) => s.value > 0);

    return {
      application: app,
      latest,
      status,
      statusLabel: statusLabelOf(latest, status),
      totalExecutions: appExecs.length,
      segments,
    };
  });
}

/** Running executions surface first (someone may be actively watching them),
 * then failing (needs attention), then skipped, then passed — each group by recency. */
export function sortProjectCards(
  cards: ProjectCardData[],
  mode: 'priority' | 'recent' | 'alpha'
): ProjectCardData[] {
  const withTime = (c: ProjectCardData) => (c.latest ? new Date(c.latest.startTime).getTime() : 0);
  const sorted = cards.slice();
  const PRIORITY: Record<ProjectStatus, number> = { RUNNING: 0, FAILED: 1, WARNING: 2, PASSED: 3 };
  if (mode === 'alpha') {
    sorted.sort((a, b) => a.application.name.localeCompare(b.application.name));
  } else if (mode === 'recent') {
    sorted.sort((a, b) => withTime(b) - withTime(a));
  } else {
    sorted.sort((a, b) => {
      const diff = PRIORITY[a.status] - PRIORITY[b.status];
      if (diff !== 0) return diff;
      return withTime(b) - withTime(a);
    });
  }
  return sorted;
}

export interface PortfolioKpis {
  activeProjects: number;
  executionsInRange: number;
  passRate: number | null;
  currentlyRunning: number;
}

export function computeKpis(
  applications: Application[],
  executions: TestExecution[],
  range: DateRangeKey
): PortfolioKpis {
  const start = rangeStartDate(range);
  const inRange = executions.filter((e) => new Date(e.startTime) >= start);
  const activeAppIds = new Set(inRange.map((e) => e.applicationId));
  const totalPassed = inRange.reduce((s, e) => s + (e.passedTestCaseCount ?? 0), 0);
  const totalExecuted = inRange.reduce((s, e) => s + (e.executedTestCaseCount ?? 0), 0);
  const currentlyRunning = executions.filter((e) => !e.endTime).length;

  return {
    activeProjects: activeAppIds.size || applications.length,
    executionsInRange: inRange.length,
    passRate: totalExecuted > 0 ? Math.round((totalPassed / totalExecuted) * 100) : null,
    currentlyRunning,
  };
}

export interface DailyPoint {
  date: string;
  executions: number;
  passed: number;
  failed: number;
}

export function computeDailyTrend(executions: TestExecution[], range: DateRangeKey): DailyPoint[] {
  const start = rangeStartDate(range);
  const now = new Date();
  const inRange = executions.filter((e) => new Date(e.startTime) >= start);

  const byDay = new Map<string, DailyPoint>();
  for (const key of dayKeysBetween(start, now)) {
    byDay.set(key, { date: key, executions: 0, passed: 0, failed: 0 });
  }
  for (const e of inRange) {
    const key = dayKey(new Date(e.startTime));
    const point = byDay.get(key);
    if (!point) continue;
    point.executions += 1;
    point.passed += e.passedTestCaseCount ?? 0;
    point.failed += e.failedTestCaseCount ?? 0;
  }
  return Array.from(byDay.values());
}

export interface MachinePoint {
  name: string;
  count: number;
}

export function computeExecutionsByMachine(executions: TestExecution[], range: DateRangeKey, topN = 8): MachinePoint[] {
  const start = rangeStartDate(range);
  const inRange = executions.filter((e) => new Date(e.startTime) >= start);
  const counts = new Map<string, number>();
  for (const e of inRange) {
    const name = e.executedBy || e.systemName || 'unknown';
    counts.set(name, (counts.get(name) ?? 0) + 1);
  }
  return Array.from(counts.entries())
    .map(([name, count]) => ({ name, count }))
    .sort((a, b) => b.count - a.count)
    .slice(0, topN);
}

export function uniqueExecutors(executions: TestExecution[]): string[] {
  const set = new Set<string>();
  for (const e of executions) {
    const name = e.executedBy || e.systemName;
    if (name) set.add(name);
  }
  return Array.from(set).sort();
}
