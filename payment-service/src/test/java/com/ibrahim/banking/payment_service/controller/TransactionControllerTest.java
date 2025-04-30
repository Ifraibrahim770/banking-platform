package com.ibrahim.banking.payment_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibrahim.banking.payment_service.dto.*;
import com.ibrahim.banking.payment_service.exception.ConcurrentTransactionException;
import com.ibrahim.banking.payment_service.exception.GlobalExceptionHandler;
import com.ibrahim.banking.payment_service.exception.ResourceNotFoundException;
import com.ibrahim.banking.payment_service.model.Transaction;
import com.ibrahim.banking.payment_service.model.TransactionStatus;
import com.ibrahim.banking.payment_service.model.TransactionType;
import com.ibrahim.banking.payment_service.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Include the GlobalExceptionHandler in the MockMvc setup
        mockMvc = MockMvcBuilders
                .standaloneSetup(transactionController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void createDepositTransaction_ValidRequest_ReturnsCreated() throws Exception {
        // Arrange
        DepositRequestDto requestDto = new DepositRequestDto();
        requestDto.setAccountId(1L);
        requestDto.setAmount(new BigDecimal("100.00"));
        requestDto.setCurrency("USD");
        requestDto.setDescription("Test deposit");
        requestDto.setUserId(1L);

        Transaction mockTransaction = createTransaction("TXN-12345678", TransactionType.DEPOSIT);
        
        when(transactionService.createDepositTransaction(
                eq(requestDto.getAccountId()),
                eq(requestDto.getAmount()),
                eq(requestDto.getCurrency()),
                eq(requestDto.getDescription()),
                eq(requestDto.getUserId())
        )).thenReturn(mockTransaction);

        // Act & Assert
        mockMvc.perform(post("/api/transactions/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message", is("Deposit transaction received for processing")))
                .andExpect(jsonPath("$.transactionReference", is(mockTransaction.getTransactionReference())))
                .andExpect(jsonPath("$.status", is(mockTransaction.getStatus().toString())));
    }

    @Test
    void createDepositTransaction_ZeroAmount_ReturnsBadRequest() throws Exception {
        // Arrange
        DepositRequestDto requestDto = new DepositRequestDto();
        requestDto.setAccountId(1L);
        requestDto.setAmount(new BigDecimal("0.00"));
        requestDto.setCurrency("USD");
        requestDto.setDescription("Test deposit");
        requestDto.setUserId(1L);

        // Act & Assert
        mockMvc.perform(post("/api/transactions/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createDepositTransaction_ConcurrentTransaction_ReturnsConflict() throws Exception {
        // Arrange
        DepositRequestDto requestDto = new DepositRequestDto();
        requestDto.setAccountId(1L);
        requestDto.setAmount(new BigDecimal("100.00"));
        requestDto.setCurrency("USD");
        requestDto.setDescription("Test deposit");
        requestDto.setUserId(1L);

        when(transactionService.createDepositTransaction(
                eq(requestDto.getAccountId()),
                eq(requestDto.getAmount()),
                eq(requestDto.getCurrency()),
                eq(requestDto.getDescription()),
                eq(requestDto.getUserId())
        )).thenThrow(new ConcurrentTransactionException(requestDto.getAccountId()));

        // Act & Assert
        mockMvc.perform(post("/api/transactions/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict());
    }

    @Test
    void createWithdrawalTransaction_ValidRequest_ReturnsCreated() throws Exception {
        // Arrange
        WithdrawalRequestDto requestDto = new WithdrawalRequestDto();
        requestDto.setAccountId(1L);
        requestDto.setAmount(new BigDecimal("100.00"));
        requestDto.setCurrency("USD");
        requestDto.setDescription("Test withdrawal");
        requestDto.setUserId(1L);

        Transaction mockTransaction = createTransaction("TXN-12345678", TransactionType.WITHDRAWAL);
        
        when(transactionService.createWithdrawalTransaction(
                eq(requestDto.getAccountId()),
                eq(requestDto.getAmount()),
                eq(requestDto.getCurrency()),
                eq(requestDto.getDescription()),
                eq(requestDto.getUserId())
        )).thenReturn(mockTransaction);

        // Act & Assert
        mockMvc.perform(post("/api/transactions/withdraw")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message", is("Withdrawal transaction received for processing")))
                .andExpect(jsonPath("$.transactionReference", is(mockTransaction.getTransactionReference())))
                .andExpect(jsonPath("$.status", is(mockTransaction.getStatus().toString())));
    }

    @Test
    void createTransferTransaction_ValidRequest_ReturnsCreated() throws Exception {
        // Arrange
        TransferRequestDto requestDto = new TransferRequestDto();
        requestDto.setSourceAccountId(1L);
        requestDto.setDestinationAccountId(2L);
        requestDto.setAmount(new BigDecimal("200.00"));
        requestDto.setCurrency("USD");
        requestDto.setDescription("Test transfer");
        requestDto.setUserId(1L);

        Transaction mockTransaction = createTransaction("TXN-12345678", TransactionType.TRANSFER);
        mockTransaction.setDestinationAccountId(2L);
        
        when(transactionService.createTransferTransaction(
                eq(requestDto.getSourceAccountId()),
                eq(requestDto.getDestinationAccountId()),
                eq(requestDto.getAmount()),
                eq(requestDto.getCurrency()),
                eq(requestDto.getDescription()),
                eq(requestDto.getUserId())
        )).thenReturn(mockTransaction);

        // Act & Assert
        mockMvc.perform(post("/api/transactions/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message", is("Transfer transaction received for processing")))
                .andExpect(jsonPath("$.transactionReference", is(mockTransaction.getTransactionReference())))
                .andExpect(jsonPath("$.status", is(mockTransaction.getStatus().toString())));
    }

    @Test
    void createTransferTransaction_SameSourceAndDestination_ReturnsBadRequest() throws Exception {
        // Arrange
        TransferRequestDto requestDto = new TransferRequestDto();
        requestDto.setSourceAccountId(1L);
        requestDto.setDestinationAccountId(1L); // Same account
        requestDto.setAmount(new BigDecimal("200.00"));
        requestDto.setCurrency("USD");
        requestDto.setDescription("Test transfer");
        requestDto.setUserId(1L);

        // Act & Assert
        mockMvc.perform(post("/api/transactions/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTransactionByReference_ExistingTransaction_ReturnsTransaction() throws Exception {
        // Arrange
        String reference = "TXN-12345678";
        Transaction mockTransaction = createTransaction(reference, TransactionType.DEPOSIT);
        
        when(transactionService.getTransactionByReference(reference)).thenReturn(Optional.of(mockTransaction));

        // Act & Assert
        mockMvc.perform(get("/api/transactions/{transactionReference}", reference))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionReference", is(reference)))
                .andExpect(jsonPath("$.type", is(mockTransaction.getType().toString())))
                .andExpect(jsonPath("$.status", is(mockTransaction.getStatus().toString())));
    }

    @Test
    void getTransactionByReference_NonExistingTransaction_ReturnsNotFound() throws Exception {
        // Arrange
        String reference = "TXN-NONEXISTENT";
        
        when(transactionService.getTransactionByReference(reference)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/transactions/{transactionReference}", reference))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTransactionsByUserId_ReturnsTransactionsList() throws Exception {
        // Arrange
        Long userId = 1L;
        Transaction transaction1 = createTransaction("TXN-12345678", TransactionType.DEPOSIT);
        Transaction transaction2 = createTransaction("TXN-87654321", TransactionType.WITHDRAWAL);
        List<Transaction> transactions = Arrays.asList(transaction1, transaction2);
        
        when(transactionService.getTransactionsByUserId(userId)).thenReturn(transactions);

        // Act & Assert
        mockMvc.perform(get("/api/transactions/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].transactionReference", is(transaction1.getTransactionReference())))
                .andExpect(jsonPath("$[1].transactionReference", is(transaction2.getTransactionReference())));
    }

    @Test
    void getTransactionsByAccountId_ReturnsTransactionsList() throws Exception {
        // Arrange
        Long accountId = 1L;
        Transaction transaction1 = createTransaction("TXN-12345678", TransactionType.DEPOSIT);
        Transaction transaction2 = createTransaction("TXN-87654321", TransactionType.WITHDRAWAL);
        List<Transaction> transactions = Arrays.asList(transaction1, transaction2);
        
        when(transactionService.getTransactionsByAccountId(accountId)).thenReturn(transactions);

        // Act & Assert
        mockMvc.perform(get("/api/transactions/account/{accountId}", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].transactionReference", is(transaction1.getTransactionReference())))
                .andExpect(jsonPath("$[1].transactionReference", is(transaction2.getTransactionReference())));
    }

    private Transaction createTransaction(String reference, TransactionType type) {
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setTransactionReference(reference);
        transaction.setType(type);
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setSourceAccountId(1L);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setCurrency("USD");
        transaction.setDescription("Test transaction");
        transaction.setCreatedAt(Instant.now());
        transaction.setUserId(1L);
        return transaction;
    }
} 