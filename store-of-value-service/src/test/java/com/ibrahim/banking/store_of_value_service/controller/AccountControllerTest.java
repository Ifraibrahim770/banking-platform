package com.ibrahim.banking.store_of_value_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibrahim.banking.store_of_value_service.dto.AccountResponse;
import com.ibrahim.banking.store_of_value_service.dto.CreateAccountRequest;
import com.ibrahim.banking.store_of_value_service.dto.UpdateAccountRequest;
import com.ibrahim.banking.store_of_value_service.exception.AccountNotFoundException;
import com.ibrahim.banking.store_of_value_service.model.AccountStatus;
import com.ibrahim.banking.store_of_value_service.model.AccountType;
import com.ibrahim.banking.store_of_value_service.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class AccountControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private AccountController accountController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(accountController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // need this for datetime stuff
    }

    @Test
    void getAccountsByProfileId_Success() throws Exception {
        // setup
        String profileId = "profile123";
        
        AccountResponse account1 = new AccountResponse();
        account1.setId(1L);
        account1.setAccountNumber("1234567890");
        account1.setProfileId(profileId);
        account1.setAccountType(AccountType.SAVINGS);
        account1.setBalance(new BigDecimal("100.50"));
        account1.setStatus(AccountStatus.ACTIVE);
        account1.setCreatedAt(LocalDateTime.now());
        account1.setUpdatedAt(LocalDateTime.now());
        
        AccountResponse account2 = new AccountResponse();
        account2.setId(2L);
        account2.setAccountNumber("0987654321");
        account2.setProfileId(profileId);
        account2.setAccountType(AccountType.CURRENT);
        account2.setBalance(new BigDecimal("500.75"));
        account2.setStatus(AccountStatus.ACTIVE);
        account2.setCreatedAt(LocalDateTime.now());
        account2.setUpdatedAt(LocalDateTime.now());
        
        List<AccountResponse> accounts = Arrays.asList(account1, account2);
        
        when(accountService.getAccountsByProfileId(profileId)).thenReturn(accounts);
        
        // do it & check
        mockMvc.perform(get("/api/accounts/user/{profileId}", profileId)
                .with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].accountNumber", is("1234567890")))
                .andExpect(jsonPath("$[0].profileId", is(profileId)))
                .andExpect(jsonPath("$[1].accountNumber", is("0987654321")))
                .andExpect(jsonPath("$[1].profileId", is(profileId)));
        
        verify(accountService, times(1)).getAccountsByProfileId(profileId);
    }
    
    @Test
    void getAccountsByProfileId_NoAccountsFound() throws Exception {
        // setup
        String profileId = "profile-no-accounts";
        
        when(accountService.getAccountsByProfileId(profileId)).thenReturn(Collections.emptyList());
        
        // do it & check
        mockMvc.perform(get("/api/accounts/user/{profileId}", profileId)
                .with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        
        verify(accountService, times(1)).getAccountsByProfileId(profileId);
    }

    @Test
    void createAccount_Success() throws Exception {
        // setup
        CreateAccountRequest request = new CreateAccountRequest();
        request.setProfileId("profile123");
        request.setAccountType(AccountType.SAVINGS);

        AccountResponse response = new AccountResponse();
        response.setId(1L);
        response.setAccountNumber("1234567890");
        response.setProfileId("profile123");
        response.setAccountType(AccountType.SAVINGS);
        response.setBalance(BigDecimal.ZERO);
        response.setStatus(AccountStatus.PENDING_ACTIVATION);
        response.setCreatedAt(LocalDateTime.now());
        response.setUpdatedAt(LocalDateTime.now());

        when(accountService.createAccount(any(CreateAccountRequest.class))).thenReturn(response);

        // do it & check
        mockMvc.perform(post("/api/accounts")
                .with(user("admin").roles("ADMIN")) // fake admin user
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountNumber", is("1234567890")))
                .andExpect(jsonPath("$.profileId", is("profile123")))
                .andExpect(jsonPath("$.accountType", is("SAVINGS")))
                .andExpect(jsonPath("$.status", is("PENDING_ACTIVATION")));

        verify(accountService, times(1)).createAccount(any(CreateAccountRequest.class));
    }

    @Test
    void createAccount_ValidationFailure() throws Exception {
        // setup
        CreateAccountRequest request = new CreateAccountRequest();
        // missing stuff

        // do it & check
        mockMvc.perform(post("/api/accounts")
                .with(user("admin").roles("ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(accountService, never()).createAccount(any(CreateAccountRequest.class));
    }



    @Test
    void updateAccount_Success() throws Exception {
        // setup
        String accountNumber = "1234567890";
        UpdateAccountRequest request = new UpdateAccountRequest();
        request.setAccountType(AccountType.CURRENT);

        AccountResponse response = new AccountResponse();
        response.setId(1L);
        response.setAccountNumber(accountNumber);
        response.setProfileId("profile123");
        response.setAccountType(AccountType.CURRENT); // changed to CURRENT
        response.setBalance(BigDecimal.ZERO);
        response.setStatus(AccountStatus.ACTIVE);
        response.setCreatedAt(LocalDateTime.now());
        response.setUpdatedAt(LocalDateTime.now());

        when(accountService.updateAccount(eq(accountNumber), any(UpdateAccountRequest.class))).thenReturn(response);

        // do it & check
        mockMvc.perform(put("/api/accounts/{accountNumber}", accountNumber)
                .with(user("admin").roles("ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber", is(accountNumber)))
                .andExpect(jsonPath("$.accountType", is("CURRENT")));

        verify(accountService, times(1)).updateAccount(eq(accountNumber), any(UpdateAccountRequest.class));
    }

    @Test
    void updateAccount_NotFound() throws Exception {
        // setup
        String accountNumber = "nonexistent";
        UpdateAccountRequest request = new UpdateAccountRequest();
        request.setAccountType(AccountType.CURRENT);

        when(accountService.updateAccount(eq(accountNumber), any(UpdateAccountRequest.class)))
                .thenThrow(new AccountNotFoundException("Account not found with number: " + accountNumber));

        // do it & check
        mockMvc.perform(put("/api/accounts/{accountNumber}", accountNumber)
                .with(user("admin").roles("ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(accountService, times(1)).updateAccount(eq(accountNumber), any(UpdateAccountRequest.class));
    }

    @Test
    void getBalance_Success() throws Exception {
        // setup
        String accountNumber = "1234567890";
        BigDecimal balance = new BigDecimal("100.00");

        when(accountService.getBalance(accountNumber)).thenReturn(balance);

        // do it & check
        mockMvc.perform(get("/api/accounts/{accountNumber}/balance", accountNumber)
                .with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().string("100.00"));

        verify(accountService, times(1)).getBalance(accountNumber);
    }



    @Test
    void getBalance_AccountNotFound() throws Exception {
        // setup
        String accountNumber = "nonexistent";

        when(accountService.getBalance(accountNumber))
                .thenThrow(new AccountNotFoundException("Account not found with number: " + accountNumber));

        // do it & check
        mockMvc.perform(get("/api/accounts/{accountNumber}/balance", accountNumber)
                .with(user("user").roles("USER")))
                .andExpect(status().isNotFound());

        verify(accountService, times(1)).getBalance(accountNumber);
    }




    @Test
    void activateAccount_Success() throws Exception {
        // setup
        String accountNumber = "1234567890";

        AccountResponse response = new AccountResponse();
        response.setId(1L);
        response.setAccountNumber(accountNumber);
        response.setProfileId("profile123");
        response.setAccountType(AccountType.SAVINGS);
        response.setBalance(BigDecimal.ZERO);
        response.setStatus(AccountStatus.ACTIVE); // active now
        response.setCreatedAt(LocalDateTime.now());
        response.setUpdatedAt(LocalDateTime.now());

        when(accountService.activateAccount(accountNumber)).thenReturn(response);

        // do it & check
        mockMvc.perform(patch("/api/accounts/{accountNumber}/activate", accountNumber)
                .with(user("admin").roles("ADMIN"))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber", is(accountNumber)))
                .andExpect(jsonPath("$.status", is("ACTIVE")));

        verify(accountService, times(1)).activateAccount(accountNumber);
    }

    @Test
    void activateAccount_AlreadyActive() throws Exception {
        // setup
        String accountNumber = "1234567890";

        AccountResponse response = new AccountResponse();
        response.setId(1L);
        response.setAccountNumber(accountNumber);
        response.setProfileId("profile123");
        response.setAccountType(AccountType.SAVINGS);
        response.setBalance(BigDecimal.ZERO);
        response.setStatus(AccountStatus.ACTIVE); // already on
        response.setCreatedAt(LocalDateTime.now());
        response.setUpdatedAt(LocalDateTime.now());

        when(accountService.activateAccount(accountNumber)).thenReturn(response);

        // do it & check
        mockMvc.perform(patch("/api/accounts/{accountNumber}/activate", accountNumber)
                .with(user("admin").roles("ADMIN"))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ACTIVE")));

        verify(accountService, times(1)).activateAccount(accountNumber);
    }




    @Test
    void deactivateAccount_Success() throws Exception {
        // Arrange
        String accountNumber = "1234567890";

        AccountResponse response = new AccountResponse();
        response.setId(1L);
        response.setAccountNumber(accountNumber);
        response.setProfileId("profile123");
        response.setAccountType(AccountType.SAVINGS);
        response.setBalance(BigDecimal.ZERO);
        response.setStatus(AccountStatus.INACTIVE); // Deactivated status
        response.setCreatedAt(LocalDateTime.now());
        response.setUpdatedAt(LocalDateTime.now());

        when(accountService.deactivateAccount(accountNumber)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(patch("/api/accounts/{accountNumber}/deactivate", accountNumber)
                .with(user("admin").roles("ADMIN"))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber", is(accountNumber)))
                .andExpect(jsonPath("$.status", is("INACTIVE")));

        verify(accountService, times(1)).deactivateAccount(accountNumber);
    }

    @Test
    void deactivateAccount_AlreadyInactive() throws Exception {
        // Arrange
        String accountNumber = "1234567890";

        AccountResponse response = new AccountResponse();
        response.setId(1L);
        response.setAccountNumber(accountNumber);
        response.setProfileId("profile123");
        response.setAccountType(AccountType.SAVINGS);
        response.setBalance(BigDecimal.ZERO);
        response.setStatus(AccountStatus.INACTIVE); // Already inactive
        response.setCreatedAt(LocalDateTime.now());
        response.setUpdatedAt(LocalDateTime.now());

        when(accountService.deactivateAccount(accountNumber)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(patch("/api/accounts/{accountNumber}/deactivate", accountNumber)
                .with(user("admin").roles("ADMIN"))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("INACTIVE")));

        verify(accountService, times(1)).deactivateAccount(accountNumber);
    }


} 