package com.ibrahim.banking.payment_service.repository;

import com.ibrahim.banking.payment_service.model.Transaction;
import com.ibrahim.banking.payment_service.model.TransactionStatus;
import com.ibrahim.banking.payment_service.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    Optional<Transaction> findByTransactionReference(String transactionReference);
    
    List<Transaction> findByUserId(Long userId);
    
    List<Transaction> findBySourceAccountId(Long accountId);
    
    List<Transaction> findByDestinationAccountId(Long accountId);
    
    List<Transaction> findByStatus(TransactionStatus status);
    
    List<Transaction> findByType(TransactionType type);
    
    List<Transaction> findByCreatedAtBetween(Instant startDate, Instant endDate);
    
    List<Transaction> findByUserIdAndCreatedAtBetween(Long userId, Instant startDate, Instant endDate);
    
    List<Transaction> findBySourceAccountIdAndCreatedAtBetween(Long accountId, Instant startDate, Instant endDate);
} 