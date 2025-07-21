package com.opossum.user.dto;

import com.opossum.common.enums.Role;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO (Data Transfer Object) pour exposer les données d'un utilisateur au
 * front.
 */
public class UserDto {

    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String avatar;
    private Role role; // Modification : utiliser l'énumération Role au lieu de String
    private boolean isActive;
    private boolean isEmailVerified;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastLoginAt;

    // Constructeur
    public UserDto(UUID id, String firstName, String lastName, String email, String phone,
            String avatar, Role role, boolean isActive, boolean isEmailVerified,
            Instant createdAt, Instant updatedAt, Instant lastLoginAt) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.avatar = avatar;
        this.role = role; // Modification : utiliser l'énumération Role au lieu de String
        this.isActive = isActive;
        this.isEmailVerified = isEmailVerified;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastLoginAt = lastLoginAt;
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getAvatar() {
        return avatar;
    }

    public Role getRole() { // Modification : utiliser l'énumération Role au lieu de String
        return role;
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean isEmailVerified() {
        return isEmailVerified;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    // Setters
    public void setId(UUID id) {
        this.id = id;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setRole(Role role) { // Modification : utiliser l'énumération Role au lieu de String
        this.role = role;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void setEmailVerified(boolean emailVerified) {
        isEmailVerified = emailVerified;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setLastLoginAt(Instant lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }
}
