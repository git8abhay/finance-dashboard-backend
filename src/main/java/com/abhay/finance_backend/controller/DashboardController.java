package com.abhay.finance_backend.controller;

import com.abhay.finance_backend.dto.response.DashboardSummaryDTO;
import com.abhay.finance_backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    // Viewers, Analysts, and Admins can all see the dashboard summary
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ROLE_VIEWER', 'ROLE_ANALYST', 'ROLE_ADMIN')")
    public ResponseEntity<DashboardSummaryDTO> getSummary() {
        return ResponseEntity.ok(dashboardService.getSystemSummary());
    }
}