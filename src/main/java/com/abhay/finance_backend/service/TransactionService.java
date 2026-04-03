package com.abhay.finance_backend.service;

import com.abhay.finance_backend.dto.request.TransactionRequestDTO;
import com.abhay.finance_backend.dto.response.TransactionResponseDTO;
import org.springframework.data.domain.Page;

public interface TransactionService {
    TransactionResponseDTO createTransaction(TransactionRequestDTO request, String userEmail);
    TransactionResponseDTO updateTransaction(Long id, TransactionRequestDTO request);
    void deleteTransaction(Long id);

    // Upgraded methods
    Page<TransactionResponseDTO> getAllTransactions(int page, int size, String sortBy, String sortDir);
    Page<TransactionResponseDTO> getMyTransactions(String userEmail, int page, int size, String sortBy, String sortDir);
    Page<TransactionResponseDTO> filterTransactions(com.abhay.finance_backend.enums.TransactionType type, String category, java.time.LocalDate startDate, java.time.LocalDate endDate, int page, int size, String sortBy, String sortDir);
}