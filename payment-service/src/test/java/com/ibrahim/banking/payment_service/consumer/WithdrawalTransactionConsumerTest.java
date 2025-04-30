package com.ibrahim.banking.payment_service.consumer;

import com.ibrahim.banking.payment_service.dto.TransactionMessageDto;
import com.ibrahim.banking.payment_service.model.Transaction;
import com.ibrahim.banking.payment_service.model.TransactionStatus;
import com.ibrahim.banking.payment_service.model.TransactionType;
import com.ibrahim.banking.payment_service.repository.TransactionRepository;
import com.ibrahim.banking.payment_service.service.NotificationPublisherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WithdrawalTransactionConsumerTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountServiceClient accountServiceClient;

    @Mock
    private NotificationPublisherService notificationPublisherService;

    @Captor
    private ArgumentCaptor<Transaction> transactionCaptor;

    private WithdrawalTransactionConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new WithdrawalTransactionConsumer(
                transactionRepository,
                accountServiceClient,
                notificationPublisherService
        );
    }

    @Test
    void processWithdrawalTransaction_Success_UpdatesTransactionStatusToCompleted() {
        // Arrange
        String reference = "TXN-12345678";
        Transaction transaction = createTransaction(reference);
        
        TransactionMessageDto messageDto = new TransactionMessageDto();
        messageDto.setTransactionReference(reference);
        
        when(transactionRepository.findByTransactionReference(reference))
                .thenReturn(Optional.of(transaction));
        when(accountServiceClient.debitAccount(
                eq(transaction.getSourceAccountId()),
                eq(transaction.getAmount()),
                eq(transaction.getCurrency()),
                eq(transaction.getTransactionReference())
        )).thenReturn(true);
        
        // Act
        consumer.processWithdrawalTransaction(messageDto);
        
        // Assert
        verify(transactionRepository).findByTransactionReference(reference);
        verify(accountServiceClient).debitAccount(
                eq(transaction.getSourceAccountId()),
                eq(transaction.getAmount()),
                eq(transaction.getCurrency()),
                eq(transaction.getTransactionReference())
        );
        
        verify(transactionRepository).save(transactionCaptor.capture());
        Transaction savedTransaction = transactionCaptor.getValue();
        assertEquals(TransactionStatus.COMPLETED, savedTransaction.getStatus());
        assertNotNull(savedTransaction.getCompletedAt());
        
        verify(notificationPublisherService).publishTransactionNotification(transaction);
    }

    @Test
    void processWithdrawalTransaction_Failure_UpdatesTransactionStatusToFailed() {
        // Arrange
        String reference = "TXN-12345678";
        Transaction transaction = createTransaction(reference);
        
        TransactionMessageDto messageDto = new TransactionMessageDto();
        messageDto.setTransactionReference(reference);
        
        when(transactionRepository.findByTransactionReference(reference))
                .thenReturn(Optional.of(transaction));
        when(accountServiceClient.debitAccount(
                eq(transaction.getSourceAccountId()),
                eq(transaction.getAmount()),
                eq(transaction.getCurrency()),
                eq(transaction.getTransactionReference())
        )).thenReturn(false);
        
        // Act
        consumer.processWithdrawalTransaction(messageDto);
        
        // Assert
        verify(transactionRepository).findByTransactionReference(reference);
        verify(accountServiceClient).debitAccount(
                eq(transaction.getSourceAccountId()),
                eq(transaction.getAmount()),
                eq(transaction.getCurrency()),
                eq(transaction.getTransactionReference())
        );
        
        verify(transactionRepository).save(transactionCaptor.capture());
        Transaction savedTransaction = transactionCaptor.getValue();
        assertEquals(TransactionStatus.FAILED, savedTransaction.getStatus());
        assertEquals("Failed to debit account", savedTransaction.getFailureReason());
        
        verify(notificationPublisherService).publishTransactionNotification(transaction);
    }

    @Test
    void processWithdrawalTransaction_TransactionNotFound_DoesNothing() {
        // Arrange
        String reference = "TXN-12345678";
        TransactionMessageDto messageDto = new TransactionMessageDto();
        messageDto.setTransactionReference(reference);
        
        when(transactionRepository.findByTransactionReference(reference))
                .thenReturn(Optional.empty());
        
        // Act
        consumer.processWithdrawalTransaction(messageDto);
        
        // Assert
        verify(transactionRepository).findByTransactionReference(reference);
        verify(accountServiceClient, never()).debitAccount(any(), any(), any(), any());
        verify(transactionRepository, never()).save(any());
        verify(notificationPublisherService, never()).publishTransactionNotification(any());
    }

    @Test
    void processWithdrawalTransaction_ExceptionThrown_LogsError() {
        // Arrange
        String reference = "TXN-12345678";
        Transaction transaction = createTransaction(reference);
        
        TransactionMessageDto messageDto = new TransactionMessageDto();
        messageDto.setTransactionReference(reference);
        
        when(transactionRepository.findByTransactionReference(reference))
                .thenReturn(Optional.of(transaction));
        when(accountServiceClient.debitAccount(
                eq(transaction.getSourceAccountId()),
                eq(transaction.getAmount()),
                eq(transaction.getCurrency()),
                eq(transaction.getTransactionReference())
        )).thenThrow(new RuntimeException("Test exception"));
        
        // Act
        consumer.processWithdrawalTransaction(messageDto);
        
        // Assert
        verify(transactionRepository).findByTransactionReference(reference);
        verify(accountServiceClient).debitAccount(
                eq(transaction.getSourceAccountId()),
                eq(transaction.getAmount()),
                eq(transaction.getCurrency()),
                eq(transaction.getTransactionReference())
        );
        
        verify(transactionRepository, never()).save(any());
        verify(notificationPublisherService, never()).publishTransactionNotification(any());
    }

    private Transaction createTransaction(String reference) {
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setTransactionReference(reference);
        transaction.setType(TransactionType.WITHDRAWAL);
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setSourceAccountId(1L);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setCurrency("USD");
        transaction.setDescription("Test withdrawal transaction");
        transaction.setCreatedAt(Instant.now());
        transaction.setUserId(1L);
        return transaction;
    }
} 