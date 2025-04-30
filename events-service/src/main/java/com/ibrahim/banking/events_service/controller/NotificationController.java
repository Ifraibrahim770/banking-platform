package com.ibrahim.banking.events_service.controller;

import com.ibrahim.banking.events_service.model.Notification;
import com.ibrahim.banking.events_service.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "API for managing notifications")
@SecurityRequirement(name = "Bearer Authentication")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Get all notifications", description = "Retrieves all notifications in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved notifications", 
                     content = @Content(array = @ArraySchema(schema = @Schema(implementation = Notification.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Server error")
    })
    @GetMapping
    public ResponseEntity<List<Notification>> getAllNotifications() {
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }

    @Operation(summary = "Get notifications by user ID", description = "Retrieves all notifications for a specific user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved notifications", 
                     content = @Content(array = @ArraySchema(schema = @Schema(implementation = Notification.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Server error")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getNotificationsByUserId(
            @Parameter(description = "ID of the user") @PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getNotificationsByUserId(userId));
    }

    @Operation(summary = "Get notifications by transaction reference", 
              description = "Retrieves all notifications for a specific transaction")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved notifications", 
                     content = @Content(array = @ArraySchema(schema = @Schema(implementation = Notification.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Server error")
    })
    @GetMapping("/transaction/{reference}")
    public ResponseEntity<List<Notification>> getNotificationsByTransactionReference(
            @Parameter(description = "Transaction reference") @PathVariable String reference) {
        return ResponseEntity.ok(notificationService.getNotificationsByTransactionReference(reference));
    }
} 