package com.opossum.admin;

import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import com.opossum.admin.user.UserAdminService;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
    private final AdminService adminService;
    private final UserAdminService userAdminService;
    private final ListingsAdminService listingsAdminService;

    public AdminController(AdminService adminService, UserAdminService userAdminService, ListingsAdminService listingsAdminService) {
        this.adminService = adminService;
        this.userAdminService = userAdminService;
        this.listingsAdminService = listingsAdminService;
    }

    // Endpoint pour récupérer les statistiques globales
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getGlobalStats() {
        return adminService.getGlobalStats();
    }

    // Endpoint pour lister les utilisateurs avec recherche, filtrage, pagination et
    // tri
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sort", defaultValue = "createdAt,desc") String sort) {
        return userAdminService.getAllUsers(search, status, page, size, sort);
    }

    // Endpoint pour bloquer un utilisateur
    @PutMapping("/users/{userId}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> blockUser(
            @PathVariable("userId") String userId,
            @RequestBody BlockUserRequest request) {
        return userAdminService.blockUser(userId, request);
    }
    // Endpoint pour débloquer un utilisateur
    @PutMapping("/users/{userId}/unblock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> unblockUser(@PathVariable("userId") String userId) {
        return userAdminService.unblockUser(userId);
    }

    // Endpoint pour supprimer définitivement un utilisateur
    @DeleteMapping("/users/{userId}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable("userId") String userId) {
        return userAdminService.deleteUser(userId);
    }

    // Endpoint pour lister toutes les annonces avec outils de modération
    @GetMapping("/announcements")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllAnnouncements(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "userId", required = false) String userId,
            @RequestParam(value = "reported", required = false) Boolean reported,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        return listingsAdminService.getAllAnnouncements(status, type, userId, reported, page, size);
    }

    // Endpoint pour bloquer une annonce
    @PutMapping("/announcements/{announcementId}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> blockAnnouncement(
            @PathVariable("announcementId") String announcementId,
            @RequestBody BlockAnnouncementRequest request) {
        return listingsAdminService.blockAnnouncement(announcementId, request);
    }

    // Endpoint pour débloquer une annonce
    @PutMapping("/announcements/{announcementId}/unblock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> unblockAnnouncement(@PathVariable("announcementId") String announcementId) {
        return listingsAdminService.unblockAnnouncement(announcementId);
    }

    // Endpoint pour supprimer (soft delete) une annonce
    @DeleteMapping("/announcements/{announcementId}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> softDeleteAnnouncement(@PathVariable("announcementId") String announcementId) {
        return listingsAdminService.softDeleteAnnouncement(announcementId);
    }

}
