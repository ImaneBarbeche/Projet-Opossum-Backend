package com.opossum.listings;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.opossum.user.UserRepository;
import java.util.List;
import com.opossum.listings.dto.UpdateListingsRequest;
import java.util.UUID;
import com.opossum.listings.Listings;
import com.opossum.listings.ListingsService;


@RestController
@RequestMapping("/api/v1/listings")

public class ListingsController {

    private final ListingsService ListingsService;
    private final UserRepository userRepository;

    public ListingsController(ListingsService ListingsService, UserRepository userRepository) {
        this.ListingsService = ListingsService;
        this.userRepository = userRepository;
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(
            @RequestBody com.opossum.listings.dto.CreateListingsRequest req,
            java.security.Principal principal
    ) {
        String email = principal.getName();
        com.opossum.user.User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        try {
            Listings listing = ListingsService.mapCreateRequestToEntity(req);
            java.util.Map<String, Object> response = ListingsService.createListingAndBuildResponse(listing, user);
            return ResponseEntity.status(201).body(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(validationError("validation", ex.getMessage()));
        }
    }

    // Méthode utilitaire pour format d'erreur de validation
    private java.util.Map<String, Object> validationError(String field, String message) {
        java.util.Map<String, Object> error = new java.util.HashMap<>();
        error.put("code", "VALIDATION_ERROR");
        error.put("message", "Données invalides");
        java.util.Map<String, Object> details = new java.util.HashMap<>();
        details.put(field, message);
        error.put("details", details);
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("success", false);
        response.put("error", error);
        response.put("timestamp", java.time.Instant.now().toString());
        return response;
    }

    @PutMapping("{id}/update")
    public ResponseEntity<?> update(
            @PathVariable UUID id,
            @RequestBody UpdateListingsRequest updateRequest,
            java.security.Principal principal
    ) {
        String email = principal.getName();
        com.opossum.user.User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        try {
            java.util.Map<String, Object> response = ListingsService.updateListing(id, updateRequest, user);
            return ResponseEntity.ok(response);
        } catch (com.opossum.listings.common.exceptions.ForbiddenException ex) {
            return ResponseEntity.status(403).body(ex.getErrorBody());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(validationError("validation", ex.getMessage()));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(404).body(validationError("not_found", ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<?> delete(@PathVariable UUID id, java.security.Principal principal) {
        String email = principal.getName();
        com.opossum.user.User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        try {
            java.util.Map<String, Object> response = ListingsService.deleteListingAndBuildResponse(id, user);
            return ResponseEntity.ok(response);
        } catch (com.opossum.listings.common.exceptions.ForbiddenException ex) {
            return ResponseEntity.status(403).body(ex.getErrorBody());
        } catch (RuntimeException ex) {
            return ResponseEntity.status(404).body(validationError("not_found", ex.getMessage()));
        }
    }


    // Recherche avancée avec filtres multiples et pagination
    @GetMapping("/advanced-search")
    public ResponseEntity<?> advancedSearch(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false, defaultValue = "10") Double radius,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false, defaultValue = "relevance") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        // Délégation au service, qui gère la recherche avancée et le formatage de la réponse
        java.util.Map<String, Object> response = ListingsService.advancedSearch(q, type, category, city, latitude, longitude, radius, dateFrom, dateTo, sortBy, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getListingDetails(@PathVariable UUID id, java.security.Principal principal) {
        String email = principal != null ? principal.getName() : null;
        java.util.Map<String, Object> response = ListingsService.getListingDetails(id, email);
        if (Boolean.FALSE.equals(response.get("success"))) {
            return ResponseEntity.status(404).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyListings(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            java.security.Principal principal
    ) {
        String username = principal.getName();
        java.util.Map<String, Object> response = ListingsService.getMyListingsResponse(username, type, status, page, size);
        return ResponseEntity.ok(response);
    }

}