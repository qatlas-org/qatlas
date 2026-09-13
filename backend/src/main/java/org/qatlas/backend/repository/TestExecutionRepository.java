package org.qatlas.backend.repository;

import org.qatlas.backend.entity.TestExecution;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TestExecutionRepository extends JpaRepository<TestExecution, Long> {

    List<TestExecution> findByArchivedFalseOrderByIdDesc();

    List<TestExecution> findByOrderByIdDesc();

    /**
     * Capped/limited history listing, most recent first. Using a Pageable
     * here (always requested at page 0) bounds the result set to
     * pageable.getPageSize() rows without needing a separate COUNT query,
     * keeping response time flat regardless of total historical volume.
     */
    List<TestExecution> findByOrderByIdDesc(Pageable pageable);

    List<TestExecution> findByArchivedFalseAndApplicationIdOrderByIdDesc(Long applicationId);

    List<TestExecution> findByApplicationIdOrderByIdDesc(Long applicationId);


    @Query("""
        SELECT COUNT(e)
        FROM TestExecution e
        WHERE e.archived = false
          AND e.startTime >= :from
        """)
    long countDashboardExecutions(@Param("from") LocalDateTime from);


    @Query("""
        SELECT COUNT(DISTINCT e.application.id)
        FROM TestExecution e
        WHERE e.archived = false
          AND e.startTime >= :from
        """)
    long countDashboardActiveProjects(@Param("from") LocalDateTime from);

    @Query("""
    SELECT COUNT(e)
    FROM TestExecution e
    WHERE e.archived = false
      AND e.endTime IS NULL
      AND e.startTime >= :runningSince
    """)
    long countCurrentlyRunning(
            @Param("runningSince") LocalDateTime runningSince
    );

    /**
     * Dashboard execution count grouped by execution start date.
     *
     * Row shape: [LocalDate executionDate, Long count]
     */
    @Query("""
    SELECT FUNCTION('DATE', e.startTime), COUNT(e)
    FROM TestExecution e
    WHERE e.archived = false
      AND e.startTime >= :from
    GROUP BY FUNCTION('DATE', e.startTime)
    ORDER BY FUNCTION('DATE', e.startTime)
    """)
    List<Object[]> countDashboardExecutionsByDay(
            @Param("from") LocalDateTime from
    );


    @Query("""
        SELECT COALESCE(e.executedBy, e.systemName), COUNT(e)
        FROM TestExecution e
        WHERE e.archived = false
          AND e.startTime >= :from
        GROUP BY COALESCE(e.executedBy, e.systemName)
        ORDER BY COUNT(e) DESC
        """)
    List<Object[]> countDashboardExecutionsByMachine(
            @Param("from") LocalDateTime from
    );


    /**
     * Dashboard execution count grouped by application.
     *
     * Row shape: [applicationId, count]
     */
    @Query("""
        SELECT e.application.id, COUNT(e)
        FROM TestExecution e
        WHERE e.archived = false
          AND e.startTime >= :from
        GROUP BY e.application.id
        """)
    List<Object[]> countDashboardExecutionsByApplication(
            @Param("from") LocalDateTime from
    );
    /**
     * Dashboard execution counts grouped by application and executor/machine.
     *
     * COALESCE follows the same rule as the dashboard machine chart:
     * prefer executedBy, otherwise use systemName.
     *
     * Row shape: [applicationId, executorOrMachine, executionCount]
     */
    @Query("""
    SELECT e.application.id,
           COALESCE(e.executedBy, e.systemName),
           COUNT(e)
    FROM TestExecution e
    WHERE e.archived = false
      AND e.startTime >= :from
    GROUP BY e.application.id,
             COALESCE(e.executedBy, e.systemName)
    ORDER BY e.application.id,
             COUNT(e) DESC
    """)
    List<Object[]> countDashboardExecutionsByApplicationAndMachine(
            @Param("from") LocalDateTime from
    );

    /**
     * Latest execution for each application in the selected range.
     *
     * Uses a correlated NOT EXISTS query so the dashboard gets one latest
     * execution per application in a single query, avoiding N+1 lookups.
     * ID is used as a deterministic tie-breaker when start times are equal.
     */
    @Query("""
        SELECT e
        FROM TestExecution e
        WHERE e.archived = false
          AND e.startTime >= :from
          AND NOT EXISTS (
              SELECT newer.id
              FROM TestExecution newer
              WHERE newer.archived = false
                AND newer.application.id = e.application.id
                AND newer.startTime >= :from
                AND (
                    newer.startTime > e.startTime
                    OR (
                        newer.startTime = e.startTime
                        AND newer.id > e.id
                    )
                )
          )
        """)
    List<TestExecution> findDashboardLatestExecutionsByApplication(
            @Param("from") LocalDateTime from
    );



    /**
     * Earliest non-archived execution start time.
     * Used to resolve the effective start date for the ALL/Lifetime dashboard.
     */
    @Query("""
        SELECT MIN(e.startTime)
        FROM TestExecution e
        WHERE e.archived = false
        """)
    LocalDateTime findDashboardEarliestExecutionTime();

    @Modifying(flushAutomatically = true)
    @Query("update TestExecution e set e.archived = true where e.id in (:testExecutionIds)")
    void archive(@Param("testExecutionIds") final List<Long> testExecutionIds);

    /**
     * Dashboard execution count grouped by application for one selected executor.
     *
     * The executor rule is identical to the dashboard machine selector:
     * prefer executedBy, otherwise fall back to systemName.
     *
     * Row shape: [applicationId, count]
     */
    @Query("""
    SELECT e.application.id,
           COUNT(e)
    FROM TestExecution e
    WHERE e.archived = false
      AND e.startTime >= :from
      AND COALESCE(e.executedBy, e.systemName) = :executor
    GROUP BY e.application.id
    """)
    List<Object[]> countDashboardExecutionsByApplicationAndExecutor(
            @Param("from") LocalDateTime from,
            @Param("executor") String executor
    );

    /**
     * Latest execution for each application for one selected executor
     * within the selected dashboard range.
     *
     * ID is used as the deterministic tie-breaker when two executions
     * have the same start time.
     */
    @Query("""
    SELECT e
    FROM TestExecution e
    WHERE e.archived = false
      AND e.startTime >= :from
      AND COALESCE(e.executedBy, e.systemName) = :executor
      AND NOT EXISTS (
          SELECT newer.id
          FROM TestExecution newer
          WHERE newer.archived = false
            AND newer.application.id = e.application.id
            AND newer.startTime >= :from
            AND COALESCE(newer.executedBy, newer.systemName) = :executor
            AND (
                newer.startTime > e.startTime
                OR (
                    newer.startTime = e.startTime
                    AND newer.id > e.id
                )
            )
      )
    """)
    List<TestExecution> findDashboardLatestExecutionsByApplicationAndExecutor(
            @Param("from") LocalDateTime from,
            @Param("executor") String executor
    );

    /**
     * Earliest non-archived execution for one project.
     * Used when Project Details is requested with ALL / lifetime.
     */
    @Query("""
    SELECT MIN(e.startTime)
    FROM TestExecution e
    WHERE e.archived = false
      AND e.application.id = :applicationId
    """)
    LocalDateTime findProjectEarliestExecutionTime(
            @Param("applicationId") Long applicationId
    );


    /**
     * Project execution totals grouped by derived execution status.
     *
     * RUNNING:
     *   end time is null AND execution started within the running safeguard.
     *
     * Otherwise:
     *   FAILED  -> at least one failed testcase
     *   WARNING -> no failures, but at least one warning testcase
     *   PASSED  -> everything else
     *
     * Row shape:
     * [status, executionCount]
     */
    @Query(value = """
    SELECT x.execution_status,
           COUNT(*) AS execution_count
    FROM (
        SELECT te.ID,
               CASE
                   WHEN te.END_TIME IS NULL
                        AND te.START_TIME >= :runningSince
                       THEN 'RUNNING'

                   WHEN SUM(
                       CASE
                           WHEN tc.EXECUTION_STATUS = 'FAILED'
                           THEN 1
                           ELSE 0
                       END
                   ) > 0
                       THEN 'FAILED'

                   WHEN SUM(
                       CASE
                           WHEN tc.EXECUTION_STATUS = 'WARNING'
                           THEN 1
                           ELSE 0
                       END
                   ) > 0
                       THEN 'WARNING'

                   ELSE 'PASSED'
               END AS execution_status

        FROM reports_db.test_execution te

        LEFT JOIN reports_db.test_suite ts
               ON ts.TEST_EXECUTION_ID = te.ID

        LEFT JOIN reports_db.test_case tc
               ON tc.TEST_SUITE_ID = ts.ID

        WHERE te.IS_ARCHIVED = 0
          AND te.APPLICATION_ID = :applicationId
          AND te.START_TIME >= :from

        GROUP BY te.ID,
                 te.END_TIME,
                 te.START_TIME
    ) x

    GROUP BY x.execution_status
    """,
            nativeQuery = true)
    List<Object[]> countProjectExecutionsByStatus(
            @Param("applicationId") Long applicationId,
            @Param("from") LocalDateTime from,
            @Param("runningSince") LocalDateTime runningSince
    );


    /**
     * Project execution status trend grouped by execution start date.
     *
     * Row shape:
     * [executionDate, status, executionCount]
     */
    @Query(value = """
    SELECT x.execution_date,
           x.execution_status,
           COUNT(*) AS execution_count

    FROM (
        SELECT te.ID,
               DATE(te.START_TIME) AS execution_date,

               CASE
                   WHEN te.END_TIME IS NULL
                        AND te.START_TIME >= :runningSince
                       THEN 'RUNNING'

                   WHEN SUM(
                       CASE
                           WHEN tc.EXECUTION_STATUS = 'FAILED'
                           THEN 1
                           ELSE 0
                       END
                   ) > 0
                       THEN 'FAILED'

                   WHEN SUM(
                       CASE
                           WHEN tc.EXECUTION_STATUS = 'WARNING'
                           THEN 1
                           ELSE 0
                       END
                   ) > 0
                       THEN 'WARNING'

                   ELSE 'PASSED'
               END AS execution_status

        FROM reports_db.test_execution te

        LEFT JOIN reports_db.test_suite ts
               ON ts.TEST_EXECUTION_ID = te.ID

        LEFT JOIN reports_db.test_case tc
               ON tc.TEST_SUITE_ID = ts.ID

        WHERE te.IS_ARCHIVED = 0
          AND te.APPLICATION_ID = :applicationId
          AND te.START_TIME >= :from

        GROUP BY te.ID,
                 te.START_TIME,
                 te.END_TIME
    ) x

    GROUP BY x.execution_date,
             x.execution_status

    ORDER BY x.execution_date
    """,
            nativeQuery = true)
    List<Object[]> countProjectExecutionsByDayAndStatus(
            @Param("applicationId") Long applicationId,
            @Param("from") LocalDateTime from,
            @Param("runningSince") LocalDateTime runningSince
    );


    /**
     * Individual project executions for the interactive execution chart.
     *
     * Each row represents exactly one execution in the selected project/range.
     * The frontend uses executionId to navigate directly to /executions/{id}.
     *
     * Row shape:
     * [executionId, executionName, executionDate, executionStatus]
     */
    @Query(value = """
    SELECT x.execution_id,
           x.execution_name,
           x.execution_date,
           x.execution_status
    FROM (
        SELECT te.ID AS execution_id,
               te.NAME AS execution_name,
               DATE(te.START_TIME) AS execution_date,
               te.START_TIME AS execution_start_time,

               CASE
                   WHEN te.END_TIME IS NULL
                        AND te.START_TIME >= :runningSince
                       THEN 'RUNNING'

                   WHEN SUM(
                       CASE
                           WHEN tc.EXECUTION_STATUS = 'FAILED'
                           THEN 1
                           ELSE 0
                       END
                   ) > 0
                       THEN 'FAILED'

                   WHEN SUM(
                       CASE
                           WHEN tc.EXECUTION_STATUS = 'WARNING'
                           THEN 1
                           ELSE 0
                       END
                   ) > 0
                       THEN 'WARNING'

                   ELSE 'PASSED'
               END AS execution_status

        FROM reports_db.test_execution te

        LEFT JOIN reports_db.test_suite ts
               ON ts.TEST_EXECUTION_ID = te.ID

        LEFT JOIN reports_db.test_case tc
               ON tc.TEST_SUITE_ID = ts.ID

        WHERE te.IS_ARCHIVED = 0
          AND te.APPLICATION_ID = :applicationId
          AND te.START_TIME >= :from

        GROUP BY te.ID,
                 te.NAME,
                 te.START_TIME,
                 te.END_TIME
    ) x

    ORDER BY x.execution_start_time,
             x.execution_id
    """,
            nativeQuery = true)
    List<Object[]> findProjectExecutionBlocks(
            @Param("applicationId") Long applicationId,
            @Param("from") LocalDateTime from,
            @Param("runningSince") LocalDateTime runningSince
    );

}