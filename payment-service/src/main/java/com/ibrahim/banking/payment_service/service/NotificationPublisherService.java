package com.ibrahim.banking.payment_service.service;

import com.ibrahim.banking.payment_service.config.RabbitMQConfig;
import com.ibrahim.banking.payment_service.dto.NotificationMessageDto;
import com.ibrahim.banking.payment_service.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class NotificationPublisherService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationPublisherService.class);
    
    private final RabbitTemplate rabbitTemplate;
    
    public NotificationPublisherService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
    
    public void publishTransactionNotification(Transaction transaction) {
        NotificationMessageDto notificationDto = createNotificationDto(transaction);
        
        logger.info("Publishing notification for transaction with reference {} to notification queue", 
                transaction.getTransactionReference());
        
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIFICATION_EXCHANGE, 
                RabbitMQConfig.NOTIFICATION_ROUTING_KEY, 
                notificationDto
        );
    }
    
    private NotificationMessageDto createNotificationDto(Transaction transaction) {
        String message = generateNotificationMessage(transaction);
        
        return new NotificationMessageDto(
                transaction.getUserId(),
                transaction.getTransactionReference(),
                transaction.getType(),
                transaction.getStatus(),
                transaction.getAmount(),
                transaction.getCurrency(),
                Instant.now(),
                message
        );
    }
    
    private String generateNotificationMessage(Transaction transaction) {
        String statusMessage = transaction.getStatus().toString().toLowerCase();
        String typeMessage = transaction.getType().toString().toLowerCase();
        
        return String.format("Your %s transaction of %s %s has been %s.", 
                typeMessage, 
                transaction.getAmount(), 
                transaction.getCurrency(),
                statusMessage);
    }
} 