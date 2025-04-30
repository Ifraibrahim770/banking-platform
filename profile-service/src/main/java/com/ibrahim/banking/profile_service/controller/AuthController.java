package com.ibrahim.banking.profile_service.controller;

import com.ibrahim.banking.profile_service.dto.JwtResponse;
import com.ibrahim.banking.profile_service.dto.LoginRequest;
import com.ibrahim.banking.profile_service.dto.MessageResponse;
import com.ibrahim.banking.profile_service.dto.SignupRequest;
import com.ibrahim.banking.profile_service.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600) // might need to adjust this later
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication management API")
public class AuthController {

    @Autowired
    AuthService authService;

    @Operation(summary = "Authenticate user", description = "Authenticate a user with email and password and return a JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful authentication", 
                     content = @Content(schema = @Schema(implementation = JwtResponse.class))),
        @ApiResponse(responseCode = "401", description = "Authentication failed", 
                     content = @Content(schema = @Schema(implementation = MessageResponse.class)))
    })
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            JwtResponse jwtResponse = authService.authenticateUser(loginRequest);
            return ResponseEntity.ok(jwtResponse);
        } catch (Exception e) {
            // should probly use better error handling here lol
            return ResponseEntity.status(401).body(new MessageResponse("Authentication failed: " + e.getMessage()));
        }
    }

    @Operation(summary = "Register user", description = "Register a new user with the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User registered successfully", 
                     content = @Content(schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid registration data", 
                     content = @Content(schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(responseCode = "500", description = "Server error during registration", 
                     content = @Content(schema = @Schema(implementation = MessageResponse.class)))
    })
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        try {
            authService.registerUser(signUpRequest);
            return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (RuntimeException e) {

             return ResponseEntity.status(500).body(new MessageResponse("Error during registration: " + e.getMessage()));
        }
    }
} 