package com.ibrahim.banking.store_of_value_service.service;

import com.ibrahim.banking.store_of_value_service.dto.CreateAccountRequest;
import com.ibrahim.banking.store_of_value_service.dto.AccountResponse;
import com.ibrahim.banking.store_of_value_service.dto.UpdateAccountRequest;
import com.ibrahim.banking.store_of_value_service.exception.AccountNotFoundException;
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
        newAccount.setBalance(BigDecimal.ZERO); // Start with zero balance
        newAccount.setStatus(AccountStatus.PENDING_ACTIVATION); // Initial status

        Account savedAccount = accountRepository.save(newAccount);
        logger.info("Account created successfully with ID: {} and Number: {}", savedAccount.getId(), savedAccount.getAccountNumber());
        return mapToAccountResponse(savedAccount);
    }

    @Transactional(readOnly = true)
    public BigDecimal getBalance(String accountNumber) {
        logger.debug("Fetching balance for account number: {}", accountNumber);
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with number: " + accountNumber));

       //Check if account is active
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

        // Add checks? e.g., cannot update if CLOSED?
        if (account.getStatus() == AccountStatus.CLOSED) {
             logger.error("Cannot update account {} because it is closed.", accountNumber);
             throw new IllegalStateException("Cannot update a closed account.");
        }

        account.setAccountType(request.getAccountType());


        Account updatedAccount = accountRepository.save(account);
        logger.info("Account {} updated successfully.", accountNumber);
        return mapToAccountResponse(updatedAccount);
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

    // --- Helper method for generating account number ---
    private String generateUniqueAccountNumber() {
        String accountNumber;
        do {

            long number = ThreadLocalRandom.current().nextLong(1_000_000_000L, 10_000_000_000L);
            accountNumber = String.valueOf(number);
        } while (accountRepository.findByAccountNumber(accountNumber).isPresent()); // Check for uniqueness
        return accountNumber;
    }
} 