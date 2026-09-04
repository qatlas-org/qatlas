package org.qatlas.backend.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.qatlas.backend.validator.ValidationGroups;
import org.qatlas.backend.validator.annotation.IpAddress;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Schema(name = "TestExecution")
public class TestExecutionVO {
    @Null(
        groups = ValidationGroups.Create.class,
        message = "ID should be empty."
    )
    @NotNull(
        groups = ValidationGroups.Update.class,
        message = "ID should not be empty."
    )
    private Long id;

    @NotNull
    @Size(
        max = 100,
        message = "{reporting.validation.constraints.TestExecution.name.Size.message}"
    )
    private String name;

    @NotNull(message = "Application ID should not be empty.")
    private Long applicationId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String applicationName;

    @Size(
        max = 50,
        message = "Application Version should not exceed 50 characters."
    )
    private String applicationVersion;

    @NotNull(message = "Environment ID should not be empty.")
    private Long environmentId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String environmentName;

    @NotNull(message = "Browser details should not be empty.")
    @Size(
        max = 100,
        message = "'Browser Details' should not exceed 100 characters."
    )
    private String browser;

    @Size(
        max = 100,
        message = "'Operating System Details' should not exceed 100 characters."
    )
    private String operatingSystem;

    @NotNull(message = "System IP Address should not be empty.")
    @Size(
        max = 50,
        message = "'System IP Address' should not exceed 50 characters."
    )
    @IpAddress
    private String systemIp;

    @Size(max = 20, message = "'System Name' should not exceed 20 characters.")
    @NotNull(message = "System Name should not be empty.")
    private String systemName;

    @Size(max = 50, message = "'Executed By' should not exceed 50 characters.")
    private String executedBy;

    @NotNull(message = "Execution Start Timestamp should not be empty.")
    @PastOrPresent(message = "Execution Start Timestamp should not be future.")
    private LocalDateTime startTime;

    @Null(
            groups = ValidationGroups.Create.class,
            message = "End Time should be empty."
    )
    private LocalDateTime endTime;

    private Boolean archived = false;

    // NOTE: these counts used to be computed on read by walking the full
    // testSuites -> testCases entity/VO graph in Java. That graph is no
    // longer loaded/mapped for this VO at all (it was lazy-loaded, JsonIgnored,
    // and triggered an N+1 query fan-out for data that was thrown away before
    // serialization anyway). These are now plain fields, populated directly
    // by TestExecutionServiceImpl via a couple of aggregate GROUP BY queries.
    private int targetedTestCaseCount = 0;
    private int executedTestCaseCount = 0;
    private int passedTestCaseCount = 0;
    private int failedTestCaseCount = 0;
    private int skippedTestCaseCount = 0;
    private int inProgressTestCaseCount = 0;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationVersion() {
        return applicationVersion;
    }

    public void setApplicationVersion(String applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    public Long getEnvironmentId() {
        return environmentId;
    }

    public void setEnvironmentId(Long environmentId) {
        this.environmentId = environmentId;
    }

    public String getEnvironmentName() {
        return environmentName;
    }

    public void setEnvironmentName(String environmentName) {
        this.environmentName = environmentName;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public String getSystemIp() {
        return systemIp;
    }

    public void setSystemIp(String systemIp) {
        this.systemIp = systemIp;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public String getExecutedBy() {
        return executedBy;
    }

    public void setExecutedBy(String executedBy) {
        this.executedBy = executedBy;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    /**
     * Plain getter now. The previous version computed a fallback end time
     * on-the-fly by walking the full testSuites/testCases graph when
     * endTime was null. That fallback is now computed once, server-side,
     * in TestExecutionServiceImpl (via an aggregate MAX() query) and
     * assigned through setEndTime() before this VO is returned.
     */
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Boolean getArchived() {
        return archived;
    }

    public void setArchived(Boolean archived) {
        this.archived = archived;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public int getTargetedTestCaseCount() {
        return targetedTestCaseCount;
    }

    public void setTargetedTestCaseCount(int targetedTestCaseCount) {
        this.targetedTestCaseCount = targetedTestCaseCount;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public int getExecutedTestCaseCount() {
        return executedTestCaseCount;
    }

    public void setExecutedTestCaseCount(int executedTestCaseCount) {
        this.executedTestCaseCount = executedTestCaseCount;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public int getPassedTestCaseCount() {
        return passedTestCaseCount;
    }

    public void setPassedTestCaseCount(int passedTestCaseCount) {
        this.passedTestCaseCount = passedTestCaseCount;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public int getFailedTestCaseCount() {
        return failedTestCaseCount;
    }

    public void setFailedTestCaseCount(int failedTestCaseCount) {
        this.failedTestCaseCount = failedTestCaseCount;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public int getSkippedTestCaseCount() {
        return skippedTestCaseCount;
    }

    public void setSkippedTestCaseCount(int skippedTestCaseCount) {
        this.skippedTestCaseCount = skippedTestCaseCount;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public int getInProgressTestCaseCount() {
        return inProgressTestCaseCount;
    }

    public void setInProgressTestCaseCount(int inProgressTestCaseCount) {
        this.inProgressTestCaseCount = inProgressTestCaseCount;
    }

}
