package org.qatlas.backend.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.qatlas.backend.validator.ValidationGroups;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.collections4.CollectionUtils;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Schema(name = "TestSuite")
public class TestSuiteVO {

    @Null(
        groups = ValidationGroups.Create.class,
        message = "Test Suite ID should be empty."
    )
    @NotNull(
        groups = ValidationGroups.Update.class,
        message = "Test Suite ID should not be empty."
    )
    private Long id;

    @NotNull(message = "Test Execution ID should not be empty.")
    private Long testExecutionId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String testExecutionName;

    @NotNull(message = "Test Suite Name should not be empty.")
    @Size(
        max = 100,
        message = "Test Suite Name should not exceed 100 characters."
    )
    private String testSuiteName;

    @NotNull(
        groups = ValidationGroups.Create.class,
        message = "Test Suite Execution Start Time should not be empty."
    )
    @Past(message = "Execution Start Time should be past time.")
    private LocalDateTime executionStartTime;

    @Null(
        groups = ValidationGroups.Create.class,
        message = "Test Suite Execution End Time should be empty."
    )
    @Past(message = "Execution End Time should be past time.")
    private LocalDateTime executionEndTime;

    @NotNull(
        groups = ValidationGroups.Create.class,
        message = "Test Suite Planned Test Cases Count should not be empty."
    )
    @PositiveOrZero
    private Integer plannedTestCaseCount;

    @JsonIgnore
    private List<TestCaseVO> testCases = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTestExecutionId() {
        return testExecutionId;
    }

    public void setTestExecutionId(Long testExecutionId) {
        this.testExecutionId = testExecutionId;
    }

    public String getTestExecutionName() {
        return testExecutionName;
    }

    public void setTestExecutionName(String testExecutionName) {
        this.testExecutionName = testExecutionName;
    }

    public String getTestSuiteName() {
        return testSuiteName;
    }

    public void setTestSuiteName(String testSuiteName) {
        this.testSuiteName = testSuiteName;
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

    public Integer getPlannedTestCaseCount() {
        return plannedTestCaseCount;
    }

    public void setPlannedTestCaseCount(Integer plannedTestCaseCount) {
        this.plannedTestCaseCount = plannedTestCaseCount;
    }

    public List<TestCaseVO> getTestCases() {
        return testCases;
    }

    public void setTestCases(List<TestCaseVO> testCases) {
        this.testCases = testCases;
    }

    @AssertTrue(message = "{msg.invalid.execution.end.time}")
    private boolean validateExecutionEndTime() {
        if(executionEndTime != null) {
            return executionEndTime.compareTo(executionStartTime) > 0;
        }
        return true;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Integer getInProgressTestCaseCount() {
        return getTestCaseCount(TestCaseVO::isInProgress);
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Integer getExecutedTestCaseCount() {
        return getTestCaseCount(TestCaseVO::isExecuted);
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Integer getPassedTestCaseCount() {
        return getTestCaseCount(TestCaseVO::isPassed);
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Integer getFailedTestCaseCount() {
        return getTestCaseCount(TestCaseVO::isFailed);
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Integer getTestCasesCountWithWarnings() {
        return getTestCaseCount(TestCaseVO::isExecutedWithWarning);
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Integer getSkippedTestCaseCount() {
        //return plannedTestCaseCount - CollectionUtils.size(testCases);
        //TODO: business need to provide requirements
        return 0;
    }

    private Integer getTestCaseCount(Predicate<TestCaseVO> predicate) {
        if(CollectionUtils.isEmpty(testCases)) {
            return 0;
        }
        if(predicate == null) {
            throw new IllegalArgumentException(
                    "Filter predicate is NULL");
        }
        return Math.toIntExact(testCases.stream().filter(predicate).count());
    }

}
