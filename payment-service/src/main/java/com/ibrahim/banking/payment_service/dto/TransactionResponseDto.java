package com.ibrahim.banking.payment_service.dto;

import com.ibrahim.banking.payment_service.model.TransactionStatus;

public class TransactionResponseDto {
    private String message;
    private String transactionReference;
    private TransactionStatus status;

    // Constructors
    public TransactionResponseDto() {
    }

    public TransactionResponseDto(String message, String transactionReference, TransactionStatus status) {
        this.message = message;
        this.transactionReference = transactionReference;
        this.status = status;
    }

    // Getters and setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }
} 