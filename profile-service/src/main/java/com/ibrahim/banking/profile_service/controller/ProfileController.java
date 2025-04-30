package com.ibrahim.banking.profile_service.controller;

import com.ibrahim.banking.profile_service.dto.MessageResponse;
import com.ibrahim.banking.profile_service.dto.ProfileUpdateRequest;
import com.ibrahim.banking.profile_service.dto.UserProfileResponse;
import com.ibrahim.banking.profile_service.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600) // configure CORS, change if neeed
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileResponse> getCurrentUserProfile() {
        UserProfileResponse profile = profileService.getCurrentUserProfile();
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()") // user needs 2 be authenticated
    public ResponseEntity<?> updateCurrentUserProfile(@Valid @RequestBody ProfileUpdateRequest updateRequest) {
        try {
            UserProfileResponse updatedProfile = profileService.updateCurrentUserProfile(updateRequest);
            return ResponseEntity.ok(updatedProfile);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
             // catch any weird errors, like if user disappears somehow
             return ResponseEntity.status(500).body(new MessageResponse("Error updating profile: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // admin only for security reasons!
    public ResponseEntity<?> getUserProfileById(@PathVariable Long id) {
        try {
            UserProfileResponse profile = profileService.getUserProfileById(id);
            return ResponseEntity.ok(profile);
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(404).body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new MessageResponse("Error fetching profile: " + e.getMessage()));
        }
    }
} 