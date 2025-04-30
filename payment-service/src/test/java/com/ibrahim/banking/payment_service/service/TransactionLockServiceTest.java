package com.ibrahim.banking.payment_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionLockServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private TransactionLockService transactionLockService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        transactionLockService = new TransactionLockService(redisTemplate);
    }

    @Test
    void acquireTransactionLock_Success_ReturnsTrue() {
        // Arrange
        Long accountId = 1L;
        String key = "transaction:account:" + accountId;
        when(valueOperations.setIfAbsent(eq(key), eq("LOCKED"), eq(12L), eq(TimeUnit.SECONDS)))
                .thenReturn(true);

        // Act
        boolean result = transactionLockService.acquireTransactionLock(accountId);

        // Assert
        assertTrue(result);
        verify(redisTemplate).opsForValue();
        verify(valueOperations).setIfAbsent(eq(key), eq("LOCKED"), eq(12L), eq(TimeUnit.SECONDS));
    }

    @Test
    void acquireTransactionLock_AlreadyLocked_ReturnsFalse() {
        // Arrange
        Long accountId = 1L;
        String key = "transaction:account:" + accountId;
        when(valueOperations.setIfAbsent(eq(key), eq("LOCKED"), eq(12L), eq(TimeUnit.SECONDS)))
                .thenReturn(false);

        // Act
        boolean result = transactionLockService.acquireTransactionLock(accountId);

        // Assert
        assertFalse(result);
        verify(redisTemplate).opsForValue();
        verify(valueOperations).setIfAbsent(eq(key), eq("LOCKED"), eq(12L), eq(TimeUnit.SECONDS));
    }

} 