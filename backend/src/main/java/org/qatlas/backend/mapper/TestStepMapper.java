package org.qatlas.backend.mapper;

import org.qatlas.backend.entity.TestCase;
import org.qatlas.backend.entity.TestStep;
import org.qatlas.backend.vo.TestStepVO;
import org.mapstruct.*;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Mapper(componentModel = "spring", uses = {TestStepAttachmentMapper.class})
@Component
public abstract class TestStepMapper {

    @AfterMapping
    protected void afterMapping(
            final TestStep entity,
            @MappingTarget
            final TestStepVO vo) {
        if (entity.getTestCase() != null) {
            vo.setTestCaseId(entity.getTestCase().getId());
            vo.setTestCaseName(entity.getTestCase().getName());
        }
    }

    public abstract TestStepVO map(TestStep entity);

    public abstract List<TestStepVO> map(Collection<TestStep> entities);

    @AfterMapping
    protected void afterMapping(
            @MappingTarget final TestStep entity,
            @Context TestCase testCase) {
        entity.setTestCase(testCase);
    }

    @Mapping(ignore = true, target = "attachments")
    public abstract TestStep map(TestStepVO vo, @Context TestCase testCase);

}
