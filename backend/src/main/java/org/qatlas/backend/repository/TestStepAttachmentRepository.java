package org.qatlas.backend.repository;

import org.qatlas.backend.entity.TestStepAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TestStepAttachmentRepository
        extends JpaRepository<TestStepAttachment, Long> {

    @Modifying(flushAutomatically = true)
    @Query("delete from TestStepAttachment a where a.testStep.testCase.testSuite.testExecution.id in (:testExecutionIds)")
    void delete(@Param("testExecutionIds") final List<Long> testExecutionIds);

    void deleteByTestStepTestCaseTestSuiteTestExecutionIdIn(final List<Long> executionIds);

}
