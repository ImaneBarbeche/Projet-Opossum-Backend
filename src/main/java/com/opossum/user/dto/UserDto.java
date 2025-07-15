package com.opossum.user.dto;

import java.util.UUID;

/**
 * DTO pour exposer les informations d’un utilisateur sans données sensibles.
 * Sert pour les réponses d'API (profil, admin, etc.).
 */
public class UserDto {

    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String avatarUrl;
    private String role;
    private boolean isActive;
    private boolean isEmailVerified;

    // === Constructeurs ===

    public UserDto() {
    }

    public UserDto(UUID id, String email, String firstName, String lastName,
                   String phone, String avatarUrl, String role, boolean isActive, boolean isEmailVerified) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.avatarUrl = avatarUrl;
        this.role = role;
        this.isActive = isActive;
        this.isEmailVerified = isEmailVerified;
    }

    // === Getters et Setters ===

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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isEmailVerified() {
        return isEmailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        isEmailVerified = emailVerified;
    }
}
