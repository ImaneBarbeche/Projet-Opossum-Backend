package com.opossum.listings;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.opossum.user.UserRepository;
import java.util.List;
import com.opossum.listings.dto.UpdateListingsRequest;
import java.util.UUID;


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
            @RequestBody Listings listings,
            java.security.Principal principal
    ) {
        String email = principal.getName();
        com.opossum.user.User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        try {
            java.util.Map<String, Object> response = ListingsService.createListingAndBuildResponse(listings, user);
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

    @GetMapping("/search")
    public ResponseEntity<List<Listings>> search(@RequestParam String title) {
        return ResponseEntity.ok(ListingsService.searchListings(title));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Listings> getListingById(@PathVariable UUID id) {
        return ListingsService.getListingById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
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