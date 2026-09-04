package org.qatlas.backend.controller;

import org.qatlas.backend.service.TestStepAttachmentService;
import org.qatlas.backend.service.TestStepService;
import org.qatlas.backend.vo.TestStepAttachmentVO;
import org.qatlas.backend.vo.TestStepVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Min;
import java.util.List;

import static org.qatlas.backend.Constants.SLASH;

@RestController
@RequestMapping(SLASH + "test-step")
@ResponseStatus(HttpStatus.OK)
//@Validated
@Tag(name = "Test Step", description = "Test Step REST API")
public class TestStepRestController {

    private Logger logger = LoggerFactory.getLogger(TestStepRestController.class);

    private TestStepService testStepService;

    private TestStepAttachmentService testStepAttachmentService;

    @Autowired
    public TestStepRestController(
            TestStepService testStepService,
            TestStepAttachmentService testStepAttachmentService) {
        this.testStepService = testStepService;
        this.testStepAttachmentService = testStepAttachmentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a test step", operationId = "createTestStep")
//    @Validated
    public TestStepVO create(
//            @Validated(ValidationGroups.Create.class)
            @RequestBody
            final TestStepVO testStepVO) {
        TestStepVO testStepSaved = testStepService.create(testStepVO);
        createTestStepAttachments(testStepSaved.getId(), testStepVO.getAttachments());
        return testStepSaved;
    }

    @GetMapping
    @Operation(summary = "Get list of all test steps", operationId = "getAllTestSteps")
    public List<TestStepVO> getAll() {
        return testStepService.getAll();
    }

    @GetMapping(path = "/{id}")
    @Operation(summary = "Get test step by ID", operationId = "getTestStepById")
    public TestStepVO get(
            @PathVariable
            @Min(1)
            final Long id) {
        return testStepService.getById(id);
    }

    private void createTestStepAttachments(
            final Long testStepId,
            final List<TestStepAttachmentVO> attachments) {
        if(CollectionUtils.isNotEmpty(attachments)) {
            logger.debug("Creating Attachments for TestStep with ID: {}", testStepId);
            attachments
                .forEach(attachmentVO -> {
                    attachmentVO.setTestStepId(testStepId);
                    testStepAttachmentService.create(attachmentVO);
                });
        }
    }

}
