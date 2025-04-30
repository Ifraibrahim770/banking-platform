package com.ibrahim.banking.store_of_value_service.controller;

import com.ibrahim.banking.store_of_value_service.dto.AccountResponse;
import com.ibrahim.banking.store_of_value_service.dto.CreateAccountRequest;
import com.ibrahim.banking.store_of_value_service.dto.UpdateAccountRequest;
import com.ibrahim.banking.store_of_value_service.dto.TransactionRequest;
import com.ibrahim.banking.store_of_value_service.dto.AccountStatusResponse;
import com.ibrahim.banking.store_of_value_service.model.AccountStatus;
import com.ibrahim.banking.store_of_value_service.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        AccountResponse response = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{accountNumber}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<AccountResponse> updateAccount(@PathVariable String accountNumber,
                                                         @Valid @RequestBody UpdateAccountRequest request) {
        AccountResponse response = accountService.updateAccount(accountNumber, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{accountNumber}/balance")
    @PreAuthorize("hasAuthority('ROLE_USER')") // users only endpoint
    public ResponseEntity<BigDecimal> getBalance(@PathVariable String accountNumber) {
        BigDecimal balance = accountService.getBalance(accountNumber);
        return ResponseEntity.ok(balance);
    }

    @PatchMapping("/{accountNumber}/activate")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<AccountResponse> activateAccount(@PathVariable String accountNumber) {
        AccountResponse response = accountService.activateAccount(accountNumber);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{accountNumber}/deactivate")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<AccountResponse> deactivateAccount(@PathVariable String accountNumber) {
        AccountResponse response = accountService.deactivateAccount(accountNumber);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{accountNumber}/credit")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<AccountResponse> creditAccount(@PathVariable String accountNumber,
                                                         @Valid @RequestBody TransactionRequest request) {
        AccountResponse response = accountService.creditAccount(accountNumber, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{accountNumber}/debit")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<AccountResponse> debitAccount(@PathVariable String accountNumber,
                                                        @Valid @RequestBody TransactionRequest request) {
        AccountResponse response = accountService.debitAccount(accountNumber, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{accountNumber}/status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<AccountStatusResponse> getAccountStatus(@PathVariable String accountNumber) {
        AccountStatusResponse statusResponse = accountService.getAccountStatus(accountNumber);
        return ResponseEntity.ok(statusResponse);
    }
} 