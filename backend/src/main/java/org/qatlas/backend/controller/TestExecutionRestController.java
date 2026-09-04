package org.qatlas.backend.controller;

import org.qatlas.backend.enums.ExecutionStatus;
import org.qatlas.backend.service.TestCaseService;
import org.qatlas.backend.service.TestExecutionService;
import org.qatlas.backend.service.TestSuiteService;
import org.qatlas.backend.vo.TestCaseVO;
import org.qatlas.backend.vo.TestExecutionVO;
import org.qatlas.backend.vo.TestSuiteVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.List;

import static org.qatlas.backend.Constants.SLASH;

@RestController
@RequestMapping(SLASH + "test-execution")
@ResponseStatus(HttpStatus.OK)
@Tag(name = "Test Execution", description = "Test Execution REST API")
public class TestExecutionRestController {

    private TestExecutionService testExecutionService;

    private TestSuiteService testSuiteService;

    private TestCaseService testCaseService;

    @Autowired
    public TestExecutionRestController(
            TestExecutionService testExecutionService,
            TestSuiteService testSuiteService,
            TestCaseService testCaseService) {
        this.testExecutionService = testExecutionService;
        this.testSuiteService = testSuiteService;
        this.testCaseService = testCaseService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a test execution", operationId = "createTestExecution")
    public TestExecutionVO create(
//            @Validated(ValidationGroups.Create.class)
            @RequestBody
            final TestExecutionVO testExecutionVO) {
        return testExecutionService.create(testExecutionVO);
    }

    @PutMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update a test execution end time", operationId = "updateTestExecutionEndTime")
    public TestExecutionVO updateEndTime(
//            @Validated(ValidationGroups.Update.class)
            @PathVariable
            @Min(1)
            final Long id,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @RequestBody
            final LocalDateTime executionEndTime) {
        return testExecutionService.update(id, executionEndTime);
    }

    @GetMapping(path = "/{id}")
    @Operation(summary = "Get a test execution by ID", operationId = "getTestExecutionById")
    public TestExecutionVO get(
            @PathVariable
            @Min(1L)
            final Long id) {
        return testExecutionService.getById(id);
    }

    @GetMapping
    @Operation(summary = "Get list of most recent test executions", operationId = "getAllTestExecutions")
    public List<TestExecutionVO> getAll(
            @RequestParam(name = "limit", required = false, defaultValue = "100")
            @Min(1)
            final int limit) {
        return testExecutionService.getAllActive(limit);
    }

    @GetMapping(path = "/{executionId}/test-suites")
    @Operation(summary = "Get list of all test suites of an execution", operationId = "getTestSuites")
    public List<TestSuiteVO> getTestSuites(
            @PathVariable
            @Min(1)
            final Long executionId) {
        return testSuiteService.getByTestExecutionId(executionId);
    }

    @GetMapping(path = "/{testExecutionId}/test-cases")
    @Operation(summary = "Get list of test cases of a test execution filtered by execution status", operationId = "getTestExecutionTestCases")
    public List<TestCaseVO> getTestCasesByStatus(
            @PathVariable
            @Min(1)
            final Long testExecutionId,
            @RequestParam(name = "status", required = false)
            @Schema(enumAsRef = true)
            final ExecutionStatus[] executionStatuses) {
        return testCaseService.getAll(null, testExecutionId, executionStatuses);
    }

    @PutMapping(path = "/archive/{deleteAttachmentsOnly}")
    @Operation(summary = "Archives the test execution(s)", operationId = "archiveTestExecution")
    public void archive(@RequestBody final List<Long> executionIds, @PathVariable final boolean deleteAttachmentsOnly) {
        testExecutionService.archive(executionIds, deleteAttachmentsOnly);
    }

    @GetMapping(path = "/download-attachments", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Operation(summary = "Download Test Step Attachment(s) of test execution(s)", operationId = "downloadAttachments")
    public ResponseEntity<StreamingResponseBody> downloadAttachments(
            @RequestParam("executionId") final List<Long> executionIds) {
        StreamingResponseBody stream = output -> testExecutionService.downloadAttachments(executionIds, output);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=attachments.zip")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(stream);
    }

    @GetMapping(path = "/{id}/export", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Operation(
        summary = "Download a self-contained offline report (HTML + attachments, no DB/backend needed to view)",
        operationId = "exportStaticReport"
    )
    public ResponseEntity<StreamingResponseBody> exportStaticReport(
            @PathVariable
            @Min(1)
            final Long id) {
        StreamingResponseBody stream = output -> testExecutionService.exportStaticReport(id, output);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=TestExecution_" + id + "_report.zip")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(stream);
    }

}
