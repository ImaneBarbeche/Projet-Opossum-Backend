package com.opossum.file;

/**
 * Repository pour l'entité FileEntity
 */
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, UUID> {
    Optional<FileEntity> findByStoredName(String storedName);
    // Trouver les fichiers soft-deleted depuis plus d'un an
    java.util.List<FileEntity> findByIsDeletedAndCreatedAtBefore(boolean isDeleted, java.time.Instant before);
}
