package com.ibrahim.banking.profile_service.controller;

import com.ibrahim.banking.profile_service.dto.JwtResponse;
import com.ibrahim.banking.profile_service.dto.LoginRequest;
import com.ibrahim.banking.profile_service.dto.MessageResponse;
import com.ibrahim.banking.profile_service.dto.SignupRequest;
import com.ibrahim.banking.profile_service.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600) // Configure CORS as needed
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthService authService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            JwtResponse jwtResponse = authService.authenticateUser(loginRequest);
            return ResponseEntity.ok(jwtResponse);
        } catch (Exception e) {
            // Consider more specific exception handling and appropriate status codes
            return ResponseEntity.status(401).body(new MessageResponse("Authentication failed: " + e.getMessage()));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        try {
            authService.registerUser(signUpRequest);
            return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (RuntimeException e) {
            // Catch potential RuntimeExceptions from role finding
             return ResponseEntity.status(500).body(new MessageResponse("Error during registration: " + e.getMessage()));
        }
    }
} 