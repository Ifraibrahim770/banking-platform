package com.ibrahim.banking.profile_service.controller;

import com.ibrahim.banking.profile_service.dto.MessageResponse;
import com.ibrahim.banking.profile_service.dto.ProfileUpdateRequest;
import com.ibrahim.banking.profile_service.dto.UserProfileResponse;
import com.ibrahim.banking.profile_service.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600) // Configure CORS as needed
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()") // Explicitly require authentication
    public ResponseEntity<UserProfileResponse> getCurrentUserProfile() {
        UserProfileResponse profile = profileService.getCurrentUserProfile();
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()") // Explicitly require authentication
    public ResponseEntity<?> updateCurrentUserProfile(@Valid @RequestBody ProfileUpdateRequest updateRequest) {
        try {
            UserProfileResponse updatedProfile = profileService.updateCurrentUserProfile(updateRequest);
            return ResponseEntity.ok(updatedProfile);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
             // Catch other potential errors (e.g., user not found unexpectedly)
             return ResponseEntity.status(500).body(new MessageResponse("Error updating profile: " + e.getMessage()));
        }
    }

    // Optional: Add other endpoints like DELETE /me or POST /me/change-password if needed
} 