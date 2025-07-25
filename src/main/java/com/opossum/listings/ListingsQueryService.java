package com.opossum.listings;

import com.opossum.common.enums.ListingStatus;
import com.opossum.common.enums.ListingType;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ListingsQueryService {
    private final ListingsService listingsService;

    public ListingsQueryService(ListingsService listingsService) {
        this.listingsService = listingsService;
    }

    /**
     * Récupère les annonces d'un utilisateur avec filtrage, tri et pagination.
     */
    public Map<String, Object> getUserListingsPaged(
            UUID userId,
            ListingType type,
            ListingStatus status,
            int page,
            int size
    ) {
        List<Listings> all = listingsService.getListingsByUser(userId);
        List<Listings> filtered = all.stream()
                .filter(l -> (type == null || l.getType() == type))
                .filter(l -> (status == null || l.getStatus() == status))
                .sorted(Comparator.comparing(Listings::getCreatedAt).reversed())
                .collect(Collectors.toList());
        int total = filtered.size();
        int from = Math.min(page * size, total);
        int to = Math.min(from + size, total);
        List<Listings> pageContent = filtered.subList(from, to);
        List<Map<String, Object>> content = pageContent.stream().map(ListingsMapper::toSummaryMap).collect(Collectors.toList());
        Map<String, Object> pageInfo = Map.of(
                "number", page,
                "size", size,
                "totalElements", total,
                "totalPages", (int) Math.ceil((double) total / size)
        );
        Map<String, Object> data = Map.of(
                "content", content,
                "page", pageInfo
        );
        return data;
    }

    /**
     * Recherche avancée d'annonces avec filtrage, tri, date, etc.
     */
    public List<Listings> searchListings(
            String type,
            String city,
            String date,
            String category,
            String status
    ) {
        final ListingType typeEnum;
        if (type != null) {
            try {
                typeEnum = ListingType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Le champ 'type' doit être LOST ou FOUND.");
            }
        } else {
            typeEnum = null;
        }
        final ListingStatus statusEnum;
        if (status != null) {
            try {
                statusEnum = ListingStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Le champ 'status' est invalide.");
            }
        } else {
            statusEnum = null;
        }
        final LocalDate filterDate;
        if (date != null) {
            try {
                filterDate = LocalDate.parse(date);
            } catch (Exception e) {
                throw new IllegalArgumentException("Le champ 'date' doit être au format YYYY-MM-DD.");
            }
        } else {
            filterDate = null;
        }
        List<Listings> all = listingsService.getAllListings();
        return all.stream()
                .filter(l -> typeEnum == null || l.getType() == typeEnum)
                .filter(l -> city == null || (l.getCity() != null && l.getCity().equalsIgnoreCase(city)))
                .filter(l -> category == null || (l.getCategory() != null && l.getCategory().equalsIgnoreCase(category)))
                .filter(l -> statusEnum == null || l.getStatus() == statusEnum)
                .filter(l -> filterDate == null || (l.getCreatedAt() != null && l.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDate().equals(filterDate)))
                .sorted(Comparator.comparing(Listings::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }
}
