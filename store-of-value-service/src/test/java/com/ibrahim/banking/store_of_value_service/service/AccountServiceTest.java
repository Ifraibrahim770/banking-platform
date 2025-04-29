package com.ibrahim.banking.store_of_value_service.service;

import com.ibrahim.banking.store_of_value_service.dto.AccountResponse;
import com.ibrahim.banking.store_of_value_service.dto.CreateAccountRequest;
import com.ibrahim.banking.store_of_value_service.dto.UpdateAccountRequest;
import com.ibrahim.banking.store_of_value_service.exception.AccountNotFoundException;
import com.ibrahim.banking.store_of_value_service.model.Account;
import com.ibrahim.banking.store_of_value_service.model.AccountStatus;
import com.ibrahim.banking.store_of_value_service.model.AccountType;
import com.ibrahim.banking.store_of_value_service.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Initialize mocks
public class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks // Inject mocks into this service instance
    private AccountService accountService;

    private Account sampleAccount;
    private String existingAccountNumber = "1112223330";
    private String nonExistingAccountNumber = "0000000000";

    @BeforeEach
    void setUp() {
        sampleAccount = new Account();
        sampleAccount.setId(1L);
        sampleAccount.setAccountNumber(existingAccountNumber);
        sampleAccount.setProfileId("profile-test-1");
        sampleAccount.setBalance(new BigDecimal("100.50"));
        sampleAccount.setAccountType(AccountType.SAVINGS);
        sampleAccount.setStatus(AccountStatus.PENDING_ACTIVATION);
        sampleAccount.setCreatedAt(LocalDateTime.now().minusDays(1));
        sampleAccount.setUpdatedAt(LocalDateTime.now());
    }

    // --- createAccount Tests ---
    @Test
    void createAccount_shouldCreateAndReturnAccount() {
        // given
        CreateAccountRequest request = new CreateAccountRequest();
        request.setProfileId("profile-new");
        request.setAccountType(AccountType.CURRENT);

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);

        // Mock repository: unique number generated, save successful
        when(accountRepository.findByAccountNumber(anyString())).thenReturn(Optional.empty());
        when(accountRepository.save(accountCaptor.capture())).thenAnswer(invocation -> {
            Account accountToSave = invocation.getArgument(0);
            accountToSave.setId(99L); // Simulate ID generation
            accountToSave.setCreatedAt(LocalDateTime.now());
            accountToSave.setUpdatedAt(LocalDateTime.now());
            return accountToSave;
        });

        // when
        AccountResponse response = accountService.createAccount(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getProfileId()).isEqualTo(request.getProfileId());
        assertThat(response.getAccountType()).isEqualTo(request.getAccountType());
        assertThat(response.getStatus()).isEqualTo(AccountStatus.PENDING_ACTIVATION);
        assertThat(response.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getAccountNumber()).isNotNull().hasSize(10); // Check generated number

        verify(accountRepository, times(1)).findByAccountNumber(anyString()); // Verify uniqueness check
        verify(accountRepository, times(1)).save(any(Account.class));

        Account savedAccount = accountCaptor.getValue();
        assertThat(savedAccount.getProfileId()).isEqualTo(request.getProfileId());
        assertThat(savedAccount.getAccountType()).isEqualTo(request.getAccountType());
        assertThat(savedAccount.getStatus()).isEqualTo(AccountStatus.PENDING_ACTIVATION);
        assertThat(savedAccount.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(savedAccount.getAccountNumber()).isEqualTo(response.getAccountNumber());
    }

    // --- getBalance Tests ---
    @Test
    void getBalance_whenAccountExists_shouldReturnBalance() {
        // given
        sampleAccount.setStatus(AccountStatus.ACTIVE);
        when(accountRepository.findByAccountNumber(existingAccountNumber)).thenReturn(Optional.of(sampleAccount));

        // when
        BigDecimal balance = accountService.getBalance(existingAccountNumber);

        // then
        assertThat(balance).isEqualByComparingTo(sampleAccount.getBalance());
        verify(accountRepository, times(1)).findByAccountNumber(existingAccountNumber);
    }

    @Test
    void getBalance_whenAccountNotFound_shouldThrowAccountNotFoundException() {
        // given
        when(accountRepository.findByAccountNumber(nonExistingAccountNumber)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> accountService.getBalance(nonExistingAccountNumber))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining(nonExistingAccountNumber);
        verify(accountRepository, times(1)).findByAccountNumber(nonExistingAccountNumber);
    }

    // --- activateAccount Tests ---
    @Test
    void activateAccount_whenPending_shouldActivateAndReturnAccount() {
        // given
        sampleAccount.setStatus(AccountStatus.PENDING_ACTIVATION);
        when(accountRepository.findByAccountNumber(existingAccountNumber)).thenReturn(Optional.of(sampleAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(sampleAccount); // Simulate save returning updated obj

        // when
        AccountResponse response = accountService.activateAccount(existingAccountNumber);

        // then
        assertThat(response.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        verify(accountRepository, times(1)).findByAccountNumber(existingAccountNumber);
        verify(accountRepository, times(1)).save(sampleAccount);
        assertThat(sampleAccount.getStatus()).isEqualTo(AccountStatus.ACTIVE); // Verify state change before save
    }

    @Test
    void activateAccount_whenAlreadyActive_shouldReturnCurrentAccount() {
        // given
        sampleAccount.setStatus(AccountStatus.ACTIVE);
        when(accountRepository.findByAccountNumber(existingAccountNumber)).thenReturn(Optional.of(sampleAccount));

        // when
        AccountResponse response = accountService.activateAccount(existingAccountNumber);

        // then
        assertThat(response.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        verify(accountRepository, times(1)).findByAccountNumber(existingAccountNumber);
        verify(accountRepository, never()).save(any(Account.class)); // Should not save if already active
    }

    @Test
    void activateAccount_whenInactive_shouldThrowIllegalStateException() {
        // given
        sampleAccount.setStatus(AccountStatus.INACTIVE);
        when(accountRepository.findByAccountNumber(existingAccountNumber)).thenReturn(Optional.of(sampleAccount));

        // when / then
        assertThatThrownBy(() -> accountService.activateAccount(existingAccountNumber))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be activated");
        verify(accountRepository, times(1)).findByAccountNumber(existingAccountNumber);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void activateAccount_whenNotFound_shouldThrowAccountNotFoundException() {
        // given
        when(accountRepository.findByAccountNumber(nonExistingAccountNumber)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> accountService.activateAccount(nonExistingAccountNumber))
                .isInstanceOf(AccountNotFoundException.class);
        verify(accountRepository, times(1)).findByAccountNumber(nonExistingAccountNumber);
        verify(accountRepository, never()).save(any(Account.class));
    }

    // --- deactivateAccount Tests ---
    @Test
    void deactivateAccount_whenActive_shouldDeactivateAndReturnAccount() {
        // given
        sampleAccount.setStatus(AccountStatus.ACTIVE);
        when(accountRepository.findByAccountNumber(existingAccountNumber)).thenReturn(Optional.of(sampleAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(sampleAccount);

        // when
        AccountResponse response = accountService.deactivateAccount(existingAccountNumber);

        // then
        assertThat(response.getStatus()).isEqualTo(AccountStatus.INACTIVE);
        verify(accountRepository, times(1)).findByAccountNumber(existingAccountNumber);
        verify(accountRepository, times(1)).save(sampleAccount);
        assertThat(sampleAccount.getStatus()).isEqualTo(AccountStatus.INACTIVE);
    }

    @Test
    void deactivateAccount_whenAlreadyInactive_shouldReturnCurrentAccount() {
        // given
        sampleAccount.setStatus(AccountStatus.INACTIVE);
        when(accountRepository.findByAccountNumber(existingAccountNumber)).thenReturn(Optional.of(sampleAccount));

        // when
        AccountResponse response = accountService.deactivateAccount(existingAccountNumber);

        // then
        assertThat(response.getStatus()).isEqualTo(AccountStatus.INACTIVE);
        verify(accountRepository, times(1)).findByAccountNumber(existingAccountNumber);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void deactivateAccount_whenPending_shouldThrowIllegalStateException() {
        // given
        sampleAccount.setStatus(AccountStatus.PENDING_ACTIVATION);
        when(accountRepository.findByAccountNumber(existingAccountNumber)).thenReturn(Optional.of(sampleAccount));

        // when / then
        assertThatThrownBy(() -> accountService.deactivateAccount(existingAccountNumber))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be ACTIVE");
        verify(accountRepository, times(1)).findByAccountNumber(existingAccountNumber);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void deactivateAccount_whenNotFound_shouldThrowAccountNotFoundException() {
        // given
        when(accountRepository.findByAccountNumber(nonExistingAccountNumber)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> accountService.deactivateAccount(nonExistingAccountNumber))
                .isInstanceOf(AccountNotFoundException.class);
        verify(accountRepository, times(1)).findByAccountNumber(nonExistingAccountNumber);
        verify(accountRepository, never()).save(any(Account.class));
    }

    // --- updateAccount Tests ---
    @Test
    void updateAccount_whenExistsAndNotClosed_shouldUpdateAndReturnAccount() {
        // given
        sampleAccount.setStatus(AccountStatus.ACTIVE); // Can update ACTIVE account
        UpdateAccountRequest request = new UpdateAccountRequest();
        request.setAccountType(AccountType.CURRENT);
        when(accountRepository.findByAccountNumber(existingAccountNumber)).thenReturn(Optional.of(sampleAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(sampleAccount);
        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);

        // when
        AccountResponse response = accountService.updateAccount(existingAccountNumber, request);

        // then
        assertThat(response.getAccountType()).isEqualTo(AccountType.CURRENT);
        verify(accountRepository, times(1)).findByAccountNumber(existingAccountNumber);
        verify(accountRepository, times(1)).save(accountCaptor.capture());
        assertThat(accountCaptor.getValue().getAccountType()).isEqualTo(AccountType.CURRENT);
    }

    @Test
    void updateAccount_whenClosed_shouldThrowIllegalStateException() {
        // given
        sampleAccount.setStatus(AccountStatus.CLOSED);
        UpdateAccountRequest request = new UpdateAccountRequest();
        request.setAccountType(AccountType.CURRENT);
        when(accountRepository.findByAccountNumber(existingAccountNumber)).thenReturn(Optional.of(sampleAccount));

        // when / then
        assertThatThrownBy(() -> accountService.updateAccount(existingAccountNumber, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("closed account");
        verify(accountRepository, times(1)).findByAccountNumber(existingAccountNumber);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void updateAccount_whenNotFound_shouldThrowAccountNotFoundException() {
        // given
        UpdateAccountRequest request = new UpdateAccountRequest();
        request.setAccountType(AccountType.CURRENT);
        when(accountRepository.findByAccountNumber(nonExistingAccountNumber)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> accountService.updateAccount(nonExistingAccountNumber, request))
                .isInstanceOf(AccountNotFoundException.class);
        verify(accountRepository, times(1)).findByAccountNumber(nonExistingAccountNumber);
        verify(accountRepository, never()).save(any(Account.class));
    }

} 