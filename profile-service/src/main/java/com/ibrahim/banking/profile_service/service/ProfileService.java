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


    private User getCurrentUserEntity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserDetails)) {
            throw new IllegalStateException("User is not authenticated.");
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userDetails.getUsername()));
    }

    // converts User obj to ProfileResponse obj..could be refactored later
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

    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile() {
        User user = getCurrentUserEntity();
        logger.info("Fetching profile for user: {}", user.getUsername());
        return mapUserToProfileResponse(user);
    }

    @Transactional
    public UserProfileResponse updateCurrentUserProfile(ProfileUpdateRequest updateRequest) {
        User user = getCurrentUserEntity();
        logger.info("Updating profile for user: {}", user.getUsername());

        boolean updated = false;


        if (StringUtils.hasText(updateRequest.getEmail()) && !updateRequest.getEmail().equals(user.getEmail())) {

            if (userRepository.existsByEmail(updateRequest.getEmail())) {
                throw new IllegalArgumentException("Error: Email is already in use!");
            }
            user.setEmail(updateRequest.getEmail());
            logger.debug("Updating email for user {}", user.getUsername());
            updated = true;
        }

        // update first name if provided n different
        if (StringUtils.hasText(updateRequest.getFirstName()) && !updateRequest.getFirstName().equals(user.getFirstName())) {
            user.setFirstName(updateRequest.getFirstName());
            logger.debug("Updating firstName for user {}", user.getUsername());
            updated = true;
        }

        // do same for last name
        if (StringUtils.hasText(updateRequest.getLastName()) && !updateRequest.getLastName().equals(user.getLastName())) {
            user.setLastName(updateRequest.getLastName());
            logger.debug("Updating lastName for user {}", user.getUsername());
            updated = true;
        }

        // only save to DB if something changed
        if (updated) {
            User savedUser = userRepository.save(user);
            logger.info("Profile updated successfully for user: {}", savedUser.getUsername());
            return mapUserToProfileResponse(savedUser);
        } else {
            logger.info("No profile changes detected for user: {}", user.getUsername());
            // just return what we already have
            return mapUserToProfileResponse(user);
        }
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfileById(Long userId) {
        logger.info("Fetching profile for user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));
        return mapUserToProfileResponse(user);
    }
} 