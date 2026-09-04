package org.qatlas.backend.controller;

import org.qatlas.backend.enums.ExecutionStatus;
import org.qatlas.backend.service.TestCaseService;
import org.qatlas.backend.service.TestStepService;
import org.qatlas.backend.vo.TestCaseVO;
import org.qatlas.backend.vo.TestStepVO;
import org.qatlas.backend.vo.TestSuiteVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Past;
import java.time.LocalDateTime;
import java.util.List;

import static org.qatlas.backend.Constants.SLASH;

@RestController
@RequestMapping(SLASH + "test-case")
@ResponseStatus(HttpStatus.OK)
//@Validated
@Tag(name = "Test Case", description = "Test Case REST API")
public class TestCaseRestController {

    private TestCaseService testCaseService;

    private TestStepService testStepService;

    @Autowired
    public TestCaseRestController(
            TestCaseService testCaseService,
            TestStepService testStepService) {
        this.testCaseService = testCaseService;
        this.testStepService = testStepService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a test case", operationId = "createTestCase")
    public TestCaseVO create(
//            @Validated(ValidationGroups.Create.class)
            @RequestBody
            final TestCaseVO testCaseVO) {
        return testCaseService.create(testCaseVO);
    }

    @GetMapping
    @Operation(summary = "Get list of all Test Cases", operationId = "getTestCases")
    public List<TestCaseVO> getAll() {
        return testCaseService.getAll();
    }

    @GetMapping(path = "/{id}")
    @Operation(summary = "Get a test cases by it's ID", operationId = "getTestCaseById")
    public TestCaseVO getById(
            @PathVariable
            @Min(1)
            final Long id) {
        return testCaseService.getById(id);
    }

    @PutMapping
    @Operation(summary = "Update a test case", operationId = "updateTestCase")
    public TestCaseVO update(
//            @Validated(ValidationGroups.Update.class)
            @RequestBody
            final TestCaseVO testCaseVO) {
        return testCaseService.update(testCaseVO);
    }

    @PutMapping(path = "/{id}/status/{executionStatus}")
    @Operation(summary = "Update execution status of a test case", operationId = "updateTestCaseStatus")
    public TestCaseVO updateStatus(
            @PathVariable
            @Min(1)
            final Long id,
            @PathVariable
            @Schema(enumAsRef = true)
            final ExecutionStatus executionStatus) {
        return testCaseService.update(id, executionStatus, null, null);
    }

    @PutMapping(
        path = "/{id}/status/{executionStatus}/startTime/{executionStartTime}"
    )
    @Operation(summary = "Update execution status and start time of a test case", operationId = "updateTestCaseStatusAndStartTime")
    public TestCaseVO updateStatusAndExecutionStartTime(
            @PathVariable
            @Min(1)
            final Long id,
            @PathVariable
            @Schema(enumAsRef = true)
            final ExecutionStatus executionStatus,
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @Past
            final LocalDateTime executionStartTime) {
        return testCaseService.update(id, executionStatus, executionStartTime, null);
    }

    @PutMapping(
            path = "/{id}/status/{executionStatus}/endTime/{executionEndTime}"
    )
    @Operation(summary = "Update execution status and end time of a test case", operationId = "updateTestCaseStatusAndExecutionEndTime")
    public TestCaseVO updateStatusAndExecutionEndTime(
            @PathVariable
            @Min(1)
            final Long id,
            @PathVariable
            @Schema(enumAsRef = true)
            final ExecutionStatus executionStatus,
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @Past
            final LocalDateTime executionEndTime) {
        return testCaseService.update(id, executionStatus, null, executionEndTime);
    }

    @GetMapping(path = "/{id}/comments")
    @Operation(summary = "Get comments of a test case", operationId = "getComments")
    public String getComments(
            @PathVariable
            @Min(1)
            final Long id) {
        return testCaseService.getById(id).getComments();
    }

    @PutMapping(path = "/{id}/comments")
    @Operation(summary = "Update comments of a test case", operationId = "updateComments")
    public String updateComments(
            @PathVariable
            @Min(1)
            final Long id,
            @RequestBody
            final String comments) {
        return testCaseService.updateComments(id, comments);
    }

    @GetMapping(path = "/{testCaseId}/test-steps")
    @Operation(summary = "Get the list of Test Steps of a Test Case", operationId = "getTestCaseTestSteps")
    public List<TestStepVO> getTestSteps(
            @PathVariable
            @Min(1)
            final Long testCaseId) {
        return testStepService.getByTestCase(testCaseId);
    }

}
