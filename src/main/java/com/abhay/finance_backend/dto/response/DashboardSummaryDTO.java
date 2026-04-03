package com.abhay.finance_backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
public class DashboardSummaryDTO {
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netBalance;
    private long totalTransactions;

    // NEW: Holds the trend data (e.g., "Groceries": 450.00, "Rent": 1200.00)
    private Map<String, BigDecimal> expensesByCategory;
}