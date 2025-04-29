package com.ibrahim.banking.store_of_value_service.model;

public enum AccountStatus {
    PENDING_ACTIVATION, // Initial state before activation
    ACTIVE,             // Account is active and usable
    INACTIVE,           // Account is temporarily deactivated
    CLOSED              // Account is permanently closed
} 