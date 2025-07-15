package com.opossum.user;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue
    private UUID id;

    private String firstname;
    private String lastname;
    private String email;
    private String phone;
    private String passwordHash;
    private String avatarUrl;
    private String role;
    private boolean isActive;
    private boolean isEmailVerified;
    private String emailVerificationToken;

    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastLoginAt;
}
