import { useQuery } from '@tanstack/react-query';
import { api } from '../api/client';
import type { ExecutionStatus } from '../types/domain';

// Stale time is deliberately generous: this is a reporting tool over
// already-completed test runs, so data doesn't change under the user's feet.
// That means switching between screens re-uses cache instead of re-fetching,
// which is most of where the "faster response" requirement comes from.
const REPORT_STALE_TIME = 60_000;

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
