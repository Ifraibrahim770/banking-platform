package com.ibrahim.banking.store_of_value_service.repository;

import com.ibrahim.banking.store_of_value_service.model.Account;
import com.ibrahim.banking.store_of_value_service.model.AccountStatus;
import com.ibrahim.banking.store_of_value_service.model.AccountType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest // Configure H2, JPA, etc. for testing repository layer
public class AccountRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void whenFindByAccountNumber_thenReturnAccount() {
        // given
        Account account = new Account();
        account.setAccountNumber("1234567890");
        account.setProfileId("profile-1");
        account.setAccountType(AccountType.SAVINGS);
        account.setBalance(BigDecimal.TEN);
        account.setStatus(AccountStatus.ACTIVE);
        entityManager.persist(account);
        entityManager.flush();

        // when
        Optional<Account> found = accountRepository.findByAccountNumber("1234567890");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getAccountNumber()).isEqualTo(account.getAccountNumber());
        assertThat(found.get().getProfileId()).isEqualTo(account.getProfileId());
        assertThat(found.get().getBalance()).isEqualByComparingTo(account.getBalance());
    }

    @Test
    void whenFindByAccountNumber_withNonExistingNumber_thenReturnEmpty() {
        // when
        Optional<Account> found = accountRepository.findByAccountNumber("0000000000");

        // then
        assertThat(found).isNotPresent();
    }
} 