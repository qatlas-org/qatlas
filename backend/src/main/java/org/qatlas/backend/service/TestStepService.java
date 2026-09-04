package org.qatlas.backend.service;

import org.qatlas.backend.vo.TestStepVO;

import java.util.List;

public interface TestStepService {
    TestStepVO create(TestStepVO testStepVO);

    List<TestStepVO> getAll();

    TestStepVO getById(Long id);

    List<TestStepVO> getByTestCase(Long testCaseId);
}
