package org.qatlas.backend.mapper;

import org.qatlas.backend.entity.TestCase;
import org.qatlas.backend.entity.TestSuite;
import org.qatlas.backend.vo.TestCaseVO;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper(componentModel = "spring", uses = {TestStepMapper.class})
@Component
public abstract class TestCaseMapper {

    @Autowired
    private TestExecutionMapper testExecutionMapper;

    /**
     *
     * @param entity
     * @param testSuite
     */
    @AfterMapping
    protected void afterMapping(
            @MappingTarget final TestCase entity,
            @Context TestSuite testSuite) {
        entity.setTestSuite(testSuite);
    }

    /**
     *
     * @param vo
     * @param testSuite
     * @return
     */
    public abstract TestCase map(TestCaseVO vo, @Context TestSuite testSuite);

    /**
     * Mapper utility method to map testSuiteId, testSuiteName & testExecution properties
     * of {@link TestCaseVO} POJO from {@link TestCase} entity
     * @param entity is {@link TestCase} entity object
     * @param vo is {@link TestCaseVO} PoJO object
     */
    @AfterMapping
    protected void afterMapping(
            final TestCase entity,
            @MappingTarget final TestCaseVO vo) {
        TestSuite testSuite = entity.getTestSuite();
        if (testSuite != null) {
            vo.setTestSuiteId(testSuite.getId());
            vo.setTestSuiteName(testSuite.getTestSuiteName());
        }
        vo.setTestExecution(
            testExecutionMapper.map(entity.getTestSuite().getTestExecution())
        );
    }

    /**
     * Mapper utility method to map {@link TestCase} entity to {@link TestCaseVO} POJO
     * @param entity of type {@link TestCase} entity
     * @return {@link TestCaseVO} POJO
     */
    public abstract TestCaseVO map(TestCase entity);

    /**
     * Mapper utility method to map {@link TestCase} entity {@link List} to {@link TestCaseVO} POJO {@link List}
     * @param entities is {@link List} of {@link TestCase} entities
     * @return {@link List} of {@link TestCaseVO} POJO
     */
    public abstract List<TestCaseVO> map(List<TestCase> entities);

}
