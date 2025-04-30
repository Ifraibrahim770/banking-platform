package com.ibrahim.banking.payment_service.exception;

public class InvalidTransactionException extends RuntimeException {
    
    private String transactionId;
    private String reason;
    
    public InvalidTransactionException(String message) {
        super(message);
    }
    
    public InvalidTransactionException(String message, String transactionId) {
        super(message);
        this.transactionId = transactionId;
    }
    
    public InvalidTransactionException(String message, String transactionId, String reason) {
        super(message);
        this.transactionId = transactionId;
        this.reason = reason;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public String getReason() {
        return reason;
    }
} 