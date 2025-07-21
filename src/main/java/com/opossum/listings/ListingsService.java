
package com.opossum.listings;
import com.opossum.user.User;
import com.opossum.user.UserRepository;
import com.opossum.listings.Listings;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.opossum.listings.common.exceptions.ForbiddenException;
import java.util.UUID;

@Service
public class ListingsService {

    private final UserRepository userRepository;

    public ListingsService(ListingsRepository listingsRepository, UserRepository userRepository) {
        this.listingsRepository = listingsRepository;
        this.userRepository = userRepository;
    }
    /**
     * Retourne les annonces de l'utilisateur connecté, filtrées et paginées.
     */
    public java.util.Map<String, Object> getMyListingsResponse(String email, String type, String status, int page, int size) {
        // Récupérer l'utilisateur par email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        UUID userId = user.getId();

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Listings> filteredListings = listingsRepository.findAll(pageable)
                .stream()
                .filter(l -> l.getUserId().equals(userId))
                .filter(l -> type == null || (type.equalsIgnoreCase("LOST") && l.getIsLost()) || (type.equalsIgnoreCase("FOUND") && !l.getIsLost()))
                .filter(l -> status == null || (l.getStatus() != null && l.getStatus().equalsIgnoreCase(status)))
                .toList();
        org.springframework.data.domain.Page<Listings> listingsPage = new org.springframework.data.domain.PageImpl<>(filteredListings, pageable, filteredListings.size());

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("success", true);
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("content", listingsPage.getContent());
        java.util.Map<String, Object> pageInfo = new java.util.HashMap<>();
        pageInfo.put("number", listingsPage.getNumber());
        pageInfo.put("size", listingsPage.getSize());
        pageInfo.put("totalElements", listingsPage.getTotalElements());
        pageInfo.put("totalPages", listingsPage.getTotalPages());
        data.put("page", pageInfo);
        response.put("data", data);
        response.put("timestamp", java.time.Instant.now().toString());
        return response;
    }

    private final ListingsRepository listingsRepository;

    public Listings createListing(Listings listing, User user) {
        // Initialisation des champs système
        listing.setUserId(user.getId());
        listing.setStatus("ACTIVE");
        listing.setCreatedAt(Instant.now());
        listing.setUpdatedAt(Instant.now());
        // Validation métier
        String title = (String) listing.getTitle();
        if (title == null || title.length() < 5 || title.length() > 200) {
            throw new IllegalArgumentException("Le titre doit contenir entre 5 et 200 caractères");
        }
        if (listing.getDescription() == null || listing.getDescription().length() < 10 || listing.getDescription().length() > 2000) {
            throw new IllegalArgumentException("La description doit contenir entre 10 et 2000 caractères");
        }
        if (listing.getCity() == null || listing.getCity().isBlank()) {
            throw new IllegalArgumentException("La ville est requise");
        }
        return listingsRepository.save(listing);
    }
    /**
     * Crée une annonce et construit la réponse attendue par l'API pour /create
     */
    public java.util.Map<String, Object> createListingAndBuildResponse(Listings listing, User user) {
        Listings saved = createListing(listing, user);
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("id", saved.getUserId());
        data.put("title", saved.getTitle());
        data.put("type", saved.getType());
        data.put("status", saved.getStatus());
        java.util.Map<String, Object> location = new java.util.HashMap<>();
        location.put("city", saved.getCity());
        data.put("location", location);
        data.put("createdAt", saved.getCreatedAt());
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("success", true);
        response.put("data", data);
        response.put("message", "Annonce créée avec succès");
        response.put("timestamp", java.time.Instant.now().toString());
        return response;
    }
    public List<Listings> getAllListings(boolean isLost) {
        return listingsRepository.findByIsLost(isLost);
    }

    public List<Listings> getListingsByUser(UUID userId) {
        return listingsRepository.findByUserId(userId);
    }

    public Optional<Listings> getListingById(UUID id) {
        return listingsRepository.findById(id);
    }

    @Transactional
    public java.util.Map<String, Object> updateListing(UUID id, com.opossum.listings.dto.UpdateListingsRequest req, com.opossum.user.User user) {
        Listings existing = listingsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Annonce non trouvée"));
        // Vérification auteur
        if (!existing.getUserId().equals(user.getId())) {
            java.util.Map<String, Object> error = new java.util.HashMap<>();
            error.put("success", false);
            java.util.Map<String, Object> err = new java.util.HashMap<>();
            err.put("code", "ACCESS_DENIED");
            err.put("message", "Vous ne pouvez modifier que vos propres annonces");
            error.put("error", err);
            error.put("timestamp", java.time.Instant.now().toString());
            throw new ForbiddenException(error);
        }
        // Validation et update des champs autorisés
        if (req.getTitle() != null) {
            if (req.getTitle().length() < 3 || req.getTitle().length() > 100) {
                throw new IllegalArgumentException("Le titre doit contenir entre 3 et 100 caractères");
            }
            existing.setTitle(req.getTitle());
        }
        if (req.getDescription() != null) {
            if (req.getDescription().length() < 10 || req.getDescription().length() > 1000) {
                throw new IllegalArgumentException("La description doit contenir entre 10 et 1000 caractères");
            }
            existing.setDescription(req.getDescription());
        }
        // Catégorie
        if (req.getCategory() != null) {
            String[] allowed = {"electronics", "clothing", "accessories", "documents", "keys", "other"};
            boolean valid = java.util.Arrays.asList(allowed).contains(req.getCategory());
            if (!valid) throw new IllegalArgumentException("Catégorie invalide");
            existing.setCategory(req.getCategory());
        }
        // Statut
        if (req.getStatus() != null) {
            String[] allowed = {"ACTIVE", "RESOLVED", "EXPIRED"};
            boolean valid = java.util.Arrays.asList(allowed).contains(req.getStatus());
            if (!valid) throw new IllegalArgumentException("Statut invalide");
            existing.setStatus(req.getStatus());
        }
        // Champs non modifiables ignorés (type, location, etc.)
        existing.setUpdatedAt(Instant.now());
        Listings saved = listingsRepository.save(existing);
        // Construction de la réponse formatée
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("id", saved.getId());
        data.put("title", saved.getTitle());
        data.put("description", saved.getDescription());
        data.put("type", saved.getType());
        data.put("category", saved.getCategory());
        data.put("status", saved.getStatus());
        java.util.Map<String, Object> location = new java.util.HashMap<>();
        location.put("latitude", saved.getLatitude());
        location.put("longitude", saved.getLongitude());
        location.put("address", saved.getAddress());
        location.put("city", saved.getCity());
        data.put("location", location);
        data.put("createdAt", saved.getCreatedAt());
        data.put("updatedAt", saved.getUpdatedAt());
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("success", true);
        response.put("data", data);
        response.put("message", "Annonce modifiée avec succès");
        response.put("timestamp", java.time.Instant.now().toString());
        return response;
    }

    public void deleteListing(UUID id) {
        listingsRepository.deleteById(id);
    }

    public List<Listings> searchListings(String title) {
        return listingsRepository.findByTitleContainingIgnoreCase(title);
    }
}