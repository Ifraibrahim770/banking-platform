package com.ibrahim.banking.store_of_value_service;

import com.ibrahim.banking.store_of_value_service.model.Account;
import com.ibrahim.banking.store_of_value_service.model.AccountStatus;
import com.ibrahim.banking.store_of_value_service.model.AccountType;
import com.ibrahim.banking.store_of_value_service.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner initData(AccountRepository accountRepository) {
        return args -> {
            // check if we already have accounts for profile 1 and 2
            List<Account> profile1Accounts = accountRepository.findByProfileId("1");
            List<Account> profile2Accounts = accountRepository.findByProfileId("2");
            
            if (profile1Accounts.isEmpty()) {
                createSampleAccount(accountRepository, "1", generateAccountNumber("1"));
                logger.info("Sample account created for profile ID: 1");
            } else {
                logger.info("Accounts already exist for profile ID: 1, skipping initialization");
            }
            
            if (profile2Accounts.isEmpty()) {
                createSampleAccount(accountRepository, "2", generateAccountNumber("2"));
                logger.info("Sample account created for profile ID: 2");
            } else {
                logger.info("Accounts already exist for profile ID: 2, skipping initialization");
            }
        };
    }
    
    private void createSampleAccount(AccountRepository repository, String profileId, String accountNumber) {
        Account account = new Account();
        account.setProfileId(profileId);
        account.setAccountNumber(accountNumber);
        account.setAccountType(AccountType.SAVINGS);
        account.setBalance(new BigDecimal("100000.00")); // 100k initial balance
        account.setStatus(AccountStatus.ACTIVE);
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());
        
        repository.save(account);
    }
    
    private String generateAccountNumber(String profileId) {
        return String.format("1234567"+profileId);
    }
} 