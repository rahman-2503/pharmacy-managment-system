package com.example.demo;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SelfPingScheduler {

    private static final Logger log = LoggerFactory.getLogger(SelfPingScheduler.class);

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    private final String healthUrl;

    public SelfPingScheduler(@Value("${eureka.instance.hostname:}") String hostname) {
        boolean hasHost = hostname != null && !hostname.isBlank();
        this.healthUrl = hasHost ? "https://" + hostname + "/health" : "";
        log.info("SelfPingScheduler {}",
                hasHost ? "enabled -> " + healthUrl : "disabled (no eureka hostname set)");
    }

    // Ping our own public /health endpoint every 5 minutes to keep the
    // Render free-tier instance from sleeping (24/7 availability).
    @Scheduled(fixedRate = 300000, initialDelay = 120000)
    public void pingSelf() {
        if (healthUrl.isEmpty()) {
            return;
        }
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(healthUrl))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                log.info("Self-ping OK: {}", healthUrl);
            } else {
                log.debug("Self-ping status {}: {}", response.statusCode(), healthUrl);
            }
        } catch (Exception e) {
            log.debug("Self-ping failed: {}", e.getMessage());
        }
    }
}
