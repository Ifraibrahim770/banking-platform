package com.ibrahim.banking.profile_service.service;

import com.ibrahim.banking.profile_service.dto.ProfileUpdateRequest;
import com.ibrahim.banking.profile_service.dto.UserProfileResponse;
import com.ibrahim.banking.profile_service.model.User;
import com.ibrahim.banking.profile_service.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProfileService {

    private static final Logger logger = LoggerFactory.getLogger(ProfileService.class);

    @Autowired
    private UserRepository userRepository;

    // Helper method to get the current user entity
    private User getCurrentUserEntity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserDetails)) {
            throw new IllegalStateException("User is not authenticated.");
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userDetails.getUsername()));
    }

    // Helper method to map User to UserProfileResponse
    private UserProfileResponse mapUserToProfileResponse(User user) {
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());

        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                roles
        );
    }

    @Transactional(readOnly = true) // Read-only transaction for fetching data
    public UserProfileResponse getCurrentUserProfile() {
        User user = getCurrentUserEntity();
        logger.info("Fetching profile for user: {}", user.getUsername());
        return mapUserToProfileResponse(user);
    }

    @Transactional // Read-write transaction for updating data
    public UserProfileResponse updateCurrentUserProfile(ProfileUpdateRequest updateRequest) {
        User user = getCurrentUserEntity();
        logger.info("Updating profile for user: {}", user.getUsername());

        boolean updated = false;

        // Update email if provided and different
        if (StringUtils.hasText(updateRequest.getEmail()) && !updateRequest.getEmail().equals(user.getEmail())) {
            // Check if the new email is already taken by another user
            if (userRepository.existsByEmail(updateRequest.getEmail())) {
                throw new IllegalArgumentException("Error: Email is already in use!");
            }
            user.setEmail(updateRequest.getEmail());
            logger.debug("Updating email for user {}", user.getUsername());
            updated = true;
        }

        // Update first name if provided
        if (StringUtils.hasText(updateRequest.getFirstName()) && !updateRequest.getFirstName().equals(user.getFirstName())) {
            user.setFirstName(updateRequest.getFirstName());
            logger.debug("Updating firstName for user {}", user.getUsername());
            updated = true;
        }

        // Update last name if provided
        if (StringUtils.hasText(updateRequest.getLastName()) && !updateRequest.getLastName().equals(user.getLastName())) {
            user.setLastName(updateRequest.getLastName());
            logger.debug("Updating lastName for user {}", user.getUsername());
            updated = true;
        }

        // Save if any changes were made
        if (updated) {
            User savedUser = userRepository.save(user);
            logger.info("Profile updated successfully for user: {}", savedUser.getUsername());
            return mapUserToProfileResponse(savedUser);
        } else {
            logger.info("No profile changes detected for user: {}", user.getUsername());
            // Return current profile if no changes
            return mapUserToProfileResponse(user);
        }
    }
} 