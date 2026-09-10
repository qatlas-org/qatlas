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

}