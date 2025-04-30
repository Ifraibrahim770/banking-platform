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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransferTransactionConsumerTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountServiceClient accountServiceClient;

    @Mock
    private NotificationPublisherService notificationPublisherService;

    @Captor
    private ArgumentCaptor<Transaction> transactionCaptor;

    private TransferTransactionConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new TransferTransactionConsumer(
                transactionRepository,
                accountServiceClient,
                notificationPublisherService
        );
    }

    @Test
    void processTransferTransaction_Success_UpdatesTransactionStatusToCompleted() {
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
        
        when(accountServiceClient.creditAccount(
                eq(transaction.getDestinationAccountId()),
                eq(transaction.getAmount()),
                eq(transaction.getCurrency()),
                eq(transaction.getTransactionReference())
        )).thenReturn(true);
        
        // Act
        consumer.processTransferTransaction(messageDto);
        
        // Assert
        verify(transactionRepository).findByTransactionReference(reference);
        verify(accountServiceClient).debitAccount(
                eq(transaction.getSourceAccountId()),
                eq(transaction.getAmount()),
                eq(transaction.getCurrency()),
                eq(transaction.getTransactionReference())
        );
        verify(accountServiceClient).creditAccount(
                eq(transaction.getDestinationAccountId()),
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
    void processTransferTransaction_DebitFails_UpdatesTransactionStatusToFailed() {
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
        consumer.processTransferTransaction(messageDto);
        
        // Assert
        verify(transactionRepository).findByTransactionReference(reference);
        verify(accountServiceClient).debitAccount(
                eq(transaction.getSourceAccountId()),
                eq(transaction.getAmount()),
                eq(transaction.getCurrency()),
                eq(transaction.getTransactionReference())
        );
        verify(accountServiceClient, never()).creditAccount(
                any(), any(), any(), any()
        );
        
        verify(transactionRepository).save(transactionCaptor.capture());
        Transaction savedTransaction = transactionCaptor.getValue();
        assertEquals(TransactionStatus.FAILED, savedTransaction.getStatus());
        assertEquals("Failed to debit source account", savedTransaction.getFailureReason());
        
        verify(notificationPublisherService).publishTransactionNotification(transaction);
    }



    @Test
    void processTransferTransaction_TransactionNotFound_DoesNothing() {
        // Arrange
        String reference = "TXN-12345678";
        TransactionMessageDto messageDto = new TransactionMessageDto();
        messageDto.setTransactionReference(reference);
        
        when(transactionRepository.findByTransactionReference(reference))
                .thenReturn(Optional.empty());
        
        // Act
        consumer.processTransferTransaction(messageDto);
        
        // Assert
        verify(transactionRepository).findByTransactionReference(reference);
        verify(accountServiceClient, never()).debitAccount(any(), any(), any(), any());
        verify(accountServiceClient, never()).creditAccount(any(), any(), any(), any());
        verify(transactionRepository, never()).save(any());
        verify(notificationPublisherService, never()).publishTransactionNotification(any());
    }

    private Transaction createTransaction(String reference) {
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setTransactionReference(reference);
        transaction.setType(TransactionType.TRANSFER);
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setSourceAccountId(1L);
        transaction.setDestinationAccountId(2L);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setCurrency("USD");
        transaction.setDescription("Test transfer transaction");
        transaction.setCreatedAt(Instant.now());
        transaction.setUserId(1L);
        return transaction;
    }
} 