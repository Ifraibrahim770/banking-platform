package com.ibrahim.banking.payment_service.consumer;

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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
public class AccountServiceClient {
    private static final Logger logger = LoggerFactory.getLogger(AccountServiceClient.class);
    
    private final RestTemplate restTemplate;
    private final StoreOfValueAuthService authService;
    
    @Value("${service.store-of-value.url}")
    private String storeOfValueServiceUrl;
    
    public AccountServiceClient(RestTemplate restTemplate, StoreOfValueAuthService authService) {
        this.restTemplate = restTemplate;
        this.authService = authService;
    }
    
    public boolean creditAccount(Long accountId, BigDecimal amount, String currency, String transactionReference) {
        try {
            String url = storeOfValueServiceUrl + "/api/accounts/" + accountId + "/credit";
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("amount", amount);
            requestBody.put("currency", currency);
            requestBody.put("transactionReference", transactionReference);
            
            HttpHeaders headers = getAuthenticatedHeaders();
            if (headers == null) {
                logger.error("Failed to get authentication token for Store of Value service");
                return false;
            }
            
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Successfully credited account {} with amount {}", accountId, amount);
                return true;
            } else {
                logger.error("Failed to credit account {}: {}", accountId, response.getStatusCode());
                return false;
            }
        } catch (Exception e) {
            logger.error("Error calling credit account API: ", e);
            return false;
        }
    }
    
    public boolean debitAccount(Long accountId, BigDecimal amount, String currency, String transactionReference) {
        try {
            String url = storeOfValueServiceUrl + "/api/accounts/" + accountId + "/debit";
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("amount", amount);
            requestBody.put("currency", currency);
            requestBody.put("transactionReference", transactionReference);
            
            HttpHeaders headers = getAuthenticatedHeaders();
            if (headers == null) {
                logger.error("Failed to get authentication token for Store of Value service");
                return false;
            }
            
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Successfully debited account {} with amount {}", accountId, amount);
                return true;
            } else {
                logger.error("Failed to debit account {}: {}", accountId, response.getStatusCode());
                return false;
            }
        } catch (Exception e) {
            logger.error("Error calling debit account API: ", e);
            return false;
        }
    }
    
    public boolean isAccountActive(Long accountId) {
        try {
            String url = storeOfValueServiceUrl + "/api/accounts/" + accountId + "/status";
            
            HttpHeaders headers = getAuthenticatedHeaders();
            if (headers == null) {
                logger.error("Failed to get authentication token for Store of Value service");
                return false;
            }
            
            HttpEntity<?> requestEntity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Boolean isActive = (Boolean) response.getBody().get("status").equals("ACTIVE");
                return isActive != null && isActive;
            } else {
                logger.error("Failed to check account status for {}: {}", accountId, response.getStatusCode());
                return false;
            }
        } catch (Exception e) {
            logger.error("Error calling account status API: ", e);
            return false;
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