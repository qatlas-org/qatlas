package org.qatlas.backend.repository;

import org.qatlas.backend.entity.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
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


    /**
     * Dashboard totals grouped by execution status for all test cases whose
     * parent execution started within the selected date range.
     *
     * Row shape: [ExecutionStatus status, Long count]
     */
    @Query("""
    SELECT tc.executionStatus, COUNT(tc)
    FROM TestCase tc
    WHERE tc.testSuite.testExecution.archived = false
      AND tc.testSuite.testExecution.startTime >= :from
    GROUP BY tc.executionStatus
    """)
    List<Object[]> countDashboardByStatus(
            @Param("from") LocalDateTime from
    );


    /**
     * Dashboard daily trend grouped by execution start date and test case status.
     *
     * Row shape: [LocalDate executionDate, ExecutionStatus status, Long count]
     */
    @Query("""
    SELECT FUNCTION('DATE', tc.testSuite.testExecution.startTime),
           tc.executionStatus,
           COUNT(tc)
    FROM TestCase tc
    WHERE tc.testSuite.testExecution.archived = false
      AND tc.testSuite.testExecution.startTime >= :from
    GROUP BY FUNCTION('DATE', tc.testSuite.testExecution.startTime),
             tc.executionStatus
    ORDER BY FUNCTION('DATE', tc.testSuite.testExecution.startTime)
    """)
    List<Object[]> countDashboardDailyByStatus(
            @Param("from") LocalDateTime from
    );


    /**
     * Dashboard execution totals grouped by application and derived execution status.
     *
     * An execution is RUNNING only when it has no end time and started within the
     * running safeguard window. Older unfinished historical executions are classified
     * from their test-case results instead of remaining RUNNING forever.
     *
     * Row shape: [applicationId, status, count]
     */
    @Query(value = """
        SELECT x.application_id,
               x.execution_status,
               COUNT(*) AS execution_count
        FROM (
            SELECT te.ID,
                   te.APPLICATION_ID AS application_id,
                   CASE
                       WHEN te.END_TIME IS NULL
                            AND te.START_TIME >= :runningSince
                           THEN 'RUNNING'
                       WHEN SUM(CASE WHEN tc.EXECUTION_STATUS = 'FAILED' THEN 1 ELSE 0 END) > 0
                           THEN 'FAILED'
                       WHEN SUM(CASE WHEN tc.EXECUTION_STATUS = 'WARNING' THEN 1 ELSE 0 END) > 0
                           THEN 'WARNING'
                       ELSE 'PASSED'
                   END AS execution_status
            FROM reports_db.test_execution te
            LEFT JOIN reports_db.test_suite ts
                   ON ts.TEST_EXECUTION_ID = te.ID
            LEFT JOIN reports_db.test_case tc
                   ON tc.TEST_SUITE_ID = ts.ID
            WHERE te.IS_ARCHIVED = 0
              AND te.START_TIME >= :from
            GROUP BY te.ID,
                     te.APPLICATION_ID,
                     te.END_TIME,
                     te.START_TIME
        ) x
        GROUP BY x.application_id,
                 x.execution_status
        """, nativeQuery = true)
    List<Object[]> countDashboardExecutionsByApplicationAndStatus(
            @Param("from") LocalDateTime from,
            @Param("runningSince") LocalDateTime runningSince
    );
    /**
     * Dashboard execution totals grouped by application and derived execution
     * status for one selected executor.
     *
     * The executor rule is the same as the dashboard machine selector:
     * prefer EXECUTED_BY, otherwise fall back to SYSTEM_NAME.
     *
     * Row shape: [applicationId, status, count]
     */
    @Query(value = """
    SELECT x.application_id,
           x.execution_status,
           COUNT(*) AS execution_count
    FROM (
        SELECT te.ID,
               te.APPLICATION_ID AS application_id,
               CASE
                   WHEN te.END_TIME IS NULL
                        AND te.START_TIME >= :runningSince
                       THEN 'RUNNING'
                   WHEN SUM(CASE WHEN tc.EXECUTION_STATUS = 'FAILED' THEN 1 ELSE 0 END) > 0
                       THEN 'FAILED'
                   WHEN SUM(CASE WHEN tc.EXECUTION_STATUS = 'WARNING' THEN 1 ELSE 0 END) > 0
                       THEN 'WARNING'
                   ELSE 'PASSED'
               END AS execution_status
        FROM reports_db.test_execution te
        LEFT JOIN reports_db.test_suite ts
               ON ts.TEST_EXECUTION_ID = te.ID
        LEFT JOIN reports_db.test_case tc
               ON tc.TEST_SUITE_ID = ts.ID
        WHERE te.IS_ARCHIVED = 0
          AND te.START_TIME >= :from
          AND COALESCE(te.EXECUTED_BY, te.SYSTEM_NAME) = :executor
        GROUP BY te.ID,
                 te.APPLICATION_ID,
                 te.END_TIME,
                 te.START_TIME
    ) x
    GROUP BY x.application_id,
             x.execution_status
    """, nativeQuery = true)
    List<Object[]> countDashboardExecutionsByApplicationAndStatusAndExecutor(
            @Param("from") LocalDateTime from,
            @Param("runningSince") LocalDateTime runningSince,
            @Param("executor") String executor
    );

}
