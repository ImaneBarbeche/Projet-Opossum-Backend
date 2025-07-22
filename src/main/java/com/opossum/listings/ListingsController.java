package com.opossum.listings;
import java.util.stream.Collectors;
import com.opossum.common.enums.ListingType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.opossum.listings.dto.CreateListingsRequest;
import com.opossum.listings.dto.UpdateListingsRequest;
import java.util.List;
import java.util.Optional;
import com.opossum.user.User;
import java.time.Instant;
import com.opossum.common.enums.ListingStatus;
import com.opossum.user.dto.UserProfileResponse;
import com.opossum.user.UserService;
import java.util.UUID;




@RestController
@RequestMapping("/api/v1/listings")

public class ListingsController {

    private final ListingsService listingsService;
    private final UserService userService;

    public ListingsController(ListingsService listingsService, UserService userService) {
        this.listingsService = listingsService;
        this.userService = userService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@AuthenticationPrincipal User user, @RequestBody CreateListingsRequest createListingsRequest) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                java.util.Map.of(
                    "success", false,
                    "error", java.util.Map.of(
                        "code", "UNAUTHORIZED",
                        "message", "Token JWT invalide ou expiré"
                    ),
                    "timestamp", java.time.Instant.now()
                )
            );
        }
        try {
            Listings created = listingsService.createListing(createListingsRequest, user.getId());
            // Mapping réponse API
            java.util.Map<String, Object> location = java.util.Map.of(
                "city", created.getCity()
                // Ajoute d'autres champs si besoin
            );
            java.util.Map<String, Object> data = java.util.Map.of(
                "id", created.getId(),
                "title", created.getTitle(),
                "type", created.getType().name(),
                "status", created.getStatus().name(),
                "location", location,
                "locationValidated", true, // à adapter selon ta logique
                "createdAt", created.getCreatedAt()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(
                java.util.Map.of(
                    "success", true,
                    "data", data,
                    "message", "Annonce créée avec succès",
                    "timestamp", java.time.Instant.now()
                )
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                java.util.Map.of(
                    "success", false,
                    "error", java.util.Map.of(
                        "code", "VALIDATION_ERROR",
                        "message", "Données invalides",
                        "details", e.getMessage()
                    ),
                    "timestamp", java.time.Instant.now()
                )
            );
        }
    }

    @PutMapping("/{id}/update")
    public ResponseEntity<?> update(
            @PathVariable UUID id,
            @RequestBody UpdateListingsRequest updateListingsRequest,
            @AuthenticationPrincipal User user
    ) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                java.util.Map.of(
                    "success", false,
                    "error", java.util.Map.of(
                        "code", "UNAUTHORIZED",
                        "message", "Token JWT invalide ou expiré"
                    ),
                    "timestamp", java.time.Instant.now()
                )
            );
        }
        Optional<Listings> opt = listingsService.getListingById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                java.util.Map.of(
                    "success", false,
                    "error", java.util.Map.of(
                        "code", "ANNOUNCEMENT_NOT_FOUND",
                        "message", "Annonce introuvable"
                    ),
                    "timestamp", java.time.Instant.now()
                )
            );
        }
        Listings l = opt.get();
        // Only owner can update
        if (!user.getId().equals(l.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                java.util.Map.of(
                    "success", false,
                    "error", java.util.Map.of(
                        "code", "ACCESS_DENIED",
                        "message", "Vous ne pouvez modifier que vos propres annonces"
                    ),
                    "timestamp", java.time.Instant.now()
                )
            );
        }
        // Validation rules
        String title = updateListingsRequest.getTitle();
        String description = updateListingsRequest.getDescription();
        String category = updateListingsRequest.getCategory();
        ListingStatus newStatus = updateListingsRequest.getStatus();
        // Validate title
        if (title != null && (title.length() < 3 || title.length() > 100)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                java.util.Map.of(
                    "success", false,
                    "error", java.util.Map.of(
                        "code", "VALIDATION_ERROR",
                        "message", "Le titre doit comporter entre 3 et 100 caractères."
                    ),
                    "timestamp", java.time.Instant.now()
                )
            );
        }
        // Validate description
        if (description != null && (description.length() < 10 || description.length() > 1000)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                java.util.Map.of(
                    "success", false,
                    "error", java.util.Map.of(
                        "code", "VALIDATION_ERROR",
                        "message", "La description doit comporter entre 10 et 1000 caractères."
                    ),
                    "timestamp", java.time.Instant.now()
                )
            );
        }
        // Validate category
        if (category != null) {
            java.util.Set<String> allowedCategories = java.util.Set.of("electronics", "clothing", "accessories", "documents", "keys", "other");
            if (!allowedCategories.contains(category)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    java.util.Map.of(
                        "success", false,
                        "error", java.util.Map.of(
                            "code", "VALIDATION_ERROR",
                            "message", "Catégorie invalide."
                        ),
                        "timestamp", java.time.Instant.now()
                    )
                );
            }
        }
        // Validate status
        if (newStatus != null) {
            if (newStatus == ListingStatus.ARCHIVED) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    java.util.Map.of(
                        "success", false,
                        "error", java.util.Map.of(
                            "code", "INVALID_STATUS_TRANSITION",
                            "message", "Seul un administrateur peut archiver une annonce."
                        ),
                        "timestamp", java.time.Instant.now()
                    )
                );
            }
            if (l.getStatus() == ListingStatus.RESOLVED && newStatus == ListingStatus.ACTIVE) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    java.util.Map.of(
                        "success", false,
                        "error", java.util.Map.of(
                            "code", "INVALID_STATUS_TRANSITION",
                            "message", "Impossible de repasser une annonce RESOLVED en ACTIVE."
                        ),
                        "timestamp", java.time.Instant.now()
                    )
                );
            }
        }
        // Update allowed fields only
        if (title != null) l.setTitle(title);
        if (description != null) l.setDescription(description);
        if (category != null) l.setCategory(category);
        Instant now = Instant.now();
        l.setUpdatedAt(now);
        ListingStatus oldStatus = l.getStatus();
        if (newStatus != null && newStatus != oldStatus) {
            l.setStatus(newStatus);
            if (newStatus == ListingStatus.RESOLVED) {
                l.setResolvedAt(now);
            }
        }
        // Save
        // Persist using repository directly (if ListingsService does not expose save)
        listingsService.save(l);
        // Build response
        java.util.Map<String, Object> location = java.util.Map.of(
            "latitude", l.getLatitude() != null ? l.getLatitude().doubleValue() : null,
            "longitude", l.getLongitude() != null ? l.getLongitude().doubleValue() : null,
            "address", l.getAddress(),
            "city", l.getCity()
        );
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("id", l.getId());
        data.put("title", l.getTitle());
        data.put("description", l.getDescription());
        data.put("type", l.getType().name());
        data.put("category", l.getCategory());
        data.put("status", l.getStatus().name());
        data.put("location", location);
        data.put("createdAt", l.getCreatedAt());
        data.put("updatedAt", l.getUpdatedAt());
        if (l.getStatus() == ListingStatus.RESOLVED) {
            data.put("resolvedAt", l.getResolvedAt());
        }
        return ResponseEntity.ok(
            java.util.Map.of(
                "success", true,
                "data", data,
                "message", "Annonce modifiée avec succès",
                "timestamp", now
            )
        );
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<?> delete(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                java.util.Map.of(
                    "success", false,
                    "error", java.util.Map.of(
                        "code", "UNAUTHORIZED",
                        "message", "Token JWT invalide ou expiré"
                    ),
                    "timestamp", java.time.Instant.now()
                )
            );
        }
        Optional<Listings> opt = listingsService.getListingById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                java.util.Map.of(
                    "success", false,
                    "error", java.util.Map.of(
                        "code", "ANNOUNCEMENT_NOT_FOUND",
                        "message", "Annonce introuvable"
                    ),
                    "timestamp", java.time.Instant.now()
                )
            );
        }
        Listings l = opt.get();
        boolean isOwner = user.getId().equals(l.getUserId());
        if (!isOwner) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                java.util.Map.of(
                    "success", false,
                    "error", java.util.Map.of(
                        "code", "ACCESS_DENIED",
                        "message", "Vous ne pouvez supprimer que vos propres annonces"
                    ),
                    "timestamp", java.time.Instant.now()
                )
            );
        }
        l.setStatus(com.opossum.common.enums.ListingStatus.DELETED);
        l.setUpdatedAt(java.time.Instant.now());
        listingsService.save(l);
        // TODO: supprimer les messages liés à l'annonce si besoin
        return ResponseEntity.ok(
            java.util.Map.of(
                "success", true,
                "message", "Annonce supprimée avec succès",
                "timestamp", java.time.Instant.now()
            )
        );
    }


    @GetMapping("/search")
    public ResponseEntity<List<Listings>> search(@RequestParam String title) {
        return ResponseEntity.ok(listingsService.searchListings(title));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Listings>> getAllListings() {
        List<Listings> listings = listingsService.getAllListings();
        return ResponseEntity.ok(listings);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getListingById(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        Optional<Listings> opt = listingsService.getListingById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                java.util.Map.of(
                    "success", false,
                    "error", java.util.Map.of(
                        "code", "ANNOUNCEMENT_NOT_FOUND",
                        "message", "Annonce introuvable"
                    ),
                    "timestamp", java.time.Instant.now()
                )
            );
        }
        Listings l = opt.get();
        // Contrôle d'accès : visible si ACTIVE ou propriétaire
        boolean isOwner = (user != null && user.getId().equals(l.getUserId()));
        if (!(l.getStatus() == ListingStatus.ACTIVE || isOwner)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                java.util.Map.of(
                    "success", false,
                    "error", java.util.Map.of(
                        "code", "ANNOUNCEMENT_NOT_FOUND",
                        "message", "Annonce introuvable"
                    ),
                    "timestamp", java.time.Instant.now()
                )
            );
        }
        // Mapping réponse API
        java.util.Map<String, Object> location = java.util.Map.of(
            "latitude", l.getLatitude() != null ? l.getLatitude().doubleValue() : null,
            "longitude", l.getLongitude() != null ? l.getLongitude().doubleValue() : null,
            "address", l.getAddress(),
            "city", l.getCity()
        );
        // Contact info (à adapter selon ton entité)
        java.util.Map<String, Object> contactInfo = java.util.Map.of(
            "phone", null,
            "email", null
        );
        // User info (public)
        java.util.Map<String, Object> userInfo = java.util.Map.of(
            "id", l.getUserId(),
            "firstName", null,
            "lastName", null,
            "avatar", null
        );
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("id", l.getId());
        data.put("title", l.getTitle());
        data.put("description", l.getDescription());
        data.put("type", l.getType().name());
        data.put("category", l.getCategory());
        data.put("status", l.getStatus().name());
        data.put("location", location);
        data.put("photoUrl", null); // à adapter si tu ajoutes ce champ
        data.put("contactInfo", contactInfo);
        data.put("user", userInfo);
        data.put("createdAt", l.getCreatedAt());
        data.put("updatedAt", l.getUpdatedAt());
        return ResponseEntity.ok(
            java.util.Map.of(
                "success", true,
                "data", data,
                "timestamp", java.time.Instant.now()
            )
        );
    }

    @GetMapping("/listing/{listingId}/user")
    public ResponseEntity<UserProfileResponse> getUserByListingId(@PathVariable UUID listingId) {
        Optional<Listings> listingOpt = listingsService.getListingById(listingId);
        if (listingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        UUID userId = listingOpt.get().getUserId();
        return userService.getUserById(userId)
                .map(userService::mapToUserProfileResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Listings>> getListingsByStatus(@PathVariable String status) {
        try {
            ListingStatus enumStatus = ListingStatus.valueOf(status.toUpperCase());
            List<Listings> listings = listingsService.getListingsByStatus(enumStatus);
            return ResponseEntity.ok(listings);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    /**
     * Endpoint conforme à la spec "Get My Listings" (vue mobile/tableau de bord)
     */
    @GetMapping("/me")
    public ResponseEntity<?> getMyListingsPaged(
            @AuthenticationPrincipal User user,
            @RequestParam(value = "type", required = false) ListingType type,
            @RequestParam(value = "status", required = false) ListingStatus status,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "20") int size
    ) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    java.util.Map.of(
                            "success", false,
                            "error", java.util.Map.of(
                                    "code", "UNAUTHORIZED",
                                    "message", "Token JWT invalide ou expiré"
                            ),
                            "timestamp", java.time.Instant.now()
                    )
            );
        }
        // Récupère toutes les annonces de l'utilisateur
        List<Listings> all = listingsService.getListingsByUser(user.getId());
        // Filtrage type/status si fourni
        List<Listings> filtered = all.stream()
                .filter(l -> (type == null || l.getType() == type))
                .filter(l -> (status == null || l.getStatus() == status))
                .sorted(java.util.Comparator.comparing(Listings::getCreatedAt).reversed())
                .collect(Collectors.toList());
        // Pagination manuelle (car repo ne supporte pas Page encore)
        int total = filtered.size();
        int from = Math.min(page * size, total);
        int to = Math.min(from + size, total);
        List<Listings> pageContent = filtered.subList(from, to);
        // Mapping pour la vue mobile (id, title, type, category, status, createdAt, thumbnailUrl)
        List<java.util.Map<String, Object>> content = pageContent.stream().map(l -> java.util.Map.of(
                "id", l.getId(),
                "title", l.getTitle(),
                "type", l.getType().name(),
                "category", l.getCategory(),
                "status", l.getStatus().name(),
                "createdAt", l.getCreatedAt(),
                "thumbnailUrl", (Object) null // ou une logique adaptée si tu ajoutes ce champ plus tard
        )).collect(Collectors.toList());
        java.util.Map<String, Object> pageInfo = java.util.Map.of(
                "number", page,
                "size", size,
                "totalElements", total,
                "totalPages", (int) Math.ceil((double) total / size)
        );
        java.util.Map<String, Object> data = java.util.Map.of(
                "content", content,
                "page", pageInfo
        );
        return ResponseEntity.ok(
                java.util.Map.of(
                        "success", true,
                        "data", data,
                        "timestamp", java.time.Instant.now()
                )
        );
    }
    /**
     * Endpoint conforme à la spec "Get Announcements List"
     */
    @GetMapping("")
    public ResponseEntity<?> searchListingsV1(
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "date", required = false) String date,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "status", required = false) String status
    ) {
        // Validation type
        final com.opossum.common.enums.ListingType typeEnum;
        if (type != null) {
            try {
                typeEnum = com.opossum.common.enums.ListingType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(
                    java.util.Map.of(
                        "timestamp", java.time.Instant.now(),
                        "status", 400,
                        "error", "Bad Request",
                        "message", "Le champ 'type' doit être LOST ou FOUND.",
                        "path", "/api/v1/listings"
                    )
                );
            }
        } else {
            typeEnum = null;
        }
        // Validation status
        final com.opossum.common.enums.ListingStatus statusEnum;
        if (status != null) {
            try {
                statusEnum = com.opossum.common.enums.ListingStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(
                    java.util.Map.of(
                        "timestamp", java.time.Instant.now(),
                        "status", 400,
                        "error", "Bad Request",
                        "message", "Le champ 'status' est invalide.",
                        "path", "/api/v1/listings"
                    )
                );
            }
        } else {
            statusEnum = null;
        }
        // Validation date
        final java.time.LocalDate filterDate;
        if (date != null) {
            try {
                filterDate = java.time.LocalDate.parse(date);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(
                    java.util.Map.of(
                        "timestamp", java.time.Instant.now(),
                        "status", 400,
                        "error", "Bad Request",
                        "message", "Le champ 'date' doit être au format YYYY-MM-DD.",
                        "path", "/api/v1/listings"
                    )
                );
            }
        } else {
            filterDate = null;
        }
        // Récupère toutes les annonces
        List<Listings> all = listingsService.getAllListings();
        // Filtrage
        List<Listings> filtered = all.stream()
                .filter(l -> typeEnum == null || l.getType() == typeEnum)
                .filter(l -> city == null || (l.getCity() != null && l.getCity().equalsIgnoreCase(city)))
                .filter(l -> category == null || (l.getCategory() != null && l.getCategory().equalsIgnoreCase(category)))
                .filter(l -> statusEnum == null || l.getStatus() == statusEnum)
                .filter(l -> filterDate == null || (l.getCreatedAt() != null && l.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDate().equals(filterDate)))
                .sorted(java.util.Comparator.comparing(Listings::getCreatedAt).reversed())
                .collect(java.util.stream.Collectors.toList());
        // Mapping réponse
        List<java.util.Map<String, Object>> result = filtered.stream().map(l -> java.util.Map.of(
                "id", l.getId(),
                "title", l.getTitle(),
                "description", l.getDescription() != null ? l.getDescription() : (Object) null,
                "type", l.getType().name(),
                "category", l.getCategory() != null ? l.getCategory() : (Object) null,
                "status", l.getStatus().name(),
                "city", l.getCity() != null ? l.getCity() : (Object) null,
                "address", l.getAddress() != null ? l.getAddress() : (Object) null,
                "created_at", l.getCreatedAt() != null ? l.getCreatedAt() : (Object) null
        )).collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(result);
    }

}