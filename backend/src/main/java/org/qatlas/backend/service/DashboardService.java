package org.qatlas.backend.service;

import org.qatlas.backend.vo.DashboardStatsVO;

import java.time.LocalDateTime;

public interface DashboardService {

    DashboardStatsVO getDashboardStats(LocalDateTime from);
}