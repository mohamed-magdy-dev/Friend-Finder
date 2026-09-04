package com.project._5.Friend_Finder.service;

import com.project._5.Friend_Finder.dto.NotificationDto;
import com.project._5.Friend_Finder.entity.Notification;
import com.project._5.Friend_Finder.entity.User;
import com.project._5.Friend_Finder.repository.NotificationRepository;
import com.project._5.Friend_Finder.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /** Retrieves all notifications for the logged-in user, sorted by date (newest first ofc).*/
    public List<NotificationDto> getUserNotifications(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);

        return notifications.stream()
                .map(notif -> new NotificationDto(
                        notif.getId(),
                        notif.getMessage(),
                        notif.isRead(),
                        notif.getCreatedAt(),
                        notif.getPostId()
                ))
                .collect(Collectors.toList());
    }

    /*** Marks a specific notification as read when the user clicks on it.*/
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setRead(true);
        notificationRepository.save(notification);
    }
}