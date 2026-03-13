package com.example.ledgerpro.controller;

import com.example.ledgerpro.dto.DashboardResponse;
import com.example.ledgerpro.service.DashboardService;
import com.example.ledgerpro.service.RecurringService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final RecurringService recurringService;

    public DashboardController(DashboardService dashboardService,
                               RecurringService recurringService) {
        this.dashboardService = dashboardService;
        this.recurringService = recurringService;
    }

    @GetMapping
    public DashboardResponse dashboard(@RequestParam(required = false) String period) {
        recurringService.processDueRules();
        return dashboardService.getDashboard(period);
    }
}
