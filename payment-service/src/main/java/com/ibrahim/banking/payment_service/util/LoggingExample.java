package com.ibrahim.banking.payment_service.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example class demonstrating how to use SLF4J with Logback in the application
 */
public class LoggingExample {
    
    // Create a logger instance for this class
    private static final Logger logger = LoggerFactory.getLogger(LoggingExample.class);
    
    public void demonstrateLogging() {
        // Different logging levels
        logger.trace("This is a TRACE level message");
        logger.debug("This is a DEBUG level message");
        logger.info("This is an INFO level message");
        logger.warn("This is a WARN level message");
        logger.error("This is an ERROR level message");
        
        // Logging with parameters
        String username = "user123";
        logger.info("User {} performed an action", username);
        
        // Logging exceptions
        try {
            throw new RuntimeException("Sample exception");
        } catch (Exception e) {
            logger.error("An error occurred", e);
        }
    }
} 