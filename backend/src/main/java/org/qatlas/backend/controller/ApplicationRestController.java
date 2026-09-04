package org.qatlas.backend.controller;

import org.qatlas.backend.service.ApplicationService;
import org.qatlas.backend.service.TestExecutionService;
import org.qatlas.backend.vo.ApplicationVO;
import org.qatlas.backend.vo.TestExecutionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Min;
import java.util.List;

import static org.qatlas.backend.Constants.SLASH;

@RestController
@RequestMapping(SLASH + "application")
@ResponseStatus(HttpStatus.OK)
@Tag(name = "Application", description = "Application REST API")
public class ApplicationRestController {

    private ApplicationService applicationService;

    private TestExecutionService testExecutionService;

    @Autowired
    public ApplicationRestController(
            ApplicationService applicationService,
            TestExecutionService testExecutionService) {
        this.applicationService = applicationService;
        this.testExecutionService = testExecutionService;
    }

    @PostMapping
    @Operation(summary = "Create an application", operationId = "createApplication")
    public ResponseEntity<ApplicationVO> create(
//            @Valid
            @RequestBody
            final ApplicationVO applicationVO) {
        ApplicationVO application = applicationService.getByName(applicationVO.getName());
        if(application != null) {
            return ResponseEntity.ok().body(application);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(applicationService.create(applicationVO));
    }

    @GetMapping(path = "/{id}")
    @Operation(summary = "Get an application by ID", operationId = "getApplicationById")
    public ApplicationVO get(@PathVariable final Long id) {
        return applicationService.get(id);
    }

    @GetMapping
    @Operation(summary = "List all the applications", operationId = "getAllApplications")
    public List<ApplicationVO> getAll() {
        return applicationService.getAll();
    }

    @PutMapping
    @Operation(summary = "Update an application", operationId = "updateApplication")
    public ApplicationVO update(
//        @Validated(ValidationGroups.Update.class)
        @RequestBody final ApplicationVO applicationVO) {
        return applicationService.update(applicationVO);
    }

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete an application", operationId = "deleteApplication")
    public void delete(@PathVariable final Long id) {
        applicationService.delete(id);
    }

    @GetMapping(path = "/{applicationId}/test-executions")
    @Operation(summary = "Get list of test executions of an application", operationId = "getTestExecutions")
    public List<TestExecutionVO> getTestExecutions(
            @PathVariable
            @Min(1L)
            final Long applicationId) {
        return testExecutionService.getAllByApplicationId(applicationId);
    }

}
