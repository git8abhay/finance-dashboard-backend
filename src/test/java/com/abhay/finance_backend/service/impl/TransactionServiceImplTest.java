package com.abhay.finance_backend.service.impl;

import com.abhay.finance_backend.dto.request.TransactionRequestDTO;
import com.abhay.finance_backend.dto.response.TransactionResponseDTO;
import com.abhay.finance_backend.entity.Transaction;
import com.abhay.finance_backend.entity.User;
import com.abhay.finance_backend.enums.Role;
import com.abhay.finance_backend.enums.TransactionType;
import com.abhay.finance_backend.exception.ResourceNotFoundException;
import com.abhay.finance_backend.repository.TransactionRepository;
import com.abhay.finance_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Tells JUnit to use Mockito
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService; // Injects the mocks above into our real service

    private User testAdmin;
    private TransactionRequestDTO testRequest;

    @BeforeEach
    void setUp() {
        // This runs before EVERY test. We set up dummy data here.
        testAdmin = User.builder()
                .id(1L)
                .name("Admin User")
                .email("admin@test.com")
                .password("hashed_password")
                .role(Role.ROLE_ADMIN)
                .isActive(true)
                .build();

        testRequest = new TransactionRequestDTO();
        testRequest.setAmount(new BigDecimal("500.00"));
        testRequest.setType(TransactionType.INCOME);
        testRequest.setCategory("Salary");
        testRequest.setTransactionDate(LocalDate.now());
        testRequest.setNotes("Monthly Salary");
    }

    @Test
    void createTransaction_Success() {
        // GIVEN: When the service looks for a user, return our dummy admin.
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(testAdmin));

        // When the service tries to save the transaction, return a saved version with an ID.
        Transaction savedTransaction = Transaction.builder()
                .id(100L)
                .user(testAdmin)
                .amount(testRequest.getAmount())
                .type(testRequest.getType())
                .category(testRequest.getCategory())
                .transactionDate(testRequest.getTransactionDate())
                .build();
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

        // WHEN: We actually call the method we are testing
        TransactionResponseDTO response = transactionService.createTransaction(testRequest, "admin@test.com");

        // THEN: We assert (verify) that the method did exactly what we expected
        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(new BigDecimal("500.00"), response.getAmount());
        assertEquals("Admin User", response.getCreatedBy());

        // Verify that the save method was called exactly once
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void createTransaction_UserNotFound_ThrowsException() {
        // GIVEN: The database cannot find the user
        when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

        // WHEN & THEN: Assert that our custom exception is thrown
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            transactionService.createTransaction(testRequest, "ghost@test.com");
        });

        assertEquals("User not found with email: ghost@test.com", exception.getMessage());

        // Verify that save was NEVER called because it crashed first
        verify(transactionRepository, never()).save(any(Transaction.class));
    }
}