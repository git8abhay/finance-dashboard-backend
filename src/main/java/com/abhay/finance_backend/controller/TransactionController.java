package com.abhay.finance_backend.controller;

import com.abhay.finance_backend.dto.request.TransactionRequestDTO;
import com.abhay.finance_backend.dto.response.TransactionResponseDTO;
import com.abhay.finance_backend.enums.TransactionType;
import com.abhay.finance_backend.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    // Only Admins and Analysts can create records
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ANALYST')")
    public ResponseEntity<TransactionResponseDTO> createTransaction(
            @Valid @RequestBody TransactionRequestDTO request,
            Authentication authentication) {

        // authentication.getName() securely grabs the email of the logged-in user from the JWT token
        String userEmail = authentication.getName();
        TransactionResponseDTO createdTransaction = transactionService.createTransaction(request, userEmail);
        return new ResponseEntity<>(createdTransaction, HttpStatus.CREATED);
    }

    // Admins and Analysts can see ALL records (Paginated & Sorted)
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ANALYST')")
    public ResponseEntity<Page<TransactionResponseDTO>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "transactionDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(transactionService.getAllTransactions(page, size, sortBy, sortDir));
    }

    // Any authenticated user can see their OWN records (Paginated & Sorted)
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<TransactionResponseDTO>> getMyTransactions(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "transactionDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(transactionService.getMyTransactions(authentication.getName(), page, size, sortBy, sortDir));
    }

    // ONLY Admins can delete records
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build(); // Returns a clean 204 No Content
    }

    // UPDATE Endpoint
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ANALYST')")
    public ResponseEntity<TransactionResponseDTO> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequestDTO request) {
        return ResponseEntity.ok(transactionService.updateTransaction(id, request));
    }

    // FILTER Endpoint (Paginated & Sorted)
    @GetMapping("/filter")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ANALYST')")
    public ResponseEntity<Page<TransactionResponseDTO>> filterTransactions(
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "transactionDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        return ResponseEntity.ok(transactionService.filterTransactions(type, category, startDate, endDate, page, size, sortBy, sortDir));
    }
}