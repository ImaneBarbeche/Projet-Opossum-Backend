package com.opossum.listings;
import com.opossum.common.utils.ResponseUtil;
import java.util.stream.Collectors;
import com.opossum.listings.ListingsMapper;
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
import com.opossum.common.enums.ListingStatus;
import com.opossum.user.UserService;
import java.util.UUID;
import java.util.Map;




@RestController
@RequestMapping("/api/v1/listings")

public class ListingsController {

    private final ListingsService listingsService;
    private final UserService userService;
    private final ListingsQueryService listingsQueryService;

    public ListingsController(ListingsService listingsService, UserService userService, ListingsQueryService listingsQueryService) {
        this.listingsService = listingsService;
        this.userService = userService;
        this.listingsQueryService = listingsQueryService;
    }

    // Crée une nouvelle annonce
    @PostMapping("/create")
    public ResponseEntity<?> create(@AuthenticationPrincipal User user, @RequestBody CreateListingsRequest createListingsRequest) {
        if (user == null) {
            return ResponseUtil.error(HttpStatus.UNAUTHORIZED.value(), "UNAUTHORIZED", "Token JWT invalide ou expiré");
        }
        try {
            Listings created = listingsService.createListing(createListingsRequest, user.getId());
            return ResponseUtil.success(ListingsMapper.toCreateMap(created));
        } catch (Exception e) {
            return ResponseUtil.error(HttpStatus.BAD_REQUEST.value(), "VALIDATION_ERROR", "Données invalides: " + e.getMessage());
        }
    }

    // Met à jour une annonce existante (propriétaire uniquement)
    @PutMapping("/{id}/update")
    public ResponseEntity<?> update(
            @PathVariable UUID id,
            @RequestBody UpdateListingsRequest updateListingsRequest,
            @AuthenticationPrincipal User user
    ) {
        if (user == null) {
            return ResponseUtil.error(HttpStatus.UNAUTHORIZED.value(), "UNAUTHORIZED", "Token JWT invalide ou expiré");
        }
        Optional<Listings> opt = listingsService.getListingById(id);
        if (opt.isEmpty()) {
            return ResponseUtil.error(HttpStatus.NOT_FOUND.value(), "ANNOUNCEMENT_NOT_FOUND", "Annonce introuvable");
        }
        Listings l = opt.get();
        if (!user.getId().equals(l.getUserId())) {
            return ResponseUtil.error(HttpStatus.FORBIDDEN.value(), "ACCESS_DENIED", "Vous ne pouvez modifier que vos propres annonces");
        }
        try {
            Optional<Listings> updated = listingsService.updateListing(id, updateListingsRequest);
            if (updated.isPresent()) {
                return ResponseUtil.success(ListingsMapper.toDetailMap(updated.get()));
            } else {
                return ResponseUtil.error(HttpStatus.NOT_FOUND.value(), "ANNOUNCEMENT_NOT_FOUND", "Annonce introuvable après mise à jour");
            }
        } catch (Exception e) {
            return ResponseUtil.error(HttpStatus.BAD_REQUEST.value(), "VALIDATION_ERROR", e.getMessage() != null ? e.getMessage() : e.toString());
        }
    }

