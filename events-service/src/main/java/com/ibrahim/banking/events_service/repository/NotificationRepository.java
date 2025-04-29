package com.ibrahim.banking.events_service.repository;

import com.ibrahim.banking.events_service.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByUserId(Long userId);
    
    List<Notification> findByTransactionReference(String transactionReference);
    
    List<Notification> findByDeliverySuccessful(boolean deliverySuccessful);
} 