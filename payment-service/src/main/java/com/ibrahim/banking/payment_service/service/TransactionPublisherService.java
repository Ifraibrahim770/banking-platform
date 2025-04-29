package com.ibrahim.banking.payment_service.service;

import com.ibrahim.banking.payment_service.config.RabbitMQConfig;
import com.ibrahim.banking.payment_service.dto.TransactionMessageDto;
import com.ibrahim.banking.payment_service.model.Transaction;
import com.ibrahim.banking.payment_service.model.TransactionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class TransactionPublisherService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionPublisherService.class);
    
    private final RabbitTemplate rabbitTemplate;
    
    public TransactionPublisherService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
    
    public void publishTransaction(Transaction transaction) {
        TransactionMessageDto messageDto = convertToMessageDto(transaction);
        
        String routingKey;
        switch (transaction.getType()) {
            case DEPOSIT:
                routingKey = RabbitMQConfig.DEPOSIT_ROUTING_KEY;
                break;
            case WITHDRAWAL:
                routingKey = RabbitMQConfig.WITHDRAWAL_ROUTING_KEY;
                break;
            case TRANSFER:
                routingKey = RabbitMQConfig.TRANSFER_ROUTING_KEY;
                break;
            default:
                logger.error("Unknown transaction type: {}", transaction.getType());
                return;
        }
        
        logger.info("Publishing transaction with reference {} to queue with routing key {}", 
                transaction.getTransactionReference(), routingKey);
        
        rabbitTemplate.convertAndSend(RabbitMQConfig.PAYMENT_EXCHANGE, routingKey, messageDto);
    }
    
    private TransactionMessageDto convertToMessageDto(Transaction transaction) {
        TransactionMessageDto dto = new TransactionMessageDto();
        dto.setId(transaction.getId());
        dto.setTransactionReference(transaction.getTransactionReference());
        dto.setType(transaction.getType());
        dto.setAmount(transaction.getAmount());
        dto.setSourceAccountId(transaction.getSourceAccountId());
        dto.setDestinationAccountId(transaction.getDestinationAccountId());
        dto.setStatus(transaction.getStatus());
        dto.setCurrency(transaction.getCurrency());
        dto.setDescription(transaction.getDescription());
        dto.setCreatedAt(transaction.getCreatedAt());
        dto.setUserId(transaction.getUserId());
        return dto;
    }
} 