package com.ibrahim.banking.payment_service.service;

import com.ibrahim.banking.payment_service.model.TransactionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class TransactionLoggingService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionLoggingService.class);

    public void logTransaction(String transactionId, BigDecimal amount, String fromAccount, 
                              String toAccount, TransactionStatus status) {
        logger.info("Transaction [{}]: Amount={}, From={}, To={}, Status={}", 
                    transactionId, amount, fromAccount, toAccount, status);
        
        if (status == TransactionStatus.FAILED) {
            logger.warn("Transaction [{}] failed", transactionId);
        } else if (status == TransactionStatus.REVERSED) {
            logger.warn("Transaction [{}] was reversed", transactionId);
        }
    }
    
    public void logTransactionCreated(String transactionId, BigDecimal amount) {
        logger.debug("New transaction initiated [{}] with amount {}", transactionId, amount);
    }
    
    public void logTransactionCompleted(String transactionId) {
        logger.info("Transaction [{}] completed successfully", transactionId);
    }
    
    public void logTransactionError(String transactionId, Exception e) {
        logger.error("Transaction [{}] failed with error: {}", transactionId, e.getMessage(), e);
    }
    
    public void logPerformanceMetrics(String operation, long startTime) {
        long executionTime = System.currentTimeMillis() - startTime;
        logger.debug("Operation [{}] completed in {} ms", operation, executionTime);
    }
    
    // Example method that would be called in your application code
    public void simulateTransaction() {
        String transactionId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();
        
        try {
            // Log at the beginning of transaction
            logTransactionCreated(transactionId, new BigDecimal("100.00"));
            
            // Business logic would go here
            // ...
            
            // Log successful transaction
            logTransaction(transactionId, new BigDecimal("100.00"), 
                          "account123", "account456", TransactionStatus.COMPLETED);
            logTransactionCompleted(transactionId);
        } catch (Exception e) {
            // Log error
            logTransactionError(transactionId, e);
        } finally {
            // Log performance
            logPerformanceMetrics("Payment transaction", startTime);
        }
    }
} 