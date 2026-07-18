package com.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.config.RabbitMQConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class NotificationProducer {

    private static final Logger log = LoggerFactory.getLogger(NotificationProducer.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private RabbitTemplate template;

    public void sendNotification(String message, String type) {
        try {
            ObjectNode json = objectMapper.createObjectNode();
            json.put("message", message);
            json.put("userId", "BROADCAST");
            json.put("type", type);

            template.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.ROUTING_KEY,
                json.toString()
            );
            log.info("Notification sent via RabbitMQ: {}", message);
        } catch (Exception e) {
            log.error("Failed to send notification via RabbitMQ: {}", e.getMessage());
        }
    }
}
