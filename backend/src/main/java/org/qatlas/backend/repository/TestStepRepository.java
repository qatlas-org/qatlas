package org.qatlas.backend.repository;

import org.qatlas.backend.entity.TestStep;

import java.util.List;

public interface TestStepRepository extends CustomRepository<TestStep, Long> {
    List<TestStep> getAllByTestCaseId(Long testCaseId);
}
