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
import org.qatlas.backend.exception.ApplicationNotFoundException;
import org.qatlas.backend.vo.ProjectExecutionStatsVO;

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

    @Override
    @Transactional(readOnly = true)
    public List<DashboardStatsVO.ProjectSummary> getDashboardProjects(
            LocalDateTime from,
            String executor) {

        LocalDateTime effectiveFrom = from;

        if (effectiveFrom == null) {

            effectiveFrom =
                    testExecutionRepository.findDashboardEarliestExecutionTime();

            if (effectiveFrom == null) {
                effectiveFrom = LocalDate.now().atStartOfDay();
            }
        }

        String effectiveExecutor =
                executor != null && !executor.isBlank()
                        ? executor
                        : null;

        return buildProjects(
                effectiveFrom,
                effectiveExecutor
        );
    }


    @Override
    @Transactional(readOnly = true)
    public ProjectExecutionStatsVO getProjectExecutionStats(
            Long applicationId,
            LocalDateTime from) {

        Application application =
                applicationRepository
                        .findById(applicationId)
                        .orElseThrow(
                                () -> new ApplicationNotFoundException(applicationId)
                        );

        /*
         * null means ALL / lifetime.
         *
         * For ALL we resolve the effective start time to the first
         * non-archived execution belonging to this project.
         */
        LocalDateTime effectiveFrom = from;

        if (effectiveFrom == null) {

            effectiveFrom =
                    testExecutionRepository
                            .findProjectEarliestExecutionTime(applicationId);

            /*
             * Project exists but has never been executed.
             */
            if (effectiveFrom == null) {
                effectiveFrom = LocalDate.now().atStartOfDay();
            }
        }

        LocalDateTime runningSince =
                LocalDateTime.now().minusHours(24);


        ProjectExecutionStatsVO stats =
                new ProjectExecutionStatsVO();

        stats.setApplicationId(application.getId());
        stats.setApplicationName(application.getName());

        /*
         * Preserve null in the response for ALL.
         */
        stats.setFrom(from);


        /*
         * ---------------------------------------------------------
         * Execution-level KPI counts
         * ---------------------------------------------------------
         */
        long total = 0;
        long passed = 0;
        long failed = 0;
        long running = 0;

        /*
         * WARNING is intentionally kept separate internally.
         *
         * The Figma does not currently expose a Warning KPI,
         * but warnings are still executions and must contribute
         * to Total and to the pass-rate denominator.
         */
        long warning = 0;


        List<Object[]> statusRows =
                testExecutionRepository
                        .countProjectExecutionsByStatus(
                                applicationId,
                                effectiveFrom,
                                runningSince
                        );


        for (Object[] row : statusRows) {

            String status = row[0].toString();

            long count =
                    ((Number) row[1]).longValue();

            total += count;

            switch (status) {

                case "PASSED":
                    passed = count;
                    break;

                case "FAILED":
                    failed = count;
                    break;

                case "RUNNING":
                    running = count;
                    break;

                case "WARNING":
                    warning = count;
                    break;

                default:
                    break;
            }
        }


        stats.setTotalExecutions(total);
        stats.setPassedExecutions(passed);
        stats.setFailedExecutions(failed);
        stats.setInProgressExecutions(running);


        /*
         * ---------------------------------------------------------
         * Daily graph data
         * ---------------------------------------------------------
         *
         * Only dates with actual executions are added.
         * This keeps ALL / lifetime responses compact.
         */
        Map<LocalDate, ProjectExecutionStatsVO.DailyExecutionTrend>
                dailyExecutions = new LinkedHashMap<>();

        Map<LocalDate, Long>
                warningByDay = new HashMap<>();


        List<Object[]> dailyRows =
                testExecutionRepository
                        .countProjectExecutionsByDayAndStatus(
                                applicationId,
                                effectiveFrom,
                                runningSince
                        );


        for (Object[] row : dailyRows) {

            LocalDate executionDate =
                    toLocalDate(row[0]);

            String status =
                    row[1].toString();

            long count =
                    ((Number) row[2]).longValue();


            ProjectExecutionStatsVO.DailyExecutionTrend trend =
                    dailyExecutions.computeIfAbsent(
                            executionDate,
                            date -> {
                                ProjectExecutionStatsVO.DailyExecutionTrend value =
                                        new ProjectExecutionStatsVO.DailyExecutionTrend();

                                value.setDate(date.toString());

                                return value;
                            }
                    );


            switch (status) {

                case "PASSED":
                    trend.setPassed(count);
                    break;

                case "FAILED":
                    trend.setFailed(count);
                    break;

                case "RUNNING":
                    trend.setRunning(count);
                    break;

                case "WARNING":
                    warningByDay.put(
                            executionDate,
                            count
                    );
                    break;

                default:
                    break;
            }
        }


        stats.setDailyExecutions(
                List.copyOf(dailyExecutions.values())
        );


        /*
         * ---------------------------------------------------------
         * Individual execution blocks for the interactive graph
         * ---------------------------------------------------------
         *
         * These are intentionally lightweight: the frontend only needs
         * the execution id/name/date/status to render a clickable block
         * and navigate to /executions/{id}.
         */
        List<ProjectExecutionStatsVO.ExecutionBlock> executionBlocks =
                new ArrayList<>();

        List<Object[]> executionBlockRows =
                testExecutionRepository.findProjectExecutionBlocks(
                        applicationId,
                        effectiveFrom,
                        runningSince
                );

        for (Object[] row : executionBlockRows) {

            ProjectExecutionStatsVO.ExecutionBlock block =
                    new ProjectExecutionStatsVO.ExecutionBlock();

            block.setExecutionId(
                    ((Number) row[0]).longValue()
            );

            block.setExecutionName(
                    row[1] != null
                            ? row[1].toString()
                            : "Execution " + block.getExecutionId()
            );

            block.setDate(
                    toLocalDate(row[2]).toString()
            );

            block.setStatus(
                    row[3] != null
                            ? row[3].toString()
                            : "PASSED"
            );

            executionBlocks.add(block);
        }

        stats.setExecutionBlocks(executionBlocks);


        /*
         * ---------------------------------------------------------
         * Pass-rate trend
         * ---------------------------------------------------------
         *
         * Same rule as the main dashboard:
         *
         * passed / (passed + failed + warning)
         *
         * Running executions are excluded because they have not
         * completed yet.
         */
        List<ProjectExecutionStatsVO.PassRateTrend>
                passRateTrend = new ArrayList<>();


        for (Map.Entry<
                LocalDate,
                ProjectExecutionStatsVO.DailyExecutionTrend> entry
                : dailyExecutions.entrySet()) {

            LocalDate trendDate =
                    entry.getKey();

            ProjectExecutionStatsVO.DailyExecutionTrend daily =
                    entry.getValue();


            long dailyWarning =
                    warningByDay.getOrDefault(
                            trendDate,
                            0L
                    );


            long completed =
                    daily.getPassed()
                            + daily.getFailed()
                            + dailyWarning;


            ProjectExecutionStatsVO.PassRateTrend passRate =
                    new ProjectExecutionStatsVO.PassRateTrend();

            passRate.setDate(
                    trendDate.toString()
            );


            if (completed > 0) {

                double rate =
                        ((double) daily.getPassed()
                                / completed) * 100.0;

                passRate.setPassRate(
                        Math.round(rate * 10.0) / 10.0
                );

            } else {

                passRate.setPassRate(null);
            }


            passRateTrend.add(passRate);
        }


        stats.setPassRateTrend(passRateTrend);

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

        stats.setProjects(
                buildProjects(
                        from,
                        null
                )
        );
    }


    private List<DashboardStatsVO.ProjectSummary> buildProjects(
            LocalDateTime from,
            String executor) {

        LocalDateTime runningSince =
                LocalDateTime.now().minusHours(24);


        /*
         * Total executions per application.
         *
         * When executor is null we keep the current ALL behaviour.
         * Otherwise the database only considers executions belonging
         * to the selected executor.
         */
        Map<Long, Long> totalByApplication =
                new HashMap<>();

        List<Object[]> totalRows =
                executor == null
                        ? testExecutionRepository
                        .countDashboardExecutionsByApplication(from)
                        : testExecutionRepository
                        .countDashboardExecutionsByApplicationAndExecutor(
                                from,
                                executor
                        );

        for (Object[] row : totalRows) {

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
         * Executors used by each project.
         *
         * For the normal ALL dashboard we preserve the complete executor
         * membership information.
         *
         * For an executor-filtered request there is no reason to run the
         * application/machine grouping query again: every returned project
         * belongs to the requested executor.
         */
        Map<Long, List<String>> executorsByApplication =
                new HashMap<>();

        if (executor == null) {

            for (Object[] row :
                    testExecutionRepository
                            .countDashboardExecutionsByApplicationAndMachine(from)) {

                Long applicationId =
                        ((Number) row[0]).longValue();

                String projectExecutor =
                        row[1] != null
                                ? row[1].toString()
                                : "Unknown";

                executorsByApplication
                        .computeIfAbsent(
                                applicationId,
                                key -> new ArrayList<>()
                        )
                        .add(projectExecutor);
            }

        } else {

            for (Long applicationId :
                    totalByApplication.keySet()) {

                executorsByApplication.put(
                        applicationId,
                        List.of(executor)
                );
            }
        }


        /*
         * Execution-status totals per project.
         */
        Map<Long, Map<String, Long>> statusByApplication =
                new HashMap<>();

        List<Object[]> statusRows =
                executor == null
                        ? testCaseRepository
                        .countDashboardExecutionsByApplicationAndStatus(
                                from,
                                runningSince
                        )
                        : testCaseRepository
                        .countDashboardExecutionsByApplicationAndStatusAndExecutor(
                                from,
                                runningSince,
                                executor
                        );

        for (Object[] row : statusRows) {

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
         * Latest execution per project.
         *
         * This is the important part of the executor filter:
         * when an executor is selected, the latest execution is the latest
         * execution by THAT executor rather than the latest execution overall.
         */
        List<TestExecution> latestExecutions =
                executor == null
                        ? testExecutionRepository
                        .findDashboardLatestExecutionsByApplication(from)
                        : testExecutionRepository
                        .findDashboardLatestExecutionsByApplicationAndExecutor(
                                from,
                                executor
                        );

        Map<Long, TestExecution> latestByApplication =
                new HashMap<>();

        for (TestExecution execution :
                latestExecutions) {

            latestByApplication.put(
                    execution.getApplication().getId(),
                    execution
            );
        }


        /*
         * Fetch testcase statuses for the selected latest executions
         * in one query to avoid N+1 database access.
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


        List<DashboardStatsVO.ProjectSummary> projects =
                new ArrayList<>();


        for (Application application :
                applicationRepository.findAll()) {

            Long applicationId =
                    application.getId();


            /*
             * For a specific executor only return projects actually
             * executed by that executor in the selected date range.
             */
            if (executor != null
                    && !totalByApplication.containsKey(applicationId)) {

                continue;
            }


            DashboardStatsVO.ProjectSummary project =
                    new DashboardStatsVO.ProjectSummary();

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


        return projects;
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