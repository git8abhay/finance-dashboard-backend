package com.abhay.finance_backend.service.impl;

import com.abhay.finance_backend.dto.response.DashboardSummaryDTO;
import com.abhay.finance_backend.enums.TransactionType;
import com.abhay.finance_backend.repository.TransactionRepository;
import com.abhay.finance_backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final TransactionRepository transactionRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "dashboardSummary")
    public DashboardSummaryDTO getSystemSummary() {
        BigDecimal totalIncome = transactionRepository.sumAmountByType(TransactionType.INCOME);
        BigDecimal totalExpenses = transactionRepository.sumAmountByType(TransactionType.EXPENSE);
        long totalTransactions = transactionRepository.countActiveTransactions();

        // Handle nulls in case the database is completely empty
        totalIncome = totalIncome != null ? totalIncome : BigDecimal.ZERO;
        totalExpenses = totalExpenses != null ? totalExpenses : BigDecimal.ZERO;

        BigDecimal netBalance = totalIncome.subtract(totalExpenses);

        // Fetch and map the trends
        List<Object[]> categoryData = transactionRepository.getExpenseTrendsByCategory();
        Map<String, BigDecimal> expensesByCategory = new HashMap<>();

        for (Object[] row : categoryData) {
            String category = (String) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            expensesByCategory.put(category, amount);
        }

        return DashboardSummaryDTO.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netBalance(netBalance)
                .totalTransactions(totalTransactions)
                .expensesByCategory(expensesByCategory) // Added to the response
                .build();
    }
}