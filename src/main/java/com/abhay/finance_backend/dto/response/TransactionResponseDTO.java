package com.abhay.finance_backend.dto.response;

import com.abhay.finance_backend.enums.TransactionType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TransactionResponseDTO {
    private Long id;
    private BigDecimal amount;
    private TransactionType type;
    private String category;
    private LocalDate transactionDate;
    private String notes;
    private String createdBy; // We will just send the user's name, not the whole User object
}