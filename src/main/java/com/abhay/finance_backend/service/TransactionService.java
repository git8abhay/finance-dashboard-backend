package com.abhay.finance_backend.service;

import com.abhay.finance_backend.dto.request.TransactionRequestDTO;
import com.abhay.finance_backend.dto.response.TransactionResponseDTO;

import java.util.List;

public interface TransactionService {
    TransactionResponseDTO createTransaction(TransactionRequestDTO request, String userEmail);
    List<TransactionResponseDTO> getAllTransactions(); // For Analysts/Admins
    List<TransactionResponseDTO> getMyTransactions(String userEmail); // For regular users
    void deleteTransaction(Long id); // Soft delete
}