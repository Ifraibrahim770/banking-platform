package com.ibrahim.banking.payment_service.service;

import com.ibrahim.banking.payment_service.consumer.AccountServiceClient;
import com.ibrahim.banking.payment_service.model.Transaction;
import com.ibrahim.banking.payment_service.model.TransactionStatus;
import com.ibrahim.banking.payment_service.model.TransactionType;
import com.ibrahim.banking.payment_service.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountServiceClient accountServiceClient;

    @Mock
    private TransactionPublisherService publisherService;

    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(transactionRepository, accountServiceClient, publisherService);
    }

    @Test
    void createDepositTransaction_ValidInput_ReturnsCreatedTransaction() {
        // Arrange
        Long accountId = 1L;
        BigDecimal amount = new BigDecimal("100.00");
        String currency = "USD";
        String description = "Test deposit";
        Long userId = 1L;

        when(accountServiceClient.isAccountActive(accountId)).thenReturn(true);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction savedTransaction = invocation.getArgument(0);
            savedTransaction.setId(1L);
            return savedTransaction;
        });

        // Act
        Transaction result = transactionService.createDepositTransaction(accountId, amount, currency, description, userId);

        // Assert
        assertNotNull(result);
        assertEquals(TransactionType.DEPOSIT, result.getType());
        assertEquals(TransactionStatus.PENDING, result.getStatus());
        assertEquals(accountId, result.getSourceAccountId());
        assertEquals(amount, result.getAmount());
        assertEquals(currency, result.getCurrency());
        assertEquals(description, result.getDescription());
        assertEquals(userId, result.getUserId());
        assertNotNull(result.getTransactionReference());
        assertTrue(result.getTransactionReference().startsWith("TXN-"));

        verify(accountServiceClient).isAccountActive(accountId);
        verify(transactionRepository).save(any(Transaction.class));
        verify(publisherService).publishTransaction(any(Transaction.class));
    }

    @Test
    void createDepositTransaction_InactiveAccount_ThrowsIllegalArgumentException() {
        // Arrange
        Long accountId = 1L;
        BigDecimal amount = new BigDecimal("100.00");
        String currency = "USD";
        String description = "Test deposit";
        Long userId = 1L;

        when(accountServiceClient.isAccountActive(accountId)).thenReturn(false);

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.createDepositTransaction(accountId, amount, currency, description, userId);
        });

        assertEquals("Account is not active or does not exist", exception.getMessage());
        verify(accountServiceClient).isAccountActive(accountId);
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(publisherService, never()).publishTransaction(any(Transaction.class));
    }

    @Test
    void createWithdrawalTransaction_ValidInput_ReturnsCreatedTransaction() {
        // Arrange
        Long accountId = 1L;
        BigDecimal amount = new BigDecimal("50.00");
        String currency = "USD";
        String description = "Test withdrawal";
        Long userId = 1L;

        when(accountServiceClient.isAccountActive(accountId)).thenReturn(true);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction savedTransaction = invocation.getArgument(0);
            savedTransaction.setId(1L);
            return savedTransaction;
        });

        // Act
        Transaction result = transactionService.createWithdrawalTransaction(accountId, amount, currency, description, userId);

        // Assert
        assertNotNull(result);
        assertEquals(TransactionType.WITHDRAWAL, result.getType());
        assertEquals(TransactionStatus.PENDING, result.getStatus());
        assertEquals(accountId, result.getSourceAccountId());
        assertEquals(amount, result.getAmount());
        assertEquals(currency, result.getCurrency());
        assertEquals(description, result.getDescription());
        assertEquals(userId, result.getUserId());
        assertNotNull(result.getTransactionReference());
        assertTrue(result.getTransactionReference().startsWith("TXN-"));

        verify(accountServiceClient).isAccountActive(accountId);
        verify(transactionRepository).save(any(Transaction.class));
        verify(publisherService).publishTransaction(any(Transaction.class));
    }

    @Test
    void createTransferTransaction_ValidInput_ReturnsCreatedTransaction() {
        // Arrange
        Long sourceAccountId = 1L;
        Long destinationAccountId = 2L;
        BigDecimal amount = new BigDecimal("200.00");
        String currency = "USD";
        String description = "Test transfer";
        Long userId = 1L;

        when(accountServiceClient.isAccountActive(sourceAccountId)).thenReturn(true);
        when(accountServiceClient.isAccountActive(destinationAccountId)).thenReturn(true);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction savedTransaction = invocation.getArgument(0);
            savedTransaction.setId(1L);
            return savedTransaction;
        });

        // Act
        Transaction result = transactionService.createTransferTransaction(
                sourceAccountId, destinationAccountId, amount, currency, description, userId);

        // Assert
        assertNotNull(result);
        assertEquals(TransactionType.TRANSFER, result.getType());
        assertEquals(TransactionStatus.PENDING, result.getStatus());
        assertEquals(sourceAccountId, result.getSourceAccountId());
        assertEquals(destinationAccountId, result.getDestinationAccountId());
        assertEquals(amount, result.getAmount());
        assertEquals(currency, result.getCurrency());
        assertEquals(description, result.getDescription());
        assertEquals(userId, result.getUserId());
        assertNotNull(result.getTransactionReference());
        assertTrue(result.getTransactionReference().startsWith("TXN-"));

        verify(accountServiceClient).isAccountActive(sourceAccountId);
        verify(accountServiceClient).isAccountActive(destinationAccountId);
        verify(transactionRepository).save(any(Transaction.class));
        verify(publisherService).publishTransaction(any(Transaction.class));
    }

    @Test
    void createTransferTransaction_InactiveSourceAccount_ThrowsIllegalArgumentException() {
        // Arrange
        Long sourceAccountId = 1L;
        Long destinationAccountId = 2L;
        BigDecimal amount = new BigDecimal("200.00");
        String currency = "USD";
        String description = "Test transfer";
        Long userId = 1L;

        when(accountServiceClient.isAccountActive(sourceAccountId)).thenReturn(false);

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.createTransferTransaction(
                    sourceAccountId, destinationAccountId, amount, currency, description, userId);
        });

        assertEquals("Source account is not active or does not exist", exception.getMessage());
        verify(accountServiceClient).isAccountActive(sourceAccountId);
        verify(accountServiceClient, never()).isAccountActive(destinationAccountId);
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(publisherService, never()).publishTransaction(any(Transaction.class));
    }

    @Test
    void createTransferTransaction_InactiveDestinationAccount_ThrowsIllegalArgumentException() {
        // Arrange
        Long sourceAccountId = 1L;
        Long destinationAccountId = 2L;
        BigDecimal amount = new BigDecimal("200.00");
        String currency = "USD";
        String description = "Test transfer";
        Long userId = 1L;

        when(accountServiceClient.isAccountActive(sourceAccountId)).thenReturn(true);
        when(accountServiceClient.isAccountActive(destinationAccountId)).thenReturn(false);

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.createTransferTransaction(
                    sourceAccountId, destinationAccountId, amount, currency, description, userId);
        });

        assertEquals("Destination account is not active or does not exist", exception.getMessage());
        verify(accountServiceClient).isAccountActive(sourceAccountId);
        verify(accountServiceClient).isAccountActive(destinationAccountId);
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(publisherService, never()).publishTransaction(any(Transaction.class));
    }

    @Test
    void getTransactionByReference_ExistingReference_ReturnsTransaction() {
        // Arrange
        String reference = "TXN-12345678";
        Transaction transaction = new Transaction();
        transaction.setTransactionReference(reference);
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setAmount(new BigDecimal("100.00"));

        when(transactionRepository.findByTransactionReference(reference)).thenReturn(Optional.of(transaction));

        // Act
        Optional<Transaction> result = transactionService.getTransactionByReference(reference);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(reference, result.get().getTransactionReference());
        verify(transactionRepository).findByTransactionReference(reference);
    }

    @Test
    void getTransactionByReference_NonExistingReference_ReturnsEmptyOptional() {
        // Arrange
        String reference = "TXN-12345678";
        when(transactionRepository.findByTransactionReference(reference)).thenReturn(Optional.empty());

        // Act
        Optional<Transaction> result = transactionService.getTransactionByReference(reference);

        // Assert
        assertFalse(result.isPresent());
        verify(transactionRepository).findByTransactionReference(reference);
    }

    @Test
    void getTransactionsByUserId_ExistingUserId_ReturnsTransactionsList() {
        // Arrange
        Long userId = 1L;
        Transaction transaction1 = new Transaction();
        transaction1.setId(1L);
        transaction1.setUserId(userId);
        Transaction transaction2 = new Transaction();
        transaction2.setId(2L);
        transaction2.setUserId(userId);
        List<Transaction> expectedTransactions = Arrays.asList(transaction1, transaction2);

        when(transactionRepository.findByUserId(userId)).thenReturn(expectedTransactions);

        // Act
        List<Transaction> result = transactionService.getTransactionsByUserId(userId);

        // Assert
        assertEquals(2, result.size());
        assertEquals(expectedTransactions, result);
        verify(transactionRepository).findByUserId(userId);
    }


} 