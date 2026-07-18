package com.example.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Notification;
import com.example.demo.repository.NotificationRepository;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository repo;

    public Notification createNotification(String message, String userId, String type) {
        Notification n = new Notification(message, userId, type);
        return repo.save(n);
    }

    public List<Notification> getNotificationsForUser(String userId) {
        return repo.findByUserIdOrBroadcast(userId);
    }

    public long countUnread(String userId) {
        return repo.countUnread(userId);
    }

    public void markAllAsRead(String userId) {
        repo.markAllAsRead(userId);
    }

    // Broadcast to all doctors
    public Notification broadcast(String message, String type) {
        Notification n = new Notification(message, "BROADCAST", type);
        return repo.save(n);
    }
}
