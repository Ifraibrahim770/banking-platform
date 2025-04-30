package com.ibrahim.banking.store_of_value_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibrahim.banking.store_of_value_service.dto.AccountResponse;
import com.ibrahim.banking.store_of_value_service.dto.CreateAccountRequest;
import com.ibrahim.banking.store_of_value_service.dto.UpdateAccountRequest;
import com.ibrahim.banking.store_of_value_service.exception.AccountNotFoundException;
import com.ibrahim.banking.store_of_value_service.exception.GlobalExceptionHandler;
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
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for AccountController with full exception handling integration.
 * This test class integrates the GlobalExceptionHandler to validate proper error responses.
 */
@ExtendWith(MockitoExtension.class)
public class AccountControllerWithExceptionHandlerTest {

    private MockMvc mockMvc;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private AccountController accountController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Setup MockMvc with the GlobalExceptionHandler
        mockMvc = MockMvcBuilders.standaloneSetup(accountController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    void createAccount_ValidationError_ShouldReturnProperErrorResponse() throws Exception {
        // Arrange
        CreateAccountRequest request = new CreateAccountRequest();
        // Missing required fields

        // Act & Assert
        mockMvc.perform(post("/api/accounts")
                .with(user("admin").roles("ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", containsString("Validation failed")));

        verify(accountService, never()).createAccount(any());
    }

    @Test
    void getBalance_NotFound_ShouldReturnProperErrorResponse() throws Exception {
        // Arrange
        String accountNumber = "nonexistent";

        when(accountService.getBalance(accountNumber))
                .thenThrow(new AccountNotFoundException("Account not found with number: " + accountNumber));

        // Act & Assert
        mockMvc.perform(get("/api/accounts/{accountNumber}/balance", accountNumber)
                .with(user("user").roles("USER")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", containsString("Account not found")));

        verify(accountService, times(1)).getBalance(accountNumber);
    }



    @Test
    void updateAccount_ValidationError_ShouldReturnProperErrorResponse() throws Exception {
        // Arrange
        String accountNumber = "1234567890";
        UpdateAccountRequest request = new UpdateAccountRequest();
        // Missing required fields

        // Act & Assert
        mockMvc.perform(put("/api/accounts/{accountNumber}", accountNumber)
                .with(user("admin").roles("ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", containsString("Validation failed")));

        verify(accountService, never()).updateAccount(any(), any());
    }

    @Test
    void activateAccount_ServerError_ShouldReturnProperErrorResponse() throws Exception {
        // Arrange
        String accountNumber = "1234567890";

        // Simulate a server-side error
        when(accountService.activateAccount(accountNumber))
                .thenThrow(new RuntimeException("Unexpected database error"));

        // Act & Assert
        mockMvc.perform(patch("/api/accounts/{accountNumber}/activate", accountNumber)
                .with(user("admin").roles("ADMIN"))
                .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status", is(500)))
                .andExpect(jsonPath("$.error", is("Internal Server Error")))
                .andExpect(jsonPath("$.message", containsString("Unexpected database error")));

        verify(accountService, times(1)).activateAccount(accountNumber);
    }
} 