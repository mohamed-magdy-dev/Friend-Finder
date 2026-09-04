package com.project._5.Friend_Finder.controller;

import com.project._5.Friend_Finder.dto.NotificationDto;
import com.project._5.Friend_Finder.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Endpoint to get all notifications for the current user.
     */
    @GetMapping
    public ResponseEntity<List<NotificationDto>> getNotifications(Principal principal) {
        List<NotificationDto> notifications = notificationService.getUserNotifications(principal.getName());
        return ResponseEntity.ok(notifications);
    }

    /**
     * Endpoint to mark a notification as read.
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<String> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok("Notification marked as read");
    }
}