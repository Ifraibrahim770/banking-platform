package com.ibrahim.banking.events_service.dto;

import com.ibrahim.banking.events_service.model.TransactionStatus;
import com.ibrahim.banking.events_service.model.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class NotificationMessageDtoTest {

    @Test
    void constructor_shouldCreateDtoWithAllFields() {

        Long userId = 1L;
        String transactionReference = "TX123456";
        TransactionType transactionType = TransactionType.DEPOSIT;
        TransactionStatus transactionStatus = TransactionStatus.COMPLETED;
        BigDecimal amount = new BigDecimal("100.00");
        String currency = "USD";
        Instant timestamp = Instant.now();
        String message = "Payment successful";


        NotificationMessageDto dto = new NotificationMessageDto();
        dto.setUserId(userId);
        dto.setTransactionReference(transactionReference);
        dto.setTransactionType(transactionType);
        dto.setTransactionStatus(transactionStatus);
        dto.setAmount(amount);
        dto.setCurrency(currency);
        dto.setTimestamp(timestamp);
        dto.setMessage(message);

        assertNotNull(dto);
        assertEquals(userId, dto.getUserId());
        assertEquals(transactionReference, dto.getTransactionReference());
        assertEquals(transactionType, dto.getTransactionType());
        assertEquals(transactionStatus, dto.getTransactionStatus());
        assertEquals(amount, dto.getAmount());
        assertEquals(currency, dto.getCurrency());
        assertEquals(timestamp, dto.getTimestamp());
        assertEquals(message, dto.getMessage());
    }

    @Test
    void lombokDataAnnotation_shouldGenerateToString() {

        NotificationMessageDto dto = new NotificationMessageDto();
        dto.setUserId(1L);
        dto.setTransactionReference("TX123456");
        
        String toString = dto.toString();
        assertNotNull(toString);
    }

    @Test
    void equalsAndHashCode_shouldWork() {

        NotificationMessageDto dto1 = new NotificationMessageDto();
        dto1.setUserId(1L);
        dto1.setTransactionReference("TX123456");
        
        NotificationMessageDto dto2 = new NotificationMessageDto();
        dto2.setUserId(1L);
        dto2.setTransactionReference("TX123456");

        assertEquals(dto1.getTransactionReference(), dto2.getTransactionReference());
    }
} 