    // Supprime une annonce (propriétaire uniquement)
    @DeleteMapping("/{id}/delete")
    public ResponseEntity<?> delete(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseUtil.error(HttpStatus.UNAUTHORIZED.value(), "UNAUTHORIZED", "Token JWT invalide ou expiré");
        }
        Optional<Listings> opt = listingsService.getListingById(id);
        if (opt.isEmpty()) {
            return ResponseUtil.error(HttpStatus.NOT_FOUND.value(), "ANNOUNCEMENT_NOT_FOUND", "Annonce introuvable");
        }
        Listings l = opt.get();
        if (!user.getId().equals(l.getUserId())) {
            return ResponseUtil.error(HttpStatus.FORBIDDEN.value(), "ACCESS_DENIED", "Vous ne pouvez supprimer que vos propres annonces");
        }
        try {
            listingsService.deleteListing(id);
            // TODO: supprimer les messages liés à l'annonce si besoin
            return ResponseUtil.success("Annonce supprimée avec succès");
        } catch (Exception e) {
            return ResponseUtil.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "INTERNAL_ERROR", e.getMessage() != null ? e.getMessage() : e.toString());
        }
    }


    // Recherche des annonces par titre (contient)
    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String title) {
        try {
            List<Listings> results = listingsService.searchListings(title);
            return ResponseUtil.success(results);
        } catch (Exception e) {
            return ResponseUtil.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "INTERNAL_ERROR", e.getMessage());
        }
    }

    // Récupère toutes les annonces (non filtrées)
    @GetMapping("/all")
    public ResponseEntity<?> getAllListings() {
        try {
            List<Listings> listings = listingsService.getAllListings();
            return ResponseUtil.success(listings);
        } catch (Exception e) {
            return ResponseUtil.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "INTERNAL_ERROR", e.getMessage());
        }
    }

    // Récupère le détail d'une annonce (visible si ACTIVE ou propriétaire)
    @GetMapping("/{id}")
    public ResponseEntity<?> getListingById(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        Optional<Listings> opt = listingsService.getListingById(id);
        if (opt.isEmpty()) {
            return ResponseUtil.error(HttpStatus.NOT_FOUND.value(), "ANNOUNCEMENT_NOT_FOUND", "Annonce introuvable");
        }
        Listings l = opt.get();
        boolean isOwner = (user != null && user.getId().equals(l.getUserId()));
        if (!(l.getStatus() == ListingStatus.ACTIVE || isOwner)) {
            return ResponseUtil.error(HttpStatus.NOT_FOUND.value(), "ANNOUNCEMENT_NOT_FOUND", "Annonce introuvable");
        }
        try {
            return ResponseUtil.success(ListingsMapper.toDetailMap(l));
        } catch (Exception e) {
            return ResponseUtil.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "INTERNAL_ERROR", e.getMessage() != null ? e.getMessage() : e.toString());
        }
    }

    // Récupère le profil utilisateur lié à une annonce
    @GetMapping("/listing/{listingId}/user")
    public ResponseEntity<?> getUserByListingId(@PathVariable UUID listingId) {
        Optional<Listings> listingOpt = listingsService.getListingById(listingId);
        if (listingOpt.isEmpty()) {
            return ResponseUtil.error(HttpStatus.NOT_FOUND.value(), "ANNOUNCEMENT_NOT_FOUND", "Annonce introuvable");
        }
        UUID userId = listingOpt.get().getUserId();
        return userService.getUserById(userId)
                .map(userService::mapToUserProfileResponse)
                .<ResponseEntity<?>>map(ResponseUtil::success)
                .orElseGet(() -> ResponseUtil.error(HttpStatus.NOT_FOUND.value(), "USER_NOT_FOUND", "Utilisateur introuvable"));
    }

    // Récupère les annonces par statut
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getListingsByStatus(@PathVariable String status) {
        try {
            ListingStatus enumStatus = ListingStatus.valueOf(status.toUpperCase());
            List<Listings> listings = listingsService.getListingsByStatus(enumStatus);
            return ResponseUtil.success(listings);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.error(HttpStatus.BAD_REQUEST.value(), "INVALID_STATUS", "Statut d'annonce invalide");
        }
    }
    /**
     * Endpoint conforme à la spec "Get My Listings" (vue mobile/tableau de bord)
     */
    // Récupère les annonces de l'utilisateur connecté (avec pagination)
    @GetMapping("/me")
    public ResponseEntity<?> getMyListingsPaged(
        
            @AuthenticationPrincipal User user,
            @RequestParam(value = "type", required = false) ListingType type,
            @RequestParam(value = "status", required = false) ListingStatus status,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "20") int size
    ) {
        //
        if (user == null) {
            return ResponseUtil.error(HttpStatus.UNAUTHORIZED.value(), "UNAUTHORIZED", "Token JWT invalide ou expiré");
        }
        try {
            Map<String, Object> data = listingsQueryService.getUserListingsPaged(user.getId(), type, status, page, size);
            return ResponseUtil.success(data);
        } catch (Exception e) {
            return ResponseUtil.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "INTERNAL_ERROR", e.getMessage() != null ? e.getMessage() : "");
        }
    }
    /**
     * Endpoint conforme à la spec "Get Announcements List"
     */
    // Recherche avancée d'annonces (filtrage type, ville, date, catégorie, statut)
    @GetMapping("")
    public ResponseEntity<?> searchListingsV1(
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "date", required = false) String date,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "status", required = false) String status
    ) {
        try {
            List<Listings> filtered = listingsQueryService.searchListings(type, city, date, category, status);
            List<java.util.Map<String, Object>> result = filtered.stream().map(ListingsMapper::toSearchMap).collect(java.util.stream.Collectors.toList());
            return ResponseUtil.success(result);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.error(HttpStatus.BAD_REQUEST.value(), "VALIDATION_ERROR", e.getMessage());
        } catch (Exception e) {
            return ResponseUtil.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "INTERNAL_ERROR", e.getMessage() != null ? e.getMessage() : e.toString());
        }
    }

}