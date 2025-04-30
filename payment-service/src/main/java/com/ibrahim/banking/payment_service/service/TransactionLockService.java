package com.ibrahim.banking.payment_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class TransactionLockService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionLockService.class);
    private static final long LOCK_TIMEOUT_SECONDS = 12; // 12 seconds lock time
    private static final String KEY_PREFIX = "transaction:account:";
    
    private final RedisTemplate<String, String> redisTemplate;
    
    public TransactionLockService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    /**
     * Attempts to acquire a lock for an account transaction
     * @param accountId The account ID to lock
     * @return true if lock was acquired, false if account already has an active transaction
     */
    public boolean acquireTransactionLock(Long accountId) {
        String key = KEY_PREFIX + accountId;
        Boolean lockAcquired = redisTemplate.opsForValue()
                .setIfAbsent(key, "LOCKED", LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        
        // Handle null case just to be safe
        boolean result = lockAcquired != null && lockAcquired;
        
        if (result) {
            logger.info("Acquired transaction lock for account: {}", accountId);
        } else {
            logger.warn("Failed to acquire transaction lock for account: {} - concurrent transaction in progress", accountId);
        }
        
        return result;
    }
    
    /**
     * Releases a transaction lock for an account (can be used if transaction completes before timeout)
     * @param accountId The account ID to unlock
     */
    public void releaseTransactionLock(Long accountId) {
        String key = KEY_PREFIX + accountId;
        redisTemplate.delete(key);
        logger.info("Released transaction lock for account: {}", accountId);
    }
} 