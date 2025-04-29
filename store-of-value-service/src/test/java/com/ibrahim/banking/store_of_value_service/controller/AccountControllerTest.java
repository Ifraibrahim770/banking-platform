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

import static org.hamcrest.Matchers.is;
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
        objectMapper.findAndRegisterModules(); // Required for LocalDateTime serialization
    }

    @Test
    void createAccount_Success() throws Exception {
        // Arrange
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

        // Act & Assert
        mockMvc.perform(post("/api/accounts")
                .with(user("admin").roles("ADMIN")) // Mock admin user
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
        // Arrange
        CreateAccountRequest request = new CreateAccountRequest();
        // Missing required fields

        // Act & Assert
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
        // Arrange
        String accountNumber = "1234567890";
        UpdateAccountRequest request = new UpdateAccountRequest();
        request.setAccountType(AccountType.CURRENT);

        AccountResponse response = new AccountResponse();
        response.setId(1L);
        response.setAccountNumber(accountNumber);
        response.setProfileId("profile123");
        response.setAccountType(AccountType.CURRENT); // Updated type
        response.setBalance(BigDecimal.ZERO);
        response.setStatus(AccountStatus.ACTIVE);
        response.setCreatedAt(LocalDateTime.now());
        response.setUpdatedAt(LocalDateTime.now());

        when(accountService.updateAccount(eq(accountNumber), any(UpdateAccountRequest.class))).thenReturn(response);

        // Act & Assert
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
        // Arrange
        String accountNumber = "nonexistent";
        UpdateAccountRequest request = new UpdateAccountRequest();
        request.setAccountType(AccountType.CURRENT);

        when(accountService.updateAccount(eq(accountNumber), any(UpdateAccountRequest.class)))
                .thenThrow(new AccountNotFoundException("Account not found with number: " + accountNumber));

        // Act & Assert
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
        // Arrange
        String accountNumber = "1234567890";
        BigDecimal balance = new BigDecimal("100.00");

        when(accountService.getBalance(accountNumber)).thenReturn(balance);

        // Act & Assert
        mockMvc.perform(get("/api/accounts/{accountNumber}/balance", accountNumber)
                .with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().string("100.00"));

        verify(accountService, times(1)).getBalance(accountNumber);
    }



    @Test
    void getBalance_AccountNotFound() throws Exception {
        // Arrange
        String accountNumber = "nonexistent";

        when(accountService.getBalance(accountNumber))
                .thenThrow(new AccountNotFoundException("Account not found with number: " + accountNumber));

        // Act & Assert
        mockMvc.perform(get("/api/accounts/{accountNumber}/balance", accountNumber)
                .with(user("user").roles("USER")))
                .andExpect(status().isNotFound());

        verify(accountService, times(1)).getBalance(accountNumber);
    }




    @Test
    void activateAccount_Success() throws Exception {
        // Arrange
        String accountNumber = "1234567890";

        AccountResponse response = new AccountResponse();
        response.setId(1L);
        response.setAccountNumber(accountNumber);
        response.setProfileId("profile123");
        response.setAccountType(AccountType.SAVINGS);
        response.setBalance(BigDecimal.ZERO);
        response.setStatus(AccountStatus.ACTIVE); // Activated status
        response.setCreatedAt(LocalDateTime.now());
        response.setUpdatedAt(LocalDateTime.now());

        when(accountService.activateAccount(accountNumber)).thenReturn(response);

        // Act & Assert
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
        // Arrange
        String accountNumber = "1234567890";

        AccountResponse response = new AccountResponse();
        response.setId(1L);
        response.setAccountNumber(accountNumber);
        response.setProfileId("profile123");
        response.setAccountType(AccountType.SAVINGS);
        response.setBalance(BigDecimal.ZERO);
        response.setStatus(AccountStatus.ACTIVE); // Already active
        response.setCreatedAt(LocalDateTime.now());
        response.setUpdatedAt(LocalDateTime.now());

        when(accountService.activateAccount(accountNumber)).thenReturn(response);

        // Act & Assert
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