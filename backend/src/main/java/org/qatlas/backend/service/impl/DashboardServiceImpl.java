package org.qatlas.backend.service.impl;


import org.qatlas.backend.entity.Application;
import org.qatlas.backend.entity.TestExecution;
import org.qatlas.backend.enums.ExecutionStatus;
import org.qatlas.backend.repository.ApplicationRepository;
import org.qatlas.backend.repository.TestCaseRepository;
import org.qatlas.backend.repository.TestExecutionRepository;
import org.qatlas.backend.service.DashboardService;
import org.qatlas.backend.vo.DashboardStatsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Service
public class DashboardServiceImpl implements DashboardService {

    private final TestExecutionRepository testExecutionRepository;

    private final TestCaseRepository testCaseRepository;

    private final ApplicationRepository applicationRepository;


    @Autowired
    public DashboardServiceImpl(
            TestExecutionRepository testExecutionRepository,
            TestCaseRepository testCaseRepository,
            ApplicationRepository applicationRepository) {

        this.testExecutionRepository = testExecutionRepository;
        this.testCaseRepository = testCaseRepository;
        this.applicationRepository = applicationRepository;
    }


    @Override
    @Transactional(readOnly = true)
    public DashboardStatsVO getDashboardStats(LocalDateTime from) {

        DashboardStatsVO stats = new DashboardStatsVO();

        /*
         * A null 'from' means ALL / Lifetime.
         * Resolve it to the earliest non-archived execution so all existing
         * aggregate queries can keep using the same indexed date predicate.
         */
        LocalDateTime effectiveFrom = from;

        if (effectiveFrom == null) {

            effectiveFrom =
                    testExecutionRepository.findDashboardEarliestExecutionTime();

            /*
             * Empty database safeguard.
             */
            if (effectiveFrom == null) {
                effectiveFrom = LocalDate.now().atStartOfDay();
            }
        }

        /*
         * Keep null in the response when the request was Lifetime/ALL.
         */
        stats.setFrom(from);

        stats.setExecutions(
                testExecutionRepository.countDashboardExecutions(effectiveFrom)
        );

        stats.setActiveProjects(
                testExecutionRepository.countDashboardActiveProjects(effectiveFrom)
        );

        stats.setCurrentlyRunning(
                testExecutionRepository.countCurrentlyRunning(
                        LocalDateTime.now().minusHours(24)
                )
        );

        populateStatusTotals(stats, effectiveFrom);
        populateDailyTrend(stats, effectiveFrom);
        populateMachineStats(stats, effectiveFrom);
        populateProjects(stats, effectiveFrom);

        return stats;
    }


    private void populateStatusTotals(
            DashboardStatsVO stats,
            LocalDateTime from) {

        List<Object[]> rows =
                testCaseRepository.countDashboardByStatus(from);

        long passed = 0;
        long failed = 0;
        long warning = 0;

        for (Object[] row : rows) {

            ExecutionStatus status = (ExecutionStatus) row[0];
            long count = ((Number) row[1]).longValue();

            switch (status) {

                case PASSED:
                    passed = count;
                    break;

                case FAILED:
                    failed = count;
                    break;

                case WARNING:
                    warning = count;
                    break;

                default:
                    // PLANNED and PROGRESS are not considered executed.
                    break;
            }
        }

        stats.setPassed(passed);
        stats.setFailed(failed);
        stats.setWarning(warning);

        long executed = passed + failed + warning;

        if (executed > 0) {

            double passRate =
                    ((double) passed / executed) * 100.0;

            stats.setPassRate(
                    Math.round(passRate * 10.0) / 10.0
            );

        } else {

            stats.setPassRate(null);
        }
    }


