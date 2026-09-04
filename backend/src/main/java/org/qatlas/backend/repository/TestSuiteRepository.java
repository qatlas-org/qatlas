package org.qatlas.backend.repository;

import org.qatlas.backend.entity.TestSuite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TestSuiteRepository extends JpaRepository<TestSuite, Long> {
    List<TestSuite> findAllByTestExecutionIdOrderByIdAsc(Long executionId);

    /**
     * Sum of planned test case counts per execution, for the given
     * execution IDs. Row shape: [Long testExecutionId, Long plannedSum]
     */
    @Query("SELECT ts.testExecution.id, SUM(ts.plannedTestCaseCount) "
        + "FROM TestSuite ts "
        + "WHERE ts.testExecution.id IN :executionIds "
        + "GROUP BY ts.testExecution.id")
    List<Object[]> sumPlannedTestCaseCountByExecutionIds(
        @Param("executionIds") List<Long> executionIds
    );
}
