package com.ibrahim.banking.events_service.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class NotificationTest {

    @Test
    void builder_shouldCreateNotificationWithAllFields() {

        Long id = 1L;
        Long userId = 2L;
        String transactionReference = "TX123456";
        TransactionType transactionType = TransactionType.DEPOSIT;
        TransactionStatus transactionStatus = TransactionStatus.COMPLETED;
        BigDecimal amount = new BigDecimal("100.00");
        String currency = "USD";
        Instant timestamp = Instant.now();
        String message = "Payment successful";
        Instant sentAt = Instant.now();
        boolean deliverySuccessful = true;

        Notification notification = Notification.builder()
                .id(id)
                .userId(userId)
                .transactionReference(transactionReference)
                .transactionType(transactionType)
                .transactionStatus(transactionStatus)
                .amount(amount)
                .currency(currency)
                .timestamp(timestamp)
                .message(message)
                .sentAt(sentAt)
                .deliverySuccessful(deliverySuccessful)
                .build();


        assertNotNull(notification);
        assertEquals(id, notification.getId());
        assertEquals(userId, notification.getUserId());
        assertEquals(transactionReference, notification.getTransactionReference());
        assertEquals(transactionType, notification.getTransactionType());
        assertEquals(transactionStatus, notification.getTransactionStatus());
        assertEquals(amount, notification.getAmount());
        assertEquals(currency, notification.getCurrency());
        assertEquals(timestamp, notification.getTimestamp());
        assertEquals(message, notification.getMessage());
        assertEquals(sentAt, notification.getSentAt());
        assertEquals(deliverySuccessful, notification.isDeliverySuccessful());
    }

    @Test
    void settersAndGetters_shouldWorkCorrectly() {

        Notification notification = new Notification();
        Long id = 1L;
        Long userId = 2L;
        String transactionReference = "TX123456";
        TransactionType transactionType = TransactionType.DEPOSIT;
        TransactionStatus transactionStatus = TransactionStatus.COMPLETED;
        BigDecimal amount = new BigDecimal("100.00");
        String currency = "USD";
        Instant timestamp = Instant.now();
        String message = "Payment successful";
        Instant sentAt = Instant.now();
        boolean deliverySuccessful = true;


        notification.setId(id);
        notification.setUserId(userId);
        notification.setTransactionReference(transactionReference);
        notification.setTransactionType(transactionType);
        notification.setTransactionStatus(transactionStatus);
        notification.setAmount(amount);
        notification.setCurrency(currency);
        notification.setTimestamp(timestamp);
        notification.setMessage(message);
        notification.setSentAt(sentAt);
        notification.setDeliverySuccessful(deliverySuccessful);

        assertEquals(id, notification.getId());
        assertEquals(userId, notification.getUserId());
        assertEquals(transactionReference, notification.getTransactionReference());
        assertEquals(transactionType, notification.getTransactionType());
        assertEquals(transactionStatus, notification.getTransactionStatus());
        assertEquals(amount, notification.getAmount());
        assertEquals(currency, notification.getCurrency());
        assertEquals(timestamp, notification.getTimestamp());
        assertEquals(message, notification.getMessage());
        assertEquals(sentAt, notification.getSentAt());
        assertEquals(deliverySuccessful, notification.isDeliverySuccessful());
    }
} 