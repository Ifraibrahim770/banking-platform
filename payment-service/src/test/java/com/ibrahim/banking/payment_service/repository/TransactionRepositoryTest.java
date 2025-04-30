package com.ibrahim.banking.payment_service.repository;

import com.ibrahim.banking.payment_service.model.Transaction;
import com.ibrahim.banking.payment_service.model.TransactionStatus;
import com.ibrahim.banking.payment_service.model.TransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class TransactionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    void findByTransactionReference_ExistingReference_ReturnsTransaction() {
        // Arrange
        String reference = "TXN-12345678";
        Transaction transaction = createTransaction(reference, TransactionType.DEPOSIT);
        entityManager.persist(transaction);
        entityManager.flush();

        // Act
        Optional<Transaction> result = transactionRepository.findByTransactionReference(reference);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(reference, result.get().getTransactionReference());
    }

    @Test
    void findByTransactionReference_NonExistingReference_ReturnsEmptyOptional() {
        // Arrange
        String reference = "TXN-NONEXISTENT";

        // Act
        Optional<Transaction> result = transactionRepository.findByTransactionReference(reference);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void findByUserId_ExistingUserId_ReturnsTransactions() {
        // Arrange
        Long userId = 1L;
        Transaction transaction1 = createTransaction("TXN-11111111", TransactionType.DEPOSIT);
        transaction1.setUserId(userId);
        Transaction transaction2 = createTransaction("TXN-22222222", TransactionType.WITHDRAWAL);
        transaction2.setUserId(userId);
        Transaction transaction3 = createTransaction("TXN-33333333", TransactionType.DEPOSIT);
        transaction3.setUserId(2L); // Different user

        entityManager.persist(transaction1);
        entityManager.persist(transaction2);
        entityManager.persist(transaction3);
        entityManager.flush();

        // Act
        List<Transaction> result = transactionRepository.findByUserId(userId);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(t -> t.getUserId().equals(userId)));
        assertEquals(1, result.stream().filter(t -> t.getTransactionReference().equals("TXN-11111111")).count());
        assertEquals(1, result.stream().filter(t -> t.getTransactionReference().equals("TXN-22222222")).count());
    }

    @Test
    void findBySourceAccountId_ExistingAccountId_ReturnsTransactions() {
        // Arrange
        Long accountId = 1L;
        Transaction transaction1 = createTransaction("TXN-11111111", TransactionType.DEPOSIT);
        transaction1.setSourceAccountId(accountId);
        Transaction transaction2 = createTransaction("TXN-22222222", TransactionType.WITHDRAWAL);
        transaction2.setSourceAccountId(accountId);
        Transaction transaction3 = createTransaction("TXN-33333333", TransactionType.DEPOSIT);
        transaction3.setSourceAccountId(2L); // Different account

        entityManager.persist(transaction1);
        entityManager.persist(transaction2);
        entityManager.persist(transaction3);
        entityManager.flush();

        // Act
        List<Transaction> result = transactionRepository.findBySourceAccountId(accountId);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(t -> t.getSourceAccountId().equals(accountId)));
        assertEquals(1, result.stream().filter(t -> t.getTransactionReference().equals("TXN-11111111")).count());
        assertEquals(1, result.stream().filter(t -> t.getTransactionReference().equals("TXN-22222222")).count());
    }

    @Test
    void findByDestinationAccountId_ExistingAccountId_ReturnsTransactions() {
        // Arrange
        Long accountId = 2L;
        Transaction transaction1 = createTransaction("TXN-11111111", TransactionType.TRANSFER);
        transaction1.setSourceAccountId(1L);
        transaction1.setDestinationAccountId(accountId);
        Transaction transaction2 = createTransaction("TXN-22222222", TransactionType.TRANSFER);
        transaction2.setSourceAccountId(3L);
        transaction2.setDestinationAccountId(accountId);
        Transaction transaction3 = createTransaction("TXN-33333333", TransactionType.TRANSFER);
        transaction3.setSourceAccountId(1L);
        transaction3.setDestinationAccountId(3L); // Different destination account

        entityManager.persist(transaction1);
        entityManager.persist(transaction2);
        entityManager.persist(transaction3);
        entityManager.flush();

        // Act
        List<Transaction> result = transactionRepository.findByDestinationAccountId(accountId);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(t -> t.getDestinationAccountId().equals(accountId)));
        assertEquals(1, result.stream().filter(t -> t.getTransactionReference().equals("TXN-11111111")).count());
        assertEquals(1, result.stream().filter(t -> t.getTransactionReference().equals("TXN-22222222")).count());
    }

    @Test
    void findByStatus_ExistingStatus_ReturnsTransactions() {
        // Arrange
        Transaction transaction1 = createTransaction("TXN-11111111", TransactionType.DEPOSIT);
        transaction1.setStatus(TransactionStatus.COMPLETED);
        Transaction transaction2 = createTransaction("TXN-22222222", TransactionType.WITHDRAWAL);
        transaction2.setStatus(TransactionStatus.COMPLETED);
        Transaction transaction3 = createTransaction("TXN-33333333", TransactionType.DEPOSIT);
        transaction3.setStatus(TransactionStatus.PENDING);

        entityManager.persist(transaction1);
        entityManager.persist(transaction2);
        entityManager.persist(transaction3);
        entityManager.flush();

        // Act
        List<Transaction> result = transactionRepository.findByStatus(TransactionStatus.COMPLETED);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(t -> t.getStatus() == TransactionStatus.COMPLETED));
        assertEquals(1, result.stream().filter(t -> t.getTransactionReference().equals("TXN-11111111")).count());
        assertEquals(1, result.stream().filter(t -> t.getTransactionReference().equals("TXN-22222222")).count());
    }

    @Test
    void findByType_ExistingType_ReturnsTransactions() {
        // Arrange
        Transaction transaction1 = createTransaction("TXN-11111111", TransactionType.DEPOSIT);
        Transaction transaction2 = createTransaction("TXN-22222222", TransactionType.DEPOSIT);
        Transaction transaction3 = createTransaction("TXN-33333333", TransactionType.WITHDRAWAL);

        entityManager.persist(transaction1);
        entityManager.persist(transaction2);
        entityManager.persist(transaction3);
        entityManager.flush();

        // Act
        List<Transaction> result = transactionRepository.findByType(TransactionType.DEPOSIT);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(t -> t.getType() == TransactionType.DEPOSIT));
        assertEquals(1, result.stream().filter(t -> t.getTransactionReference().equals("TXN-11111111")).count());
        assertEquals(1, result.stream().filter(t -> t.getTransactionReference().equals("TXN-22222222")).count());
    }

    @Test
    void findByCreatedAtBetween_DateRange_ReturnsTransactions() {
        // Arrange
        Instant now = Instant.now();
        Instant oneDayAgo = now.minus(1, ChronoUnit.DAYS);
        Instant twoDaysAgo = now.minus(2, ChronoUnit.DAYS);
        Instant threeDaysAgo = now.minus(3, ChronoUnit.DAYS);

        Transaction transaction1 = createTransaction("TXN-11111111", TransactionType.DEPOSIT);
        transaction1.setCreatedAt(oneDayAgo);
        Transaction transaction2 = createTransaction("TXN-22222222", TransactionType.WITHDRAWAL);
        transaction2.setCreatedAt(twoDaysAgo);
        Transaction transaction3 = createTransaction("TXN-33333333", TransactionType.DEPOSIT);
        transaction3.setCreatedAt(threeDaysAgo);

        entityManager.persist(transaction1);
        entityManager.persist(transaction2);
        entityManager.persist(transaction3);
        entityManager.flush();

        // Act
        List<Transaction> result = transactionRepository.findByCreatedAtBetween(
                twoDaysAgo.minus(1, ChronoUnit.HOURS), 
                oneDayAgo.plus(1, ChronoUnit.HOURS)
        );

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(t -> t.getTransactionReference().equals("TXN-11111111")));
        assertTrue(result.stream().anyMatch(t -> t.getTransactionReference().equals("TXN-22222222")));
    }

    @Test
    void findByUserIdAndCreatedAtBetween_ExistingUserAndDateRange_ReturnsTransactions() {
        // Arrange
        Long userId = 1L;
        Instant now = Instant.now();
        Instant oneDayAgo = now.minus(1, ChronoUnit.DAYS);
        Instant twoDaysAgo = now.minus(2, ChronoUnit.DAYS);
        Instant threeDaysAgo = now.minus(3, ChronoUnit.DAYS);

        Transaction transaction1 = createTransaction("TXN-11111111", TransactionType.DEPOSIT);
        transaction1.setUserId(userId);
        transaction1.setCreatedAt(oneDayAgo);
        
        Transaction transaction2 = createTransaction("TXN-22222222", TransactionType.WITHDRAWAL);
        transaction2.setUserId(userId);
        transaction2.setCreatedAt(twoDaysAgo);
        
        Transaction transaction3 = createTransaction("TXN-33333333", TransactionType.DEPOSIT);
        transaction3.setUserId(2L); // Different user
        transaction3.setCreatedAt(oneDayAgo);
        
        Transaction transaction4 = createTransaction("TXN-44444444", TransactionType.WITHDRAWAL);
        transaction4.setUserId(userId);
        transaction4.setCreatedAt(threeDaysAgo); // Outside date range

        entityManager.persist(transaction1);
        entityManager.persist(transaction2);
        entityManager.persist(transaction3);
        entityManager.persist(transaction4);
        entityManager.flush();

        // Act
        List<Transaction> result = transactionRepository.findByUserIdAndCreatedAtBetween(
                userId,
                twoDaysAgo.minus(1, ChronoUnit.HOURS), 
                oneDayAgo.plus(1, ChronoUnit.HOURS)
        );

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(t -> t.getUserId().equals(userId)));
        assertTrue(result.stream().anyMatch(t -> t.getTransactionReference().equals("TXN-11111111")));
        assertTrue(result.stream().anyMatch(t -> t.getTransactionReference().equals("TXN-22222222")));
    }

    private Transaction createTransaction(String reference, TransactionType type) {
        Transaction transaction = new Transaction();
        transaction.setTransactionReference(reference);
        transaction.setType(type);
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setSourceAccountId(1L);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setCurrency("USD");
        transaction.setDescription("Test transaction");
        transaction.setCreatedAt(Instant.now());
        transaction.setUserId(1L);
        return transaction;
    }
} 