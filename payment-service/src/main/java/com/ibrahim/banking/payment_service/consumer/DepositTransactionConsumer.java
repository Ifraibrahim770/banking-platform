package com.ibrahim.banking.payment_service.consumer;

import com.ibrahim.banking.payment_service.config.RabbitMQConfig;
import com.ibrahim.banking.payment_service.dto.TransactionMessageDto;
import com.ibrahim.banking.payment_service.model.Transaction;
import com.ibrahim.banking.payment_service.model.TransactionStatus;
import com.ibrahim.banking.payment_service.repository.TransactionRepository;
import com.ibrahim.banking.payment_service.service.NotificationPublisherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
public class DepositTransactionConsumer {
    private static final Logger logger = LoggerFactory.getLogger(DepositTransactionConsumer.class);
    
    private final TransactionRepository transactionRepository;
    private final AccountServiceClient accountServiceClient;
    private final NotificationPublisherService notificationPublisherService;
    
    public DepositTransactionConsumer(
            TransactionRepository transactionRepository,
            AccountServiceClient accountServiceClient,
            NotificationPublisherService notificationPublisherService) {
        this.transactionRepository = transactionRepository;
        this.accountServiceClient = accountServiceClient;
        this.notificationPublisherService = notificationPublisherService;
    }
    
    @RabbitListener(queues = RabbitMQConfig.PAYMENT_DEPOSIT_QUEUE)
    public void processDepositTransaction(TransactionMessageDto messageDto) {
        logger.info("Received deposit transaction with reference: {}", messageDto.getTransactionReference());
        
        try {
            // Find the transaction in the database
            Optional<Transaction> optionalTransaction = transactionRepository.findByTransactionReference(
                    messageDto.getTransactionReference());
            
            if (optionalTransaction.isEmpty()) {
                logger.error("Transaction not found: {}", messageDto.getTransactionReference());
                return;
            }
            
            Transaction transaction = optionalTransaction.get();
            
            // Call Store of Value service to credit the account
            boolean depositSuccessful = accountServiceClient.creditAccount(
                    transaction.getSourceAccountId(),
                    transaction.getAmount(),
                    transaction.getCurrency(),
                    transaction.getTransactionReference()
            );
            
            if (depositSuccessful) {
                // Update transaction status
                transaction.setStatus(TransactionStatus.COMPLETED);
                transaction.setCompletedAt(Instant.now());
                transactionRepository.save(transaction);
                
                logger.info("Deposit transaction completed successfully: {}", transaction.getTransactionReference());
            } else {
                // Mark as failed
                transaction.setStatus(TransactionStatus.FAILED);
                transaction.setFailureReason("Failed to credit account");
                transactionRepository.save(transaction);
                
                logger.error("Failed to complete deposit transaction: {}", transaction.getTransactionReference());
            }
            
            // Send notification
            notificationPublisherService.publishTransactionNotification(transaction);
            
        } catch (Exception e) {
            logger.error("Error processing deposit transaction: {}", messageDto.getTransactionReference(), e);
        }
    }
} 