package org.qatlas.backend.service;

import org.qatlas.backend.vo.TestExecutionVO;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.List;

public interface TestExecutionService {

    TestExecutionVO create(TestExecutionVO testExecutionVO);

    TestExecutionVO getById(Long id);

    /**
     * Returns the most recent {@code limit} active test executions,
     * newest first, with per-execution test case status counts populated.
     * Bounded by limit so response time stays flat regardless of how much
     * historical execution data has accumulated.
     */
    List<TestExecutionVO> getAllActive(int limit);

    List<TestExecutionVO> getAllByApplicationId(Long applicationId);

    TestExecutionVO update(Long id, LocalDateTime executionEndTime);

    void archive(final List<Long> executionIds, final boolean deleteAttachmentsOnly);

    void downloadAttachments(final List<Long> executionIds, OutputStream output) throws IOException;

    /**
     * Streams a self-contained, offline-viewable report for a single test
     * execution: one HTML file (inline CSS/JS, no backend or network
     * calls needed to view it) plus its attachment images, zipped
     * together. Meant to be opened directly from disk - no server, DB, or
     * running backend required - so it can be shared with clients or
     * used to reproduce a UI issue without the full dev environment.
     */
    void exportStaticReport(final Long executionId, OutputStream output) throws IOException;
}
