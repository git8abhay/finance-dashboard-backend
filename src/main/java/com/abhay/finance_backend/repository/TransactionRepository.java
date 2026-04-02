package com.abhay.finance_backend.repository;

import com.abhay.finance_backend.entity.Transaction;
import com.abhay.finance_backend.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // Only fetch records that haven't been soft-deleted
    List<Transaction> findByIsDeletedFalse();

    // For when users want to see only their own transactions
    List<Transaction> findByUserIdAndIsDeletedFalse(Long userId);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.type = :type AND t.isDeleted = false")
    BigDecimal sumAmountByType(@Param("type") TransactionType type);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.isDeleted = false")
    long countActiveTransactions();
}