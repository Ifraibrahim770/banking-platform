package com.ibrahim.banking.payment_service.exception;

import com.ibrahim.banking.payment_service.model.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, HttpServletRequest request) {
        
        logger.error("Resource not found: {} with ID: {}", ex.getResourceType(), ex.getResourceId());
        
        ApiErrorResponse errorResponse = new ApiErrorResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request.getRequestURI(),
                "RESOURCE_NOT_FOUND"
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PaymentProcessingException.class)
    public ResponseEntity<ApiErrorResponse> handlePaymentProcessingException(
            PaymentProcessingException ex, HttpServletRequest request) {
        
        logger.error("Payment processing error: {}", ex.getMessage(), ex);
        
        ApiErrorResponse errorResponse = new ApiErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage(),
                request.getRequestURI(),
                ex.getErrorCode() != null ? ex.getErrorCode() : "PAYMENT_PROCESSING_ERROR"
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InvalidTransactionException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidTransactionException(
            InvalidTransactionException ex, HttpServletRequest request) {
        
        logger.error("Invalid transaction: {}, Reason: {}", ex.getTransactionId(), ex.getReason());
        
        ApiErrorResponse errorResponse = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request.getRequestURI(),
                "INVALID_TRANSACTION"
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(ConcurrentTransactionException.class)
    public ResponseEntity<ApiErrorResponse> handleConcurrentTransactionException(
            ConcurrentTransactionException ex, HttpServletRequest request) {
        
        logger.warn("Concurrent transaction detected: {}", ex.getMessage());
        
        ApiErrorResponse errorResponse = new ApiErrorResponse(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                request.getRequestURI(),
                "CONCURRENT_TRANSACTION"
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {
        
        logger.error("Bad request: {}", ex.getMessage());
        
        ApiErrorResponse errorResponse = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request.getRequestURI(),
                "INVALID_ARGUMENT"
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
            
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                    FieldError::getField, 
                    fieldError -> fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "Invalid value"
                ));
        
        String errorMessage = "Validation failed: " + errors.entrySet()
                .stream()
                .map(entry -> entry.getKey() + " " + entry.getValue())
                .collect(Collectors.joining(", "));
        
        logger.warn("Validation error: {}", errorMessage);
        
        ApiErrorResponse errorResponse = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST,
                errorMessage,
                request.getRequestURI(),
                "VALIDATION_FAILED"
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
            
        String errorMessage = String.format("Parameter '%s' should be of type '%s'", 
                ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        
        logger.warn("Type mismatch error: {}", errorMessage);
        
        ApiErrorResponse errorResponse = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST,
                errorMessage,
                request.getRequestURI(),
                "TYPE_MISMATCH"
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
            
        logger.warn("Missing parameter: {}", ex.getParameterName());
        
        ApiErrorResponse errorResponse = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST,
                String.format("Required parameter '%s' is missing", ex.getParameterName()),
                request.getRequestURI(),
                "MISSING_PARAMETER"
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {
            
        logger.warn("Access denied: {}", ex.getMessage());
        
        ApiErrorResponse errorResponse = new ApiErrorResponse(
                HttpStatus.FORBIDDEN,
                "Access denied: You don't have permission to access this resource",
                request.getRequestURI(),
                "ACCESS_DENIED"
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
            
        logger.error("Unhandled exception occurred", ex);
        
        ApiErrorResponse errorResponse = new ApiErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.",
                request.getRequestURI(),
                "INTERNAL_SERVER_ERROR"
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
} 