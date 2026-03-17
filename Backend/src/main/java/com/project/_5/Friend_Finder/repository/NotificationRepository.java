package com.project._5.Friend_Finder.repository;

import com.project._5.Friend_Finder.entity.Notification;
import com.project._5.Friend_Finder.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // this for notification, based on the user ofc and its message.
    List<Notification> findByUserAndMessage(User user, String message);
}