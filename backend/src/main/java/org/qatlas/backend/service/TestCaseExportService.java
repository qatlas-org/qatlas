package org.qatlas.backend.service;

import org.qatlas.backend.enums.ExecutionStatus;
import org.qatlas.backend.enums.ExportFormat;
import org.qatlas.backend.vo.TestCaseVO;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Exports the test cases of a test execution as CSV or Excel.
 */
public interface TestCaseExportService {

    /**
     * Loads the test cases of an execution, using the same filters as the
     * "Test Cases" table in the UI.
     *
     * @param testExecutionId   execution to export (must exist, otherwise 404)
     * @param executionStatuses optional status filter
     * @param search            optional case-insensitive text matched against test case and suite name
     */
    List<TestCaseVO> findTestCases(Long testExecutionId, ExecutionStatus[] executionStatuses, String search);

    /**
     * Writes the given test cases to {@code outputStream} in the given format.
     * The stream is not closed.
     */
    void write(List<TestCaseVO> testCases, ExportFormat format, OutputStream outputStream) throws IOException;
}
