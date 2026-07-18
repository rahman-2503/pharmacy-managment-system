package com.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.config.RabbitMQConfig;
import com.example.demo.entity.Notification;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void receiveMessage(String message) {
        log.info("Received RabbitMQ message: {}", message);

        try {
            // Try parsing as JSON first (structured message from producers)
            if (message.trim().startsWith("{")) {
                JsonNode json = objectMapper.readTree(message);
                String msg = json.has("message") ? json.get("message").asText() : message;
                String userId = json.has("userId") ? json.get("userId").asText() : "BROADCAST";
                String type = json.has("type") ? json.get("type").asText() : "ORDER";

                notificationService.createNotification(msg, userId, type);
            } else {
                // Legacy plain text message from order/payment service
                String type = "ORDER";
                if (message.contains("Payment")) {
                    type = "PAYMENT";
                }

                // Broadcast system notifications to all
                notificationService.createNotification(message, "BROADCAST", type);
            }

            log.info("Notification saved successfully");
        } catch (Exception e) {
            log.error("Failed to process notification message: {}", e.getMessage());
        }
    }
}
