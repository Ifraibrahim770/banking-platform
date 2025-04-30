package com.ibrahim.banking.profile_service.controller;

import com.ibrahim.banking.profile_service.dto.MessageResponse;
import com.ibrahim.banking.profile_service.dto.ProfileUpdateRequest;
import com.ibrahim.banking.profile_service.dto.UserProfileResponse;
import com.ibrahim.banking.profile_service.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600) // configure CORS, change if neeed
@RestController
@RequestMapping("/api/profile")
@Tag(name = "Profile", description = "User profile management API")
@SecurityRequirement(name = "Bearer Authentication")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @Operation(summary = "Get current user profile", description = "Retrieves the profile of the currently authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile retrieved successfully", 
                     content = @Content(schema = @Schema(implementation = UserProfileResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated", 
                     content = @Content)
    })
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileResponse> getCurrentUserProfile() {
        UserProfileResponse profile = profileService.getCurrentUserProfile();
        return ResponseEntity.ok(profile);
    }

    @Operation(summary = "Update current user profile", description = "Updates the profile of the currently authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile updated successfully", 
                     content = @Content(schema = @Schema(implementation = UserProfileResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid update data", 
                     content = @Content(schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated", 
                     content = @Content),
        @ApiResponse(responseCode = "500", description = "Server error", 
                     content = @Content(schema = @Schema(implementation = MessageResponse.class)))
    })
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

    @Operation(summary = "Get user profile by ID", description = "Retrieves a user profile by ID (Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile retrieved successfully", 
                     content = @Content(schema = @Schema(implementation = UserProfileResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated", 
                     content = @Content),
        @ApiResponse(responseCode = "403", description = "Forbidden - User not authorized", 
                     content = @Content),
        @ApiResponse(responseCode = "404", description = "Profile not found", 
                     content = @Content(schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(responseCode = "500", description = "Server error", 
                     content = @Content(schema = @Schema(implementation = MessageResponse.class)))
    })
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