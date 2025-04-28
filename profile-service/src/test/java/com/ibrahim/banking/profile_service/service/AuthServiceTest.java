package com.ibrahim.banking.profile_service.service;

import com.ibrahim.banking.profile_service.dto.JwtResponse;
import com.ibrahim.banking.profile_service.dto.LoginRequest;
import com.ibrahim.banking.profile_service.dto.SignupRequest;
import com.ibrahim.banking.profile_service.model.ERole;
import com.ibrahim.banking.profile_service.model.Role;
import com.ibrahim.banking.profile_service.model.User;
import com.ibrahim.banking.profile_service.repository.RoleRepository;
import com.ibrahim.banking.profile_service.repository.UserRepository;
import com.ibrahim.banking.profile_service.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks // Inject mocks into AuthService instance
    private AuthService authService;

    private SignupRequest signupRequest;
    private LoginRequest loginRequest;
    private Role userRole;
    private Role adminRole;
    private User user;

    @BeforeEach
    void setUp() {
        signupRequest = new SignupRequest();
        signupRequest.setUsername("testuser");
        signupRequest.setEmail("test@example.com");
        signupRequest.setPassword("password123");
        signupRequest.setFirstName("Test");
        signupRequest.setLastName("User");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        userRole = new Role(ERole.ROLE_USER);
        userRole.setId(1);
        adminRole = new Role(ERole.ROLE_ADMIN);
        adminRole.setId(2);

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setRoles(Set.of(userRole));
    }

    // --- registerUser Tests ---

    @Test
    void registerUser_whenUsernameExists_shouldThrowException() {
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.registerUser(signupRequest);
        });

        assertThat(exception.getMessage()).contains("Username is already taken");
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_whenEmailExists_shouldThrowException() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.registerUser(signupRequest);
        });

        assertThat(exception.getMessage()).contains("Email is already in use");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_withDefaultRole_shouldRegisterSuccessfully() {
        signupRequest.setRole(null); // Ensure default role is used
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Return the user passed to save

        User registeredUser = authService.registerUser(signupRequest);

        assertThat(registeredUser).isNotNull();
        assertThat(registeredUser.getUsername()).isEqualTo(signupRequest.getUsername());
        assertThat(registeredUser.getEmail()).isEqualTo(signupRequest.getEmail());
        assertThat(registeredUser.getPassword()).isEqualTo("encodedPassword");
        assertThat(registeredUser.getRoles()).containsExactly(userRole);

        verify(passwordEncoder).encode("password123");
        verify(roleRepository).findByName(ERole.ROLE_USER);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_withAdminRole_shouldRegisterSuccessfully() {
        signupRequest.setRole(Set.of("admin"));
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleRepository.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User registeredUser = authService.registerUser(signupRequest);

        assertThat(registeredUser.getRoles()).containsExactly(adminRole);
        verify(roleRepository).findByName(ERole.ROLE_ADMIN);
        verify(userRepository).save(any(User.class));
    }

     @Test
    void registerUser_withUserAndAdminRole_shouldRegisterSuccessfully() {
        signupRequest.setRole(Set.of("user", "admin"));
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(roleRepository.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User registeredUser = authService.registerUser(signupRequest);

        assertThat(registeredUser.getRoles()).containsExactlyInAnyOrder(userRole, adminRole);
        verify(roleRepository).findByName(ERole.ROLE_USER);
        verify(roleRepository).findByName(ERole.ROLE_ADMIN);
        verify(userRepository).save(any(User.class));
    }


    @Test
    void registerUser_whenRoleNotFound_shouldThrowException() {
        signupRequest.setRole(Set.of("admin"));
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleRepository.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.empty()); // Role not found

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.registerUser(signupRequest);
        });

        assertThat(exception.getMessage()).contains("Role ADMIN is not found");
        verify(userRepository, never()).save(any(User.class));
    }

    // --- authenticateUser Tests ---

    @Test
    void authenticateUser_withValidCredentials_shouldReturnJwtResponse() {
        // Mock Authentication object
        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toList());
        org.springframework.security.core.userdetails.User userDetails = 
            new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), authorities);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtUtils.generateJwtToken(any(Authentication.class))).thenReturn("mockJwtToken");
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        JwtResponse jwtResponse = authService.authenticateUser(loginRequest);

        assertThat(jwtResponse).isNotNull();
        assertThat(jwtResponse.getToken()).isEqualTo("mockJwtToken");
        assertThat(jwtResponse.getUsername()).isEqualTo(user.getUsername());
        assertThat(jwtResponse.getEmail()).isEqualTo(user.getEmail());
        assertThat(jwtResponse.getRoles()).containsExactly(ERole.ROLE_USER.name());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils).generateJwtToken(authentication);
        verify(userRepository).findByUsername(user.getUsername());
    }

    @Test
    void authenticateUser_withInvalidCredentials_shouldThrowException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(BadCredentialsException.class, () -> {
            authService.authenticateUser(loginRequest);
        });

        verify(jwtUtils, never()).generateJwtToken(any(Authentication.class));
        verify(userRepository, never()).findByUsername(anyString());
    }

    @Test
    void authenticateUser_whenUserNotFoundAfterAuth_shouldThrowException() {
         // This is an edge case, should ideally not happen if UserDetailsService works
         List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(ERole.ROLE_USER.name()));
         org.springframework.security.core.userdetails.User userDetails = 
            new org.springframework.security.core.userdetails.User("testuser", "encodedPassword", authorities);
         Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtUtils.generateJwtToken(any(Authentication.class))).thenReturn("mockJwtToken");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty()); // User disappears after auth

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.authenticateUser(loginRequest);
        });

        assertThat(exception.getMessage()).contains("User not found after authentication");
    }
} 