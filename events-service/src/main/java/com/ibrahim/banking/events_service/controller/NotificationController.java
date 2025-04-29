package com.ibrahim.banking.events_service.controller;

import com.ibrahim.banking.events_service.model.Notification;
import com.ibrahim.banking.events_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<Notification>> getAllNotifications() {
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getNotificationsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getNotificationsByUserId(userId));
    }

    @GetMapping("/transaction/{reference}")
    public ResponseEntity<List<Notification>> getNotificationsByTransactionReference(
            @PathVariable String reference) {
        return ResponseEntity.ok(notificationService.getNotificationsByTransactionReference(reference));
    }
} 