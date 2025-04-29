package com.ibrahim.banking.store_of_value_service.dto;

import com.ibrahim.banking.store_of_value_service.model.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountStatusResponse {
    private String accountNumber;
    private AccountStatus status;
} 