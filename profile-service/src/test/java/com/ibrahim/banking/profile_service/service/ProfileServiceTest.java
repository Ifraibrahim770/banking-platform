package com.ibrahim.banking.profile_service.service;

import com.ibrahim.banking.profile_service.dto.ProfileUpdateRequest;
import com.ibrahim.banking.profile_service.dto.UserProfileResponse;
import com.ibrahim.banking.profile_service.model.ERole;
import com.ibrahim.banking.profile_service.model.Role;
import com.ibrahim.banking.profile_service.model.User;
import com.ibrahim.banking.profile_service.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private Authentication authentication;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private ProfileService profileService;

    private MockedStatic<SecurityContextHolder> mockedSecurityContextHolder;
    private User testUser;
    private Role userRole;

    @BeforeEach
    void setUp() {
        userRole = new Role(ERole.ROLE_USER);
        userRole.setId(1);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("currentuser");
        testUser.setEmail("current@example.com");
        testUser.setFirstName("Current");
        testUser.setLastName("User");
        testUser.setPassword("encodedPassword");
        testUser.setRoles(Set.of(userRole));

        // Setup mock for SecurityContextHolder BEFORE each test that needs it
        mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class);
        mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
    }

    @AfterEach
    void tearDown() {
        // Close the static mock AFTER each test
        mockedSecurityContextHolder.close();
    }

    private void mockAuthentication() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn(testUser.getUsername());
        when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));
    }

    // --- getCurrentUserProfile Tests ---

    @Test
    void getCurrentUserProfile_whenAuthenticated_shouldReturnProfile() {
        mockAuthentication();

        UserProfileResponse response = profileService.getCurrentUserProfile();

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(testUser.getId());
        assertThat(response.getUsername()).isEqualTo(testUser.getUsername());
        assertThat(response.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(response.getFirstName()).isEqualTo(testUser.getFirstName());
        assertThat(response.getLastName()).isEqualTo(testUser.getLastName());
        assertThat(response.getRoles()).containsExactly(ERole.ROLE_USER.name());

        verify(userRepository).findByUsername(testUser.getUsername());
    }

    @Test
    void getCurrentUserProfile_whenNotAuthenticated_shouldThrowException() {
        when(securityContext.getAuthentication()).thenReturn(null); // No authentication

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            profileService.getCurrentUserProfile();
        });
        assertThat(exception.getMessage()).isEqualTo("User is not authenticated.");
    }

    @Test
    void getCurrentUserProfile_whenUserNotFoundInRepo_shouldThrowException() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("ghostuser");
        when(userRepository.findByUsername("ghostuser")).thenReturn(Optional.empty()); // User not found

        assertThrows(UsernameNotFoundException.class, () -> {
            profileService.getCurrentUserProfile();
        });
    }

    // --- updateCurrentUserProfile Tests ---

    @Test
    void updateCurrentUserProfile_withValidChanges_shouldUpdateAndReturnProfile() {
        mockAuthentication();
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setEmail("new@example.com");
        request.setFirstName("UpdatedFirst");
        request.setLastName("UpdatedLast");

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false); // New email is available
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            // Simulate DB save by copying updated fields back
            testUser.setEmail(savedUser.getEmail());
            testUser.setFirstName(savedUser.getFirstName());
            testUser.setLastName(savedUser.getLastName());
            return testUser;
        });

        UserProfileResponse response = profileService.updateCurrentUserProfile(request);

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("new@example.com");
        assertThat(response.getFirstName()).isEqualTo("UpdatedFirst");
        assertThat(response.getLastName()).isEqualTo("UpdatedLast");
        assertThat(response.getUsername()).isEqualTo(testUser.getUsername()); // Username shouldn't change

        verify(userRepository).findByUsername(testUser.getUsername());
        verify(userRepository).existsByEmail("new@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateCurrentUserProfile_withNoChanges_shouldNotSaveAndReturnCurrentProfile() {
        mockAuthentication();
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setEmail(testUser.getEmail()); // Same email
        request.setFirstName(testUser.getFirstName()); // Same first name
        request.setLastName(null); // Last name not provided

        UserProfileResponse response = profileService.updateCurrentUserProfile(request);

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(response.getFirstName()).isEqualTo(testUser.getFirstName());
        assertThat(response.getLastName()).isEqualTo(testUser.getLastName());

        verify(userRepository).findByUsername(testUser.getUsername());
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateCurrentUserProfile_whenNewEmailExists_shouldThrowException() {
        mockAuthentication();
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setEmail("existing@example.com");

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true); // Email already taken

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            profileService.updateCurrentUserProfile(request);
        });

        assertThat(exception.getMessage()).contains("Email is already in use");
        verify(userRepository).findByUsername(testUser.getUsername());
        verify(userRepository).existsByEmail("existing@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateCurrentUserProfile_whenNotAuthenticated_shouldThrowException() {
        when(securityContext.getAuthentication()).thenReturn(null); // No authentication
        ProfileUpdateRequest request = new ProfileUpdateRequest();

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
             profileService.updateCurrentUserProfile(request);
        });
        assertThat(exception.getMessage()).isEqualTo("User is not authenticated.");
         verify(userRepository, never()).save(any(User.class));
    }
} 