package org.qatlas.backend.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.qatlas.backend.enums.ExecutionStatus;
import org.qatlas.backend.validator.ValidationGroups;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

import static org.qatlas.backend.enums.ExecutionStatus.*;

@Schema(name = "TestStep")
public class TestStepVO {

    @Null(
        groups = ValidationGroups.Create.class,
        message = "Test Step ID should be empty."
    )
    @NotNull(
        groups = ValidationGroups.Update.class,
        message = "Test Step ID should not be empty."
    )
    private Long id;

    @NotNull(message = "Test Case ID should not be empty.")
    private Long testCaseId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String testCaseName;

    @NotNull(message = "Test Step Description should not be empty.")
    @Size(
        max = 255,
        message = "Test Step Description should not exceed {max} characters."
    )
    private String description;

    private String objectName;

    private String operation;

    private String result;

    private Long executionTime;

    @NotNull(message = "Execution Status should not be empty.")
    private ExecutionStatus executionStatus;

    private List<TestStepAttachmentVO> attachments = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTestCaseId() {
        return testCaseId;
    }

    public void setTestCaseId(Long testCaseId) {
        this.testCaseId = testCaseId;
    }

    public String getTestCaseName() {
        return testCaseName;
    }

    public void setTestCaseName(String testCaseName) {
        this.testCaseName = testCaseName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Long executionTime) {
        this.executionTime = executionTime;
    }

    public ExecutionStatus getExecutionStatus() {
        return executionStatus;
    }

    public void setExecutionStatus(ExecutionStatus executionStatus) {
        this.executionStatus = executionStatus;
    }

    @JsonIgnore
    public boolean isInProgress() {
        return this.executionStatus == PROGRESS;
    }

    @JsonIgnore
    public boolean isExecuted() {
        return this.executionStatus.isExecuted();
    }

    @JsonIgnore
    public boolean isPassed() {
        return this.executionStatus == PASSED;
    }

    @JsonIgnore
    public boolean isFailed() {
        return this.executionStatus == FAILED;
    }

    @JsonIgnore
    public boolean isExecutedWithWarning() {
        return this.executionStatus == WARNING;
    }

    public List<TestStepAttachmentVO> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<TestStepAttachmentVO> attachments) {
        this.attachments = attachments;
    }

}
