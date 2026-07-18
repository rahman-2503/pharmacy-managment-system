package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Get notifications for a specific user OR all broadcast notifications
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId OR n.userId = 'BROADCAST' ORDER BY n.timestamp DESC")
    List<Notification> findByUserIdOrBroadcast(String userId);

    // Count unread notifications for a user (including broadcasts)
    @Query("SELECT COUNT(n) FROM Notification n WHERE (n.userId = :userId OR n.userId = 'BROADCAST') AND n.read = false")
    long countUnread(String userId);

    // Mark all as read for a user
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.read = true WHERE (n.userId = :userId OR n.userId = 'BROADCAST') AND n.read = false")
    void markAllAsRead(String userId);
}
