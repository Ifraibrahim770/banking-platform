package com.ibrahim.banking.profile_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibrahim.banking.profile_service.dto.JwtResponse;
import com.ibrahim.banking.profile_service.dto.LoginRequest;
import com.ibrahim.banking.profile_service.dto.SignupRequest;
import com.ibrahim.banking.profile_service.model.User;
import com.ibrahim.banking.profile_service.security.AuthEntryPointJwt;
import com.ibrahim.banking.profile_service.security.JwtUtils;
import com.ibrahim.banking.profile_service.security.SecurityConfig;
import com.ibrahim.banking.profile_service.security.UserDetailsServiceImpl;
import com.ibrahim.banking.profile_service.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Focus only on AuthController, load web layer + security config
@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; // For JSON serialization/deserialization

    @MockBean
    private AuthService authService;

    // Mock security components potentially needed by filters activated by @WebMvcTest
    @MockBean
    private UserDetailsServiceImpl userDetailsService;
    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private AuthEntryPointJwt unauthorizedHandler;

    // --- Sign In Tests (/api/auth/signin) ---

    @Test
    void signin_withValidCredentials_shouldReturnJwtResponse() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        JwtResponse jwtResponse = new JwtResponse("mockToken", 1L, "testuser", "test@example.com", List.of("ROLE_USER"));

        when(authService.authenticateUser(any(LoginRequest.class))).thenReturn(jwtResponse);

        mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("mockToken"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));

        verify(authService).authenticateUser(any(LoginRequest.class));
    }

    @Test
    void signin_withInvalidCredentials_shouldReturnUnauthorized() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("wrongpassword");

        when(authService.authenticateUser(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid Credentials"));

        mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                // If GlobalExceptionHandler was loaded, we could check the body
                //.andExpect(jsonPath("$.message").value("Authentication failed: Invalid Credentials"));
                ;

        verify(authService).authenticateUser(any(LoginRequest.class));
    }

    @Test
    void signin_withMissingUsername_shouldReturnBadRequest() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());

         verify(authService, never()).authenticateUser(any(LoginRequest.class));
    }

    // --- Sign Up Tests (/api/auth/signup) ---

    @Test
    void signup_withValidData_shouldReturnSuccessMessage() throws Exception {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("newuser");
        signupRequest.setEmail("new@example.com");
        signupRequest.setPassword("password123");
        signupRequest.setFirstName("New");
        signupRequest.setLastName("User");

        // Fix: Use when().thenReturn() as registerUser returns a User
        when(authService.registerUser(any(SignupRequest.class))).thenReturn(new User());

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully!"));

        verify(authService).registerUser(any(SignupRequest.class));
    }

    @Test
    void signup_whenUsernameExists_shouldReturnBadRequest() throws Exception {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("existinguser");
        signupRequest.setEmail("new@example.com");
        signupRequest.setPassword("password123");
        signupRequest.setFirstName("New");
        signupRequest.setLastName("User");

        String errorMessage = "Error: Username is already taken!";
        // Fix: Use when().thenThrow() for non-void methods
        when(authService.registerUser(any(SignupRequest.class)))
            .thenThrow(new IllegalArgumentException(errorMessage));

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest())
                // If GlobalExceptionHandler was loaded, we could check the body
                // .andExpect(jsonPath("$.message").value(errorMessage));
                ;

        verify(authService).registerUser(any(SignupRequest.class));
    }

    @Test
    void signup_withInvalidEmail_shouldReturnBadRequest() throws Exception {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("newuser");
        signupRequest.setEmail("invalid-email");
        signupRequest.setPassword("password123");
        signupRequest.setFirstName("New");
        signupRequest.setLastName("User");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).registerUser(any(SignupRequest.class));
    }

    @Test
    void signup_withShortPassword_shouldReturnBadRequest() throws Exception {
         SignupRequest signupRequest = new SignupRequest();
         signupRequest.setUsername("newuser");
         signupRequest.setEmail("valid@example.com");
         signupRequest.setPassword("short");
         signupRequest.setFirstName("New");
         signupRequest.setLastName("User");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).registerUser(any(SignupRequest.class));
    }
} 