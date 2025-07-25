package com.opossum.listings;
import com.opossum.listings.ListingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.opossum.user.User;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/announcements/map")
public class AnnouncementsMapController {

    private final ListingsService listingsService;

    @Autowired
    public AnnouncementsMapController(ListingsService listingsService) {
        this.listingsService = listingsService;
    }

    /**
     * Endpoint Google Maps : récupère les annonces avec coordonnées GPS valides, format markers
     */
    @GetMapping
    public ResponseEntity<?> getAnnouncementsForMap(
            @RequestParam(value = "latitude", required = false) Double latitude,
            @RequestParam(value = "longitude", required = false) Double longitude,
            @RequestParam(value = "radius", required = false, defaultValue = "10") Integer radius,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "50") Integer size,
            @AuthenticationPrincipal User currentUser
    ) {
        // Validation des coordonnées
        if (latitude != null && (latitude < -90 || latitude > 90)) {
            return ResponseEntity.badRequest().body(
                Map.of("success", false,
                       "error", Map.of(
                           "code", "INVALID_COORDINATES",
                           "message", "Latitude doit être entre -90 et 90",
                           "details", Map.of("latitude", "Doit être entre -90 et 90")
                       ),
                       "timestamp", java.time.Instant.now().toString())
            );
        }
        if (longitude != null && (longitude < -180 || longitude > 180)) {
            return ResponseEntity.badRequest().body(
                Map.of("success", false,
                       "error", Map.of(
                           "code", "INVALID_COORDINATES",
                           "message", "Longitude doit être entre -180 et 180",
                           "details", Map.of("longitude", "Doit être entre -180 et 180")
                       ),
                       "timestamp", java.time.Instant.now().toString())
            );
        }
        if (radius != null && (radius < 1 || radius > 50)) {
            return ResponseEntity.badRequest().body(
                Map.of("success", false,
                       "error", Map.of(
                           "code", "INVALID_RADIUS",
                           "message", "Rayon maximum 50km",
                           "details", Map.of("radius", "Rayon maximum 50km")
                       ),
                       "timestamp", java.time.Instant.now().toString())
            );
        }
        // Appel service (implémentation à faire)
        return listingsService.getAnnouncementsForMap(latitude, longitude, radius, type, category, page, size, currentUser);
    }
}
