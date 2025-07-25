package com.opossum.listings.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import com.opossum.common.enums.ListingStatus;

public class ListingsDto {
    // Ajout pour accès direct à la première image
    private String photoUrl;
    private String thumbnailUrl;

    private UUID id;
    private String title;
    private String description;
    private String type;
    private String category;
    private ListingStatus status;
    private Double latitude;
    private Double longitude;
    private String address;
    private String city;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant resolvedAt;
    private UUID userId;
    private List<String> imageUrls;

    public ListingsDto(UUID id, String title, String description, String type, String category, ListingStatus status, Double latitude, Double longitude, String address, String city, Instant createdAt, Instant updatedAt, Instant resolvedAt, UUID userId, List<String> imageUrls) {
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
        this.imageUrls = imageUrls;
        // Initialisation des champs photoUrl et thumbnailUrl
        if (imageUrls != null && !imageUrls.isEmpty()) {
            this.photoUrl = imageUrls.get(0);
            this.thumbnailUrl = imageUrls.get(0); // À adapter si tu as une vraie miniature
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setStatus(ListingStatus status) {
        this.status = status;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
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

    public ListingStatus getStatus() {
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

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
}
