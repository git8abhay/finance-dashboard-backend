package com.abhay.finance_backend.service;

import com.abhay.finance_backend.dto.response.DashboardSummaryDTO;

public interface DashboardService {
    DashboardSummaryDTO getSystemSummary();
}