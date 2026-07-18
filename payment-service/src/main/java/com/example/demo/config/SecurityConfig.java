package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // ✅ REQUIRED FIX (THIS WAS MISSING → CAUSED CRASH)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ✅ SECURITY RULES
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth

                // ✅ ALLOW PAYMENT APIs (VERY IMPORTANT)
                .requestMatchers("/payment/**").permitAll()

                // ✅ Allow actuator (optional)
                .requestMatchers("/actuator/**").permitAll()

                // ✅ Allow OPTIONS (for frontend calls)
                .requestMatchers("/**").permitAll()

                // ✅ everything else requires auth
                .anyRequest().authenticated()
            );

        return http.build();
    }
}