package com.ibrahim.banking.payment_service.dto;

import com.ibrahim.banking.payment_service.model.Transaction;
import com.ibrahim.banking.payment_service.model.TransactionStatus;
import com.ibrahim.banking.payment_service.model.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public class TransactionDetailsDto {
    private Long id;
    private String transactionReference;
    private TransactionType type;
    private BigDecimal amount;
    private Long sourceAccountId;
    private Long destinationAccountId;
    private TransactionStatus status;
    private String currency;
    private String description;
    private Instant createdAt;
    private Instant completedAt;
    private Long userId;
    private String failureReason;

    // Constructors
    public TransactionDetailsDto() {
    }

    public TransactionDetailsDto(Transaction transaction) {
        this.id = transaction.getId();
        this.transactionReference = transaction.getTransactionReference();
        this.type = transaction.getType();
        this.amount = transaction.getAmount();
        this.sourceAccountId = transaction.getSourceAccountId();
        this.destinationAccountId = transaction.getDestinationAccountId();
        this.status = transaction.getStatus();
        this.currency = transaction.getCurrency();
        this.description = transaction.getDescription();
        this.createdAt = transaction.getCreatedAt();
        this.completedAt = transaction.getCompletedAt();
        this.userId = transaction.getUserId();
        this.failureReason = transaction.getFailureReason();
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Long getSourceAccountId() {
        return sourceAccountId;
    }

    public void setSourceAccountId(Long sourceAccountId) {
        this.sourceAccountId = sourceAccountId;
    }

    public Long getDestinationAccountId() {
        return destinationAccountId;
    }

    public void setDestinationAccountId(Long destinationAccountId) {
        this.destinationAccountId = destinationAccountId;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
} 