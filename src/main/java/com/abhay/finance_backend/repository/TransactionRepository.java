package com.abhay.finance_backend.repository;

import com.abhay.finance_backend.entity.Transaction;
import com.abhay.finance_backend.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Upgraded to Page
    Page<Transaction> findByIsDeletedFalse(Pageable pageable);

    // Upgraded to Page
    Page<Transaction> findByUserIdAndIsDeletedFalse(Long userId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.type = :type AND t.isDeleted = false")
    BigDecimal sumAmountByType(@Param("type") TransactionType type);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.isDeleted = false")
    long countActiveTransactions();

    // Upgraded the filter to handle Pagination and Sorting dynamically
    @Query("SELECT t FROM Transaction t WHERE t.isDeleted = false " +
            "AND (:type IS NULL OR t.type = :type) " +
            "AND (:category IS NULL OR t.category = :category) " +
            "AND (cast(:startDate as date) IS NULL OR t.transactionDate >= :startDate) " +
            "AND (cast(:endDate as date) IS NULL OR t.transactionDate <= :endDate)")
    Page<Transaction> filterTransactions(
            @Param("type") TransactionType type,
            @Param("category") String category,
            @Param("startDate") java.time.LocalDate startDate,
            @Param("endDate") java.time.LocalDate endDate,
            Pageable pageable);

    // NEW: Aggregation query for dashboard trends
    @Query("SELECT t.category, SUM(t.amount) FROM Transaction t WHERE t.type = 'EXPENSE' AND t.isDeleted = false GROUP BY t.category")
    java.util.List<Object[]> getExpenseTrendsByCategory();
}