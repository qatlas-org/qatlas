import { useQuery } from '@tanstack/react-query';
import { api } from '../api/client';
import type { ExecutionStatus } from '../types/domain';
import { rangeStartDate, type DateRangeKey } from '../lib/dateRange';
// Stale time is deliberately generous: this is a reporting tool over
// already-completed test runs, so data doesn't change under the user's feet.
// That means switching between screens re-uses cache instead of re-fetching,
// which is most of where the "faster response" requirement comes from.
const REPORT_STALE_TIME = 60_000;

export const useDashboardStats = (range: DateRangeKey) =>
    useQuery({
        queryKey: ['dashboard', range],
        queryFn: () => {

            if (range === 'ALL') {
                return api.dashboard.stats();
            }

            const from = rangeStartDate(range);

            const year = from.getFullYear();
            const month = String(from.getMonth() + 1).padStart(2, '0');
            const day = String(from.getDate()).padStart(2, '0');

            return api.dashboard.stats(
                `${year}-${month}-${day}T00:00:00`
            );
        },
        staleTime: REPORT_STALE_TIME,
    });
export const useDashboardProjects = (
    range: DateRangeKey,
    executor: string
) =>
    useQuery({
        queryKey: ['dashboard-projects', range, executor],

        queryFn: () => {
            const selectedExecutor =
                executor === 'All'
                    ? undefined
                    : executor;

            if (range === 'ALL') {
                return api.dashboard.projects(
                    undefined,
                    selectedExecutor
                );
            }

            const from = rangeStartDate(range);

            const year = from.getFullYear();
            const month = String(
                from.getMonth() + 1
            ).padStart(2, '0');

            const day = String(
                from.getDate()
            ).padStart(2, '0');

            return api.dashboard.projects(
                `${year}-${month}-${day}T00:00:00`,
                selectedExecutor
            );
        },

        enabled: executor !== 'All',

        staleTime: REPORT_STALE_TIME,
    });
export function useProjectExecutionStats(
    applicationId: number | undefined,
    range: DateRangeKey
) {
    return useQuery({
        queryKey: ['project-execution-stats', applicationId, range],

        queryFn: () => {
            if (!applicationId) {
                throw new Error('applicationId is required');
            }

            const from =
                range === 'ALL'
                    ? undefined
                    : (() => {
                        const start = rangeStartDate(range);

                        const year = start.getFullYear();
                        const month = String(start.getMonth() + 1).padStart(2, '0');
                        const day = String(start.getDate()).padStart(2, '0');

                        return `${year}-${month}-${day}T00:00:00`;
                    })();

            return api.dashboard.projectStats(applicationId, from);
        },

        enabled: Boolean(applicationId),

        staleTime: REPORT_STALE_TIME,
    });
}
export const useApplications = () =>
    useQuery({ queryKey: ['applications'], queryFn: api.applications.list, staleTime: REPORT_STALE_TIME });

export const useEnvironments = () =>
    useQuery({ queryKey: ['environments'], queryFn: api.environments.list, staleTime: REPORT_STALE_TIME });

export const useExecutions = () =>
    useQuery({ queryKey: ['executions'], queryFn: api.executions.list, staleTime: REPORT_STALE_TIME });

export const useExecution = (id: number) =>
    useQuery({
        queryKey: ['execution', id],
        queryFn: () => api.executions.getById(id),
        staleTime: REPORT_STALE_TIME,
        enabled: !!id,
    });

export const useExecutionSuites = (executionId: number) =>
    useQuery({
        queryKey: ['execution', executionId, 'suites'],
        queryFn: () => api.executions.suites(executionId),
        staleTime: REPORT_STALE_TIME,
        enabled: !!executionId,
    });

export const useSuiteTestCases = (suiteId: number) =>
    useQuery({
        queryKey: ['suite', suiteId, 'testCases'],
        queryFn: () => api.suites.testCases(suiteId),
        staleTime: REPORT_STALE_TIME,
        enabled: !!suiteId,
    });

export const useTestCaseSteps = (testCaseId: number) =>
    useQuery({
        queryKey: ['testCase', testCaseId, 'steps'],
        queryFn: () => api.testCases.testSteps(testCaseId),
        staleTime: REPORT_STALE_TIME,
        enabled: !!testCaseId,
    });

// kept for future filtered views (e.g. "show only failed cases" toolbar)
export const useExecutionTestCases = (executionId: number, statuses?: ExecutionStatus[]) =>
    useQuery({
        queryKey: ['execution', executionId, 'testCases', statuses],
        queryFn: () => api.executions.testCases(executionId, statuses),
        staleTime: REPORT_STALE_TIME,
        enabled: !!executionId,
    });
