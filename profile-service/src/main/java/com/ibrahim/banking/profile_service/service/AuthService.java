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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Transactional
    public User registerUser(SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new IllegalArgumentException("Error: Username is already taken!");
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new IllegalArgumentException("Error: Email is already in use!");
        }

        // Create new user's account
        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(encoder.encode(signUpRequest.getPassword()));
        user.setFirstName(signUpRequest.getFirstName());
        user.setLastName(signUpRequest.getLastName());

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null || strRoles.isEmpty()) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role USER is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                if (role.equalsIgnoreCase("admin")) {
                    Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                            .orElseThrow(() -> new RuntimeException("Error: Role ADMIN is not found."));
                    roles.add(adminRole);
                } else {
                    Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                            .orElseThrow(() -> new RuntimeException("Error: Role USER is not found."));
                    roles.add(userRole);
                }
            });
        }

        user.setRoles(roles);
        User savedUser = userRepository.save(user);
        logger.info("User registered successfully: {}", savedUser.getUsername());
        return savedUser;
    }

    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        // Get UserDetails from Authentication principal
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // We need the User entity to get the ID and Email (UserDetails doesn't have them)
        // It's a bit redundant, but necessary unless we store more in JWT or UserDetails impl
        User user = userRepository.findByUsername(userDetails.getUsername())
                 .orElseThrow(() -> new RuntimeException("Error: User not found after authentication."));

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        logger.info("User authenticated successfully: {}", userDetails.getUsername());

        return new JwtResponse(jwt, user.getId(), user.getUsername(), user.getEmail(), roles);
    }
} 