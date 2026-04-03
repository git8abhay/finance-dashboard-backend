package com.abhay.finance_backend.service.impl;

import com.abhay.finance_backend.dto.response.DashboardSummaryDTO;
import com.abhay.finance_backend.enums.TransactionType;
import com.abhay.finance_backend.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @BeforeEach
    void setUp() {
        // Mockito will automatically initialize the mocks before each test
    }

    @Test
    void getSystemSummary_ReturnsCorrectCalculationsAndTrends() {
        // GIVEN: We train our "fake" repository to return specific numbers
        BigDecimal fakeTotalIncome = new BigDecimal("5000.00");
        BigDecimal fakeTotalExpenses = new BigDecimal("2000.00");

        when(transactionRepository.sumAmountByType(TransactionType.INCOME)).thenReturn(fakeTotalIncome);
        when(transactionRepository.sumAmountByType(TransactionType.EXPENSE)).thenReturn(fakeTotalExpenses);
        when(transactionRepository.countActiveTransactions()).thenReturn(15L);

        // We simulate the raw SQL Object[] array that our custom Query returns for trends
        List<Object[]> fakeTrends = new ArrayList<>();
        fakeTrends.add(new Object[]{"Rent", new BigDecimal("1200.00")});
        fakeTrends.add(new Object[]{"Groceries", new BigDecimal("800.00")});

        when(transactionRepository.getExpenseTrendsByCategory()).thenReturn(fakeTrends);

        // WHEN: We call the actual service method
        DashboardSummaryDTO result = dashboardService.getSystemSummary();

        // THEN: We verify the math and logic in the service is flawless
        assertNotNull(result);
        assertEquals(fakeTotalIncome, result.getTotalIncome());
        assertEquals(fakeTotalExpenses, result.getTotalExpenses());

        // Income (5000) - Expenses (2000) should equal a Net Balance of 3000
        assertEquals(new BigDecimal("3000.00"), result.getNetBalance());
        assertEquals(15L, result.getTotalTransactions());

        // Verify the map conversion worked perfectly
        assertNotNull(result.getExpensesByCategory());
        assertEquals(2, result.getExpensesByCategory().size());
        assertEquals(new BigDecimal("1200.00"), result.getExpensesByCategory().get("Rent"));
        assertEquals(new BigDecimal("800.00"), result.getExpensesByCategory().get("Groceries"));
    }

    @Test
    void getSystemSummary_WithEmptyDatabase_HandlesNullsSafely() {
        // GIVEN: The database is completely empty (returns null for sums)
        when(transactionRepository.sumAmountByType(TransactionType.INCOME)).thenReturn(null);
        when(transactionRepository.sumAmountByType(TransactionType.EXPENSE)).thenReturn(null);
        when(transactionRepository.countActiveTransactions()).thenReturn(0L);
        when(transactionRepository.getExpenseTrendsByCategory()).thenReturn(new ArrayList<>());

        // WHEN: We call the service
        DashboardSummaryDTO result = dashboardService.getSystemSummary();

        // THEN: It shouldn't crash with a NullPointerException, it should return Zeros
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getTotalIncome());
        assertEquals(BigDecimal.ZERO, result.getTotalExpenses());
        assertEquals(BigDecimal.ZERO, result.getNetBalance());
        assertEquals(0L, result.getTotalTransactions());
        assertEquals(0, result.getExpensesByCategory().size());
    }
}