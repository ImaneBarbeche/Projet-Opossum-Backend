package com.opossum.file;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;
import java.io.IOException;

/**
 * Service métier pour la gestion des fichiers
 */
@Service
public class FileService {

    @Value("${FILE_STORAGE_PATH:./uploads}")
    private String fileStoragePath;

    private final FileRepository fileRepository;
    private final Cloudinary cloudinary;

    @Autowired
    public FileService(FileRepository fileRepository, Cloudinary cloudinary) {
        this.fileRepository = fileRepository;
        this.cloudinary = cloudinary;
    }

public ResponseEntity<?> uploadFile(MultipartFile file, java.util.UUID uploadedBy) {
        // 1. Validation
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("Aucun fichier envoyé");
        }
        String originalName = StringUtils.cleanPath(file.getOriginalFilename());
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

        // 2. Génération d'un nom unique
        String extension = StringUtils.getFilenameExtension(originalName);
        String storedName = UUID.randomUUID().toString() + (extension != null ? "." + extension : "");

        // 3. Stockage sur disque
        
    try {
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
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

        // Réponse enrichie
        Map<String, Object> response = Map.of(
            "id", entity.getId(),
            "url", entity.getUrl(),
            "thumbnailUrl", entity.getThumbnailUrl(),
            "originalName", entity.getOriginalName(),
            "storedName", entity.getStoredName(),
            "mimeType", entity.getMimeType(),
            "fileSize", entity.getFileSize(),
            "uploadedBy", entity.getUploadedBy(),
            "createdAt", entity.getCreatedAt()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } catch (Exception e) {
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
}
