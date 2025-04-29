package com.ibrahim.banking.events_service.service.impl;

import com.ibrahim.banking.events_service.dto.NotificationMessageDto;
import com.ibrahim.banking.events_service.model.Notification;
import com.ibrahim.banking.events_service.repository.NotificationRepository;
import com.ibrahim.banking.events_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    
    private final NotificationRepository notificationRepository;
    
    @Override
    public Notification saveNotification(NotificationMessageDto notificationDto, boolean deliverySuccessful) {
        Notification notification = Notification.builder()
                .userId(notificationDto.getUserId())
                .transactionReference(notificationDto.getTransactionReference())
                .transactionType(notificationDto.getTransactionType())
                .transactionStatus(notificationDto.getTransactionStatus())
                .amount(notificationDto.getAmount())
                .currency(notificationDto.getCurrency())
                .timestamp(notificationDto.getTimestamp())
                .message(notificationDto.getMessage())
                .sentAt(Instant.now())
                .deliverySuccessful(deliverySuccessful)
                .build();
        
        return notificationRepository.save(notification);
    }
    
    @Override
    public List<Notification> getNotificationsByUserId(Long userId) {
        return notificationRepository.findByUserId(userId);
    }
    
    @Override
    public List<Notification> getNotificationsByTransactionReference(String transactionReference) {
        return notificationRepository.findByTransactionReference(transactionReference);
    }
    
    @Override
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }
} 