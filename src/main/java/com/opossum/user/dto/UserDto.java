package com.opossum.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class UserDto {

    private UUID id;
    private String firstname;
    private String lastname;
    private String email;
    private String phone;
    private String avatarUrl;
    private String role;
    private boolean isActive;
    private boolean isEmailVerified;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastLoginAt;
}
