package com.ibrahim.banking.payment_service.service;

import com.ibrahim.banking.payment_service.consumer.AccountServiceClient;
import com.ibrahim.banking.payment_service.exception.ConcurrentTransactionException;
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
    private final TransactionLockService transactionLockService;
    
    public TransactionService(
            TransactionRepository transactionRepository,
            AccountServiceClient accountServiceClient,
            TransactionPublisherService publisherService,
            TransactionLockService transactionLockService) {
        this.transactionRepository = transactionRepository;
        this.accountServiceClient = accountServiceClient;
        this.publisherService = publisherService;
        this.transactionLockService = transactionLockService;
    }
    
    @Transactional
    public Transaction createDepositTransaction(Long accountId, BigDecimal amount, String currency, 
                                              String description, Long userId) {
        // check if account exist and active
        if (!accountServiceClient.isAccountActive(accountId)) {
            throw new IllegalArgumentException("Account is not active or does not exist");
        }
        
        // Check if a transaction for this account is already in progress
        if (!transactionLockService.acquireTransactionLock(accountId)) {
            throw new ConcurrentTransactionException(accountId);
        }
        
        try {
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
        } catch (Exception e) {
            // In case of exception, manually release the lock to prevent deadlock
            transactionLockService.releaseTransactionLock(accountId);
            throw e;
        }
        // We don't release the lock here, it will auto-expire after the configured timeout (12 seconds)
    }
    
    @Transactional
    public Transaction createWithdrawalTransaction(Long accountId, BigDecimal amount, String currency, 
                                                String description, Long userId) {
        // validate account is active
        if (!accountServiceClient.isAccountActive(accountId)) {
            throw new IllegalArgumentException("Account is not active or does not exist");
        }
        
        // Check if a transaction for this account is already in progress
        if (!transactionLockService.acquireTransactionLock(accountId)) {
            throw new ConcurrentTransactionException(accountId);
        }
        
        try {
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
        } catch (Exception e) {
            // In case of exception, manually release the lock to prevent deadlock
            transactionLockService.releaseTransactionLock(accountId);
            throw e;
        }
        // We don't release the lock here, it will auto-expire after the configured timeout (12 seconds)
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
        
        // Check if transactions for these accounts are already in progress
        boolean sourceAccountLocked = transactionLockService.acquireTransactionLock(sourceAccountId);
        if (!sourceAccountLocked) {
            throw new ConcurrentTransactionException(sourceAccountId);
        }
        
        boolean destinationAccountLocked = false;
        try {
            destinationAccountLocked = transactionLockService.acquireTransactionLock(destinationAccountId);
            if (!destinationAccountLocked) {
                // Release source account lock if destination is locked
                transactionLockService.releaseTransactionLock(sourceAccountId);
                throw new ConcurrentTransactionException(destinationAccountId);
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
        } catch (Exception e) {
            // In case of exception, manually release the locks to prevent deadlock
            if (sourceAccountLocked) {
                transactionLockService.releaseTransactionLock(sourceAccountId);
            }
            if (destinationAccountLocked) {
                transactionLockService.releaseTransactionLock(destinationAccountId);
            }
            throw e;
        }
        // We don't release the locks here, they will auto-expire after the configured timeout (12 seconds)
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