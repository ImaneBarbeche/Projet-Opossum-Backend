
package com.opossum.admin;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import com.opossum.listings.ListingsRepository;
import java.util.UUID;
import java.util.Map;
import java.util.List;

@Service
public class AdminService {
    private final com.opossum.user.UserRepository userRepository;
    private final ListingsRepository listingsRepository;

    @Autowired
    public AdminService(com.opossum.user.UserRepository userRepository, ListingsRepository listingsRepository) {
        this.userRepository = userRepository;
        this.listingsRepository = listingsRepository;
    }

    public ResponseEntity<?> getGlobalStats() {
        // Statistiques mockées pour l'exemple
        Map<String, Object> users = Map.of(
                "total", 1250,
                "active", 1100,
                "newThisMonth", 85
        );
        Map<String, Object> announcements = Map.of(
                "total", 2340,
                "active", 456,
                "lost", 234,
                "found", 222,
                "resolvedThisMonth", 89
        );
        Map<String, Object> conversations = Map.of(
                "total", 890,
                "activeToday", 45
        );
        Map<String, Object> files = Map.of(
                "totalSize", "2.5GB",
                "totalCount", 3456
        );
        Map<String, Object> data = Map.of(
                "users", users,
                "announcements", announcements,
                "conversations", conversations,
                "files", files
        );
        Map<String, Object> response = Map.of(
                "success", true,
                "data", data,
                "timestamp", java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC).toString()
        );
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> getAllUsers(String search, String status, int page, int size, String sort) {
        // Pagination et tri
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by(sort.replace(",", " ").split(" ")));

        // Recherche et filtrage dynamique
        org.springframework.data.jpa.domain.Specification<com.opossum.user.User> spec = (root, query, cb) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            if (search != null && !search.isEmpty()) {
                String like = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("firstName")), like),
                        cb.like(cb.lower(root.get("lastName")), like),
                        cb.like(cb.lower(root.get("email")), like)
                ));
            }
            if (status != null) {
                if (status.equalsIgnoreCase("ACTIVE")) predicates.add(cb.isTrue(root.get("isActive")));
                else if (status.equalsIgnoreCase("BLOCKED")) predicates.add(cb.isFalse(root.get("isActive")));
                // Ajout d'autres statuts si besoin
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        org.springframework.data.domain.Page<com.opossum.user.User> userPage =
            userRepository.findAll(spec, pageable);

        java.util.List<java.util.Map<String, Object>> content = new java.util.ArrayList<>();
        for (com.opossum.user.User user : userPage.getContent()) {
            java.util.Map<String, Object> u = new java.util.HashMap<>();
            u.put("id", user.getId().toString());
            u.put("email", user.getEmail());
            u.put("firstName", user.getFirstName());
            u.put("lastName", user.getLastName());
            u.put("phone", user.getPhone());
            u.put("status", user.isActive() ? "ACTIVE" : "BLOCKED");
            u.put("role", user.getRole());
            u.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
            u.put("lastLoginAt", user.getLastLoginAt() != null ? user.getLastLoginAt().toString() : null);
            // Compte le nombre d'annonces pour l'utilisateur
            int announcementCount = listingsRepository.findByUserId(user.getId()).size();
            u.put("announcementCount", announcementCount);
            // conversationCount à implémenter plus tard
            u.put("conversationCount", 0);
            content.add(u);
        }

        java.util.Map<String, Object> pageInfo = java.util.Map.of(
                "number", userPage.getNumber(),
                "size", userPage.getSize(),
                "totalElements", userPage.getTotalElements(),
                "totalPages", userPage.getTotalPages()
        );
        java.util.Map<String, Object> data = java.util.Map.of(
                "content", content,
                "page", pageInfo
        );
        java.util.Map<String, Object> response = java.util.Map.of(
                "success", true,
                "data", data,
                "timestamp", java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC).toString()
        );
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> blockUser(String userId, BlockUserRequest request) {
        String now = java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC).toString();
        java.util.Optional<com.opossum.user.User> userOpt = userRepository.findById(java.util.UUID.fromString(userId));
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(java.util.Map.of(
                "success", false,
                "error", java.util.Map.of(
                    "code", "USER_NOT_FOUND",
                    "message", "Utilisateur introuvable"
                ),
                "timestamp", now
            ));
        }
        com.opossum.user.User user = userOpt.get();
        user.setActive(false);
        userRepository.save(user);

        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("userId", userId);
        data.put("status", "BLOCKED");
        data.put("blockedAt", now);
        data.put("blockReason", request.reason);
        // Optionnel: gérer la durée de blocage (champ à ajouter si besoin)

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("success", true);
        response.put("data", data);
        response.put("message", "Utilisateur bloqué avec succès");
        response.put("timestamp", now);
        return ResponseEntity.ok(response);
    }
    public ResponseEntity<?> deleteUser(String userId) {
        String now = java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC).toString();
        java.util.Optional<com.opossum.user.User> userOpt = userRepository.findById(java.util.UUID.fromString(userId));
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(java.util.Map.of(
                "success", false,
                "error", java.util.Map.of(
                    "code", "USER_NOT_FOUND",
                    "message", "Utilisateur introuvable"
                ),
                "timestamp", now
            ));
        }
        userRepository.deleteById(java.util.UUID.fromString(userId));
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("success", true);
        response.put("message", "Compte utilisateur supprimé définitivement");
        response.put("timestamp", now);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> getAllAnnouncements(String status, String type, String userId, Boolean reported, int page, int size) {
        // Exemple d'annonce mockée
        java.util.Map<String, Object> author = new java.util.HashMap<>();
        author.put("id", "550e8400-e29b-41d4-a716-446655440001");
        author.put("email", "marie.dupont@example.com");
        author.put("firstName", "Marie");
        author.put("lastName", "Dupont");

        java.util.Map<String, Object> announcement = new java.util.HashMap<>();
        announcement.put("id", "550e8400-e29b-41d4-a716-446655440002");
        announcement.put("title", "iPhone 13 Pro trouvé");
        announcement.put("type", type != null ? type : "FOUND");
        announcement.put("status", status != null ? status : "ACTIVE");
        announcement.put("createdAt", "2025-07-09T08:30:00Z");
        announcement.put("author", author);
        announcement.put("reportCount", reported != null && reported ? 2 : 0);
        announcement.put("lastReportAt", reported != null && reported ? "2025-07-09T19:00:00Z" : null);
        announcement.put("moderationNotes", "");

        java.util.List<java.util.Map<String, Object>> content = java.util.List.of(announcement);
        java.util.Map<String, Object> pageInfo = java.util.Map.of(
                "number", page,
                "size", size,
                "totalElements", 1,
                "totalPages", 1
        );
        java.util.Map<String, Object> data = java.util.Map.of(
                "content", content,
                "page", pageInfo
        );
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("success", true);
        response.put("data", data);
        response.put("timestamp", java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC).toString());
        return ResponseEntity.ok(response);
    }
    
    public ResponseEntity<?> blockAnnouncement(String announcementId, BlockAnnouncementRequest request) {
        String now = java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC).toString();
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("announcementId", announcementId);
        data.put("status", "BLOCKED");
        data.put("blockedAt", now);
        data.put("blockReason", request.reason);
        data.put("moderationNotes", request.moderationNotes != null ? request.moderationNotes : "");

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("success", true);
        response.put("data", data);
        response.put("message", "Annonce bloquée avec succès");
        response.put("timestamp", now);
        return ResponseEntity.ok(response);
    }
}