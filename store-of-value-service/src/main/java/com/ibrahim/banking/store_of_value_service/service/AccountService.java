package com.ibrahim.banking.store_of_value_service.service;

import com.ibrahim.banking.store_of_value_service.dto.CreateAccountRequest;
import com.ibrahim.banking.store_of_value_service.dto.AccountResponse;
import com.ibrahim.banking.store_of_value_service.dto.UpdateAccountRequest;
import com.ibrahim.banking.store_of_value_service.dto.TransactionRequest;
import com.ibrahim.banking.store_of_value_service.dto.AccountStatusResponse;
import com.ibrahim.banking.store_of_value_service.exception.AccountNotFoundException;
import com.ibrahim.banking.store_of_value_service.exception.InsufficientFundsException;
import com.ibrahim.banking.store_of_value_service.model.Account;
import com.ibrahim.banking.store_of_value_service.model.AccountStatus;
import com.ibrahim.banking.store_of_value_service.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);
    private final AccountRepository accountRepository;


    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        logger.info("Attempting to create account for profileId: {}", request.getProfileId());

        String newAccountNumber = generateUniqueAccountNumber();
        logger.info("Generated unique account number: {}", newAccountNumber);

        Account newAccount = new Account();
        newAccount.setAccountNumber(newAccountNumber);
        newAccount.setProfileId(request.getProfileId());
        newAccount.setAccountType(request.getAccountType());
        newAccount.setBalance(BigDecimal.ZERO); // start with 0
        newAccount.setStatus(AccountStatus.PENDING_ACTIVATION); // set initial status

        Account savedAccount = accountRepository.save(newAccount);
        logger.info("Account created successfully with ID: {} and Number: {}", savedAccount.getId(), savedAccount.getAccountNumber());
        return mapToAccountResponse(savedAccount);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsByProfileId(String profileId) {
        logger.info("Fetching all accounts for profileId: {}", profileId);
        List<Account> accounts = accountRepository.findByProfileId(profileId);
        logger.info("Found {} accounts for profileId: {}", accounts.size(), profileId);
        return accounts.stream()
                .map(this::mapToAccountResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BigDecimal getBalance(String accountNumber) {
        logger.debug("Fetching balance for account number: {}", accountNumber);
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with number: " + accountNumber));

       // gotta check if account is active first
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Account is not active.");
         }

        logger.debug("Balance found for account number {}: {}", accountNumber, account.getBalance());
        return account.getBalance();
    }

    @Transactional
    public AccountResponse activateAccount(String accountNumber) {
        logger.info("Attempting to activate account number: {}", accountNumber);
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with number: " + accountNumber));

        if (account.getStatus() == AccountStatus.ACTIVE) {
             logger.warn("Account {} is already active. Returning current state.", accountNumber);
             return mapToAccountResponse(account);
        } else if (account.getStatus() != AccountStatus.PENDING_ACTIVATION) {
             logger.error("Cannot activate account {} in state: {}", accountNumber, account.getStatus());
             throw new IllegalStateException("Account cannot be activated from its current state: " + account.getStatus());
        }

        account.setStatus(AccountStatus.ACTIVE);
        Account updatedAccount = accountRepository.save(account);
        logger.info("Account {} activated successfully.", accountNumber);
        return mapToAccountResponse(updatedAccount);
    }

    @Transactional
    public AccountResponse deactivateAccount(String accountNumber) {
        logger.info("Attempting to deactivate account number: {}", accountNumber);
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with number: " + accountNumber));

         if (account.getStatus() == AccountStatus.INACTIVE) {
             logger.warn("Account {} is already inactive. Returning current state.", accountNumber);
             return mapToAccountResponse(account);
        } else if (account.getStatus() != AccountStatus.ACTIVE) {
             logger.error("Cannot deactivate account {} in state: {}", accountNumber, account.getStatus());
            throw new IllegalStateException("Account must be ACTIVE to be deactivated. Current state: " + account.getStatus());
        }


        account.setStatus(AccountStatus.INACTIVE);
        Account updatedAccount = accountRepository.save(account);
        logger.info("Account {} deactivated successfully.", accountNumber);
        return mapToAccountResponse(updatedAccount);
    }

    @Transactional
    public AccountResponse updateAccount(String accountNumber, UpdateAccountRequest request) {
        logger.info("Attempting to update account type for account number: {}", accountNumber);
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with number: " + accountNumber));

        // cant update closed accounts duh
        if (account.getStatus() == AccountStatus.CLOSED) {
             logger.error("Cannot update account {} because it is closed.", accountNumber);
             throw new IllegalStateException("Cannot update a closed account.");
        }

        account.setAccountType(request.getAccountType());


        Account updatedAccount = accountRepository.save(account);
        logger.info("Account {} updated successfully.", accountNumber);
        return mapToAccountResponse(updatedAccount);
    }

    @Transactional
    public AccountResponse creditAccount(String accountNumber, TransactionRequest request) {
        logger.info("Attempting to credit account number: {} with amount: {}", accountNumber, request.getAmount());
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with number: " + accountNumber));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            logger.error("Cannot credit account {} because it is not active. Current status: {}", accountNumber, account.getStatus());
            throw new IllegalStateException("Account must be ACTIVE to be credited. Current state: " + account.getStatus());
        }

        // add money to balance
        BigDecimal newBalance = account.getBalance().add(request.getAmount());
        account.setBalance(newBalance);

        Account updatedAccount = accountRepository.save(account);
        logger.info("Account {} credited successfully. New balance: {}", accountNumber, updatedAccount.getBalance());
        return mapToAccountResponse(updatedAccount);
    }

    @Transactional
    public AccountResponse debitAccount(String accountNumber, TransactionRequest request) {
        logger.info("Attempting to debit account number: {} with amount: {}", accountNumber, request.getAmount());
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with number: " + accountNumber));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            logger.error("Cannot debit account {} because it is not active. Current status: {}", accountNumber, account.getStatus());
            throw new IllegalStateException("Account must be ACTIVE to be debited. Current state: " + account.getStatus());
        }

        // check if enough money
        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            logger.error("Insufficient funds in account {}. Current balance: {}, Requested amount: {}", 
                accountNumber, account.getBalance(), request.getAmount());
            throw new InsufficientFundsException("Insufficient funds for this transaction. Current balance: " + account.getBalance());
        }

        // take out money from balance
        BigDecimal newBalance = account.getBalance().subtract(request.getAmount());
        account.setBalance(newBalance);

        Account updatedAccount = accountRepository.save(account);
        logger.info("Account {} debited successfully. New balance: {}", accountNumber, updatedAccount.getBalance());
        return mapToAccountResponse(updatedAccount);
    }

    @Transactional(readOnly = true)
    public AccountStatusResponse getAccountStatus(String accountNumber) {
        logger.debug("Fetching status for account number: {}", accountNumber);
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with number: " + accountNumber));

        logger.debug("Status for account number {}: {}", accountNumber, account.getStatus());
        return new AccountStatusResponse(accountNumber, account.getStatus());
    }

    private AccountResponse mapToAccountResponse(Account account) {
        AccountResponse response = new AccountResponse();
        response.setId(account.getId());
        response.setAccountNumber(account.getAccountNumber());
        response.setProfileId(account.getProfileId());
        response.setAccountType(account.getAccountType());
        response.setBalance(account.getBalance());
        response.setStatus(account.getStatus());
        response.setCreatedAt(account.getCreatedAt());
        response.setUpdatedAt(account.getUpdatedAt());
        return response;
    }

    // helper for making account numbers
    private String generateUniqueAccountNumber() {
        String accountNumber;
        do {

            long number = ThreadLocalRandom.current().nextLong(1_000_000_000L, 10_000_000_000L);
            accountNumber = String.valueOf(number);
        } while (accountRepository.findByAccountNumber(accountNumber).isPresent()); // make sure its unique
        return accountNumber;
    }
} 