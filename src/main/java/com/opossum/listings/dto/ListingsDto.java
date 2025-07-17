package com.opossum.listings.dto;

import java.time.Instant;
import java.util.UUID;

public class ListingsDto {
    private UUID id;
    private String title;
    private String description;
    private boolean isLost;
    private UUID userId;
    private Instant createdAt;
    private Instant updatedAt;

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
