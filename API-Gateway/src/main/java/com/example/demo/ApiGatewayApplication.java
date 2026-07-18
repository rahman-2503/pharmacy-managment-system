package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

    static {
        try {
            io.github.cdimascio.dotenv.Dotenv dotenv = io.github.cdimascio.dotenv.Dotenv.configure().load();
            dotenv.entries().forEach(e -> {
                if (System.getenv(e.getKey()) == null) {
                    System.setProperty(e.getKey(), e.getValue());
                }
            });
        } catch (Exception e) {
            // .env not found — using OS env vars or application.properties defaults
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @Bean
    public RouteLocator customRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("user-service", r -> r.path("/users/**")
                .filters(f -> f.addRequestHeader("X-Request-Id", "12345"))
                .uri("https://user-service-id7r.onrender.com"))
            .route("order-service", r -> r.path("/orders/**")
                .uri("https://order-service-l60x.onrender.com"))
            .route("inventory-service", r -> r.path("/inventory/**")
                .uri("https://supplier-inventory-service.onrender.com"))
            .route("payment-service", r -> r.path("/payment/**")
                .uri("https://payment-service-4w2d.onrender.com"))
            .route("notification-service", r -> r.path("/notification/**")
                .uri("https://notification-service-1636.onrender.com"))
            .route("eureka-server", r -> r.path("/eureka/**")
                .uri("https://eureka-server-3nyd.onrender.com"))
            .build();
    }
}
