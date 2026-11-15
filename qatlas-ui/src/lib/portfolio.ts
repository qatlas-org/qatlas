import type {
  Application,
  DashboardProjectSummary,
  TestExecution,
} from '../types/domain';

import {
  dayKey,
  dayKeysBetween,
  rangeStartDate,
  type DateRangeKey,
} from './dateRange';


export type ProjectStatus =
    | 'FAILED'
    | 'RUNNING'
    | 'WARNING'
    | 'PASSED';


/**
 * Only the execution fields that a dashboard project card actually needs.
 *
 * TestExecution also satisfies this type, which means the existing legacy
 * buildProjectCards() function can continue to work.
 */
export interface ProjectCardLatestExecution {
  id: number;
  executedBy?: string | null;
  systemName?: string | null;
  startTime: string;
}


export interface ProjectCardData {
  application: Application;
  latest: ProjectCardLatestExecution | null;
  status: ProjectStatus;
  statusLabel: string;
  totalExecutions: number;

  /**
   * Execution-status distribution for the selected dashboard range.
   */
  segments: {
    status: ProjectStatus;
    value: number;
  }[];

  /**
   * Every executor/machine that executed this project
   * in the currently selected dashboard range.
   */
  executors: string[];
}


function statusOf(exec: TestExecution): ProjectStatus {

  if (!exec.endTime) {
    return 'RUNNING';
  }

  if ((exec.failedTestCaseCount ?? 0) > 0) {
    return 'FAILED';
  }

  if ((exec.skippedTestCaseCount ?? 0) > 0) {
    return 'WARNING';
  }

  return 'PASSED';
}


function statusLabelOf(
    exec: TestExecution,
    status: ProjectStatus
): string {

  if (status === 'RUNNING') {
    return 'Running';
  }

  if (status === 'FAILED') {
    return `${exec.failedTestCaseCount ?? 0} failed`;
  }

  if (status === 'WARNING') {
    return `${exec.skippedTestCaseCount ?? 0} skipped`;
  }

  return 'All passed';
}


/**
 * Status label used by the new dashboard aggregate response.
 *
 * The backend already determines the latest execution status, including
 * the 24-hour safeguard for old executions with a missing end time.
 */
function dashboardStatusLabel(
    status: ProjectStatus,
    hasExecution: boolean
): string {

  if (!hasExecution) {
    return 'No executions in range';
  }

  switch (status) {

    case 'RUNNING':
      return 'Running';

    case 'FAILED':
      return 'Failed';

    case 'WARNING':
      return 'Warning';

    case 'PASSED':
    default:
      return 'Passed';
  }
}


/**
 * Builds dashboard project cards directly from the aggregate /dashboard
 * project summaries.
 *
 * This is the correct source for the dashboard because these values already
 * respect 30D / 3M / 6M / 1Y / ALL and are not limited to the latest
 * /test-execution rows.
 */
export function buildDashboardProjectCards(
    projects: DashboardProjectSummary[]
): ProjectCardData[] {

  return projects.map((project) => {

    const application: Application = {
      id: project.applicationId,
      name: project.applicationName,
      description: project.applicationDescription ?? undefined,
    };


    const latest: ProjectCardLatestExecution | null =
        project.latestExecutionId != null &&
        project.latestStartTime
            ? {
              id: project.latestExecutionId,
              executedBy: project.latestExecutedBy,
              systemName: project.latestSystemName,
              startTime: project.latestStartTime,
            }
            : null;


    const status: ProjectStatus =
        project.latestStatus ?? 'PASSED';


    const rawSegments: {
      status: ProjectStatus;
      value: number;
    }[] = [
      {
        status: 'PASSED',
        value: project.passedExecutions,
      },
      {
        status: 'FAILED',
        value: project.failedExecutions,
      },
      {
        status: 'WARNING',
        value: project.warningExecutions,
      },
      {
        status: 'RUNNING',
        value: project.runningExecutions,
      },
    ];


    const segments =
        rawSegments.filter((segment) => segment.value > 0);


    return {
      application,
      latest,
      status,
      statusLabel: dashboardStatusLabel(
          status,
          latest !== null
      ),
      totalExecutions: project.totalExecutions,
      segments,
      executors: project.executors ?? [],
    };
  });
}


