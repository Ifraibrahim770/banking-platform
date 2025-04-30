package com.ibrahim.banking.payment_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class StoreOfValueAuthService {
    private static final Logger logger = LoggerFactory.getLogger(StoreOfValueAuthService.class);
    
    private final RestTemplate restTemplate;
    
    @Value("${service.auth.url}")
    private String storeOfValueServiceUrl;
    
    @Value("${service.auth.username}")
    private String username;
    
    @Value("${service.auth.password}")
    private String password;
    
    // Token expiration time is usually 24 hours (in milliseconds)
    private static final long TOKEN_EXPIRATION_TIME = 23 * 60 * 60 * 1000;
    
    private final AtomicReference<String> cachedToken = new AtomicReference<>();
    private Instant tokenExpirationTime;
    
    public StoreOfValueAuthService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    public synchronized String getAuthToken() {
        // Check if we have a valid token
        if (cachedToken.get() != null && tokenExpirationTime != null && 
                Instant.now().isBefore(tokenExpirationTime)) {
            return cachedToken.get();
        }
        
        // Token is null or expired, need to authenticate
        try {
            String authUrl = storeOfValueServiceUrl + "/api/auth/signin";
            logger.info("Attempting to authenticate with profile service at URL: {}", authUrl);
            logger.info("Using username: {}", username);
            // Log partial password for debugging (first two chars and last two)
            String maskedPassword = password.length() > 4 ? 
                    password.substring(0, 2) + "****" + password.substring(password.length() - 2) : 
                    "****";
            logger.info("Using password (masked): {}", maskedPassword);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            
            // Create request body
            Map<String, String> requestBody = Map.of(
                    "username", username,
                    "password", password
            );
            
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            try {
                logger.info("Sending authentication request to profile service...");
                ResponseEntity<Map> response = restTemplate.postForEntity(
                        authUrl,
                        requestEntity,
                        Map.class
                );
                
                logger.info("Authentication response received with status: {}", response.getStatusCode());
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    // Extract token from response
                    String token = (String) response.getBody().get("token");
                    String tokenType = (String) response.getBody().get("type");
                    
                    if (token != null && tokenType != null) {
                        // Cache the token
                        String fullToken = tokenType + " " + token;
                        cachedToken.set(fullToken);
                        
                        // Set expiration time (with a small safety margin)
                        tokenExpirationTime = Instant.now().plusMillis(TOKEN_EXPIRATION_TIME);
                        
                        logger.info("Successfully authenticated with profile service. Token acquired (first 10 chars): {}...", 
                                token.substring(0, Math.min(10, token.length())));
                        return fullToken;
                    } else {
                        logger.error("Authentication response did not contain token or type. Response body: {}", response.getBody());
                    }
                } else {
                    logger.error("Authentication failed with status code: {}", response.getStatusCode());
                    if (response.getBody() != null) {
                        logger.error("Response body: {}", response.getBody());
                    }
                }
            } catch (Exception e) {
                logger.error("Exception while making authentication request: {}", e.getMessage(), e);
            }
            
            return null;
            
        } catch (Exception e) {
            logger.error("Error authenticating with profile service", e);
            return null;
        }
    }
} 