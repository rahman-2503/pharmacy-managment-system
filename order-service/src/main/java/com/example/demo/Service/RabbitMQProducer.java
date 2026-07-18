package com.example.demo.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class RabbitMQProducer {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQProducer.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final RabbitTemplate template;

    public RabbitMQProducer(RabbitTemplate template) {
        this.template = template;
    }

    private static final String EXCHANGE = "order_exchange";
    private static final String ROUTING_KEY = "order_routing";

    public void sendMessage(String message) {
        try {
            ObjectNode json = objectMapper.createObjectNode();
            json.put("message", message);
            json.put("userId", "BROADCAST");
            json.put("type", "ORDER");

            template.convertAndSend(EXCHANGE, ROUTING_KEY, json.toString());
            log.info("Order notification sent: {}", message);
        } catch (Exception e) {
            log.error("Failed to send order notification: {}", e.getMessage());
        }
    }
}
