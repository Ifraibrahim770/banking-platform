package com.ibrahim.banking.payment_service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class StartupLogger implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(StartupLogger.class);
    
    @Value("${service.auth.url}")
    private String authServiceUrl;
    
    @Value("${service.auth.username}")
    private String authUsername;
    
    @Value("${service.auth.password}")
    private String authPassword;
    
    @Value("${service.store-of-value.url}")
    private String storeOfValueServiceUrl;
    
    private final Environment environment;
    
    public StartupLogger(Environment environment) {
        this.environment = environment;
    }
    
    @Override
    public void run(String... args) {
        logger.info("=================== PAYMENT SERVICE STARTUP LOGS ===================");
        logger.info("Auth Service URL: {}", authServiceUrl);
        logger.info("Auth Username: {}", authUsername);
        // Mask part of the password for security
        String maskedPassword = authPassword.length() > 4 ? 
                authPassword.substring(0, 2) + "****" + authPassword.substring(authPassword.length() - 2) : 
                "****";
        logger.info("Auth Password (masked): {}", maskedPassword);
        logger.info("Store of Value Service URL: {}", storeOfValueServiceUrl);
        
        // Log from environment directly to verify there's no parsing issues
        logger.info("AUTH_SERVICE_URL from env: {}", environment.getProperty("AUTH_SERVICE_URL"));
        logger.info("AUTH_SERVICE_USERNAME from env: {}", environment.getProperty("AUTH_SERVICE_USERNAME"));
        
        // Also log Java system properties
        logger.info("Java system property for AUTH_SERVICE_PASSWORD: {}", 
                System.getProperty("AUTH_SERVICE_PASSWORD"));
        
        // Log if Docker environment variables are being picked up
        String dockerAuthPassword = System.getenv("AUTH_SERVICE_PASSWORD");
        logger.info("AUTH_SERVICE_PASSWORD from Docker env: {}", 
                dockerAuthPassword != null ? dockerAuthPassword.substring(0, 2) + "****" : "null");
        
        logger.info("=================================================================");
    }
} 