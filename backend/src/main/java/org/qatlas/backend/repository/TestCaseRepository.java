package org.qatlas.backend.repository;

import org.qatlas.backend.entity.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TestCaseRepository extends JpaRepository<TestCase, Long> {

    /**
     * Returns one row per (testExecutionId, executionStatus) combination with
     * the count of test cases in that status, for the given execution IDs.
     * Replaces loading the full TestSuite/TestCase entity graph just to
     * count statuses in Java.
     * Row shape: [Long testExecutionId, ExecutionStatus status, Long count]
     */
    @Query("SELECT tc.testSuite.testExecution.id, tc.executionStatus, COUNT(tc) "
        + "FROM TestCase tc "
        + "WHERE tc.testSuite.testExecution.id IN :executionIds "
        + "GROUP BY tc.testSuite.testExecution.id, tc.executionStatus")
    List<Object[]> countByExecutionIdsGroupedByStatus(
        @Param("executionIds") List<Long> executionIds
    );

    /**
     * Returns the latest execution end time among "executed" (non
     * PLANNED/PROGRESS) test cases, per execution ID. Used as a fallback
     * when TestExecution.endTime hasn't been explicitly set yet.
     * Row shape: [Long testExecutionId, LocalDateTime maxExecutionEndTime]
     */
    @Query("SELECT tc.testSuite.testExecution.id, MAX(tc.executionEndTime) "
        + "FROM TestCase tc "
        + "WHERE tc.testSuite.testExecution.id IN :executionIds "
        + "AND tc.executionStatus <> org.qatlas.backend.enums.ExecutionStatus.PLANNED "
        + "AND tc.executionStatus <> org.qatlas.backend.enums.ExecutionStatus.PROGRESS "
        + "GROUP BY tc.testSuite.testExecution.id")
    List<Object[]> maxExecutionEndTimeByExecutionIds(
        @Param("executionIds") List<Long> executionIds
    );
}
