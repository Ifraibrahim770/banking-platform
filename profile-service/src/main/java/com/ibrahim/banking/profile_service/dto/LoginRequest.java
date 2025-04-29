package com.ibrahim.banking.profile_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data // Lombok annotation for getters, setters, toString, equals, hashCode
public class LoginRequest {

    @NotBlank(message = "Username cannot be blank")
    private String username;

    @NotBlank(message = "Password cannot be blank")
    private String password;
} 