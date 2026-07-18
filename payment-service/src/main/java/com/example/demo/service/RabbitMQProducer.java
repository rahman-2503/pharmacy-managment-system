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
public class RabbitMQProducer {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQProducer.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private RabbitTemplate template;

    private static final String EXCHANGE = "order_exchange";
    private static final String ROUTING_KEY = "order_routing";

    public void sendMessage(String message) {
        try {
            ObjectNode json = objectMapper.createObjectNode();
            json.put("message", message);
            json.put("userId", "BROADCAST");
            json.put("type", "PAYMENT");

            template.convertAndSend(EXCHANGE, ROUTING_KEY, json.toString());
            log.info("Payment notification sent: {}", message);
        } catch (Exception e) {
            log.error("Failed to send payment notification: {}", e.getMessage());
        }
    }
}
