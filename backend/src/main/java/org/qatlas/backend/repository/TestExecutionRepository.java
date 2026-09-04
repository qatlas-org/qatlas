package org.qatlas.backend.repository;

import org.qatlas.backend.entity.TestExecution;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Modifying(flushAutomatically = true)
    @Query("update TestExecution e set e.archived = true where e.id in (:testExecutionIds)")
    void archive(@Param("testExecutionIds") final List<Long> testExecutionIds);

}
