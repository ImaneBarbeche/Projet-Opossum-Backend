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
    private String city;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant resolvedAt;
    private UUID userId;

    public ListingsDto(UUID id, String title, String description, String type, String category, String status, Double latitude, Double longitude, String address, String city, Instant createdAt, Instant updatedAt, Instant resolvedAt, UUID userId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
        this.category = category;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.city = city;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.resolvedAt = resolvedAt;
        this.userId = userId;
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

    public String getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public String getStatus() {
        return status;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public UUID getUserId() {
        return userId;
    }
}
