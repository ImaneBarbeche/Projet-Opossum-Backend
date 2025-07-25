package com.opossum.listings;
import java.util.*;

public class ListingsMapper {
    public static Map<String, Object> toSummaryMap(Listings l) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", l.getId());
        map.put("title", l.getTitle());
        map.put("type", l.getType() != null ? l.getType().name() : null);
        map.put("category", l.getCategory());
        map.put("status", l.getStatus() != null ? l.getStatus().name() : null);
        map.put("createdAt", l.getCreatedAt());
        // Ajout photoUrl et thumbnailUrl
        List<String> imageUrls = l.getImages() != null ? l.getImages().stream().map(f -> f.getUrl()).toList() : List.of();
        map.put("photoUrl", !imageUrls.isEmpty() ? imageUrls.get(0) : null);
        map.put("thumbnailUrl", !imageUrls.isEmpty() ? imageUrls.get(0) : null); // à adapter si miniature
        return map;
    }

    public static Map<String, Object> toSearchMap(Listings l) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", l.getId() != null ? l.getId() : null);
        map.put("title", l.getTitle() != null ? l.getTitle() : null);
        map.put("description", l.getDescription() != null ? l.getDescription() : null);
        map.put("type", l.getType() != null ? l.getType().name() : null);
        map.put("category", l.getCategory() != null ? l.getCategory() : null);
        map.put("status", l.getStatus() != null ? l.getStatus().name() : null);
        map.put("city", l.getCity() != null ? l.getCity() : null);
        map.put("address", l.getAddress() != null ? l.getAddress() : null);
        map.put("created_at", l.getCreatedAt() != null ? l.getCreatedAt() : null);
        return map;
    }

    public static Map<String, Object> toDetailMap(Listings l) {
        Map<String, Object> location = new HashMap<>();
        location.put("latitude", l.getLatitude() != null ? l.getLatitude().doubleValue() : null);
        location.put("longitude", l.getLongitude() != null ? l.getLongitude().doubleValue() : null);
        location.put("address", l.getAddress() != null ? l.getAddress() : null);
        location.put("city", l.getCity() != null ? l.getCity() : null);

        Map<String, Object> contactInfo = new HashMap<>();
        contactInfo.put("phone", null);
        contactInfo.put("email", null);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", l.getUserId() != null ? l.getUserId() : null);
        userInfo.put("firstName", null);
        userInfo.put("lastName", null);
        userInfo.put("avatar", null);

        // Expose la liste des URLs d'images
        List<String> imageUrls = l.getImages() != null ? l.getImages().stream().map(f -> f.getUrl()).toList() : List.of();

        Map<String, Object> data = new HashMap<>();
        data.put("id", l.getId() != null ? l.getId() : null);
        data.put("title", l.getTitle() != null ? l.getTitle() : null);
        data.put("description", l.getDescription() != null ? l.getDescription() : null);
        data.put("type", l.getType() != null ? l.getType().name() : null);
        data.put("category", l.getCategory() != null ? l.getCategory() : null);
        data.put("status", l.getStatus() != null ? l.getStatus().name() : null);
        data.put("location", location);
        data.put("imageUrls", imageUrls);
        // Ajout photoUrl et thumbnailUrl
        data.put("photoUrl", !imageUrls.isEmpty() ? imageUrls.get(0) : null);
        data.put("thumbnailUrl", !imageUrls.isEmpty() ? imageUrls.get(0) : null); // à adapter si miniature
        data.put("contactInfo", contactInfo);
        data.put("user", userInfo);
        data.put("createdAt", l.getCreatedAt() != null ? l.getCreatedAt() : null);
        data.put("updatedAt", l.getUpdatedAt() != null ? l.getUpdatedAt() : null);
        if (l.getStatus() != null && l.getStatus() == com.opossum.common.enums.ListingStatus.RESOLVED) {
            data.put("resolvedAt", l.getResolvedAt() != null ? l.getResolvedAt() : null);
        }
        return data;
    }

    public static Map<String, Object> toCreateMap(Listings l) {
        Map<String, Object> location = new HashMap<>();
        location.put("city", l.getCity());
        Map<String, Object> data = new HashMap<>();
        data.put("id", l.getId());
        data.put("title", l.getTitle());
        data.put("type", l.getType() != null ? l.getType().name() : null);
        data.put("status", l.getStatus() != null ? l.getStatus().name() : null);
        data.put("location", location);
        data.put("locationValidated", true); // à adapter selon ta logique
        data.put("createdAt", l.getCreatedAt());
        return data;
    }
}
