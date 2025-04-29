package com.ibrahim.banking.events_service.consumer;

import com.ibrahim.banking.events_service.dto.NotificationMessageDto;
import com.ibrahim.banking.events_service.model.Notification;
import com.ibrahim.banking.events_service.model.TransactionStatus;
import com.ibrahim.banking.events_service.model.TransactionType;
import com.ibrahim.banking.events_service.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationConsumerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationConsumer notificationConsumer;

    private NotificationMessageDto notificationDto;
    private Notification mockNotification;

    @BeforeEach
    void setUp() {
        // Setup notification DTO
        notificationDto = new NotificationMessageDto();
        notificationDto.setUserId(1L);
        notificationDto.setTransactionReference("TX123456");
        notificationDto.setTransactionType(TransactionType.DEPOSIT);
        notificationDto.setTransactionStatus(TransactionStatus.COMPLETED);
        notificationDto.setAmount(new BigDecimal("100.00"));
        notificationDto.setCurrency("USD");
        notificationDto.setTimestamp(Instant.now());
        notificationDto.setMessage("Payment successful");

        // Setup mock notification entity
        mockNotification = Notification.builder()
                .id(1L)
                .userId(1L)
                .transactionReference("TX123456")
                .transactionType(TransactionType.DEPOSIT)
                .transactionStatus(TransactionStatus.COMPLETED)
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .timestamp(Instant.now())
                .message("Payment successful")
                .sentAt(Instant.now())
                .deliverySuccessful(true)
                .build();
    }

    @Test
    void processNotification_whenSuccessful_shouldSaveWithSuccessStatus() {

        when(notificationService.saveNotification(any(NotificationMessageDto.class), eq(true)))
                .thenReturn(mockNotification);


        notificationConsumer.processNotification(notificationDto);


        verify(notificationService).saveNotification(notificationDto, true);
    }

    @Test
    void processNotification_whenException_shouldSaveWithFailureStatus() {

        when(notificationService.saveNotification(any(NotificationMessageDto.class), eq(false)))
                .thenReturn(mockNotification);
        doThrow(new RuntimeException("Test exception")).when(notificationService)
                .saveNotification(any(NotificationMessageDto.class), eq(true));


        notificationConsumer.processNotification(notificationDto);


        verify(notificationService).saveNotification(notificationDto, false);
    }
} 