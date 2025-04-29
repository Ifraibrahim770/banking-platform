package com.ibrahim.banking.profile_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfileUpdateRequest {

    // Use @NotBlank if these fields are mandatory for update
    // Use @Size and @Email for validation as needed

    @Size(max = 50)
    @Email
    private String email; // Optional: Only include if email update is allowed

    @Size(max = 50)
    private String firstName;

    @Size(max = 50)
    private String lastName;

    // Add other updatable fields here
} 