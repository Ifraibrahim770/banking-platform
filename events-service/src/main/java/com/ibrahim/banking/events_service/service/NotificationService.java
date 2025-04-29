package com.ibrahim.banking.events_service.service;

import com.ibrahim.banking.events_service.dto.NotificationMessageDto;
import com.ibrahim.banking.events_service.model.Notification;

import java.util.List;

public interface NotificationService {
    
    Notification saveNotification(NotificationMessageDto notificationDto, boolean deliverySuccessful);
    
    List<Notification> getNotificationsByUserId(Long userId);
    
    List<Notification> getNotificationsByTransactionReference(String transactionReference);
    
    List<Notification> getAllNotifications();
} 