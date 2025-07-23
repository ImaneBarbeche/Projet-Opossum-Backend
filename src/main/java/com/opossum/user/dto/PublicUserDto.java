package com.opossum.user.dto;

import java.time.Instant;
import java.util.UUID;

public class PublicUserDto {
    private UUID id;
    private String firstName;
    private String lastName;
    private String avatar;
    private Instant createdAt;

    public PublicUserDto(UUID id, String firstName, String lastName, String avatar, Instant createdAt) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.avatar = avatar;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getAvatar() { return avatar; }
    public Instant getCreatedAt() { return createdAt; }
}
