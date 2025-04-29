package com.ibrahim.banking.events_service.repository;

import com.ibrahim.banking.events_service.model.Notification;
import com.ibrahim.banking.events_service.model.TransactionStatus;
import com.ibrahim.banking.events_service.model.TransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DataJpaTest
@ActiveProfiles("test")
class NotificationRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    void findByUserId_shouldReturnNotificationsForUser() {

        Notification notification1 = createTestNotification(1L, "TX123", true);
        Notification notification2 = createTestNotification(1L, "TX456", true);
        Notification notification3 = createTestNotification(2L, "TX789", true);
        
        entityManager.persist(notification1);
        entityManager.persist(notification2);
        entityManager.persist(notification3);
        entityManager.flush();
        

        List<Notification> notifications = notificationRepository.findByUserId(1L);
        

        assertEquals(2, notifications.size());
        assertEquals("TX123", notifications.get(0).getTransactionReference());
        assertEquals("TX456", notifications.get(1).getTransactionReference());
    }
    
    @Test
    void findByTransactionReference_shouldReturnMatchingNotifications() {

        Notification notification1 = createTestNotification(1L, "TX123", true);
        Notification notification2 = createTestNotification(2L, "TX123", false);
        Notification notification3 = createTestNotification(3L, "TX456", true);
        
        entityManager.persist(notification1);
        entityManager.persist(notification2);
        entityManager.persist(notification3);
        entityManager.flush();
        

        List<Notification> notifications = notificationRepository.findByTransactionReference("TX123");
        

        assertEquals(2, notifications.size());
        assertEquals(1L, notifications.get(0).getUserId());
        assertEquals(2L, notifications.get(1).getUserId());
    }
    
    @Test
    void findByDeliverySuccessful_shouldReturnMatchingNotifications() {

        Notification notification1 = createTestNotification(1L, "TX123", true);
        Notification notification2 = createTestNotification(2L, "TX456", false);
        Notification notification3 = createTestNotification(3L, "TX789", true);
        
        entityManager.persist(notification1);
        entityManager.persist(notification2);
        entityManager.persist(notification3);
        entityManager.flush();
        

        List<Notification> successfulNotifications = notificationRepository.findByDeliverySuccessful(true);
        List<Notification> failedNotifications = notificationRepository.findByDeliverySuccessful(false);
        

        assertEquals(2, successfulNotifications.size());
        assertEquals(1, failedNotifications.size());
        assertFalse(failedNotifications.get(0).isDeliverySuccessful());
    }
    
    private Notification createTestNotification(Long userId, String transactionReference, boolean deliverySuccessful) {
        return Notification.builder()
                .userId(userId)
                .transactionReference(transactionReference)
                .transactionType(TransactionType.DEPOSIT)
                .transactionStatus(TransactionStatus.COMPLETED)
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .timestamp(Instant.now())
                .message("Test notification")
                .sentAt(Instant.now())
                .deliverySuccessful(deliverySuccessful)
                .build();
    }
} 