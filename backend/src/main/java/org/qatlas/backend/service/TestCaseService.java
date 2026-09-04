package org.qatlas.backend.service;

import org.qatlas.backend.enums.ExecutionStatus;
import org.qatlas.backend.vo.TestCaseVO;

import java.time.LocalDateTime;
import java.util.List;

public interface TestCaseService {
    TestCaseVO create(TestCaseVO testCaseVO);

    List<TestCaseVO> getAll();

    List<TestCaseVO> getAll(
            Long testSuiteId,
            Long testExecutionId,
            ExecutionStatus[] executionStatuses);

    TestCaseVO getById(Long id);

    TestCaseVO update(TestCaseVO testCaseVO);

    TestCaseVO update(
            Long id,
            ExecutionStatus executionStatus,
            LocalDateTime executionStartTime,
            LocalDateTime executionEndTime);

    String updateComments(final Long id, final String comments);
}
