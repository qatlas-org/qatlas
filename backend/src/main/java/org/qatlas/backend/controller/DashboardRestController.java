package org.qatlas.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.qatlas.backend.service.DashboardService;
import org.qatlas.backend.vo.DashboardStatsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import static org.qatlas.backend.Constants.SLASH;

@RestController
@RequestMapping(SLASH + "dashboard")
@ResponseStatus(HttpStatus.OK)
@Tag(name = "Dashboard", description = "Dashboard REST API")
public class DashboardRestController {

    private final DashboardService dashboardService;

    @Autowired
    public DashboardRestController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    @Operation(
            summary = "Get dashboard statistics from the selected date, or lifetime when omitted",
            operationId = "getDashboardStats"
    )
    public DashboardStatsVO getDashboardStats(
            @RequestParam(name = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final LocalDateTime from) {

        return dashboardService.getDashboardStats(from);
    }
}
