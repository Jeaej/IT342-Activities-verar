package com.verar.oauth2login.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        // Allow unauthenticated access to the home ("/") and login ("/login") pages
                        .requestMatchers("/", "/login").permitAll()
                        // Require authentication for all other requests
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        // Redirect the user to "/user-info" after a successful OAuth2 login
                        .defaultSuccessUrl("/user-info", true)
                )
                .logout(logout -> logout
                        // Redirect to home ("/") after logging out
                        .logoutSuccessUrl("/login")
                        // Invalidate session and clear authentication on logout
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                )
                // Disable CSRF protection (useful for APIs, but reconsider for forms)
                .csrf(AbstractHttpConfigurer::disable)
                .build();
    }
}
