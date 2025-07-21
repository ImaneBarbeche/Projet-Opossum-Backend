
package com.opossum.listings;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "listings")
public class Listings {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(name = "type", length = 10, nullable = false)
    private String type; // LOST/FOUND

    @Column(name = "category", length = 50, nullable = false)
    private String category;

    @Column(nullable = false)
    private boolean isLost;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "latitude", precision = 10, scale = 8, nullable = false)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 11, scale = 8, nullable = false)
    private BigDecimal longitude;

    @Column(name = "address")
    private String address;

    @Column(name = "city", length = 100, nullable = false)
    private String city;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;
   


    public Listings() {
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public java.math.BigDecimal getLatitude() {
        return latitude;
    }

    public java.math.BigDecimal getLongitude() {
        return longitude;
    }

    public String getAddress() {
        return address;
    }

    public UUID getUserId() {
        return userId;
    }
    
    public String getCity() {
        return city;
    }

    public String getType() {
        return type;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public String getStatus() {
        return status;
    }

    // setters
    public void setId(UUID id) {
        this.id = id;
    }

    public void setCategory(String category) {
        this.category = category;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setIsLost(boolean isLost) {
        this.isLost = isLost;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public boolean getIsLost() {
        return isLost;
    }

     

}