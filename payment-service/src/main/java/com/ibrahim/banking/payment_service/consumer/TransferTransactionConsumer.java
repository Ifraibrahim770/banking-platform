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
public class TransferTransactionConsumer {
    private static final Logger logger = LoggerFactory.getLogger(TransferTransactionConsumer.class);
    
    private final TransactionRepository transactionRepository;
    private final AccountServiceClient accountServiceClient;
    private final NotificationPublisherService notificationPublisherService;
    
    public TransferTransactionConsumer(
            TransactionRepository transactionRepository,
            AccountServiceClient accountServiceClient,
            NotificationPublisherService notificationPublisherService) {
        this.transactionRepository = transactionRepository;
        this.accountServiceClient = accountServiceClient;
        this.notificationPublisherService = notificationPublisherService;
    }
    
    @RabbitListener(queues = RabbitMQConfig.PAYMENT_TRANSFER_QUEUE)
    public void processTransferTransaction(TransactionMessageDto messageDto) {
        logger.info("Received transfer transaction with reference: {}", messageDto.getTransactionReference());
        
        try {
            // Find the transaction in the database
            Optional<Transaction> optionalTransaction = transactionRepository.findByTransactionReference(
                    messageDto.getTransactionReference());
            
            if (optionalTransaction.isEmpty()) {
                logger.error("Transaction not found: {}", messageDto.getTransactionReference());
                return;
            }
            
            Transaction transaction = optionalTransaction.get();
            
            // Validate destination account
            if (transaction.getDestinationAccountId() == null) {
                transaction.setStatus(TransactionStatus.FAILED);
                transaction.setFailureReason("Destination account ID is required for transfers");
                transactionRepository.save(transaction);
                
                logger.error("Transfer failed: Destination account ID is required");
                notificationPublisherService.publishTransactionNotification(transaction);
                return;
            }
            
            // First debit the source account
            boolean debitSuccessful = accountServiceClient.debitAccount(
                    transaction.getSourceAccountId(),
                    transaction.getAmount(),
                    transaction.getCurrency(),
                    transaction.getTransactionReference()
            );
            
            if (!debitSuccessful) {
                // Mark as failed if debit fails
                transaction.setStatus(TransactionStatus.FAILED);
                transaction.setFailureReason("Failed to debit source account");
                transactionRepository.save(transaction);
                
                logger.error("Transfer failed: Could not debit source account {}", transaction.getSourceAccountId());
                notificationPublisherService.publishTransactionNotification(transaction);
                return;
            }
            
            // Then credit the destination account
            boolean creditSuccessful = accountServiceClient.creditAccount(
                    transaction.getDestinationAccountId(),
                    transaction.getAmount(),
                    transaction.getCurrency(),
                    transaction.getTransactionReference()
            );
            
            if (creditSuccessful) {
                // Update transaction status
                transaction.setStatus(TransactionStatus.COMPLETED);
                transaction.setCompletedAt(Instant.now());
                transactionRepository.save(transaction);
                
                logger.info("Transfer transaction completed successfully: {}", transaction.getTransactionReference());
            } else {
                // If credit fails, we need to reverse the debit
                boolean reversalSuccessful = accountServiceClient.creditAccount(
                        transaction.getSourceAccountId(),
                        transaction.getAmount(),
                        transaction.getCurrency(),
                        transaction.getTransactionReference() + "-reversal"
                );
                
                transaction.setStatus(TransactionStatus.FAILED);
                transaction.setFailureReason("Failed to credit destination account, debit was reversed");
                
                if (!reversalSuccessful) {
                    transaction.setFailureReason("Failed to credit destination account, debit reversal also failed");
                    logger.error("Critical: Transfer failed and reversal also failed: {}", 
                            transaction.getTransactionReference());
                } else {
                    logger.warn("Transfer failed but debit was successfully reversed: {}", 
                            transaction.getTransactionReference());
                }
                
                transactionRepository.save(transaction);
            }
            
            // Send notification
            notificationPublisherService.publishTransactionNotification(transaction);
            
        } catch (Exception e) {
            logger.error("Error processing transfer transaction: {}", messageDto.getTransactionReference(), e);
        }
    }
} 