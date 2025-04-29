package com.ibrahim.banking.events_service.consumer;

import com.ibrahim.banking.events_service.config.RabbitMQConfig;
import com.ibrahim.banking.events_service.dto.NotificationMessageDto;
import com.ibrahim.banking.events_service.model.Notification;
import com.ibrahim.banking.events_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationConsumer {
    private static final Logger logger = LoggerFactory.getLogger(NotificationConsumer.class);
    
    private final NotificationService notificationService;
    
    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void processNotification(NotificationMessageDto notification) {
        logger.info("Received notification for transaction: {}", notification.getTransactionReference());
        
        try {

            // For now, we'll just log the notification
            logger.info("Sending notification to user {}: {}", 
                    notification.getUserId(), notification.getMessage());
            
            // Simulate notification delivery

            boolean notificationDelivered = true;
            
            if (notificationDelivered) {
                logger.info("Successfully delivered notification for transaction: {}", 
                        notification.getTransactionReference());
                
                // Save notification details to the database
                Notification savedNotification = notificationService.saveNotification(notification, true);
                logger.info("Saved notification with ID: {}", savedNotification.getId());
            } else {
                logger.error("Failed to deliver notification for transaction: {}", 
                        notification.getTransactionReference());
                
                // Save the failed notification attempt
                notificationService.saveNotification(notification, false);
            }
            
        } catch (Exception e) {
            logger.error("Error processing notification for transaction: {}", 
                    notification.getTransactionReference(), e);
            
            // Save the failed notification attempt due to exception
            notificationService.saveNotification(notification, false);
        }
    }
} 