    private void populateDailyTrend(
            DashboardStatsVO stats,
            LocalDateTime from) {

        Map<LocalDate, DashboardStatsVO.DailyTrend> daily =
                new LinkedHashMap<>();

        /*
         * Create every date in the requested range first.
         * This means days with no executions still appear in the chart as 0.
         */
        LocalDate day = from.toLocalDate();
        LocalDate today = LocalDate.now();

        while (!day.isAfter(today)) {

            DashboardStatsVO.DailyTrend trend =
                    new DashboardStatsVO.DailyTrend();

            trend.setDate(day.toString());

            daily.put(day, trend);

            day = day.plusDays(1);
        }


        /*
         * Number of executions per day.
         */
        List<Object[]> executionRows =
                testExecutionRepository.countDashboardExecutionsByDay(from);

        for (Object[] row : executionRows) {

            LocalDate executionDate = toLocalDate(row[0]);
            long executions = ((Number) row[1]).longValue();

            DashboardStatsVO.DailyTrend trend =
                    daily.get(executionDate);

            if (trend != null) {
                trend.setExecutions(executions);
            }
        }


        /*
         * Passed / failed / warning test-case counts per day.
         */
        List<Object[]> statusRows =
                testCaseRepository.countDashboardDailyByStatus(from);

        for (Object[] row : statusRows) {

            LocalDate executionDate = toLocalDate(row[0]);
            ExecutionStatus status = (ExecutionStatus) row[1];
            long count = ((Number) row[2]).longValue();

            DashboardStatsVO.DailyTrend trend =
                    daily.get(executionDate);

            if (trend == null) {
                continue;
            }

            switch (status) {

                case PASSED:
                    trend.setPassed(count);
                    break;

                case FAILED:
                    trend.setFailed(count);
                    break;

                case WARNING:
                    trend.setWarning(count);
                    break;

                default:
                    // Ignore PLANNED and PROGRESS.
                    break;
            }
        }

        stats.setDailyTrend(
                List.copyOf(daily.values())
        );
    }


    private void populateMachineStats(
            DashboardStatsVO stats,
            LocalDateTime from) {

        List<Object[]> rows =
                testExecutionRepository.countDashboardExecutionsByMachine(from);

        List<DashboardStatsVO.MachineStats> machines =
                rows.stream()
                        .map(row -> {

                            DashboardStatsVO.MachineStats machine =
                                    new DashboardStatsVO.MachineStats();

                            machine.setMachine(
                                    row[0] != null
                                            ? row[0].toString()
                                            : "Unknown"
                            );

                            machine.setExecutions(
                                    ((Number) row[1]).longValue()
                            );

                            return machine;
                        })
                        .toList();

        stats.setExecutionsByMachine(machines);
    }


