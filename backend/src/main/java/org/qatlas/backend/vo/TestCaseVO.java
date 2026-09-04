package org.qatlas.backend.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.qatlas.backend.enums.ExecutionStatus;
import org.qatlas.backend.validator.ValidationGroups;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.collections4.CollectionUtils;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static org.qatlas.backend.enums.ExecutionStatus.*;

@Schema(name = "TestCase")
public class TestCaseVO {
    @Null(
            groups = ValidationGroups.Create.class,
            message = "Test Case ID should be empty."
    )
    @NotNull(
            groups = ValidationGroups.Update.class,
            message = "Test Case ID can not be empty."
    )
    private Long id;

    @NotNull(message = "Test Suite ID can not be empty.")
    private Long testSuiteId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String testSuiteName;

    @NotNull(message = "Test Case Name can not be empty.")
    @Size(max = 100, message = "Test Case Name can not exceed 100 characters.")
    private String name;

    @Size(max = 255, message = "Test Case Reference ID can not exceed 255 characters.")
    private String referenceId;

    @NotNull(message = "Execution Status should not be empty.")
    @Schema(enumAsRef = true)
    private ExecutionStatus executionStatus;

    @NotNull(
            groups = ValidationGroups.Create.class,
            message = "Test Case Execution Start Timestamp should not be empty"
    )
    @Past
    private LocalDateTime executionStartTime;

    @Null(
            groups = ValidationGroups.Create.class,
            message = "Test Case Execution End Timestamp should be empty"
    )
    @Past
    private LocalDateTime executionEndTime;

    private String comments;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<TestStepVO> testSteps;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private TestExecutionVO testExecution;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTestSuiteId() {
        return testSuiteId;
    }

    public void setTestSuiteId(Long testSuiteId) {
        this.testSuiteId = testSuiteId;
    }

    public String getTestSuiteName() {
        return testSuiteName;
    }

    public void setTestSuiteName(String testSuiteName) {
        this.testSuiteName = testSuiteName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
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

    public LocalDateTime getExecutionStartTime() {
        return executionStartTime;
    }

    public void setExecutionStartTime(LocalDateTime executionStartTime) {
        this.executionStartTime = executionStartTime;
    }

    public LocalDateTime getExecutionEndTime() {
        return executionEndTime;
    }

    public void setExecutionEndTime(LocalDateTime executionEndTime) {
        this.executionEndTime = executionEndTime;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public List<TestStepVO> getTestSteps() {
        return testSteps;
    }

    public void setTestSteps(List<TestStepVO> testSteps) {
        this.testSteps = testSteps;
    }

    public TestExecutionVO getTestExecution() {
        return testExecution;
    }

    public void setTestExecution(TestExecutionVO testExecution) {
        this.testExecution = testExecution;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Long getExecutionTime() {
        return executionStartTime != null && executionEndTime != null
            ? ChronoUnit.MILLIS.between(executionStartTime, executionEndTime)
                : 0;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Integer getTotalTestStepCount() {
        return CollectionUtils.size(testSteps);
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Integer getExecutedTestStepCount() {
        return getTestStepCount(testSteps, TestStepVO::isExecuted);
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Integer getPassedTestStepCount() {
        return getTestStepCount(testSteps, TestStepVO::isPassed);
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Integer getFailedTestStepCount() {
        return getTestStepCount(testSteps, TestStepVO::isFailed);
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Integer getTestStepCountWithWarnings() {
        return getTestStepCount(testSteps, TestStepVO::isExecutedWithWarning);
    }

    private Integer getTestStepCount(
            List<TestStepVO> testSteps, Predicate<TestStepVO> predicate) {
        if(CollectionUtils.isEmpty(testSteps)) {
            return 0;
        }
        if(predicate == null) {
            throw new IllegalArgumentException(
                    "Filter predicate is NULL");
        }
        return Math.toIntExact(testSteps.stream().filter(predicate).count());
    }

}
