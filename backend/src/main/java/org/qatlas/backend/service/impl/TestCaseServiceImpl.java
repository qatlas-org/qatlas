package org.qatlas.backend.service.impl;

import org.qatlas.backend.entity.TestCase;
import org.qatlas.backend.entity.TestSuite;
import org.qatlas.backend.enums.ExecutionStatus;
import org.qatlas.backend.exception.TestCaseNotFoundException;
import org.qatlas.backend.exception.TestSuiteNotFoundException;
import org.qatlas.backend.mapper.TestCaseMapper;
import org.qatlas.backend.repository.TestCaseRepository;
import org.qatlas.backend.repository.TestSuiteRepository;
import org.qatlas.backend.service.TestCaseService;
import org.qatlas.backend.vo.TestCaseVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TestCaseServiceImpl implements TestCaseService {

    @PersistenceContext
    private EntityManager entityManager;

    private TestCaseRepository testCaseRepository;

    private TestSuiteRepository testSuiteRepository;

    private TestCaseMapper testCaseMapper;

    @Autowired
    public TestCaseServiceImpl(
            TestCaseRepository testCaseRepository,
            TestSuiteRepository testSuiteRepository,
            TestCaseMapper testCaseMapper) {
        this.testCaseRepository = testCaseRepository;
        this.testSuiteRepository = testSuiteRepository;
        this.testCaseMapper = testCaseMapper;
    }

    @Override
    @Transactional
    public TestCaseVO create(final TestCaseVO testCaseVO) {
        TestSuite testSuite = getTestSuite(testCaseVO.getTestSuiteId());
        return save(testCaseMapper.map(testCaseVO, testSuite));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TestCaseVO> getAll() {
        return testCaseMapper.map(
            testCaseRepository.findAll()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<TestCaseVO> getAll(
            final Long testSuiteId,
            final Long testExecutionId,
            final ExecutionStatus[] executionStatuses) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<TestCase> query = cb.createQuery(TestCase.class);
        Root<TestCase> root = query.from(TestCase.class);

        List<Predicate> conditions = new ArrayList<>();

        if(testSuiteId != null) {
            conditions.add(
                cb.equal(root.get("testSuite").get("id"), testSuiteId)
            );
        }
        if(testExecutionId != null) {
            conditions.add(
                cb.equal(
                    root.get("testSuite").get("testExecution").get("id"),
                        testExecutionId)
            );
        }
        if(executionStatuses != null
            && executionStatuses.length > 0) {
            conditions.add(
                root.get("executionStatus").in(executionStatuses)
            );
        }

        if(CollectionUtils.isNotEmpty(conditions)) {
            query.where(
                cb.and(
                    conditions.toArray(
                        new Predicate[]{}
                        )
                )
            );
        }

        query.orderBy(
            cb.asc(root.get("testSuite").get("id")),
            cb.asc(root.get("id"))
        );

        return testCaseMapper
            .map(
                entityManager
                    .createQuery(query)
                    .getResultList()
            );
    }

    @Override
    @Transactional(readOnly = true)
    public TestCaseVO getById(final Long id) {
        return testCaseMapper.map(get(id));
    }

    @Override
    @Transactional
    public TestCaseVO update(final TestCaseVO testCaseVO) {
        TestCase testCase = get(testCaseVO.getId());
        return save(
            testCaseMapper
                .map(testCaseVO, testCase.getTestSuite())
        );
    }

    @Override
    @Transactional
    public TestCaseVO update(
            final Long id,
            final ExecutionStatus executionStatus,
            final LocalDateTime executionStartTime,
            final LocalDateTime executionEndTime) {
        TestCase testCase = get(id);
        testCase.setExecutionStatus(executionStatus);
        if(executionStartTime != null) {
            testCase.setExecutionStartTime(executionStartTime);
        }
        if(executionEndTime != null) {
            testCase.setExecutionEndTime(executionEndTime);
        }
        return save(testCase);
    }

    @Override
    @Transactional
    public String updateComments(Long id, String comments) {
        TestCase testCase = get(id);
        if(StringUtils.isNotBlank(comments)) {
            testCase.setComments(comments);
        } else {
            testCase.setComments(null);
        }
        return testCaseRepository.save(testCase).getComments();
    }

    private TestCase get(final Long id) {
        return testCaseRepository
            .findById(id)
            .orElseThrow(() ->
                new TestCaseNotFoundException(id)
            );
    }

    private TestCaseVO save(final TestCase testCase) {
        return testCaseMapper.map(
            testCaseRepository.save(testCase)
        );
    }

    private TestSuite getTestSuite(Long testSuiteId) {
        return testSuiteRepository
            .findById(testSuiteId)
            .orElseThrow(() ->
                new TestSuiteNotFoundException(testSuiteId)
            );
    }

}
