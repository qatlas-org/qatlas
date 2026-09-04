// Types mirror the QAtlas backend REST DTOs (org.qatlas.backend.*)
// Kept hand-written (not codegen) so we can trim to what the UI needs.

export type ExecutionStatus = 'PROGRESS' | 'PASSED' | 'FAILED' | 'WARNING' | 'SKIPPED';

export type AttachmentType = 'SNAPSHOT' | 'OTHER';

export interface Application {
  id: number;
  name: string;
  description?: string;
}

export interface Environment {
  id: number;
  name: string;
  description?: string;
}

export interface TestExecution {
  id: number;
  name: string;
  applicationId: number;
  applicationName?: string;
  applicationVersion?: string;
  environmentId: number;
  environmentName?: string;
  browser: string;
  operatingSystem?: string;
  systemIp: string;
  systemName: string;
  executedBy?: string;
  startTime: string;
  endTime?: string;
  targetedTestCaseCount?: number;
  executedTestCaseCount?: number;
  passedTestCaseCount?: number;
  failedTestCaseCount?: number;
  skippedTestCaseCount?: number;
  inProgressTestCaseCount?: number;
}

export interface TestSuite {
  id: number;
  testExecutionId: number;
  testExecutionName?: string;
  testSuiteName: string;
  executionStartTime: string;
  executionEndTime?: string;
  plannedTestCaseCount: number;
  executedTestCaseCount?: number;
  passedTestCaseCount?: number;
  failedTestCaseCount?: number;
  skippedTestCaseCount?: number;
  inProgressTestCaseCount?: number;
  testCasesCountWithWarnings?: number;
}

export interface TestCase {
  id: number;
  testSuiteId: number;
  testSuiteName?: string;
  name: string;
  executionStatus: ExecutionStatus;
  executionStartTime: string;
  executionEndTime?: string;
  testSteps?: TestStep[];
  executionTime?: number;
  totalTestStepCount?: number;
  executedTestStepCount?: number;
  passedTestStepCount?: number;
  failedTestStepCount?: number;
  testStepCountWithWarnings?: number;
}

export interface TestStepAttachment {
  id?: number;
  testStepId?: number;
  attachmentType: AttachmentType;
  fileName: string;
  attachmentRelativePath?: string;
}

export interface TestStep {
  id: number;
  testCaseId: number;
  testCaseName?: string;
  description: string;
  objectName?: string;
  operation?: string;
  result?: string;
  executionTime?: number;
  executionStatus: ExecutionStatus;
  attachments?: TestStepAttachment[];
}
