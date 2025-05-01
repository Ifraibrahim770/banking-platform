package com.ibrahim.banking.events_service.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("userSecurity")
public class UserSecurity {
    
    /**
     * Checks if the authenticated user is accessing their own data
     * @param userId ID of the user whose data is being accessed
     * @return true if the current user is accessing their own data
     */
    public boolean isCurrentUser(Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        // Get username (which should be the user ID as a string) from the authentication
        String currentUserIdString = authentication.getName();
        
        try {
            Long currentUserId = Long.parseLong(currentUserIdString);
            return currentUserId.equals(userId);
        } catch (NumberFormatException e) {
            // If the username is not a valid number (not using ID as username)
            return false;
        }
    }
} 