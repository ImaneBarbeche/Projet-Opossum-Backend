package com.opossum.file;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.UUID;

/**
 * Service métier pour la gestion des fichiers
 */
@Service
public class FileService {

    @Value("${FILE_STORAGE_PATH:./uploads}")
    private String fileStoragePath;

    private final FileRepository fileRepository;
    private final Cloudinary cloudinary;

    public FileService(FileRepository fileRepository, Cloudinary cloudinary) {
        this.fileRepository = fileRepository;
        this.cloudinary = cloudinary;
    }

public ResponseEntity<?> uploadFile(MultipartFile file, java.util.UUID uploadedBy) {
        // 1. Validation
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("Aucun fichier envoyé");
        }
        // originalName inutilisé, supprimé
        String mimeType = file.getContentType();
        long fileSize = file.getSize();
        // Types autorisés
        if (!(mimeType.equals("image/jpeg") || mimeType.equals("image/png") || mimeType.equals("image/webp"))) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body("Format non supporté (JPG, PNG, WEBP)");
        }
        // Taille max 10MB
        if (fileSize > 10 * 1024 * 1024) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body("Le fichier dépasse la taille maximum autorisée (10MB)");
        }

    try {
        // Transformation Cloudinary : compression automatique et format optimisé
        @SuppressWarnings("unchecked")
        Map<String, Object> uploadResult = (Map<String, Object>) cloudinary.uploader().upload(
            file.getBytes(),
            ObjectUtils.asMap(
                "quality", "auto",
                "fetch_format", "auto"
            )
        );
        String url = (String) uploadResult.get("secure_url");

        FileEntity entity = new FileEntity();
        entity.setOriginalName(file.getOriginalFilename());
        entity.setStoredName((String) uploadResult.get("public_id"));
        entity.setUrl(url);
        entity.setThumbnailUrl((String) uploadResult.get("thumbnail_url"));
        entity.setMimeType(file.getContentType());
        entity.setFileSize((int) file.getSize());
        entity.setUploadedBy(uploadedBy);
        fileRepository.save(entity);

        // Réponse enrichie (null-safe)
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("id", entity.getId());
        response.put("url", entity.getUrl());
        response.put("thumbnailUrl", entity.getThumbnailUrl());
        response.put("originalName", entity.getOriginalName());
        response.put("storedName", entity.getStoredName());
        response.put("mimeType", entity.getMimeType());
        response.put("fileSize", entity.getFileSize());
        response.put("uploadedBy", entity.getUploadedBy());
        response.put("createdAt", entity.getCreatedAt());

        // Structure compatible frontend
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("success", true);
        result.put("data", response);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    } catch (Exception e) {
        e.printStackTrace(); // ou logger.error("Erreur upload Cloudinary", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de l'upload Cloudinary");
    }
}

    public ResponseEntity<?> getFile(UUID fileId, boolean thumbnail, com.opossum.user.User currentUser) {
        // Recherche du fichier
        FileEntity entity = fileRepository.findById(fileId).orElse(null);
        if (entity == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Fichier introuvable");
        }
        if (entity.isDeleted()) {
            return ResponseEntity.status(HttpStatus.GONE).body("Fichier supprimé");
        }
        // Vérification des droits : uploader ou admin
        boolean isOwner = currentUser != null && entity.getUploadedBy() != null && entity.getUploadedBy().equals(currentUser.getId());
        boolean isAdmin = currentUser != null && currentUser.getRole() != null && currentUser.getRole().name().equals("ADMIN");
        if (!isOwner && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Accès refusé : vous n'êtes pas propriétaire ni admin");
        }
        String redirectUrl = thumbnail && entity.getThumbnailUrl() != null ? entity.getThumbnailUrl() : entity.getUrl();
        return ResponseEntity.status(HttpStatus.FOUND).header("Location", redirectUrl).build();
    }
        public ResponseEntity<?> deleteFile(UUID fileId, com.opossum.user.User currentUser) {
        FileEntity entity = fileRepository.findById(fileId).orElse(null);
        if (entity == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Fichier introuvable");
        }
        if (entity.isDeleted()) {
            return ResponseEntity.status(HttpStatus.GONE).body("Fichier déjà supprimé");
        }
        boolean isOwner = currentUser != null && entity.getUploadedBy() != null && entity.getUploadedBy().equals(currentUser.getId());
        boolean isAdmin = currentUser != null && currentUser.getRole() != null && currentUser.getRole().name().equals("ADMIN");
        if (!isOwner && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Suppression refusée : vous n'êtes pas propriétaire ni admin");
        }
        entity.setDeleted(true);
        fileRepository.save(entity);
        return ResponseEntity.ok().body("Fichier supprimé (soft delete)");
    }
    
    /**
     * Supprime physiquement un fichier sur Cloudinary à partir du public_id (storedName)
     */
    public void deleteFromCloudinary(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            // Log ou gestion d'erreur si besoin
        }
    }
}
