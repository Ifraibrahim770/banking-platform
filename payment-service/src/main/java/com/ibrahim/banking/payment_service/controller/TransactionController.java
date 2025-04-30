package com.ibrahim.banking.payment_service.controller;

import com.ibrahim.banking.payment_service.dto.*;
import com.ibrahim.banking.payment_service.model.Transaction;
import com.ibrahim.banking.payment_service.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);
    
    private final TransactionService transactionService;
    
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
    
    @PostMapping("/deposit")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<?> createDepositTransaction(@RequestBody DepositRequestDto request) {
        try {
            // Validate amount
            if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponseDto("Amount must be greater than zero"));
            }
            
            Transaction transaction = transactionService.createDepositTransaction(
                    request.getAccountId(), 
                    request.getAmount(), 
                    request.getCurrency(), 
                    request.getDescription(), 
                    request.getUserId());
            
            TransactionResponseDto response = new TransactionResponseDto(
                    "Deposit transaction received for processing",
                    transaction.getTransactionReference(),
                    transaction.getStatus()
            );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Bad request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ErrorResponseDto(e.getMessage()));
            
        } catch (Exception e) {
            logger.error("Error creating deposit transaction", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseDto("Failed to process deposit transaction"));
        }
    }
    
    @PostMapping("/withdrawal")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<?> createWithdrawalTransaction(@RequestBody WithdrawalRequestDto request) {
        try {
            // Validate amount
            if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponseDto("Amount must be greater than zero"));
            }
            
            Transaction transaction = transactionService.createWithdrawalTransaction(
                    request.getAccountId(), 
                    request.getAmount(), 
                    request.getCurrency(), 
                    request.getDescription(), 
                    request.getUserId());
            
            TransactionResponseDto response = new TransactionResponseDto(
                    "Withdrawal transaction received for processing",
                    transaction.getTransactionReference(),
                    transaction.getStatus()
            );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Bad request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ErrorResponseDto(e.getMessage()));
            
        } catch (Exception e) {
            logger.error("Error creating withdrawal transaction", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseDto("Failed to process withdrawal transaction"));
        }
    }
    
    @PostMapping("/transfer")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<?> createTransferTransaction(@RequestBody TransferRequestDto request) {
        try {
            // Validate amount
            if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponseDto("Amount must be greater than zero"));
            }
            
            // Validate accounts are different
            if (request.getSourceAccountId().equals(request.getDestinationAccountId())) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponseDto("Source and destination accounts must be different"));
            }
            
            Transaction transaction = transactionService.createTransferTransaction(
                    request.getSourceAccountId(), 
                    request.getDestinationAccountId(), 
                    request.getAmount(), 
                    request.getCurrency(), 
                    request.getDescription(), 
                    request.getUserId());
            
            TransactionResponseDto response = new TransactionResponseDto(
                    "Transfer transaction received for processing",
                    transaction.getTransactionReference(),
                    transaction.getStatus()
            );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Bad request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ErrorResponseDto(e.getMessage()));
            
        } catch (Exception e) {
            logger.error("Error creating transfer transaction", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseDto("Failed to process transfer transaction"));
        }
    }
    
    @GetMapping("/{transactionReference}")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<?> getTransactionByReference(@PathVariable String transactionReference) {
        try {
            Optional<Transaction> optionalTransaction = transactionService.getTransactionByReference(transactionReference);
            
            if (optionalTransaction.isPresent()) {
                TransactionDetailsDto transactionDetails = new TransactionDetailsDto(optionalTransaction.get());
                return ResponseEntity.ok(transactionDetails);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponseDto("Transaction not found"));
            }
            
        } catch (Exception e) {
            logger.error("Error retrieving transaction", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseDto("Failed to retrieve transaction"));
        }
    }
    
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<?> getTransactionsByUserId(@PathVariable Long userId) {
        try {
            List<Transaction> transactions = transactionService.getTransactionsByUserId(userId);
            List<TransactionDetailsDto> transactionDetails = transactions.stream()
                    .map(TransactionDetailsDto::new)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(transactionDetails);
            
        } catch (Exception e) {
            logger.error("Error retrieving transactions for user {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseDto("Failed to retrieve transactions"));
        }
    }
    
    @GetMapping("/account/{accountId}")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<?> getTransactionsByAccountId(@PathVariable Long accountId) {
        try {
            List<Transaction> transactions = transactionService.getTransactionsByAccountId(accountId);
            List<TransactionDetailsDto> transactionDetails = transactions.stream()
                    .map(TransactionDetailsDto::new)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(transactionDetails);
            
        } catch (Exception e) {
            logger.error("Error retrieving transactions for account {}", accountId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseDto("Failed to retrieve transactions"));
        }
    }
} 