package com.opossum.admin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.opossum.auth.EmailService;
import com.opossum.listings.ListingsRepository;

@Service
public class ListingsAdminService {
    private final EmailService emailService;
    private final ListingsRepository listingsRepository;

     @Autowired
    public ListingsAdminService(ListingsRepository listingsRepository, EmailService emailService) {
        this.listingsRepository = listingsRepository;
        this.emailService = emailService;
    }
    
    public ResponseEntity<?> getAllAnnouncements(String status, String type, String userId, Boolean reported, int page,
            int size) {
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
                "totalPages", 1);
        java.util.Map<String, Object> data = java.util.Map.of(
                "content", content,
                "page", pageInfo);
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("success", true);
        response.put("data", data);
        response.put("timestamp", java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC).toString());
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> blockAnnouncement(String announcementId, BlockAnnouncementRequest request) {
        String now = java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC).toString();
        java.util.Optional<com.opossum.listings.Listings> announcementOpt = listingsRepository
                .findById(java.util.UUID.fromString(announcementId));
        if (announcementOpt.isEmpty()) {
            return ResponseEntity.status(404).body(java.util.Map.of(
                    "success", false,
                    "error", java.util.Map.of(
                            "code", "ANNOUNCEMENT_NOT_FOUND",
                            "message", "Annonce introuvable"),
                    "timestamp", now));
        }
        com.opossum.listings.Listings announcement = announcementOpt.get();
        announcement.setStatus(com.opossum.common.enums.ListingStatus.ARCHIVED);
        listingsRepository.save(announcement);

        // Notifier l'auteur par email
        com.opossum.user.User author = announcement.getUser();
        if (author != null) {
            // EmailService doit être injecté dans AdminService
            emailService.sendAnnouncementBlockedNotification(
                    author.getEmail(),
                    author.getFirstName(),
                    announcement.getTitle(),
                    request.reason,
                    request.moderationNotes);
        }

        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("announcementId", announcementId);
        data.put("status", "ARCHIVED");
        data.put("archivedAt", now);
        data.put("blockReason", request.reason);
        data.put("moderationNotes", request.moderationNotes != null ? request.moderationNotes : "");

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("success", true);
        response.put("data", data);
        response.put("message", "Annonce archivée (bloquée) avec succès");
        response.put("timestamp", now);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> softDeleteAnnouncement(String announcementId) {
        String now = java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC).toString();
        java.util.Optional<com.opossum.listings.Listings> announcementOpt = listingsRepository
                .findById(java.util.UUID.fromString(announcementId));
        if (announcementOpt.isEmpty()) {
            return ResponseEntity.status(404).body(java.util.Map.of(
                    "success", false,
                    "error", java.util.Map.of(
                            "code", "ANNOUNCEMENT_NOT_FOUND",
                            "message", "Annonce introuvable"),
                    "timestamp", now));
        }
        com.opossum.listings.Listings announcement = announcementOpt.get();
        announcement.setStatus(com.opossum.common.enums.ListingStatus.DELETED);
        listingsRepository.save(announcement);
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("announcementId", announcementId);
        data.put("status", "DELETED");
        data.put("deletedAt", now);
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("success", true);
        response.put("data", data);
        response.put("message", "Annonce supprimée (soft delete) avec succès");
        response.put("timestamp", now);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> unblockAnnouncement(String announcementId) {
        String now = java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC).toString();
        java.util.Optional<com.opossum.listings.Listings> announcementOpt = listingsRepository
                .findById(java.util.UUID.fromString(announcementId));
        if (announcementOpt.isEmpty()) {
            return ResponseEntity.status(404).body(java.util.Map.of(
                    "success", false,
                    "error", java.util.Map.of(
                            "code", "ANNOUNCEMENT_NOT_FOUND",
                            "message", "Annonce introuvable"),
                    "timestamp", now));
        }
        com.opossum.listings.Listings announcement = announcementOpt.get();
        announcement.setStatus(com.opossum.common.enums.ListingStatus.ACTIVE);
        listingsRepository.save(announcement);
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("announcementId", announcementId);
        data.put("status", "ACTIVE");
        data.put("unblockedAt", now);
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("success", true);
        response.put("data", data);
        response.put("message", "Annonce débloquée avec succès");
        response.put("timestamp", now);
        return ResponseEntity.ok(response);
    }

}
