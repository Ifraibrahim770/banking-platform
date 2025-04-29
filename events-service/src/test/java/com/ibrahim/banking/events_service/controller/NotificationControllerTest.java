package com.ibrahim.banking.events_service.controller;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    private Notification mockNotification;

    @BeforeEach
    void setUp() {

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
    void getAllNotifications_shouldReturnAllNotifications() {

        List<Notification> mockList = Arrays.asList(mockNotification);
        when(notificationService.getAllNotifications()).thenReturn(mockList);


        ResponseEntity<List<Notification>> response = notificationController.getAllNotifications();


        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(notificationService).getAllNotifications();
    }

    @Test
    void getAllNotifications_whenEmpty_shouldReturnEmptyList() {

        when(notificationService.getAllNotifications()).thenReturn(Collections.emptyList());


        ResponseEntity<List<Notification>> response = notificationController.getAllNotifications();


        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().size());
        verify(notificationService).getAllNotifications();
    }

    @Test
    void getNotificationsByUserId_shouldReturnUserNotifications() {

        Long userId = 1L;
        List<Notification> mockList = Arrays.asList(mockNotification);
        when(notificationService.getNotificationsByUserId(userId)).thenReturn(mockList);


        ResponseEntity<List<Notification>> response = notificationController.getNotificationsByUserId(userId);


        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(userId, response.getBody().get(0).getUserId());
        verify(notificationService).getNotificationsByUserId(userId);
    }

    @Test
    void getNotificationsByTransactionReference_shouldReturnTransactionNotifications() {

        String reference = "TX123456";
        List<Notification> mockList = Arrays.asList(mockNotification);
        when(notificationService.getNotificationsByTransactionReference(reference)).thenReturn(mockList);


        ResponseEntity<List<Notification>> response = notificationController.getNotificationsByTransactionReference(reference);


        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(reference, response.getBody().get(0).getTransactionReference());
        verify(notificationService).getNotificationsByTransactionReference(reference);
    }
} 