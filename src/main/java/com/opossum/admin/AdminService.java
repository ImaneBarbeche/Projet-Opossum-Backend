
package com.opossum.admin;

import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.List;

@Service
public class AdminService {
    // Logique métier admin à ajouter ici

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
        // Exemple de données utilisateur mockées (HashMap pour plus de 10 paires)
        java.util.Map<String, Object> user = new java.util.HashMap<>();
        user.put("id", "550e8400-e29b-41d4-a716-446655440001");
        user.put("email", "marie.dupont@example.com");
        user.put("firstName", "Marie");
        user.put("lastName", "Dupont");
        user.put("phone", "+33123456789");
        user.put("status", status != null ? status : "ACTIVE");
        user.put("role", "USER");
        user.put("createdAt", "2025-07-01T10:30:00Z");
        user.put("lastLoginAt", "2025-07-09T08:00:00Z");
        user.put("announcementCount", 3);
        user.put("conversationCount", 5);

        java.util.List<java.util.Map<String, Object>> content = java.util.List.of(user);
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
        java.util.Map<String, Object> response = java.util.Map.of(
                "success", true,
                "data", data,
                "timestamp", java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC).toString()
        );
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> blockUser(String userId, BlockUserRequest request) {
        // Simule le blocage d'un utilisateur
        String now = java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC).toString();
        String unblockAt = null;
        if (request.duration != null) {
            java.time.ZonedDateTime unblockDate = java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC).plusDays(request.duration);
            unblockAt = unblockDate.toString();
        }
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("userId", userId);
        data.put("status", "BLOCKED");
        data.put("blockedAt", now);
        data.put("blockReason", request.reason);
        if (unblockAt != null) data.put("unblockAt", unblockAt);

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("success", true);
        response.put("data", data);
        response.put("message", "Utilisateur bloqué avec succès");
        response.put("timestamp", now);
        return ResponseEntity.ok(response);
    }
    public ResponseEntity<?> deleteUser(String userId) {
        String now = java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC).toString();
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