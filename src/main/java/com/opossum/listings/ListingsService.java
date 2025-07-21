package com.opossum.listings;

import com.opossum.user.User;
import com.opossum.user.UserRepository;
import com.opossum.listings.common.exceptions.ForbiddenException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;
import java.util.UUID;

@Service public class ListingsService {

    private final ListingsRepository listingsRepository;
    private final UserRepository userRepository;

    public ListingsService(ListingsRepository listingsRepository, UserRepository userRepository) {
        this.listingsRepository = listingsRepository;
        this.userRepository = userRepository;
    }
        /**
         * Retourne les détails d'une annonce, formatés selon la doc API.
         * Vérifie la visibilité (ACTIVE ou propriétaire), ajoute infos user/contact/photo, gère 404, incrémente vues.
         */
        public java.util.Map<String, Object> getListingDetails(UUID id, String email) {
            java.util.Optional<Listings> opt = listingsRepository.findById(id);
            if (opt.isEmpty()) {
                java.util.Map<String, Object> error = new java.util.HashMap<>();
                error.put("success", false);
                java.util.Map<String, Object> err = new java.util.HashMap<>();
                err.put("code", "ANNOUNCEMENT_NOT_FOUND");
                err.put("message", "Annonce introuvable");
                error.put("error", err);
                error.put("timestamp", java.time.Instant.now().toString());
                return error;
            }
            Listings l = opt.get();
            // Récupérer l'utilisateur connecté (si email fourni)
            User user = null;
            if (email != null) {
                user = userRepository.findByEmail(email).orElse(null);
            }
            boolean isOwner = user != null && l.getUserId().equals(user.getId());
            boolean isVisible = "ACTIVE".equalsIgnoreCase(l.getStatus()) || isOwner;
            if (!isVisible) {
                java.util.Map<String, Object> error = new java.util.HashMap<>();
                error.put("success", false);
                java.util.Map<String, Object> err = new java.util.HashMap<>();
                err.put("code", "ANNOUNCEMENT_NOT_FOUND");
                err.put("message", "Annonce introuvable");
                error.put("error", err);
                error.put("timestamp", java.time.Instant.now().toString());
                return error;
            }
            // Incrémenter le compteur de vues (à implémenter si champ présent)
            // if (!isOwner) { l.setViews(l.getViews() + 1); listingsRepository.save(l); }
            // Construction de la réponse formatée
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("id", l.getId());
            data.put("title", l.getTitle());
            data.put("description", l.getDescription());
            data.put("type", l.getType());
            data.put("category", l.getCategory());
            data.put("status", l.getStatus());
            java.util.Map<String, Object> location = new java.util.HashMap<>();
            location.put("latitude", l.getLatitude());
            location.put("longitude", l.getLongitude());
            location.put("address", l.getAddress());
            location.put("city", l.getCity());
            data.put("location", location);
            data.put("photoUrl", l.getPhotoUrl());
            java.util.Map<String, Object> contact = new java.util.HashMap<>();
            contact.put("phone", l.getContactPhone());
            contact.put("email", l.getContactEmail());
            data.put("contactInfo", contact);
            // Infos user (auteur)
            if (l.getUserId() != null) {
                user = user != null && isOwner ? user : userRepository.findById(l.getUserId()).orElse(null);
                if (user != null) {
                    java.util.Map<String, Object> author = new java.util.HashMap<>();
                    author.put("id", user.getId());
                    author.put("firstName", user.getFirstName());
                    author.put("lastName", user.getLastName());
                    author.put("avatar", user.getAvatar());
                    data.put("user", author);
                }
            }
            data.put("createdAt", l.getCreatedAt());
            data.put("updatedAt", l.getUpdatedAt());
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", true);
            response.put("data", data);
            response.put("timestamp", java.time.Instant.now().toString());
            return response;
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
            // Filtrer les annonces par utilisateur et éventuellement par type et statut
            List<Listings> filteredListings = listingsRepository.findAll(pageable)
                .stream()
                .filter(l -> l.getUserId().equals(userId))
                .filter(l -> type == null || type.isBlank() || type.equalsIgnoreCase(l.getType()))
                .filter(l -> status == null || status.isBlank() || status.equalsIgnoreCase(l.getStatus()))
                .toList();
    
            org.springframework.data.domain.Page<Listings> listingsPage = new org.springframework.data.domain.PageImpl<>(filteredListings, pageable, filteredListings.size());
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("content", listingsPage.getContent());
            java.util.Map<String, Object> pageInfo = new java.util.HashMap<>();
            pageInfo.put("number", listingsPage.getNumber());
            pageInfo.put("size", listingsPage.getSize());
            pageInfo.put("totalElements", listingsPage.getTotalElements());
            pageInfo.put("totalPages", listingsPage.getTotalPages());
            data.put("page", pageInfo);
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", true);
            response.put("data", data);
            response.put("timestamp", java.time.Instant.now().toString());
            return response;
        }
    /**
     * Mappe le DTO CreateListingsRequest vers l'entité Listings
     */
    public static Listings mapCreateRequestToEntity(com.opossum.listings.dto.CreateListingsRequest req) {
        Listings l = new Listings();
        l.setTitle(req.getTitle());
        l.setDescription(req.getDescription());
        l.setType(req.getType());
        l.setCategory(req.getCategory());
        // Location
        if (req.getLocation() != null) {
            if (req.getLocation().getLatitude() != null) l.setLatitude(java.math.BigDecimal.valueOf(req.getLocation().getLatitude()));
            if (req.getLocation().getLongitude() != null) l.setLongitude(java.math.BigDecimal.valueOf(req.getLocation().getLongitude()));
            l.setAddress(req.getLocation().getAddress());
            l.setCity(req.getLocation().getCity());
        }
        // Contact info
        if (req.getContactInfo() != null) {
            l.setContactPhone(req.getContactInfo().getPhone());
            l.setContactEmail(req.getContactInfo().getEmail());
        }
        // Photos (on ne prend que la première pour photoUrl)
        if (req.getPhotos() != null && !req.getPhotos().isEmpty()) {
            l.setPhotoUrl(req.getPhotos().get(0));
        }
        // Champs système et autres valeurs par défaut seront gérés dans createListing
        return l;
    }
        /**
         * Crée une annonce avec validation métier et valeurs par défaut.
         */
        public Listings createListing(Listings listing, User user) {
            listing.setUserId(user.getId());
            listing.setStatus("ACTIVE");
            listing.setCreatedAt(Instant.now());
            listing.setUpdatedAt(Instant.now());
            // Validation métier
            String title = listing.getTitle();
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
    
        public java.util.Map<String, Object> deleteListingAndBuildResponse(UUID id, com.opossum.user.User user) {
            Listings listing = listingsRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Annonce non trouvée"));
            // Vérification auteur ou admin (enum, champ unique)
            boolean isAdmin = user.getRole() == com.opossum.user.Role.ADMIN;
            if (!listing.getUserId().equals(user.getId()) && !isAdmin) {
                java.util.Map<String, Object> error = new java.util.HashMap<>();
                error.put("success", false);
                java.util.Map<String, Object> err = new java.util.HashMap<>();
                err.put("code", "ACCESS_DENIED");
                err.put("message", "Vous ne pouvez supprimer que vos propres annonces");
                error.put("error", err);
                error.put("timestamp", java.time.Instant.now().toString());
                throw new com.opossum.listings.common.exceptions.ForbiddenException(error);
            }
            listingsRepository.deleteById(id);
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", true);
            response.put("message", "Annonce supprimée avec succès");
            response.put("timestamp", java.time.Instant.now().toString());
            return response;
        }
    
        public List<Listings> searchListings(String title) {
            return listingsRepository.findByTitleContainingIgnoreCase(title);
        }
    
        /**
         * Recherche avancée d'annonces avec filtres multiples, pagination, tri et calcul de distance.
         * Les paramètres correspondent à ceux attendus par le controller.
         * À compléter avec la logique métier réelle.
         */
        public java.util.Map<String, Object> advancedSearch(
                String q,
                String type,
                String category,
                String city,
                Double latitude,
                Double longitude,
                Double radius,
                String dateFrom,
                String dateTo,
                String sortBy,
                int page,
                int size
        ) {
            // Recherche avancée avec critères simples : mot-clé (titre ou description), type, catégorie, ville, pagination
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            List<Listings> filtered = listingsRepository.findAll(pageable)
                    .stream()
                    .filter(l -> {
                        boolean match = true;
                        if (q != null && !q.isBlank()) {
                            String lowerQ = q.toLowerCase();
                            match &= (l.getTitle() != null && l.getTitle().toLowerCase().contains(lowerQ))
                                    || (l.getDescription() != null && l.getDescription().toLowerCase().contains(lowerQ));
                        }
                        if (type != null && !type.isBlank()) {
                            if (type.equalsIgnoreCase("LOST")) match &= l.getIsLost();
                            else if (type.equalsIgnoreCase("FOUND")) match &= !l.getIsLost();
                        }
                        if (category != null && !category.isBlank()) {
                            match &= category.equalsIgnoreCase(l.getCategory());
                        }
                        if (city != null && !city.isBlank()) {
                            match &= city.equalsIgnoreCase(l.getCity());
                        }
                        return match;
                    })
                    .toList();
            org.springframework.data.domain.Page<Listings> listingsPage = new org.springframework.data.domain.PageImpl<>(filtered, pageable, filtered.size());
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("content", listingsPage.getContent());
            java.util.Map<String, Object> pageInfo = new java.util.HashMap<>();
            pageInfo.put("number", listingsPage.getNumber());
            pageInfo.put("size", listingsPage.getSize());
            pageInfo.put("totalElements", listingsPage.getTotalElements());
            pageInfo.put("totalPages", listingsPage.getTotalPages());
            data.put("page", pageInfo);
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", true);
            response.put("data", data);
            response.put("timestamp", java.time.Instant.now().toString());
            return response;
        }
    }