import type { ExecutionStatus, TestCase, TestStep } from '../types/domain';

export interface StatusCounts {
  passed: number;
  failed: number;
  warning: number;
  progress: number;
  skipped: number;
}

export function emptyCounts(): StatusCounts {
  return { passed: 0, failed: 0, warning: 0, progress: 0, skipped: 0 };
}

export function countByStatus<T extends { executionStatus: ExecutionStatus }>(items: T[]): StatusCounts {
  const counts = emptyCounts();
  for (const item of items) {
    switch (item.executionStatus) {
      case 'PASSED': counts.passed += 1; break;
      case 'FAILED': counts.failed += 1; break;
      case 'WARNING': counts.warning += 1; break;
      case 'PROGRESS': counts.progress += 1; break;
      case 'SKIPPED': counts.skipped += 1; break;
    }
  }
  return counts;
}

export function firstIndexWithStatus(steps: TestStep[], status: ExecutionStatus): number {
  return steps.findIndex((s) => s.executionStatus === status);
}

/** The backend's TestCase entity has no "reference" field — this derives a
 * stable, readable stand-in from the numeric id so the UI has something to
 * show where the design calls for a reference code. Flagged in the README
 * as a placeholder pending a real backend field if one gets added later. */
export function deriveReference(testCase: Pick<TestCase, 'id'>): string {
  return `TC-${testCase.id}`;
}
