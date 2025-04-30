package com.ibrahim.banking.payment_service.controller;

import com.ibrahim.banking.payment_service.dto.*;
import com.ibrahim.banking.payment_service.model.Transaction;
import com.ibrahim.banking.payment_service.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Transactions", description = "API for managing financial transactions")
@SecurityRequirement(name = "Bearer Authentication")
public class TransactionController {
    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);
    
    private final TransactionService transactionService;
    
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
    
    @Operation(summary = "Create deposit transaction", description = "Creates a new deposit transaction for an account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Deposit transaction created successfully", 
                     content = @Content(schema = @Schema(implementation = TransactionResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data", 
                     content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Server error", 
                     content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
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
    
    @Operation(summary = "Create withdrawal transaction", description = "Creates a new withdrawal transaction from an account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Withdrawal transaction created successfully", 
                     content = @Content(schema = @Schema(implementation = TransactionResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data", 
                     content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Server error", 
                     content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
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
    
    @Operation(summary = "Create transfer transaction", description = "Creates a new transfer transaction between accounts")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Transfer transaction created successfully", 
                     content = @Content(schema = @Schema(implementation = TransactionResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data", 
                     content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Server error", 
                     content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
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
    
    @Operation(summary = "Get transaction by reference", description = "Retrieves a specific transaction by its reference")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved transaction", 
                     content = @Content(schema = @Schema(implementation = TransactionDetailsDto.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Transaction not found", 
                     content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
        @ApiResponse(responseCode = "500", description = "Server error", 
                     content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping("/{transactionReference}")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<?> getTransactionByReference(
            @Parameter(description = "Reference of the transaction to retrieve") @PathVariable String transactionReference) {
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
    
    @Operation(summary = "Get transactions by user ID", description = "Retrieves all transactions for a specific user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved transactions", 
                     content = @Content(array = @ArraySchema(schema = @Schema(implementation = TransactionDetailsDto.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Server error", 
                     content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<?> getTransactionsByUserId(
            @Parameter(description = "ID of the user") @PathVariable Long userId) {
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
    
    @Operation(summary = "Get transactions by account ID", description = "Retrieves all transactions for a specific account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved transactions", 
                     content = @Content(array = @ArraySchema(schema = @Schema(implementation = TransactionDetailsDto.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Server error", 
                     content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping("/account/{accountId}")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<?> getTransactionsByAccountId(
            @Parameter(description = "ID of the account") @PathVariable Long accountId) {
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