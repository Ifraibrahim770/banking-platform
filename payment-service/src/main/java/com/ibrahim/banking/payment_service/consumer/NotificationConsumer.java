package com.ibrahim.banking.payment_service.consumer;

import com.ibrahim.banking.payment_service.config.RabbitMQConfig;
import com.ibrahim.banking.payment_service.dto.NotificationMessageDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {
    private static final Logger logger = LoggerFactory.getLogger(NotificationConsumer.class);
    
    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void processNotification(NotificationMessageDto notification) {
        logger.info("Received notification for transaction: {}", notification.getTransactionReference());
        
        try {
            // In a real implementation, this would connect to a notification service
            // to send SMS, email, or push notifications to the user
            
            // For now, we'll just log the notification
            logger.info("Sending notification to user {}: {}", 
                    notification.getUserId(), notification.getMessage());
            
            // Simulate notification delivery
            // In a real application, this would call an actual notification service API
            boolean notificationDelivered = true;
            
            if (notificationDelivered) {
                logger.info("Successfully delivered notification for transaction: {}", 
                        notification.getTransactionReference());
            } else {
                logger.error("Failed to deliver notification for transaction: {}", 
                        notification.getTransactionReference());
                // In a real application, we might want to retry or take other actions
            }
            
        } catch (Exception e) {
            logger.error("Error processing notification for transaction: {}", 
                    notification.getTransactionReference(), e);
        }
    }
} 