package com.ibrahim.banking.store_of_value_service.controller;

import com.ibrahim.banking.store_of_value_service.dto.AccountResponse;
import com.ibrahim.banking.store_of_value_service.dto.CreateAccountRequest;
import com.ibrahim.banking.store_of_value_service.dto.UpdateAccountRequest;
import com.ibrahim.banking.store_of_value_service.dto.TransactionRequest;
import com.ibrahim.banking.store_of_value_service.dto.AccountStatusResponse;
import com.ibrahim.banking.store_of_value_service.model.AccountStatus;
import com.ibrahim.banking.store_of_value_service.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Account", description = "Account management API")
@SecurityRequirement(name = "Bearer Authentication")
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "Create new account", description = "Creates a new bank account (Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Account created successfully", 
                     content = @Content(schema = @Schema(implementation = AccountResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - requires admin role")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        AccountResponse response = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get accounts by profile ID", description = "Retrieves all accounts for a given user profile ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved accounts", 
                     content = @Content(array = @ArraySchema(schema = @Schema(implementation = AccountResponse.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/user/{profileId}")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<List<AccountResponse>> getAccountsByProfileId(
            @Parameter(description = "Profile ID of the user") @PathVariable String profileId) {
        List<AccountResponse> accounts = accountService.getAccountsByProfileId(profileId);
        return ResponseEntity.ok(accounts);
    }

    @Operation(summary = "Update account", description = "Updates an existing account's details (Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account updated successfully", 
                     content = @Content(schema = @Schema(implementation = AccountResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - requires admin role"),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @PutMapping("/{accountNumber}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<AccountResponse> updateAccount(
            @Parameter(description = "Account number to update") @PathVariable String accountNumber,
            @Valid @RequestBody UpdateAccountRequest request) {
        AccountResponse response = accountService.updateAccount(accountNumber, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get account balance", description = "Retrieves the current balance for an account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved balance", 
                     content = @Content(schema = @Schema(implementation = BigDecimal.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @GetMapping("/{accountNumber}/balance")
    @PreAuthorize("hasAuthority('ROLE_USER')") // users only endpoint
    public ResponseEntity<BigDecimal> getBalance(
            @Parameter(description = "Account number") @PathVariable String accountNumber) {
        BigDecimal balance = accountService.getBalance(accountNumber);
        return ResponseEntity.ok(balance);
    }

    @Operation(summary = "Activate account", description = "Activates a deactivated account (Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account activated successfully", 
                     content = @Content(schema = @Schema(implementation = AccountResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - requires admin role"),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @PatchMapping("/{accountNumber}/activate")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<AccountResponse> activateAccount(
            @Parameter(description = "Account number to activate") @PathVariable String accountNumber) {
        AccountResponse response = accountService.activateAccount(accountNumber);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Deactivate account", description = "Deactivates an active account (Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account deactivated successfully", 
                     content = @Content(schema = @Schema(implementation = AccountResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - requires admin role"),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @PatchMapping("/{accountNumber}/deactivate")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<AccountResponse> deactivateAccount(
            @Parameter(description = "Account number to deactivate") @PathVariable String accountNumber) {
        AccountResponse response = accountService.deactivateAccount(accountNumber);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Credit account", description = "Adds funds to an account (Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account credited successfully", 
                     content = @Content(schema = @Schema(implementation = AccountResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - requires admin role"),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @PostMapping("/{accountNumber}/credit")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<AccountResponse> creditAccount(
            @Parameter(description = "Account number to credit") @PathVariable String accountNumber,
            @Valid @RequestBody TransactionRequest request) {
        AccountResponse response = accountService.creditAccount(accountNumber, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Debit account", description = "Withdraws funds from an account (Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account debited successfully", 
                     content = @Content(schema = @Schema(implementation = AccountResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data or insufficient funds"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - requires admin role"),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @PostMapping("/{accountNumber}/debit")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<AccountResponse> debitAccount(
            @Parameter(description = "Account number to debit") @PathVariable String accountNumber,
            @Valid @RequestBody TransactionRequest request) {
        AccountResponse response = accountService.debitAccount(accountNumber, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get account status", description = "Retrieves the current status of an account (Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved account status", 
                     content = @Content(schema = @Schema(implementation = AccountStatusResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - requires admin role"),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @GetMapping("/{accountNumber}/status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<AccountStatusResponse> getAccountStatus(
            @Parameter(description = "Account number") @PathVariable String accountNumber) {
        AccountStatusResponse statusResponse = accountService.getAccountStatus(accountNumber);
        return ResponseEntity.ok(statusResponse);
    }
} 