    private void populateProjects(
            DashboardStatsVO stats,
            LocalDateTime from) {

        LocalDateTime runningSince =
                LocalDateTime.now().minusHours(24);

        /*
         * Total executions per application in the selected range.
         */
        Map<Long, Long> totalByApplication =
                new HashMap<>();

        for (Object[] row :
                testExecutionRepository.countDashboardExecutionsByApplication(from)) {

            Long applicationId =
                    ((Number) row[0]).longValue();

            long count =
                    ((Number) row[1]).longValue();

            totalByApplication.put(
                    applicationId,
                    count
            );
        }


        /*
         * Executors / machines used by each application
         * in the selected dashboard range.
         *
         * This is used by the "Executed by" filter.
         */
        Map<Long, List<String>> executorsByApplication =
                new HashMap<>();

        for (Object[] row :
                testExecutionRepository
                        .countDashboardExecutionsByApplicationAndMachine(from)) {

            Long applicationId =
                    ((Number) row[0]).longValue();

            String executor =
                    row[1] != null
                            ? row[1].toString()
                            : "Unknown";

            executorsByApplication
                    .computeIfAbsent(
                            applicationId,
                            key -> new ArrayList<>()
                    )
                    .add(executor);
        }


        /*
         * Execution-status segment totals per application.
         *
         * The repository query classifies every execution once as
         * RUNNING / FAILED / WARNING / PASSED.
         */
        Map<Long, Map<String, Long>> statusByApplication =
                new HashMap<>();

        for (Object[] row :
                testCaseRepository
                        .countDashboardExecutionsByApplicationAndStatus(
                                from,
                                runningSince)) {

            Long applicationId =
                    ((Number) row[0]).longValue();

            String status =
                    row[1].toString();

            long count =
                    ((Number) row[2]).longValue();

            statusByApplication
                    .computeIfAbsent(
                            applicationId,
                            key -> new HashMap<>()
                    )
                    .put(
                            status,
                            count
                    );
        }


        /*
         * Latest execution for each application,
         * retrieved in one query.
         */
        Map<Long, TestExecution> latestByApplication =
                new HashMap<>();

        List<TestExecution> latestExecutions =
                testExecutionRepository
                        .findDashboardLatestExecutionsByApplication(from);

        for (TestExecution execution : latestExecutions) {

            latestByApplication.put(
                    execution.getApplication().getId(),
                    execution
            );
        }


        /*
         * Fetch testcase statuses for all latest executions in one query.
         * This lets us derive the latest card status without N+1 queries.
         */
        Map<Long, Map<ExecutionStatus, Long>> latestExecutionStatuses =
                new HashMap<>();

        List<Long> latestExecutionIds =
                latestExecutions.stream()
                        .map(TestExecution::getId)
                        .toList();

        if (!latestExecutionIds.isEmpty()) {

            for (Object[] row :
                    testCaseRepository
                            .countByExecutionIdsGroupedByStatus(
                                    latestExecutionIds)) {

                Long executionId =
                        ((Number) row[0]).longValue();

                ExecutionStatus status =
                        (ExecutionStatus) row[1];

                long count =
                        ((Number) row[2]).longValue();

                latestExecutionStatuses
                        .computeIfAbsent(
                                executionId,
                                key -> new HashMap<>()
                        )
                        .put(
                                status,
                                count
                        );
            }
        }


        /*
         * Build one project summary for every configured application.
         *
         * Applications with no executions in the selected range
         * are still returned with zero totals and no latest execution.
         */
        List<DashboardStatsVO.ProjectSummary> projects =
                new ArrayList<>();

        for (Application application :
                applicationRepository.findAll()) {

            DashboardStatsVO.ProjectSummary project =
                    new DashboardStatsVO.ProjectSummary();

            Long applicationId =
                    application.getId();

            project.setApplicationId(
                    applicationId
            );

            project.setApplicationName(
                    application.getName()
            );

            project.setApplicationDescription(
                    application.getDescription()
            );

            project.setTotalExecutions(
                    totalByApplication.getOrDefault(
                            applicationId,
                            0L
                    )
            );

            /*
             * Important:
             * Keep every executor that ran this project
             * during the selected dashboard range.
             */
            project.setExecutors(
                    executorsByApplication.getOrDefault(
                            applicationId,
                            List.of()
                    )
            );


            Map<String, Long> projectStatuses =
                    statusByApplication.getOrDefault(
                            applicationId,
                            Map.of()
                    );

            project.setPassedExecutions(
                    projectStatuses.getOrDefault(
                            "PASSED",
                            0L
                    )
            );

            project.setFailedExecutions(
                    projectStatuses.getOrDefault(
                            "FAILED",
                            0L
                    )
            );

            project.setWarningExecutions(
                    projectStatuses.getOrDefault(
                            "WARNING",
                            0L
                    )
            );

            project.setRunningExecutions(
                    projectStatuses.getOrDefault(
                            "RUNNING",
                            0L
                    )
            );


            TestExecution latest =
                    latestByApplication.get(applicationId);

            if (latest != null) {

                project.setLatestExecutionId(
                        latest.getId()
                );

                project.setLatestExecutedBy(
                        latest.getExecutedBy()
                );

                project.setLatestSystemName(
                        latest.getSystemName()
                );

                project.setLatestStartTime(
                        latest.getStartTime()
                );

                project.setLatestStatus(
                        determineExecutionStatus(
                                latest,
                                latestExecutionStatuses.getOrDefault(
                                        latest.getId(),
                                        Map.of()
                                ),
                                runningSince
                        )
                );
            }

            projects.add(project);
        }

        stats.setProjects(projects);
    }


    private String determineExecutionStatus(
            TestExecution execution,
            Map<ExecutionStatus, Long> statuses,
            LocalDateTime runningSince) {

        if (execution.getEndTime() == null
                && !execution.getStartTime().isBefore(runningSince)) {

            return "RUNNING";
        }

        if (statuses.getOrDefault(
                ExecutionStatus.FAILED,
                0L) > 0) {

            return "FAILED";
        }

        if (statuses.getOrDefault(
                ExecutionStatus.WARNING,
                0L) > 0) {

            return "WARNING";
        }

        return "PASSED";
    }


    private LocalDate toLocalDate(Object value) {

        if (value instanceof LocalDate) {
            return (LocalDate) value;
        }

        if (value instanceof Date) {
            return ((Date) value).toLocalDate();
        }

        return LocalDate.parse(
                value.toString()
        );
    }
}