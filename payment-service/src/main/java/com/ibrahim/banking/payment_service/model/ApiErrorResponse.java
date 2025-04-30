package com.ibrahim.banking.payment_service.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public class ApiErrorResponse {
    
    private HttpStatus status;
    private int statusCode;
    private String message;
    private String path;
    private String errorCode;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    
    public ApiErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }
    
    public ApiErrorResponse(HttpStatus status, String message, String path) {
        this();
        this.status = status;
        this.statusCode = status.value();
        this.message = message;
        this.path = path;
    }
    
    public ApiErrorResponse(HttpStatus status, String message, String path, String errorCode) {
        this(status, message, path);
        this.errorCode = errorCode;
    }

    // Getters and setters
    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
        this.statusCode = status.value();
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
} 