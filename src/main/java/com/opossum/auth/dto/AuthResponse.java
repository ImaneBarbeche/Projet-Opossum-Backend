package com.opossum.auth.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Cette classe représente la réponse envoyée au client
 * après une authentification réussie (login ou register).
 */
public class AuthResponse {

    // ID unique de l'utilisateur
    private UUID id;

    // Email de l'utilisateur
    private String email;

    // Prénom
    private String firstName;

    // Nom
    private String lastName;

    // Rôle de l'utilisateur (ex: USER, ADMIN)
    private String role;

    // Jeton d'accès JWT à courte durée de vie (ex: 30 min)
    private String accessToken;

    // Jeton de rafraîchissement JWT à plus longue durée (ex: 7 jours)
    private String refreshToken;

    // Durée de validité du accessToken en secondes (ex: 1800s = 30min)
    private long expiresIn;

    // Horodatage de la réponse
    private Instant timestamp;

    /**
     * Constructeur vide (requis par Spring et Jackson
     * pour faire la désérialisation automatique)
     */
    public AuthResponse() {
    }

    /**
     * Constructeur complet utilisé pour construire l'objet à la main
     * dans le AuthService avant de le retourner au client.
     */
    public AuthResponse(UUID id, String email, String firstName, String lastName,
                        String role, String accessToken, String refreshToken, long expiresIn, Instant timestamp) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.timestamp = timestamp;
    }

    // ==== Getters et Setters ====

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
