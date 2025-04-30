package com.ibrahim.banking.payment_service.service;

import com.ibrahim.banking.payment_service.config.RabbitMQConfig;
import com.ibrahim.banking.payment_service.dto.TransactionMessageDto;
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
public class TransactionPublisherServiceTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Captor
    private ArgumentCaptor<TransactionMessageDto> messageDtoCaptor;

    private TransactionPublisherService publisherService;

    @BeforeEach
    void setUp() {
        publisherService = new TransactionPublisherService(rabbitTemplate);
    }

    @Test
    void publishTransaction_DepositTransaction_PublishesToDepositQueue() {
        // Arrange
        Transaction transaction = createTransaction(TransactionType.DEPOSIT);

        // Act
        publisherService.publishTransaction(transaction);

        // Assert
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.PAYMENT_EXCHANGE),
                eq(RabbitMQConfig.DEPOSIT_ROUTING_KEY),
                messageDtoCaptor.capture());

        TransactionMessageDto capturedDto = messageDtoCaptor.getValue();
        assertTransactionMessageDtoMatchesTransaction(transaction, capturedDto);
    }

    @Test
    void publishTransaction_WithdrawalTransaction_PublishesToWithdrawalQueue() {
        // Arrange
        Transaction transaction = createTransaction(TransactionType.WITHDRAWAL);

        // Act
        publisherService.publishTransaction(transaction);

        // Assert
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.PAYMENT_EXCHANGE),
                eq(RabbitMQConfig.WITHDRAWAL_ROUTING_KEY),
                messageDtoCaptor.capture());

        TransactionMessageDto capturedDto = messageDtoCaptor.getValue();
        assertTransactionMessageDtoMatchesTransaction(transaction, capturedDto);
    }

    @Test
    void publishTransaction_TransferTransaction_PublishesToTransferQueue() {
        // Arrange
        Transaction transaction = createTransaction(TransactionType.TRANSFER);
        transaction.setDestinationAccountId(2L);

        // Act
        publisherService.publishTransaction(transaction);

        // Assert
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.PAYMENT_EXCHANGE),
                eq(RabbitMQConfig.TRANSFER_ROUTING_KEY),
                messageDtoCaptor.capture());

        TransactionMessageDto capturedDto = messageDtoCaptor.getValue();
        assertTransactionMessageDtoMatchesTransaction(transaction, capturedDto);
    }

    private Transaction createTransaction(TransactionType type) {
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setTransactionReference("TXN-12345678");
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

    private void assertTransactionMessageDtoMatchesTransaction(Transaction transaction, TransactionMessageDto dto) {
        assertNotNull(dto);
        assertEquals(transaction.getId(), dto.getId());
        assertEquals(transaction.getTransactionReference(), dto.getTransactionReference());
        assertEquals(transaction.getType(), dto.getType());
        assertEquals(transaction.getAmount(), dto.getAmount());
        assertEquals(transaction.getSourceAccountId(), dto.getSourceAccountId());
        assertEquals(transaction.getDestinationAccountId(), dto.getDestinationAccountId());
        assertEquals(transaction.getStatus(), dto.getStatus());
        assertEquals(transaction.getCurrency(), dto.getCurrency());
        assertEquals(transaction.getDescription(), dto.getDescription());
        assertEquals(transaction.getCreatedAt(), dto.getCreatedAt());
        assertEquals(transaction.getUserId(), dto.getUserId());
    }
} 