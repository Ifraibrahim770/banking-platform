package com.ibrahim.banking.payment_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StoreOfValueAuthServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private StoreOfValueAuthService authService;

    @BeforeEach
    void setUp() {
        authService = new StoreOfValueAuthService(restTemplate);
        // Set required fields using reflection since they're normally injected by Spring
        ReflectionTestUtils.setField(authService, "storeOfValueServiceUrl", "https://auth-api.example.com");
        ReflectionTestUtils.setField(authService, "username", "test-user");
        ReflectionTestUtils.setField(authService, "password", "test-password");
    }

    @Test
    void getAuthToken_FirstCall_ReturnsNewToken() {
        // Arrange
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("token", "test-token");
        responseMap.put("type", "Bearer");
        
        when(restTemplate.postForEntity(
                anyString(),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(ResponseEntity.ok(responseMap));

        // Act
        String token = authService.getAuthToken();

        // Assert
        assertEquals("Bearer test-token", token);
        verify(restTemplate).postForEntity(
                eq("https://auth-api.example.com/api/auth/signin"),
                any(HttpEntity.class),
                eq(Map.class)
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void getAuthToken_CachedToken_ReturnsCachedToken() {
        // Arrange
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("token", "test-token");
        responseMap.put("type", "Bearer");
        
        when(restTemplate.postForEntity(
                anyString(),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(ResponseEntity.ok(responseMap));

        // First call to get a token
        String firstToken = authService.getAuthToken();
        
        // Act - Second call should return cached token
        String secondToken = authService.getAuthToken();

        // Assert
        assertEquals("Bearer test-token", firstToken);
        assertEquals("Bearer test-token", secondToken);
        // Verify exchange was only called once
        verify(restTemplate, times(1)).postForEntity(
                eq("https://auth-api.example.com/api/auth/signin"),
                any(HttpEntity.class),
                eq(Map.class)
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void getAuthToken_ExpiredToken_ReturnsNewToken() throws Exception {
        // Arrange
        Map<String, String> firstResponseMap = new HashMap<>();
        firstResponseMap.put("token", "first-token");
        firstResponseMap.put("type", "Bearer");
        
        Map<String, String> secondResponseMap = new HashMap<>();
        secondResponseMap.put("token", "second-token");
        secondResponseMap.put("type", "Bearer");
        
        when(restTemplate.postForEntity(
                anyString(),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(
                ResponseEntity.ok(firstResponseMap),
                ResponseEntity.ok(secondResponseMap)
        );

        // First call to get a token
        String firstToken = authService.getAuthToken();
        
        // Manually set token expiration to now to simulate expired token
        // This is safer than using Thread.sleep which can make tests flaky
        ReflectionTestUtils.setField(authService, "tokenExpirationTime", java.time.Instant.now().minusSeconds(1));
        
        // Act - Second call should get a new token
        String secondToken = authService.getAuthToken();

        // Assert
        assertEquals("Bearer first-token", firstToken);
        assertEquals("Bearer second-token", secondToken);
        // Verify exchange was called twice
        verify(restTemplate, times(2)).postForEntity(
                eq("https://auth-api.example.com/api/auth/signin"),
                any(HttpEntity.class),
                eq(Map.class)
        );
    }
} 