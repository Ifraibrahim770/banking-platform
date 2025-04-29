package com.ibrahim.banking.payment_service.dto;

public class ErrorResponseDto {
    private String error;

    // Constructors
    public ErrorResponseDto() {
    }

    public ErrorResponseDto(String error) {
        this.error = error;
    }

    // Getters and setters
    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
} 