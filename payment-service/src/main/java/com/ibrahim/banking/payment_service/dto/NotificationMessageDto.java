package com.ibrahim.banking.payment_service.dto;

import com.ibrahim.banking.payment_service.model.TransactionStatus;
import com.ibrahim.banking.payment_service.model.TransactionType;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

public class NotificationMessageDto implements Serializable {
    private Long userId;
    private String transactionReference;
    private TransactionType transactionType;
    private TransactionStatus transactionStatus;
    private BigDecimal amount;
    private String currency;
    private Instant timestamp;
    private String message;
    
    // Constructors
    public NotificationMessageDto() {
    }
    
    public NotificationMessageDto(Long userId, String transactionReference, TransactionType transactionType,
                                 TransactionStatus transactionStatus, BigDecimal amount, String currency,
                                 Instant timestamp, String message) {
        this.userId = userId;
        this.transactionReference = transactionReference;
        this.transactionType = transactionType;
        this.transactionStatus = transactionStatus;
        this.amount = amount;
        this.currency = currency;
        this.timestamp = timestamp;
        this.message = message;
    }
    
    // Getters and setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public TransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(TransactionStatus transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
} 