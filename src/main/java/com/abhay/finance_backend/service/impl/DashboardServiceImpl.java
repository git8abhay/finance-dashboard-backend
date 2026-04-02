package com.abhay.finance_backend.service.impl;

import com.abhay.finance_backend.dto.response.DashboardSummaryDTO;
import com.abhay.finance_backend.enums.TransactionType;
import com.abhay.finance_backend.repository.TransactionRepository;
import com.abhay.finance_backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final TransactionRepository transactionRepository;

    @Override
    @Transactional(readOnly = true) // readOnly = true optimizes database performance for GET requests
    public DashboardSummaryDTO getSystemSummary() {
        BigDecimal totalIncome = transactionRepository.sumAmountByType(TransactionType.INCOME);
        BigDecimal totalExpenses = transactionRepository.sumAmountByType(TransactionType.EXPENSE);
        long totalTransactions = transactionRepository.countActiveTransactions();

        BigDecimal netBalance = totalIncome.subtract(totalExpenses);

        return DashboardSummaryDTO.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netBalance(netBalance)
                .totalTransactions(totalTransactions)
                .build();
    }
}