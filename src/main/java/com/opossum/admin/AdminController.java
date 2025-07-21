
package com.opossum.admin;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // Endpoint pour récupérer les statistiques globales
    @GetMapping("/v1/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getGlobalStats() {
        return adminService.getGlobalStats();
    }

    // Endpoint pour lister les utilisateurs avec recherche, filtrage, pagination et tri
    @GetMapping("/v1/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sort", defaultValue = "createdAt,desc") String sort
    ) {
        return adminService.getAllUsers(search, status, page, size, sort);
    }
        // Endpoint pour bloquer un utilisateur
    @PutMapping("/v1/admin/users/{userId}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> blockUser(
            @PathVariable("userId") String userId,
            @RequestBody BlockUserRequest request
    ) {
        return adminService.blockUser(userId, request);
    }
        // Endpoint pour supprimer définitivement un utilisateur
    @DeleteMapping("/v1/admin/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable("userId") String userId) {
        return adminService.deleteUser(userId);
    }
    // Endpoint pour lister toutes les annonces avec outils de modération
    @GetMapping("/v1/admin/announcements")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllAnnouncements(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "userId", required = false) String userId,
            @RequestParam(value = "reported", required = false) Boolean reported,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        return adminService.getAllAnnouncements(status, type, userId, reported, page, size);
    }
    // Endpoint pour bloquer une annonce
    @PutMapping("/v1/admin/announcements/{announcementId}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> blockAnnouncement(
            @PathVariable("announcementId") String announcementId,
            @RequestBody BlockAnnouncementRequest request
    ) {
        return adminService.blockAnnouncement(announcementId, request);
    }

}
