package org.qatlas.backend.mapper;

import org.qatlas.backend.entity.TestCase;
import org.qatlas.backend.entity.TestExecution;
import org.qatlas.backend.entity.TestSuite;
import org.qatlas.backend.vo.TestCaseVO;
import org.qatlas.backend.vo.TestSuiteVO;
import org.mapstruct.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring", uses = {TestCaseMapper.class})
@Component
public abstract class TestSuiteMapper {

    @AfterMapping
    protected void afterMapping(
            @MappingTarget final TestSuite entity,
            @Context TestExecution testExecution) {
        entity.setTestExecution(testExecution);
    }

    public abstract TestSuite map(
            TestSuiteVO vo, @Context TestExecution testExecution);

    @AfterMapping
    protected void afterMapping(
            final TestSuite entity,
            @MappingTarget
            final TestSuiteVO vo) {
        TestExecution testExecution = entity.getTestExecution();
        if (testExecution != null) {
            vo.setTestExecutionId(testExecution.getId());
            vo.setTestExecutionName(testExecution.getName());
        }
    }

//    @Mapping(target = "testCases.testSteps", ignore = true)
//    public abstract TestSuiteVO map(TestSuite entity);
    public TestSuiteVO map(TestSuite entity) {
        if ( entity == null ) {
            return null;
        }

        TestSuiteVO testSuiteVO = new TestSuiteVO();

        testSuiteVO.setId( entity.getId() );
        testSuiteVO.setTestSuiteName( entity.getTestSuiteName() );
        testSuiteVO.setExecutionStartTime( entity.getExecutionStartTime() );
        testSuiteVO.setExecutionEndTime( entity.getExecutionEndTime() );
        testSuiteVO.setPlannedTestCaseCount( entity.getPlannedTestCaseCount() );
        testSuiteVO.setTestCases( testCaseListToTestCaseVOList( entity.getTestCases() ) );

        afterMapping( entity, testSuiteVO );

        return testSuiteVO;
    }

    protected List<TestCaseVO> testCaseListToTestCaseVOList(List<TestCase> list) {
        if ( list == null ) {
            return null;
        }

        List<TestCaseVO> list1 = new ArrayList<TestCaseVO>( list.size() );
        for ( TestCase testCase : list ) {
            list1.add( map( testCase ) );
        }

        return list1;
    }

    public TestCaseVO map(TestCase entity) {
        if ( entity == null ) {
            return null;
        }

        TestCaseVO testCaseVO = new TestCaseVO();

        testCaseVO.setId( entity.getId() );
        testCaseVO.setName( entity.getName() );
        testCaseVO.setExecutionStatus( entity.getExecutionStatus() );
        testCaseVO.setExecutionStartTime( entity.getExecutionStartTime() );
        testCaseVO.setExecutionEndTime( entity.getExecutionEndTime() );
//        testCaseVO.setTestSteps( testStepListToTestStepVOList( entity.getTestSteps() ) );

        afterMapping( entity, testCaseVO );

        return testCaseVO;
    }

    private void afterMapping(
            final TestCase entity,
            @MappingTarget final TestCaseVO vo) {
        TestSuite testSuite = entity.getTestSuite();
        if (testSuite != null) {
            vo.setTestSuiteId(testSuite.getId());
            vo.setTestSuiteName(testSuite.getTestSuiteName());
        }
        /*vo.setTestExecution(
            testExecutionMapper.map(entity.getTestSuite().getTestExecution())
        );*/
    }

    public abstract List<TestSuiteVO> map(List<TestSuite> entities);

}
