package com.learning.globallearningcalendar.controller;

import com.learning.globallearningcalendar.dto.NotificationDTO;
import com.learning.globallearningcalendar.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Notifications", description = "User notification management")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all notifications for a user")
    public ResponseEntity<List<NotificationDTO>> getUserNotifications(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getUserNotifications(userId));
    }

    @GetMapping("/user/{userId}/unread")
    @Operation(summary = "Get unread notifications for a user")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getUnreadNotifications(userId));
    }

    @GetMapping("/user/{userId}/unread-count")
    @Operation(summary = "Get unread notification count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@PathVariable Long userId) {
        Long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PutMapping("/{notificationId}/read")
    @Operation(summary = "Mark a notification as read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long notificationId,
            @RequestParam Long userId) {
        notificationService.markAsRead(notificationId, userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/user/{userId}/mark-all-read")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<Void> markAllAsRead(@PathVariable Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{notificationId}")
    @Operation(summary = "Delete a notification")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long notificationId,
            @RequestParam Long userId) {
        notificationService.deleteNotification(notificationId, userId);
        return ResponseEntity.ok().build();
    }
}
