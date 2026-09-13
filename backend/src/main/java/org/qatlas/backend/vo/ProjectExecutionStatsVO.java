package org.qatlas.backend.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Schema(name = "ProjectExecutionStats")
public class ProjectExecutionStatsVO {

    private Long applicationId;

    private String applicationName;

    /**
     * Null means ALL / lifetime.
     */
    private LocalDateTime from;

    private long totalExecutions;

    private long passedExecutions;

    private long failedExecutions;

    private long inProgressExecutions;

    private List<DailyExecutionTrend> dailyExecutions = new ArrayList<>();

    private List<PassRateTrend> passRateTrend = new ArrayList<>();


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

    public LocalDateTime getFrom() {
        return from;
    }

    public void setFrom(LocalDateTime from) {
        this.from = from;
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

    public long getInProgressExecutions() {
        return inProgressExecutions;
    }

    public void setInProgressExecutions(long inProgressExecutions) {
        this.inProgressExecutions = inProgressExecutions;
    }

    public List<DailyExecutionTrend> getDailyExecutions() {
        return dailyExecutions;
    }

    public void setDailyExecutions(List<DailyExecutionTrend> dailyExecutions) {
        this.dailyExecutions = dailyExecutions;
    }

    public List<PassRateTrend> getPassRateTrend() {
        return passRateTrend;
    }

    public void setPassRateTrend(List<PassRateTrend> passRateTrend) {
        this.passRateTrend = passRateTrend;
    }


    public static class DailyExecutionTrend {

        private String date;

        private long passed;

        private long failed;

        private long running;


        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
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

        public long getRunning() {
            return running;
        }

        public void setRunning(long running) {
            this.running = running;
        }
    }


    public static class PassRateTrend {

        private String date;

        private Double passRate;


        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public Double getPassRate() {
            return passRate;
        }

        public void setPassRate(Double passRate) {
            this.passRate = passRate;
        }
    }
}