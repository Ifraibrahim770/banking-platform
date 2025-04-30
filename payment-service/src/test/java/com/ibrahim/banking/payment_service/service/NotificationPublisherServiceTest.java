package com.ibrahim.banking.payment_service.service;

import com.ibrahim.banking.payment_service.config.RabbitMQConfig;
import com.ibrahim.banking.payment_service.dto.NotificationMessageDto;
import com.ibrahim.banking.payment_service.model.Transaction;
import com.ibrahim.banking.payment_service.model.TransactionStatus;
import com.ibrahim.banking.payment_service.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class NotificationPublisherServiceTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Captor
    private ArgumentCaptor<NotificationMessageDto> messageDtoCaptor;

    private NotificationPublisherService publisherService;

    @BeforeEach
    void setUp() {
        publisherService = new NotificationPublisherService(rabbitTemplate);
    }

    @Test
    void publishTransactionNotification_CompletedDepositTransaction_SendsCorrectMessage() {
        // Arrange
        Transaction transaction = createTransaction(TransactionType.DEPOSIT, TransactionStatus.COMPLETED);

        // Act
        publisherService.publishTransactionNotification(transaction);

        // Assert
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.NOTIFICATION_EXCHANGE),
                eq(RabbitMQConfig.NOTIFICATION_ROUTING_KEY),
                messageDtoCaptor.capture());

        NotificationMessageDto capturedDto = messageDtoCaptor.getValue();
        assertNotNull(capturedDto);
        assertEquals(transaction.getUserId(), capturedDto.getUserId());
        assertEquals("Your deposit transaction of 100.00 USD has been completed.", capturedDto.getMessage());
    }

    @Test
    void publishTransactionNotification_FailedWithdrawalTransaction_SendsCorrectMessage() {
        // Arrange
        Transaction transaction = createTransaction(TransactionType.WITHDRAWAL, TransactionStatus.FAILED);
        transaction.setFailureReason("Insufficient funds");

        // Act
        publisherService.publishTransactionNotification(transaction);

        // Assert
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.NOTIFICATION_EXCHANGE),
                eq(RabbitMQConfig.NOTIFICATION_ROUTING_KEY),
                messageDtoCaptor.capture());

        NotificationMessageDto capturedDto = messageDtoCaptor.getValue();
        assertNotNull(capturedDto);
        assertEquals(transaction.getUserId(), capturedDto.getUserId());
        assertEquals("Your withdrawal transaction of 100.00 USD has been failed.", capturedDto.getMessage());
    }

    @Test
    void publishTransactionNotification_CompletedTransferTransaction_SendsCorrectMessage() {
        // Arrange
        Transaction transaction = createTransaction(TransactionType.TRANSFER, TransactionStatus.COMPLETED);
        transaction.setDestinationAccountId(2L);

        // Act
        publisherService.publishTransactionNotification(transaction);

        // Assert
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.NOTIFICATION_EXCHANGE),
                eq(RabbitMQConfig.NOTIFICATION_ROUTING_KEY),
                messageDtoCaptor.capture());

        NotificationMessageDto capturedDto = messageDtoCaptor.getValue();
        assertNotNull(capturedDto);
        assertEquals(transaction.getUserId(), capturedDto.getUserId());
        assertEquals("Your transfer transaction of 100.00 USD has been completed.", capturedDto.getMessage());
    }

    @Test
    void publishTransactionNotification_PendingTransaction_SendsCorrectMessage() {
        // Arrange
        Transaction transaction = createTransaction(TransactionType.DEPOSIT, TransactionStatus.PENDING);

        // Act
        publisherService.publishTransactionNotification(transaction);

        // Assert
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.NOTIFICATION_EXCHANGE),
                eq(RabbitMQConfig.NOTIFICATION_ROUTING_KEY),
                messageDtoCaptor.capture());

        NotificationMessageDto capturedDto = messageDtoCaptor.getValue();
        assertNotNull(capturedDto);
        assertEquals(transaction.getUserId(), capturedDto.getUserId());
        assertEquals("Your deposit transaction of 100.00 USD has been pending.", capturedDto.getMessage());
    }

    private Transaction createTransaction(TransactionType type, TransactionStatus status) {
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setTransactionReference("TXN-12345678");
        transaction.setType(type);
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setSourceAccountId(1L);
        transaction.setStatus(status);
        transaction.setCurrency("USD");
        transaction.setDescription("Test transaction");
        transaction.setCreatedAt(Instant.now());
        transaction.setUserId(1L);
        if (status == TransactionStatus.COMPLETED) {
            transaction.setCompletedAt(Instant.now());
        }
        return transaction;
    }
} 