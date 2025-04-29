package com.ibrahim.banking.store_of_value_service.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestAuthController {

    private static final Logger logger = LoggerFactory.getLogger(TestAuthController.class);

    @GetMapping("/auth-details")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')") // Example: Require USER or ADMIN role
    public String getAuthenticationDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            logger.warn("No authentication found in SecurityContext.");
            return "No authentication details found.";
        }

        String username = authentication.getName(); // Gets the principal name (username)
        var authorities = authentication.getAuthorities(); // Gets the GrantedAuthority list

        logger.info("Authenticated User Details:");
        logger.info("Username: {}", username);
        logger.info("Authorities: {}", authorities);

        return "Authentication details logged for user: " + username + ". Authorities: " + authorities;
    }

    @GetMapping("/hello")
    public String helloPublic() {
        return "Hello from store-of-value-service (public endpoint)!";
    }
} 