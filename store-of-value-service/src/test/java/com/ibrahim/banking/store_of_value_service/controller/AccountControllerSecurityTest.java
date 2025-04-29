package com.ibrahim.banking.store_of_value_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibrahim.banking.store_of_value_service.dto.AccountResponse;
import com.ibrahim.banking.store_of_value_service.dto.CreateAccountRequest;
import com.ibrahim.banking.store_of_value_service.model.AccountStatus;
import com.ibrahim.banking.store_of_value_service.model.AccountType;
import com.ibrahim.banking.store_of_value_service.security.AuthEntryPointJwt;
import com.ibrahim.banking.store_of_value_service.security.AuthTokenFilter;
import com.ibrahim.banking.store_of_value_service.security.JwtUtils;
import com.ibrahim.banking.store_of_value_service.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Security-focused integration tests for AccountController.
 * These tests validate that the security rules are properly enforced
 * and that the custom security handler is functioning correctly.
 */
@WebMvcTest(AccountController.class)
@Import({AuthEntryPointJwt.class, AuthTokenFilter.class})
public class AccountControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @MockBean
    private JwtUtils jwtUtils;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        // Clear security context
        SecurityContextHolder.clearContext();
    }

    @Test
    void createAccount_Unauthenticated_ShouldReturn401() throws Exception {
        // Arrange
        CreateAccountRequest request = new CreateAccountRequest();
        request.setProfileId("profile123");
        request.setAccountType(AccountType.SAVINGS);

        // Act & Assert - No authentication provided
        mockMvc.perform(post("/api/accounts")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER") // Regular user role
    void createAccount_InsufficientPermissions_ShouldReturn403() throws Exception {
        // Arrange
        CreateAccountRequest request = new CreateAccountRequest();
        request.setProfileId("profile123");
        request.setAccountType(AccountType.SAVINGS);

        // Act & Assert - Not admin role
        mockMvc.perform(post("/api/accounts")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN") // Admin role
    void createAccount_AuthenticatedAsAdmin_ShouldSucceed() throws Exception {
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
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountNumber", is("1234567890")));
    }

    @Test
    @WithMockUser(roles = "USER") // Regular user role
    void getBalance_AuthenticatedAsUser_ShouldSucceed() throws Exception {
        // Arrange
        String accountNumber = "1234567890";
        BigDecimal balance = new BigDecimal("100.00");

        when(accountService.getBalance(accountNumber)).thenReturn(balance);

        // Act & Assert
        mockMvc.perform(get("/api/accounts/{accountNumber}/balance", accountNumber))
                .andExpect(status().isOk())
                .andExpect(content().string("100.00"));
    }

    @Test
    @WithMockUser(roles = "ADMIN") // Admin role - but doesn't have permission to check balance
    void getBalance_AdminNotAuthorized_ShouldReturn403() throws Exception {
        // Arrange
        String accountNumber = "1234567890";

        // Act & Assert - The endpoint requires USER role specifically
        mockMvc.perform(get("/api/accounts/{accountNumber}/balance", accountNumber))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER") // Regular user role
    void activateAccount_InsufficientPermissions_ShouldReturn403() throws Exception {
        // Arrange
        String accountNumber = "1234567890";

        // Act & Assert - Not admin role
        mockMvc.perform(patch("/api/accounts/{accountNumber}/activate", accountNumber)
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN") // Admin role
    void deactivateAccount_AuthenticatedAsAdmin_ShouldSucceed() throws Exception {
        // Arrange
        String accountNumber = "1234567890";

        AccountResponse response = new AccountResponse();
        response.setId(1L);
        response.setAccountNumber(accountNumber);
        response.setProfileId("profile123");
        response.setAccountType(AccountType.SAVINGS);
        response.setBalance(BigDecimal.ZERO);
        response.setStatus(AccountStatus.INACTIVE);
        response.setCreatedAt(LocalDateTime.now());
        response.setUpdatedAt(LocalDateTime.now());

        when(accountService.deactivateAccount(accountNumber)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(patch("/api/accounts/{accountNumber}/deactivate", accountNumber)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("INACTIVE")));
    }

    @Test
    void testMissingCsrfToken_ShouldFail() throws Exception {
        // Spring Security is configured to require CSRF tokens for state-changing operations
        // But our security config disabled it since we're using JWT

        // Simulate authenticated user with admin role
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "admin",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                )
        );

        // Create a request - POST without CSRF token
        CreateAccountRequest request = new CreateAccountRequest();
        request.setProfileId("profile123");
        request.setAccountType(AccountType.SAVINGS);

        // This test should pass due to CSRF being disabled in our security config
        mockMvc.perform(post("/api/accounts")
                // No CSRF token here
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated()); // Expect success even without CSRF
    }
} 