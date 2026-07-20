package com.smartrecruit.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for stateless REST APIs and easy Swagger testing
            .csrf(csrf -> csrf.disable())
            
            // TEMPORARY DEV CONFIGURATION:
            // Allow unrestricted access to ALL endpoints (including Swagger UI)
            // Replace this with proper OAuth2 rules once Keycloak is configured!
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            );
            
        return http.build();
    }
}