/**
 * Legacy builder retained for other UI code that may still use raw
 * TestExecution responses.
 */
export function buildProjectCards(
    applications: Application[],
    executions: TestExecution[]
): ProjectCardData[] {

  const byApp =
      new Map<number, TestExecution[]>();

  for (const exec of executions) {

    const list =
        byApp.get(exec.applicationId) ?? [];

    list.push(exec);

    byApp.set(
        exec.applicationId,
        list
    );
  }


  return applications.map((app) => {

    const appExecs =
        (byApp.get(app.id) ?? [])
            .slice()
            .sort(
                (a, b) =>
                    new Date(b.startTime).getTime() -
                    new Date(a.startTime).getTime()
            );


    const latest =
        appExecs[0] ?? null;


    const executors =
        Array.from(
            new Set(
                appExecs
                    .map(
                        (exec) =>
                            exec.executedBy ||
                            exec.systemName
                    )
                    .filter(
                        (name): name is string =>
                            Boolean(name)
                    )
            )
        );


    if (!latest) {

      return {
        application: app,
        latest: null,
        status: 'PASSED',
        statusLabel: 'No executions yet',
        totalExecutions: 0,
        segments: [],
        executors: [],
      };
    }


    const status =
        statusOf(latest);

    const passed =
        latest.passedTestCaseCount ?? 0;

    const failed =
        latest.failedTestCaseCount ?? 0;

    const skipped =
        latest.skippedTestCaseCount ?? 0;

    const inProgress =
        latest.inProgressTestCaseCount ?? 0;


    const rawSegments: {
      status: ProjectStatus;
      value: number;
    }[] = [
      {
        status: 'PASSED',
        value: passed,
      },
      {
        status: 'FAILED',
        value: failed,
      },
      {
        status: 'WARNING',
        value: skipped,
      },
      {
        status: 'RUNNING',
        value: inProgress,
      },
    ];


    const segments =
        rawSegments.filter(
            (segment) =>
                segment.value > 0
        );


    return {
      application: app,
      latest,
      status,
      statusLabel:
          statusLabelOf(
              latest,
              status
          ),
      totalExecutions:
      appExecs.length,
      segments,
      executors,
    };
  });
}


/**
 * Running executions surface first,
 * then failing,
 * then warning,
 * then passed.
 *
 * Within each group use execution recency.
 */
export function sortProjectCards(
    cards: ProjectCardData[],
    mode: 'priority' | 'recent' | 'alpha'
): ProjectCardData[] {

  const withTime =
      (card: ProjectCardData) =>
          card.latest
              ? new Date(
                  card.latest.startTime
              ).getTime()
              : 0;


  const sorted =
      cards.slice();


  const PRIORITY:
      Record<ProjectStatus, number> = {

    RUNNING: 0,
    FAILED: 1,
    WARNING: 2,
    PASSED: 3,
  };


  if (mode === 'alpha') {

    sorted.sort(
        (a, b) =>
            a.application.name.localeCompare(
                b.application.name
            )
    );

  } else if (mode === 'recent') {

    sorted.sort(
        (a, b) =>
            withTime(b) -
            withTime(a)
    );

  } else {

    sorted.sort(
        (a, b) => {

          const diff =
              PRIORITY[a.status] -
              PRIORITY[b.status];

          if (diff !== 0) {
            return diff;
          }

          return (
              withTime(b) -
              withTime(a)
          );
        }
    );
  }


  return sorted;
}


export interface PortfolioKpis {
  activeProjects: number;
  executionsInRange: number;
  passRate: number | null;
  currentlyRunning: number;
}


