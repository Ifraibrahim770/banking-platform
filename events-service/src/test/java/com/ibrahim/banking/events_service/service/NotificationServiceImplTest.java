package com.ibrahim.banking.events_service.service;

import com.ibrahim.banking.events_service.dto.NotificationMessageDto;
import com.ibrahim.banking.events_service.model.Notification;
import com.ibrahim.banking.events_service.model.TransactionStatus;
import com.ibrahim.banking.events_service.model.TransactionType;
import com.ibrahim.banking.events_service.repository.NotificationRepository;
import com.ibrahim.banking.events_service.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private NotificationMessageDto notificationDto;
    private Notification mockNotification;

    @BeforeEach
    void setUp() {

        notificationDto = new NotificationMessageDto();
        notificationDto.setUserId(1L);
        notificationDto.setTransactionReference("TX123456");
        notificationDto.setTransactionType(TransactionType.DEPOSIT);
        notificationDto.setTransactionStatus(TransactionStatus.COMPLETED);
        notificationDto.setAmount(new BigDecimal("100.00"));
        notificationDto.setCurrency("USD");
        notificationDto.setTimestamp(Instant.now());
        notificationDto.setMessage("Payment successful");


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
    void saveNotification_shouldSaveAndReturnNotification() {

        when(notificationRepository.save(any(Notification.class))).thenReturn(mockNotification);


        Notification result = notificationService.saveNotification(notificationDto, true);


        assertNotNull(result);
        assertEquals(mockNotification.getId(), result.getId());
        assertEquals(mockNotification.getTransactionReference(), result.getTransactionReference());
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void getNotificationsByUserId_shouldReturnNotificationsList() {

        Long userId = 1L;
        List<Notification> mockList = Arrays.asList(mockNotification);
        when(notificationRepository.findByUserId(userId)).thenReturn(mockList);


        List<Notification> result = notificationService.getNotificationsByUserId(userId);


        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(mockNotification.getUserId(), result.get(0).getUserId());
        verify(notificationRepository).findByUserId(userId);
    }

    @Test
    void getNotificationsByTransactionReference_shouldReturnNotificationsList() {

        String reference = "TX123456";
        List<Notification> mockList = Arrays.asList(mockNotification);
        when(notificationRepository.findByTransactionReference(reference)).thenReturn(mockList);


        List<Notification> result = notificationService.getNotificationsByTransactionReference(reference);


        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(mockNotification.getTransactionReference(), result.get(0).getTransactionReference());
        verify(notificationRepository).findByTransactionReference(reference);
    }

    @Test
    void getAllNotifications_shouldReturnAllNotifications() {

        List<Notification> mockList = Arrays.asList(mockNotification);
        when(notificationRepository.findAll()).thenReturn(mockList);


        List<Notification> result = notificationService.getAllNotifications();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(notificationRepository).findAll();
    }
} 