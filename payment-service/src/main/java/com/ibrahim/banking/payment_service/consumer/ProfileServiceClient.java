package com.ibrahim.banking.payment_service.consumer;

import com.ibrahim.banking.payment_service.dto.UserProfileDto;
import com.ibrahim.banking.payment_service.service.StoreOfValueAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ProfileServiceClient {
    private static final Logger logger = LoggerFactory.getLogger(ProfileServiceClient.class);
    
    private final RestTemplate restTemplate;
    private final StoreOfValueAuthService authService;
    
    @Value("${service.store-of-value.url}")
    private String profileServiceUrl;
    
    public ProfileServiceClient(RestTemplate restTemplate, StoreOfValueAuthService authService) {
        this.restTemplate = restTemplate;
        this.authService = authService;
    }
    
    public UserProfileDto getUserProfile(Long userId) {
        try {
            String url = profileServiceUrl + "/api/profile/" + userId;
            
            HttpHeaders headers = getAuthenticatedHeaders();
            if (headers == null) {
                logger.error("Failed to get authentication token for Profile service");
                return null;
            }
            
            HttpEntity<?> requestEntity = new HttpEntity<>(headers);
            
            ResponseEntity<UserProfileDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    UserProfileDto.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                logger.info("Successfully retrieved profile for user id: {}", userId);
                return response.getBody();
            } else {
                logger.error("Failed to retrieve profile for user id {}: {}", userId, response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            logger.error("Error calling profile service API for user id {}: ", userId, e);
            return null;
        }
    }
    
    private HttpHeaders getAuthenticatedHeaders() {
        String authToken = authService.getAuthToken();
        if (authToken == null) {
            return null;
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", authToken);
        return headers;
    }
} 