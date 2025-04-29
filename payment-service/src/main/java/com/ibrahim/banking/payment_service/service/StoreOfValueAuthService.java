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
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            
            // Create request body
            Map<String, String> requestBody = Map.of(
                    "username", username,
                    "password", password
            );
            
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    authUrl,
                    requestEntity,
                    Map.class
            );
            
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
                    
                    logger.info("Successfully authenticated with Store of Value service");
                    return fullToken;
                }
            }
            
            logger.error("Failed to authenticate with Store of Value service: {}", 
                    response.getStatusCode());
            return null;
            
        } catch (Exception e) {
            logger.error("Error authenticating with Store of Value service", e);
            return null;
        }
    }
} 