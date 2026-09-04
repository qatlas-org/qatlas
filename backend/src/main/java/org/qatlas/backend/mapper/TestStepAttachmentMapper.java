package org.qatlas.backend.mapper;

import org.qatlas.backend.entity.TestStep;
import org.qatlas.backend.entity.TestStepAttachment;
import org.qatlas.backend.vo.TestStepAttachmentVO;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
@Component
public abstract class TestStepAttachmentMapper {

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    @AfterMapping
    protected void afterMapping(
            @MappingTarget final TestStepAttachment entity,
            @Context TestStep testStep) {
        entity.setTestStep(testStep);
    }

    public abstract TestStepAttachment map(
            TestStepAttachmentVO vo,
            @Context TestStep testStep);

    @AfterMapping
    protected void afterMapping(
            final TestStepAttachment entity,
            @MappingTarget
            final TestStepAttachmentVO vo) {
        if (entity.getTestStep() != null) {
            vo.setTestStepId(entity.getTestStep().getId());
        }
        //Is DEV environment
        if(StringUtils.isEmpty(activeProfile)) {
            vo.setAttachmentRelativePath("../attachments/" + entity.getAttachmentRelativePath());
        }
    }

    public abstract TestStepAttachmentVO map(TestStepAttachment entity);

}
