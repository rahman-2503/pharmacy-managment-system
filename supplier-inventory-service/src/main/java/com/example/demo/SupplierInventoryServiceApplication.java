package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class SupplierInventoryServiceApplication {

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
        SpringApplication.run(SupplierInventoryServiceApplication.class, args);
    }
}
