package org.qatlas.backend.service;

import org.qatlas.backend.vo.DashboardStatsVO;

import java.time.LocalDateTime;
import java.util.List;
import org.qatlas.backend.vo.ProjectExecutionStatsVO;
public interface DashboardService {

    DashboardStatsVO getDashboardStats(LocalDateTime from);

    List<DashboardStatsVO.ProjectSummary> getDashboardProjects(
            LocalDateTime from,
            String executor
    );
    ProjectExecutionStatsVO getProjectExecutionStats(
            Long applicationId,
            LocalDateTime from
    );
}