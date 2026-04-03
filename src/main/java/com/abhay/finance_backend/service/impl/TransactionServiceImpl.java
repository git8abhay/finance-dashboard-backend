package com.abhay.finance_backend.service.impl;

import com.abhay.finance_backend.dto.request.TransactionRequestDTO;
import com.abhay.finance_backend.dto.response.TransactionResponseDTO;
import com.abhay.finance_backend.entity.Transaction;
import com.abhay.finance_backend.entity.User;
import com.abhay.finance_backend.enums.TransactionType;
import com.abhay.finance_backend.exception.ResourceNotFoundException;
import com.abhay.finance_backend.repository.TransactionRepository;
import com.abhay.finance_backend.repository.UserRepository;
import com.abhay.finance_backend.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j // Enables the logging object
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    @CacheEvict(value = "dashboardSummary", allEntries = true)
    public TransactionResponseDTO createTransaction(TransactionRequestDTO request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Transaction transaction = Transaction.builder()
                .amount(request.getAmount())
                .type(request.getType())
                .category(request.getCategory())
                .transactionDate(request.getTransactionDate())
                .notes(request.getNotes())
                .user(user)
                .isDeleted(false)
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        // LOG
        log.info("New transaction created! ID: {}, Type: {}, Amount: {}, By User: {}",
                savedTransaction.getId(), savedTransaction.getType(), savedTransaction.getAmount(), userEmail);

        return mapToResponseDTO(savedTransaction);
    }

    @Override
    @Transactional
    @CacheEvict(value = "dashboardSummary", allEntries = true)
    public TransactionResponseDTO updateTransaction(Long id, TransactionRequestDTO request) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + id));

        // Update the fields
        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType());
        transaction.setCategory(request.getCategory());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setNotes(request.getNotes());

        Transaction updatedTransaction = transactionRepository.save(transaction);

        // LOG
        log.info("Transaction updated! ID: {}", updatedTransaction.getId());

        return mapToResponseDTO(updatedTransaction);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponseDTO> getAllTransactions(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return transactionRepository.findByIsDeletedFalse(pageable).map(this::mapToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponseDTO> getMyTransactions(String userEmail, int page, int size, String sortBy, String sortDir) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return transactionRepository.findByUserIdAndIsDeletedFalse(user.getId(), pageable).map(this::mapToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponseDTO> filterTransactions(
            TransactionType type, String category, LocalDate startDate, LocalDate endDate,
            int page, int size, String sortBy, String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return transactionRepository.filterTransactions(type, category, startDate, endDate, pageable).map(this::mapToResponseDTO);
    }

    @Override
    @Transactional
    @CacheEvict(value = "dashboardSummary", allEntries = true)
    public void deleteTransaction(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + id));

        transaction.setDeleted(true);
        transactionRepository.save(transaction);

        // LOG IT!
        log.info("Transaction ID: {} was soft-deleted", id);
    }

    private TransactionResponseDTO mapToResponseDTO(Transaction transaction) {
        TransactionResponseDTO dto = new TransactionResponseDTO();
        dto.setId(transaction.getId());
        dto.setAmount(transaction.getAmount());
        dto.setType(transaction.getType());
        dto.setCategory(transaction.getCategory());
        dto.setTransactionDate(transaction.getTransactionDate());
        dto.setNotes(transaction.getNotes());
        dto.setCreatedBy(transaction.getUser().getName());
        return dto;
    }
}