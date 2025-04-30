package com.ibrahim.banking.payment_service.exception;

public class ConcurrentTransactionException extends RuntimeException {
    
    public ConcurrentTransactionException(String message) {
        super(message);
    }
    
    public ConcurrentTransactionException(Long accountId) {
        super("A similar transaction for account " + accountId + " is already in progress. Please try again later.");
    }
} 