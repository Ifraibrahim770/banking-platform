package com.ibrahim.banking.store_of_value_service.dto;

import com.ibrahim.banking.store_of_value_service.model.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateAccountRequest {

    @NotBlank(message = "Profile ID cannot be blank")
    private String profileId;

    @NotNull(message = "Account type cannot be null")
    private AccountType accountType;
} 