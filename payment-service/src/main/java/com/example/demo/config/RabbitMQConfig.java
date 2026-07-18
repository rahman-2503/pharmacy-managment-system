package com.example.demo.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE = "order_queue";
    public static final String EXCHANGE = "order_exchange";
    public static final String ROUTING_KEY = "order_routing";

    // ✅ Define queue
    @Bean
    public Queue queue() {
        return new Queue(QUEUE, true); // durable queue
    }

    // ✅ Define exchange
    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE);
    }

    // ✅ Bind queue to exchange with routing key
    @Bean
    public Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue)
                .to(exchange)
                .with(ROUTING_KEY);
    }
}
