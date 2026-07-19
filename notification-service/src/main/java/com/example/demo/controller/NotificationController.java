package com.example.demo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.entity.Notification;
import com.example.demo.service.NotificationService;

@RestController
@RequestMapping({"/notification", "/notification/"})
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    // Get notifications for a specific user (includes broadcasts)
    @GetMapping
    public ResponseEntity<List<Notification>> getByUser(@RequestParam(defaultValue = "") String userId) {
        if (userId.isEmpty()) {
            // Return all if no userId specified (backward compat)
            return ResponseEntity.ok(notificationService.getNotificationsForUser(""));
        }
        return ResponseEntity.ok(notificationService.getNotificationsForUser(userId));
    }

    // Get unread count
    @GetMapping("/unread")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@RequestParam String userId) {
        long count = notificationService.countUnread(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    // Mark all as read for a user
    @PutMapping("/read")
    public ResponseEntity<String> markAllRead(@RequestParam String userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok("All notifications marked as read");
    }

    // Create a notification (admin broadcast or targeted)
    @PostMapping
    public ResponseEntity<Notification> create(@RequestBody Map<String, String> body) {
        String message = body.getOrDefault("message", "");
        String userId = body.getOrDefault("userId", "BROADCAST");
        String type = body.getOrDefault("type", "BROADCAST");

        if (message.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Notification n = notificationService.createNotification(message, userId, type);
        return ResponseEntity.ok(n);
    }
}
