package com.ibrahim.banking.profile_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibrahim.banking.profile_service.dto.ProfileUpdateRequest;
import com.ibrahim.banking.profile_service.dto.UserProfileResponse;
import com.ibrahim.banking.profile_service.security.JwtUtils;
import com.ibrahim.banking.profile_service.security.SecurityConfig;
import com.ibrahim.banking.profile_service.security.UserDetailsServiceImpl;
import com.ibrahim.banking.profile_service.service.ProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfileController.class)
@Import(SecurityConfig.class)
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProfileService profileService;

    // Mock security beans potentially needed by filters/security context
    @MockBean
    private UserDetailsServiceImpl userDetailsService;
    @MockBean
    private JwtUtils jwtUtils;

    // --- Get Profile Tests (/api/profile/me - GET) ---

    @Test
    @WithMockUser // Simulate an authenticated user
    void getCurrentUserProfile_whenAuthenticated_shouldReturnProfile() throws Exception {
        UserProfileResponse profileResponse = new UserProfileResponse(
            1L, "testuser", "test@example.com", "Test", "User", List.of("ROLE_USER")
        );
        when(profileService.getCurrentUserProfile()).thenReturn(profileResponse);

        mockMvc.perform(get("/api/profile/me"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(profileService).getCurrentUserProfile();
    }

    @Test
    void getCurrentUserProfile_whenNotAuthenticated_shouldReturnUnauthorized() throws Exception {
        // Explicitly mark as anonymous
        mockMvc.perform(get("/api/profile/me")
                .with(anonymous()))
                .andExpect(status().isForbidden());

        verify(profileService, never()).getCurrentUserProfile();
    }

    // --- Update Profile Tests (/api/profile/me - PUT) ---

    @Test
    @WithMockUser
    void updateCurrentUserProfile_withValidData_shouldReturnUpdatedProfile() throws Exception {
        ProfileUpdateRequest updateRequest = new ProfileUpdateRequest();
        updateRequest.setEmail("new@example.com");
        updateRequest.setFirstName("Updated");
        updateRequest.setLastName("User");

        UserProfileResponse updatedResponse = new UserProfileResponse(
            1L, "testuser", "new@example.com", "Updated", "User", List.of("ROLE_USER")
        );

        when(profileService.updateCurrentUserProfile(any(ProfileUpdateRequest.class))).thenReturn(updatedResponse);

        mockMvc.perform(put("/api/profile/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("new@example.com"))
                .andExpect(jsonPath("$.firstName").value("Updated"));

        verify(profileService).updateCurrentUserProfile(any(ProfileUpdateRequest.class));
    }

    @Test
    @WithMockUser
    void updateCurrentUserProfile_whenServiceThrowsIllegalArgument_shouldReturnBadRequest() throws Exception {
        ProfileUpdateRequest updateRequest = new ProfileUpdateRequest();
        updateRequest.setEmail("existing@example.com"); // Invalid data causing service error

        String errorMessage = "Error: Email is already in use!";
        when(profileService.updateCurrentUserProfile(any(ProfileUpdateRequest.class)))
            .thenThrow(new IllegalArgumentException(errorMessage));

        mockMvc.perform(put("/api/profile/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(errorMessage));

        verify(profileService).updateCurrentUserProfile(any(ProfileUpdateRequest.class));
    }

    @Test
    @WithMockUser
    void updateCurrentUserProfile_withInvalidEmailFormat_shouldReturnBadRequest() throws Exception {
        ProfileUpdateRequest updateRequest = new ProfileUpdateRequest();
        updateRequest.setEmail("invalid-email"); // Invalid format according to @Email
        updateRequest.setFirstName("Valid");
        updateRequest.setLastName("Name");

        mockMvc.perform(put("/api/profile/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest()); // Validation failure

        verify(profileService, never()).updateCurrentUserProfile(any(ProfileUpdateRequest.class));
    }

    @Test
    void updateCurrentUserProfile_whenNotAuthenticated_shouldReturnUnauthorized() throws Exception {
        ProfileUpdateRequest updateRequest = new ProfileUpdateRequest();
        updateRequest.setEmail("new@example.com");

        // Explicitly mark as anonymous (and keep csrf for PUT just in case)
        mockMvc.perform(put("/api/profile/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
                .with(csrf())
                .with(anonymous()))
                .andExpect(status().isForbidden());

        verify(profileService, never()).updateCurrentUserProfile(any(ProfileUpdateRequest.class));
    }
} 