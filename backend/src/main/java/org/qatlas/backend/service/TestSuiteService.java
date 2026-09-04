package org.qatlas.backend.service;

import org.qatlas.backend.vo.TestSuiteVO;

import java.util.List;

public interface TestSuiteService {
    TestSuiteVO create(TestSuiteVO testSuiteVO);

    List<TestSuiteVO> getAll();

    List<TestSuiteVO> getByTestExecutionId(Long executionId);

    TestSuiteVO get(Long id);

    TestSuiteVO update(TestSuiteVO testSuiteVO);
}
