package com.opossum.admin.user;

import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;
import com.opossum.user.UserRepository;
import com.opossum.common.enums.UserStatus;
import java.util.Optional;
import java.util.UUID;
import com.opossum.admin.BlockUserRequest;

@Service
public class UserAdminService {

    private final UserRepository userRepository;

    public UserAdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ResponseEntity<?> blockUser(String userId, BlockUserRequest request) {
        String now = java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC).toString();
        Optional<com.opossum.user.User> userOpt = userRepository.findById(UUID.fromString(userId));
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(java.util.Map.of(
                "success", false,
                "error", java.util.Map.of(
                    "code", "USER_NOT_FOUND",
                    "message", "Utilisateur introuvable"),
                "timestamp", now));
        }
        com.opossum.user.User user = userOpt.get();
        user.setStatus(UserStatus.BLOCKED);
        java.time.ZonedDateTime blockedUntil = null;
        if (request.duration != null && request.duration > 0) {
            blockedUntil = java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC).plusDays(request.duration);
            user.setBlockedUntil(blockedUntil);
        } else {
            user.setBlockedUntil(null);
        }
        userRepository.save(user);
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("userId", userId);
        data.put("status", "BLOCKED");
        data.put("blockedAt", now);
        data.put("blockReason", request.reason);
        if (blockedUntil != null) {
            data.put("unblockAt", blockedUntil.toString());
        }
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("success", true);
        response.put("data", data);
        response.put("message", "Utilisateur bloqué avec succès");
        response.put("timestamp", now);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> unblockUser(String userId) {
        String now = java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC).toString();
        Optional<com.opossum.user.User> userOpt = userRepository.findById(UUID.fromString(userId));
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(java.util.Map.of(
                "success", false,
                "error", java.util.Map.of(
                    "code", "USER_NOT_FOUND",
                    "message", "Utilisateur introuvable"),
                "timestamp", now));
        }
        com.opossum.user.User user = userOpt.get();
        user.setStatus(UserStatus.ACTIVE);
        user.setBlockedUntil(null);
        userRepository.save(user);
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("userId", userId);
        data.put("status", "ACTIVE");
        data.put("unblockedAt", now);
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("success", true);
        response.put("data", data);
        response.put("message", "Utilisateur débloqué avec succès");
        response.put("timestamp", now);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> deleteUser(String userId) {
        String now = java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC).toString();
        Optional<com.opossum.user.User> userOpt = userRepository.findById(UUID.fromString(userId));
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(java.util.Map.of(
                "success", false,
                "error", java.util.Map.of(
                    "code", "USER_NOT_FOUND",
                    "message", "Utilisateur introuvable"),
                "timestamp", now));
        }
        com.opossum.user.User user = userOpt.get();
        user.setStatus(UserStatus.DELETED);
        userRepository.save(user);
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("userId", userId);
        data.put("status", "DELETED");
        data.put("deletedAt", now);
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("success", true);
        response.put("data", data);
        response.put("message", "Compte utilisateur supprimé (soft delete) avec succès");
        response.put("timestamp", now);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> getAllUsers(String search, String status, int page, int size, String sort) {
        org.springframework.data.domain.Pageable pageable;
        if (sort != null && sort.contains(",")) {
            String[] sortParts = sort.split(",");
            String sortField = sortParts[0];
            String sortDir = sortParts.length > 1 ? sortParts[1] : "asc";
            org.springframework.data.domain.Sort.Direction direction = sortDir.equalsIgnoreCase("desc")
                    ? org.springframework.data.domain.Sort.Direction.DESC
                    : org.springframework.data.domain.Sort.Direction.ASC;
            pageable = org.springframework.data.domain.PageRequest.of(page, size,
                    org.springframework.data.domain.Sort.by(direction, sortField));
        } else {
            pageable = org.springframework.data.domain.PageRequest.of(page, size);
        }

        org.springframework.data.jpa.domain.Specification<com.opossum.user.User> spec = (root, query, cb) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            if (search != null && !search.isEmpty()) {
                String like = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("firstName")), like),
                        cb.like(cb.lower(root.get("lastName")), like),
                        cb.like(cb.lower(root.get("email")), like)));
            }
            if (status != null) {
                if (status.equalsIgnoreCase("ACTIVE"))
                    predicates.add(cb.isTrue(root.get("isActive")));
                else if (status.equalsIgnoreCase("BLOCKED"))
                    predicates.add(cb.isFalse(root.get("isActive")));
                // Ajout d'autres statuts si besoin
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        org.springframework.data.domain.Page<com.opossum.user.User> userPage = userRepository.findAll(spec, pageable);

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
            // Si besoin, injecte ListingsRepository dans ce service
            u.put("announcementCount", 0); // À adapter si tu veux le vrai nombre
            u.put("conversationCount", 0);
            content.add(u);
        }

        java.util.Map<String, Object> pageInfo = java.util.Map.of(
                "number", userPage.getNumber(),
                "size", userPage.getSize(),
                "totalElements", userPage.getTotalElements(),
                "totalPages", userPage.getTotalPages());
        java.util.Map<String, Object> data = java.util.Map.of(
                "content", content,
                "page", pageInfo);
        java.util.Map<String, Object> response = java.util.Map.of(
                "success", true,
                "data", data,
                "timestamp", java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC).toString());
        return ResponseEntity.ok(response);
    }
}
