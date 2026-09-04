package org.qatlas.backend.controller;

import org.qatlas.backend.service.EnvironmentService;
import org.qatlas.backend.vo.EnvironmentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.qatlas.backend.Constants.SLASH;

@RestController
@RequestMapping(SLASH + "environment")
@ResponseStatus(HttpStatus.OK)
@Tag(name = "Environment", description = "Environment REST API")
public class EnvironmentRestController {

    private EnvironmentService environmentService;

    @Autowired
    public EnvironmentRestController(EnvironmentService environmentService) {
        this.environmentService = environmentService;
    }

    @PostMapping
    @Operation(summary = "Create an environment", operationId = "createEnvironment")
    public ResponseEntity<EnvironmentVO> create(
//            @Valid
            @RequestBody
            final EnvironmentVO environmentVO) {
        EnvironmentVO environment = environmentService.getByName(environmentVO.getName());
        if(environment != null) {
            return ResponseEntity.ok(environment);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(environmentService.create(environmentVO));
    }

    @GetMapping(path = "/{id}")
    @Operation(summary = "Get an environment by ID", operationId = "getEnvironmentById")
    public EnvironmentVO get(@PathVariable final Long id) {
        return environmentService.get(id);
    }

    @GetMapping
    @Operation(summary = "List all environments", operationId = "getAllEnvironments")
    public List<EnvironmentVO> getAll() {
        return environmentService.getAll();
    }

    @PutMapping
    @Operation(summary = "Update an environment", operationId = "updateEnvironment")
    public EnvironmentVO update(
//            @Validated(ValidationGroups.Update.class)
            @RequestBody
            final EnvironmentVO environmentVO) {
        return environmentService.update(environmentVO);
    }

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete an environment", operationId = "deleteEnvironment")
    public void delete(@PathVariable final Long id) {
        environmentService.delete(id);
    }

}
