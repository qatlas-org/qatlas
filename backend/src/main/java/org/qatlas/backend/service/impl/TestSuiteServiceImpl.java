package org.qatlas.backend.service.impl;

import org.qatlas.backend.entity.TestExecution;
import org.qatlas.backend.exception.TestExecutionNotFoundException;
import org.qatlas.backend.exception.TestSuiteNotFoundException;
import org.qatlas.backend.mapper.TestSuiteMapper;
import org.qatlas.backend.repository.TestExecutionRepository;
import org.qatlas.backend.repository.TestSuiteRepository;
import org.qatlas.backend.service.TestSuiteService;
import org.qatlas.backend.vo.TestSuiteVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TestSuiteServiceImpl implements TestSuiteService {

    private TestSuiteRepository testSuiteRepository;

    private TestExecutionRepository testExecutionRepository;

    private TestSuiteMapper testSuiteMapper;

    @Autowired
    public TestSuiteServiceImpl(
            TestSuiteRepository testSuiteRepository,
            TestExecutionRepository testExecutionRepository,
            TestSuiteMapper testSuiteMapper) {
        this.testSuiteRepository = testSuiteRepository;
        this.testExecutionRepository = testExecutionRepository;
        this.testSuiteMapper = testSuiteMapper;
    }

    @Override
    @Transactional
    public TestSuiteVO create(final TestSuiteVO testSuiteVO) {
        return save(testSuiteVO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TestSuiteVO> getAll() {
        return testSuiteMapper.map(
            testSuiteRepository.findAll()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<TestSuiteVO> getByTestExecutionId(final Long executionId) {
        return testSuiteMapper.map(
            testSuiteRepository.findAllByTestExecutionIdOrderByIdAsc(executionId)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public TestSuiteVO get(final Long id) {
        return testSuiteRepository
            .findById(id)
            .map(testSuiteMapper::map)
            .orElseThrow(() -> new TestSuiteNotFoundException(id));
    }

    @Override
    @Transactional
    public TestSuiteVO update(final TestSuiteVO testSuiteVO) {
        if (!testSuiteRepository.existsById(testSuiteVO.getId())) {
            throw new TestSuiteNotFoundException(testSuiteVO.getId());
        }
        return save(testSuiteVO);
    }

    private TestSuiteVO save(final TestSuiteVO testSuiteVO) {
        Long testExecutionId = testSuiteVO.getTestExecutionId();
        TestExecution testExecution = testExecutionRepository
            .findById(testExecutionId)
            .orElseThrow(() ->
                new TestExecutionNotFoundException(testExecutionId)
            );
        return testSuiteMapper.map(
            testSuiteRepository.save(
                testSuiteMapper.map(testSuiteVO, testExecution)
            )
        );
    }

}