/**
 * Legacy range helper.
 *
 * ALL has no fixed start date, so use Unix epoch only for these old
 * frontend helper functions. The real dashboard does NOT use this:
 * ALL is handled correctly by the backend /dashboard endpoint.
 */
function legacyRangeStart(
    range: DateRangeKey
): Date {

  if (range === 'ALL') {
    return new Date(0);
  }

  return rangeStartDate(range);
}


export function computeKpis(
    applications: Application[],
    executions: TestExecution[],
    range: DateRangeKey
): PortfolioKpis {

  const start =
      legacyRangeStart(range);


  const inRange =
      executions.filter(
          (execution) =>
              new Date(
                  execution.startTime
              ) >= start
      );


  const activeAppIds =
      new Set(
          inRange.map(
              (execution) =>
                  execution.applicationId
          )
      );


  const totalPassed =
      inRange.reduce(
          (sum, execution) =>
              sum +
              (
                  execution.passedTestCaseCount ??
                  0
              ),
          0
      );


  const totalExecuted =
      inRange.reduce(
          (sum, execution) =>
              sum +
              (
                  execution.executedTestCaseCount ??
                  0
              ),
          0
      );


  const currentlyRunning =
      executions.filter(
          (execution) =>
              !execution.endTime
      ).length;


  return {
    activeProjects:
        activeAppIds.size ||
        applications.length,

    executionsInRange:
    inRange.length,

    passRate:
        totalExecuted > 0
            ? Math.round(
                (
                    totalPassed /
                    totalExecuted
                ) * 100
            )
            : null,

    currentlyRunning,
  };
}


export interface DailyPoint {
  date: string;
  executions: number;
  passed: number;
  failed: number;
}


export function computeDailyTrend(
    executions: TestExecution[],
    range: DateRangeKey
): DailyPoint[] {

  const start =
      legacyRangeStart(range);

  const now =
      new Date();


  const inRange =
      executions.filter(
          (execution) =>
              new Date(
                  execution.startTime
              ) >= start
      );


  const byDay =
      new Map<string, DailyPoint>();


  for (
      const key of
      dayKeysBetween(
          start,
          now
      )
      ) {

    byDay.set(
        key,
        {
          date: key,
          executions: 0,
          passed: 0,
          failed: 0,
        }
    );
  }


  for (const execution of inRange) {

    const key =
        dayKey(
            new Date(
                execution.startTime
            )
        );


    const point =
        byDay.get(key);


    if (!point) {
      continue;
    }


    point.executions += 1;

    point.passed +=
        execution.passedTestCaseCount ??
        0;

    point.failed +=
        execution.failedTestCaseCount ??
        0;
  }


  return Array.from(
      byDay.values()
  );
}


export interface MachinePoint {
  name: string;
  count: number;
}


export function computeExecutionsByMachine(
    executions: TestExecution[],
    range: DateRangeKey,
    topN = 8
): MachinePoint[] {

  const start =
      legacyRangeStart(range);


  const inRange =
      executions.filter(
          (execution) =>
              new Date(
                  execution.startTime
              ) >= start
      );


  const counts =
      new Map<string, number>();


  for (const execution of inRange) {

    const name =
        execution.executedBy ||
        execution.systemName ||
        'unknown';


    counts.set(
        name,
        (
            counts.get(name) ??
            0
        ) + 1
    );
  }


  return Array.from(
      counts.entries()
  )
      .map(
          ([name, count]) => ({
            name,
            count,
          })
      )
      .sort(
          (a, b) =>
              b.count -
              a.count
      )
      .slice(
          0,
          topN
      );
}


export function uniqueExecutors(
    executions: TestExecution[]
): string[] {

  const set =
      new Set<string>();


  for (const execution of executions) {

    const name =
        execution.executedBy ||
        execution.systemName;


    if (name) {
      set.add(name);
    }
  }


  return Array.from(set)
      .sort();
}