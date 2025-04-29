package com.ibrahim.banking.store_of_value_service.dto;

import com.ibrahim.banking.store_of_value_service.model.AccountType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateAccountRequest {

    @NotNull(message = "Account type cannot be null")
    private AccountType accountType;

} 