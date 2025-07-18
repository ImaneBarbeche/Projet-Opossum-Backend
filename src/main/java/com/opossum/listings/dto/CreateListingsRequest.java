package com.opossum.listings.dto;

import java.util.UUID;

public class CreateListingsRequest {

    private String title;
    private String description;
    private boolean isLost;
    private UUID userId;

    public CreateListingsRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isLost() {
        return isLost;
    }

    public void setLost(boolean isLost) {
        this.isLost = isLost;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}