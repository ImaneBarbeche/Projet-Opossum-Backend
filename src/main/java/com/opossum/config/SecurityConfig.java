package com.opossum.config;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.opossum.auth.JwtFilter;

/**
 * Configuration de sécurité Spring Security
 * <p>
 * - Définit le filtre JWT pour l'authentification par token
 * - Configure les routes publiques et protégées
 * - Désactive la protection CSRF (API REST)
 * - Définit l'encodeur de mot de passe (BCrypt)
 */
@Configuration
public class SecurityConfig {
    /**
     * Filtre JWT injecté pour valider les tokens sur chaque requête.
     */
    private final JwtFilter jwtFilter;

    /**
     * Constructeur avec injection du filtre JWT.
     */
    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Bean Spring pour récupérer l'AuthenticationManager (utilisé pour l'authentification).
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Bean principal de configuration de la sécurité HTTP.
     * <p>
     * - Désactive CSRF (inutile pour une API REST)
     * - Autorise l'accès public aux routes /api/v1/auth/** et /api/v1/public/**
     * - Protège toutes les autres routes (authentification requise)
     * - Ajoute le filtre JWT avant le filtre d'authentification standard
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/listings/me").authenticated()
                        .anyRequest().permitAll())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
