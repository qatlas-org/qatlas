package org.qatlas.backend.service.impl;

import org.qatlas.backend.entity.TestCase;
import org.qatlas.backend.entity.TestStep;
import org.qatlas.backend.exception.TestCaseNotFoundException;
import org.qatlas.backend.exception.TestStepNotFoundException;
import org.qatlas.backend.mapper.TestStepMapper;
import org.qatlas.backend.repository.TestCaseRepository;
import org.qatlas.backend.repository.TestStepRepository;
import org.qatlas.backend.service.TestStepService;
import org.qatlas.backend.vo.TestStepVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TestStepServiceImpl implements TestStepService {

    private Logger logger = LoggerFactory.getLogger(TestStepServiceImpl.class);

    private TestStepRepository testStepRepository;

    private TestCaseRepository testCaseRepository;

    private TestStepMapper testStepMapper;

    @Autowired
    public TestStepServiceImpl(
            TestStepRepository testStepRepository,
            TestCaseRepository testCaseRepository,
            TestStepMapper testStepMapper) {
        this.testStepRepository = testStepRepository;
        this.testCaseRepository = testCaseRepository;
        this.testStepMapper = testStepMapper;
    }

    @Override
    @Transactional
    public TestStepVO create(final TestStepVO testStepVO) {
        logger.debug(
            "Creating Test Step {} for Test Case ID: {}",
            testStepVO.getDescription(),
            testStepVO.getTestCaseId()
        );
        TestCase testCase = getTestCase(testStepVO.getTestCaseId());
        TestStep testStep = testStepRepository.save(
            testStepMapper.map(testStepVO, testCase)
        );
        logger.debug("Created Test Step {}", testStep);
        testStepRepository.refresh(testStep); // FIXME why do we need to do this.
        return testStepMapper.map(testStep);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TestStepVO> getAll() {
        return testStepMapper.map(
            testStepRepository.findAll()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public TestStepVO getById(final Long id) {
        return testStepRepository
            .findById(id)
            .map(testStepMapper::map)
            .orElseThrow(() -> new TestStepNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TestStepVO> getByTestCase(Long testCaseId) {
        return testStepRepository
            .getAllByTestCaseId(testCaseId)
            .stream()
            .map(testStepMapper::map)
            .collect(Collectors.toList());
    }

    private TestCase getTestCase(Long testCaseId) {
        return testCaseRepository
            .findById(testCaseId)
            .orElseThrow(() ->
                new TestCaseNotFoundException(testCaseId)
            );
    }

}
