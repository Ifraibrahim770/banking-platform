package com.ibrahim.banking.payment_service.service;

import com.ibrahim.banking.payment_service.consumer.AccountServiceClient;
import com.ibrahim.banking.payment_service.model.Transaction;
import com.ibrahim.banking.payment_service.model.TransactionStatus;
import com.ibrahim.banking.payment_service.model.TransactionType;
import com.ibrahim.banking.payment_service.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TransactionService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);
    
    private final TransactionRepository transactionRepository;
    private final AccountServiceClient accountServiceClient;
    private final TransactionPublisherService publisherService;
    
    public TransactionService(
            TransactionRepository transactionRepository,
            AccountServiceClient accountServiceClient,
            TransactionPublisherService publisherService) {
        this.transactionRepository = transactionRepository;
        this.accountServiceClient = accountServiceClient;
        this.publisherService = publisherService;
    }
    
    @Transactional
    public Transaction createDepositTransaction(Long accountId, BigDecimal amount, String currency, 
                                              String description, Long userId) {
        // check if account exist and active
        if (!accountServiceClient.isAccountActive(accountId)) {
            throw new IllegalArgumentException("Account is not active or does not exist");
        }
        
        Transaction transaction = new Transaction();
        transaction.setTransactionReference(generateTransactionReference());
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setAmount(amount);
        transaction.setSourceAccountId(accountId);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setCurrency(currency);
        transaction.setDescription(description);
        transaction.setCreatedAt(Instant.now());
        transaction.setUserId(userId);
        
        // Save to db
        Transaction savedTransaction = transactionRepository.save(transaction);
        logger.info("Created deposit transaction: {}", savedTransaction.getTransactionReference());
        
        // send to que
        publisherService.publishTransaction(savedTransaction);
        logger.info("Published deposit transaction to queue: {}", savedTransaction.getTransactionReference());
        
        return savedTransaction;
    }
    
    @Transactional
    public Transaction createWithdrawalTransaction(Long accountId, BigDecimal amount, String currency, 
                                                String description, Long userId) {
        // validate account is active
        if (!accountServiceClient.isAccountActive(accountId)) {
            throw new IllegalArgumentException("Account is not active or does not exist");
        }
        
        Transaction transaction = new Transaction();
        transaction.setTransactionReference(generateTransactionReference());
        transaction.setType(TransactionType.WITHDRAWAL);
        transaction.setAmount(amount);
        transaction.setSourceAccountId(accountId);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setCurrency(currency);
        transaction.setDescription(description);
        transaction.setCreatedAt(Instant.now());
        transaction.setUserId(userId);
        
        Transaction savedTransaction = transactionRepository.save(transaction);
        logger.info("Created withdrawal transaction: {}", savedTransaction.getTransactionReference());
        
        publisherService.publishTransaction(savedTransaction);
        logger.info("Published withdrawal transaction to queue: {}", savedTransaction.getTransactionReference());
        
        return savedTransaction;
    }
    
    @Transactional
    public Transaction createTransferTransaction(Long sourceAccountId, Long destinationAccountId, 
                                               BigDecimal amount, String currency, 
                                               String description, Long userId) {
        // chek both accounts
        if (!accountServiceClient.isAccountActive(sourceAccountId)) {
            throw new IllegalArgumentException("Source account is not active or does not exist");
        }
        
        if (!accountServiceClient.isAccountActive(destinationAccountId)) {
            throw new IllegalArgumentException("Destination account is not active or does not exist");
        }
        
        Transaction transaction = new Transaction();
        transaction.setTransactionReference(generateTransactionReference());
        transaction.setType(TransactionType.TRANSFER);
        transaction.setAmount(amount);
        transaction.setSourceAccountId(sourceAccountId);
        transaction.setDestinationAccountId(destinationAccountId);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setCurrency(currency);
        transaction.setDescription(description);
        transaction.setCreatedAt(Instant.now());
        transaction.setUserId(userId);
        
        // save it
        Transaction savedTransaction = transactionRepository.save(transaction);
        logger.info("Created transfer transaction: {}", savedTransaction.getTransactionReference());
        
        // publish to queue
        publisherService.publishTransaction(savedTransaction);
        logger.info("Published transfer transaction to queue: {}", savedTransaction.getTransactionReference());
        
        return savedTransaction;
    }
    
    public Optional<Transaction> getTransactionByReference(String transactionReference) {
        return transactionRepository.findByTransactionReference(transactionReference);
    }
    
    public List<Transaction> getTransactionsByUserId(Long userId) {
        return transactionRepository.findByUserId(userId);
    }
    
    public List<Transaction> getTransactionsByAccountId(Long accountId) {
        List<Transaction> sourceTransactions = transactionRepository.findBySourceAccountId(accountId);
        List<Transaction> destinationTransactions = transactionRepository.findByDestinationAccountId(accountId);
        
        // combine lists
        sourceTransactions.addAll(destinationTransactions);
        return sourceTransactions;
    }
    
    private String generateTransactionReference() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
} 