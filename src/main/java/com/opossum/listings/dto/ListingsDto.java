package com.opossum.listings.dto;

import java.time.Instant;
import java.util.UUID;

public class ListingsDto {

    private UUID id;
    private String title;
    private String description;
    private String type;
    private String category;
    private String status;
    private Double latitude;
    private Double longitude;
    private String address;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant resolvedAt;
    private UUID userId;

    private String city;

    public ListingsDto(UUID id, String title, String description, String type, String category, String status, Double latitude, Double longitude, String address, Instant createdAt, Instant updatedAt, Instant resolvedAt, UUID userId, String city) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
        this.category = category;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.resolvedAt = resolvedAt;
        this.userId = userId;
        this.city = city;
    }
    created_at timestamp[
    not null, default: `CURRENT_TIMESTAMP
    `]
updated_at timestamp[
    default: `CURRENT_TIMESTAMP
    `]
resolved_at timestamp // plusieurs listings pour 1 user

    user_id uuid[
    not

    null]

    public ListingsDto(UUID id, String title, String description, boolean isLost, UUID userId, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.isLost = isLost;
        this.userId = userId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public boolean isLost() {
        return isLost;
    }

    public UUID getUserId() {
        return userId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
