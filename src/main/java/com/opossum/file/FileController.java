/**
 * Contrôleur REST pour la gestion des fichiers
 */
package com.opossum.file;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    private final FileService fileService;

    @Autowired
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * Upload d'un fichier (image)
     */
    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file, @org.springframework.security.core.annotation.AuthenticationPrincipal com.opossum.user.User currentUser) {
        // Passe l'ID utilisateur au service
        return fileService.uploadFile(file, currentUser != null ? currentUser.getId() : null);
    }

    /**
     * Récupérer un fichier par son ID (ou miniature via paramètre)
     */
    @GetMapping("/{fileId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getFile(
            @PathVariable UUID fileId,
            @RequestParam(value = "thumbnail", required = false, defaultValue = "false") boolean thumbnail,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.opossum.user.User currentUser) {
        // Seul l'uploader ou un admin peut accéder au fichier
        return fileService.getFile(fileId, thumbnail, currentUser);
    }
        /**
     * Suppression d'un fichier (soft delete)
     */
    @DeleteMapping("/{fileId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteFile(@PathVariable UUID fileId, @org.springframework.security.core.annotation.AuthenticationPrincipal com.opossum.user.User currentUser) {
        return fileService.deleteFile(fileId, currentUser);
    }
}
    // Contrôleur File à implémenter

