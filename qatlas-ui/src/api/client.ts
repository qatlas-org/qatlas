import axios from 'axios';
import type {
  Application,
  Environment,
  ExecutionStatus,
  TestCase,
  TestExecution,
  TestStep,
  TestSuite,
} from '../types/domain';
import { getToken, clearToken } from '../lib/auth';

// Base URL is injected at build/deploy time via VITE_API_BASE_URL
// (e.g. "http://backend:8080" in docker-compose, "" for same-origin/nginx-proxied prod).
// Never hardcode a host here — that's what breaks when this moves between envs.
const baseURL = import.meta.env.VITE_API_BASE_URL ?? '';

export const http = axios.create({
  baseURL: `${baseURL}/rs`,
  timeout: 30_000,
});

// Attach the admin token when present. Harmless on every other endpoint since
// only the archive/delete route on the backend actually checks it.
http.interceptors.request.use((config) => {
  const token = getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// If the backend ever rejects the token (expired, revoked), drop it locally too
// so the UI reflects "logged out" instead of silently retrying with a dead token.
http.interceptors.response.use(
  (res) => res,
  (error) => {
    if (error?.response?.status === 401) {
      clearToken();
    }
    return Promise.reject(error);
  }
);

export const api = {
  auth: {
    login: (username: string, password: string) =>
      http.post<{ token: string; username: string }>('/auth/login', { username, password }).then((r) => r.data),
  },

  applications: {
    list: () => http.get<Application[]>('/application').then((r) => r.data),
    getById: (id: number) => http.get<Application>(`/application/${id}`).then((r) => r.data),
  },

  environments: {
    list: () => http.get<Environment[]>('/environment').then((r) => r.data),
  },

  executions: {
    list: () => http.get<TestExecution[]>('/test-execution').then((r) => r.data),
    getById: (id: number) => http.get<TestExecution>(`/test-execution/${id}`).then((r) => r.data),
    suites: (executionId: number) =>
      http.get<TestSuite[]>(`/test-execution/${executionId}/test-suites`).then((r) => r.data),
    // status filter mirrors classic UI: ?status=PASSED&status=FAILED...
    testCases: (executionId: number, statuses?: ExecutionStatus[]) =>
      http
        .get<TestCase[]>(`/test-execution/${executionId}/test-cases`, {
          params: statuses?.length ? { status: statuses } : undefined,
          paramsSerializer: { indexes: null },
        })
        .then((r) => r.data),
    // Requires admin auth (see AdminOnlyFilter on the backend). deleteAttachmentsOnly=true
    // removes just the screenshots; false fully archives (soft-deletes) the execution(s).
    archive: (executionIds: number[], deleteAttachmentsOnly: boolean) =>
      http.put<void>(`/test-execution/archive/${deleteAttachmentsOnly}`, executionIds),
  },

  suites: {
    getById: (id: number) => http.get<TestSuite>(`/test-suite/${id}`).then((r) => r.data),
    testCases: (suiteId: number) =>
      http.get<TestCase[]>(`/test-suite/${suiteId}/test-cases`).then((r) => r.data),
  },

  testCases: {
    getById: (id: number) => http.get<TestCase>(`/test-case/${id}`).then((r) => r.data),
    testSteps: (testCaseId: number) =>
      http.get<TestStep[]>(`/test-case/${testCaseId}/test-steps`).then((r) => r.data),
  },
};
