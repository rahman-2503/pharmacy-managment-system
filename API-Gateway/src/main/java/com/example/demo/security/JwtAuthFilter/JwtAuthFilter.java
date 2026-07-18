package com.example.demo.security.JwtAuthFilter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class JwtAuthFilter implements GlobalFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    @Value("${jwt.secret}")
    private String secret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        HttpMethod method = exchange.getRequest().getMethod();

        // Public endpoints
        if (path.equals("/users/signup") || path.equals("/users/login") || path.startsWith("/payment/public")) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        try {
            String token = authHeader.substring(7);

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String role = claims.get("role", String.class);

            // ✅ ADMIN rules
            if ("ADMIN".equals(role)) {
                // Block placing orders
                if (path.startsWith("/orders") && method == HttpMethod.POST) {
                    return forbidden(exchange);
                }
                // Block payments
                if (path.startsWith("/payment")) {
                    return forbidden(exchange);
                }
                return forward(exchange, chain, authHeader); // Admin can do everything else
            }

            // ✅ DOCTOR rules
            if ("DOCTOR".equals(role)) {
                // Doctor can view/get user profile
                if (path.startsWith("/users")) {
                    return forward(exchange, chain, authHeader);
                }
                // Doctor can view/get notifications
                if (path.startsWith("/notification")) {
                    return forward(exchange, chain, authHeader);
                }
                // Doctor can view inventory
                if (path.startsWith("/inventory") && method == HttpMethod.GET) {
                    return forward(exchange, chain, authHeader);
                }
                // Doctor can place, cancel, retry, and view orders
                if (path.startsWith("/orders") &&
                        (method == HttpMethod.POST   // place
                        || path.contains("/cancel") // cancel
                        || path.contains("/retry")  // retry
                        || path.contains("/update-status") // allow client status sync on pay
                        || method == HttpMethod.GET)) { // view orders
                    return forward(exchange, chain, authHeader);
                }
                // Doctor can make payments
                if (path.startsWith("/payment")) {
                    return forward(exchange, chain, authHeader);
                }
                return forbidden(exchange);
            }

            // ✅ PAYMENT SERVICE rules
            if ("PAYMENT".equals(role)) {
                if (path.startsWith("/orders/fail") || path.startsWith("/orders/update-status")) {
                    return forward(exchange, chain, authHeader);
                }
                return forbidden(exchange);
            }

            return forbidden(exchange);

        } catch (Exception e) {
            log.error("JWT ERROR: {}", e.getMessage());
            return unauthorized(exchange);
        }
    }

    private Mono<Void> forward(ServerWebExchange exchange, GatewayFilterChain chain, String authHeader) {
        ServerHttpRequest request = exchange.getRequest()
                .mutate()
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .build();
        return chain.filter(exchange.mutate().request(request).build());
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    private Mono<Void> forbidden(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        return exchange.getResponse().setComplete();
    }
}
