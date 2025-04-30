package com.ibrahim.banking.payment_service.controller;

import com.ibrahim.banking.payment_service.exception.InvalidTransactionException;
import com.ibrahim.banking.payment_service.exception.PaymentProcessingException;
import com.ibrahim.banking.payment_service.exception.ResourceNotFoundException;
import com.ibrahim.banking.payment_service.service.TransactionLoggingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    
    @Autowired
    private TransactionLoggingService transactionLoggingService;
    
    @PostMapping("/process")
    public ResponseEntity<Map<String, Object>> processPayment(
            @RequestParam String fromAccount,
            @RequestParam String toAccount,
            @RequestParam BigDecimal amount) {
        
        logger.info("Payment request received: from={}, to={}, amount={}", fromAccount, toAccount, amount);
        
        // Example validation logic
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("Invalid payment amount: {}", amount);
            throw new InvalidTransactionException("Payment amount must be greater than zero", null, "INVALID_AMOUNT");
        }
        
        String transactionId = UUID.randomUUID().toString();
        
        try {
            // Simulate transaction processing
            transactionLoggingService.simulateTransaction();
            
            // Return success response
            Map<String, Object> response = new HashMap<>();
            response.put("transactionId", transactionId);
            response.put("status", "COMPLETED");
            response.put("message", "Payment processed successfully");
            
            logger.info("Payment processed successfully: {}", transactionId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error processing payment", e);
            throw new PaymentProcessingException("Error processing payment", e, "PAYMENT_FAILED");
        }
    }
    
    @GetMapping("/{transactionId}")
    public ResponseEntity<Map<String, Object>> getPaymentDetails(@PathVariable String transactionId) {
        logger.info("Fetching details for transaction: {}", transactionId);
        
        // Simulate looking up a transaction
        if ("invalid-id".equals(transactionId)) {
            logger.warn("Invalid transaction ID format: {}", transactionId);
            throw new InvalidTransactionException("Invalid transaction ID format", transactionId);
        }
        
        if ("not-found".equals(transactionId)) {
            logger.error("Transaction not found: {}", transactionId);
            throw new ResourceNotFoundException("Transaction", transactionId);
        }
        
        // Return mock response
        Map<String, Object> response = new HashMap<>();
        response.put("transactionId", transactionId);
        response.put("fromAccount", "account123");
        response.put("toAccount", "account456");
        response.put("amount", new BigDecimal("100.00"));
        response.put("status", "COMPLETED");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
} 