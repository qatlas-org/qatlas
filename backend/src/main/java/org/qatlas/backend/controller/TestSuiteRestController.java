package org.qatlas.backend.controller;

import org.qatlas.backend.enums.ExecutionStatus;
import org.qatlas.backend.service.TestCaseService;
import org.qatlas.backend.service.TestSuiteService;
import org.qatlas.backend.vo.TestCaseVO;
import org.qatlas.backend.vo.TestSuiteVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Min;
import java.util.List;

import static org.qatlas.backend.Constants.SLASH;

@RestController
@RequestMapping(SLASH + "test-suite")
@ResponseStatus(HttpStatus.OK)
//@Validated
@Tag(name = "Test Suite", description = "Test Suite REST API")
public class TestSuiteRestController {

    private TestSuiteService testSuiteService;

    private TestCaseService testCaseService;

    @Autowired
    public TestSuiteRestController(
            TestSuiteService testSuiteService,
            TestCaseService testCaseService) {
        this.testSuiteService = testSuiteService;
        this.testCaseService = testCaseService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a test suite", operationId = "createTestSuite")
    public TestSuiteVO create(
//            @Validated(ValidationGroups.Create.class)
            @RequestBody
            final TestSuiteVO testSuiteVO) {
        return testSuiteService.create(testSuiteVO);
    }

    @GetMapping
    @Operation(summary = "Get list of test suites", operationId = "getAllTestSuites")
    public List<TestSuiteVO> getAll() {
        return testSuiteService.getAll();
    }

    @GetMapping(path = "/{id}")
    @Operation(summary = "Get a test suites by ID", operationId = "getTestSuiteById")
    public TestSuiteVO get(
            @PathVariable
            @Min(1)
            final Long id) {
        return testSuiteService.get(id);
    }

    @PutMapping
    @Operation(summary = "Update a test suite", operationId = "updateTestSuite")
    public TestSuiteVO update(
//            @Validated(ValidationGroups.Update.class)
            @RequestBody
            final TestSuiteVO testSuiteVO) {
        return testSuiteService.update(testSuiteVO);
    }

    @GetMapping(
        path = "/{testSuiteId}/test-cases"
    )
    @Operation(summary = "Get list of test cases of a test suite filtered by execution status", operationId = "getTestSuiteTestCases")
    public List<TestCaseVO> getTestCases(
            @PathVariable
            @Min(1)
            final Long testSuiteId,
            @RequestParam(name = "status", required = false)
            @Schema(enumAsRef = true)
            final ExecutionStatus[] executionStatuses) {
        return testCaseService.getAll(testSuiteId, null, executionStatuses);
    }

}
