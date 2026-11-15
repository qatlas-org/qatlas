package org.qatlas.backend.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Schema(name = "DashboardStats")
public class DashboardStatsVO {

    private LocalDateTime from;

    private long activeProjects;

    private long executions;

    private long passed;

    private long failed;

    private long warning;

    private Double passRate;

    private List<ProjectSummary> projects = new ArrayList<>();

    /**
     * Number of executions currently running.
     * This is a live metric and is not restricted by the selected date range.
     */
    private long currentlyRunning;

    private List<DailyTrend> dailyTrend = new ArrayList<>();

    private List<MachineStats> executionsByMachine = new ArrayList<>();


    public LocalDateTime getFrom() {
        return from;
    }

    public void setFrom(LocalDateTime from) {
        this.from = from;
    }

    public long getActiveProjects() {
        return activeProjects;
    }

    public void setActiveProjects(long activeProjects) {
        this.activeProjects = activeProjects;
    }

    public long getExecutions() {
        return executions;
    }

    public void setExecutions(long executions) {
        this.executions = executions;
    }

    public long getPassed() {
        return passed;
    }

    public void setPassed(long passed) {
        this.passed = passed;
    }

    public long getFailed() {
        return failed;
    }

    public void setFailed(long failed) {
        this.failed = failed;
    }

    public long getWarning() {
        return warning;
    }

    public void setWarning(long warning) {
        this.warning = warning;
    }

    public Double getPassRate() {
        return passRate;
    }

    public void setPassRate(Double passRate) {
        this.passRate = passRate;
    }

    public List<ProjectSummary> getProjects() {
        return projects;
    }

    public void setProjects(List<ProjectSummary> projects) {
        this.projects = projects;
    }

    public long getCurrentlyRunning() {
        return currentlyRunning;
    }

    public void setCurrentlyRunning(long currentlyRunning) {
        this.currentlyRunning = currentlyRunning;
    }

    public List<DailyTrend> getDailyTrend() {
        return dailyTrend;
    }

    public void setDailyTrend(List<DailyTrend> dailyTrend) {
        this.dailyTrend = dailyTrend;
    }

    public List<MachineStats> getExecutionsByMachine() {
        return executionsByMachine;
    }

    public void setExecutionsByMachine(List<MachineStats> executionsByMachine) {
        this.executionsByMachine = executionsByMachine;
    }


    public static class ProjectSummary {

        private Long applicationId;

        private String applicationName;

        private String applicationDescription;

        private Long latestExecutionId;

        private String latestExecutedBy;

        private String latestSystemName;

        private LocalDateTime latestStartTime;

        private String latestStatus;

        private long totalExecutions;

        private long passedExecutions;

        private long failedExecutions;

        private long warningExecutions;

        private long runningExecutions;

        /**
         * All executors/machines that executed this project
         * within the selected dashboard date range.
         */
        private List<String> executors = new ArrayList<>();


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

        public String getApplicationDescription() {
            return applicationDescription;
        }

        public void setApplicationDescription(String applicationDescription) {
            this.applicationDescription = applicationDescription;
        }

        public Long getLatestExecutionId() {
            return latestExecutionId;
        }

        public void setLatestExecutionId(Long latestExecutionId) {
            this.latestExecutionId = latestExecutionId;
        }

        public String getLatestExecutedBy() {
            return latestExecutedBy;
        }

        public void setLatestExecutedBy(String latestExecutedBy) {
            this.latestExecutedBy = latestExecutedBy;
        }

        public String getLatestSystemName() {
            return latestSystemName;
        }

        public void setLatestSystemName(String latestSystemName) {
            this.latestSystemName = latestSystemName;
        }

        public LocalDateTime getLatestStartTime() {
            return latestStartTime;
        }

        public void setLatestStartTime(LocalDateTime latestStartTime) {
            this.latestStartTime = latestStartTime;
        }

        public String getLatestStatus() {
            return latestStatus;
        }

        public void setLatestStatus(String latestStatus) {
            this.latestStatus = latestStatus;
        }

        public long getTotalExecutions() {
            return totalExecutions;
        }

        public void setTotalExecutions(long totalExecutions) {
            this.totalExecutions = totalExecutions;
        }

        public long getPassedExecutions() {
            return passedExecutions;
        }

        public void setPassedExecutions(long passedExecutions) {
            this.passedExecutions = passedExecutions;
        }

        public long getFailedExecutions() {
            return failedExecutions;
        }

        public void setFailedExecutions(long failedExecutions) {
            this.failedExecutions = failedExecutions;
        }

        public long getWarningExecutions() {
            return warningExecutions;
        }

        public void setWarningExecutions(long warningExecutions) {
            this.warningExecutions = warningExecutions;
        }

        public long getRunningExecutions() {
            return runningExecutions;
        }

        public void setRunningExecutions(long runningExecutions) {
            this.runningExecutions = runningExecutions;
        }

        public List<String> getExecutors() {
            return executors;
        }

        public void setExecutors(List<String> executors) {
            this.executors = executors;
        }
    }


    public static class DailyTrend {

        private String date;

        private long executions;

        private long passed;

        private long failed;

        private long warning;


        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public long getExecutions() {
            return executions;
        }

        public void setExecutions(long executions) {
            this.executions = executions;
        }

        public long getPassed() {
            return passed;
        }

        public void setPassed(long passed) {
            this.passed = passed;
        }

        public long getFailed() {
            return failed;
        }

        public void setFailed(long failed) {
            this.failed = failed;
        }

        public long getWarning() {
            return warning;
        }

        public void setWarning(long warning) {
            this.warning = warning;
        }
    }


    public static class MachineStats {

        private String machine;

        private long executions;


        public String getMachine() {
            return machine;
        }

        public void setMachine(String machine) {
            this.machine = machine;
        }

        public long getExecutions() {
            return executions;
        }

        public void setExecutions(long executions) {
            this.executions = executions;
        }
    }
}