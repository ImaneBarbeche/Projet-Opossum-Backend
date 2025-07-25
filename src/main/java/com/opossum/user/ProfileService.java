package com.opossum.user;

import com.opossum.user.dto.UpdateProfileRequest;
import com.opossum.user.dto.UserDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.opossum.file.FileRepository;
import java.time.Instant;

@Service
public class ProfileService {
    private final UserRepository userRepository;
    private final FileRepository fileRepository;

    public ProfileService(UserRepository userRepository, FileRepository fileRepository) {
        this.userRepository = userRepository;
        this.fileRepository = fileRepository;
    }

    /**
     * Récupère le profil utilisateur (privé si 'me', public sinon)
     * @param id identifiant ou "me"
     * @param currentUser utilisateur authentifié (peut être null)
     * @return ResponseEntity avec le profil ou une erreur
     */
    public ResponseEntity<?> getUserProfile(String id, User currentUser) {
        if ("me".equalsIgnoreCase(id)) {
            if (currentUser == null) {
                return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).body(
                    java.util.Map.of(
                        "success", false,
                        "error", java.util.Map.of(
                            "code", "UNAUTHORIZED",
                            "message", "Token JWT invalide ou expiré"
                        ),
                        "timestamp", java.time.Instant.now()
                    )
                );
            }
            // Recharge l'utilisateur depuis la base pour avoir toutes les infos à jour
            User user = userRepository.findById(currentUser.getId()).orElse(currentUser);
            if (!user.isActive()) {
                return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).body(
                    java.util.Map.of(
                        "success", false,
                        "error", java.util.Map.of(
                            "code", "USER_BLOCKED",
                            "message", "Ce profil n’est pas accessible"
                        ),
                        "timestamp", java.time.Instant.now()
                    )
                );
            }
            UserDto dto = new UserDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getAvatar(),
                user.getRole(),
                user.isActive(),
                user.isEmailVerified(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getLastLoginAt()
            );
            return ResponseEntity.ok(
                java.util.Map.of(
                    "success", true,
                    "data", dto,
                    "timestamp", java.time.Instant.now()
                )
            );
        }
        // UUID demandé
        java.util.UUID uuid;
        try {
            uuid = java.util.UUID.fromString(id);
        } catch (Exception e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND).body(
                java.util.Map.of(
                    "success", false,
                    "error", java.util.Map.of(
                        "code", "USER_NOT_FOUND",
                        "message", "Utilisateur introuvable"
                    ),
                    "timestamp", java.time.Instant.now()
                )
            );
        }
        java.util.Optional<User> userOpt = userRepository.findById(uuid);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND).body(
                java.util.Map.of(
                    "success", false,
                    "error", java.util.Map.of(
                        "code", "USER_NOT_FOUND",
                        "message", "Utilisateur introuvable"
                    ),
                    "timestamp", java.time.Instant.now()
                )
            );
        }
        User user = userOpt.get();
        if (!user.isActive()) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).body(
                java.util.Map.of(
                    "success", false,
                    "error", java.util.Map.of(
                        "code", "USER_BLOCKED",
                        "message", "Ce profil n’est pas accessible"
                    ),
                    "timestamp", java.time.Instant.now()
                )
            );
        }
        com.opossum.user.dto.PublicUserDto publicDto = new com.opossum.user.dto.PublicUserDto(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getAvatar(),
            user.getCreatedAt()
        );
        return ResponseEntity.ok(
            java.util.Map.of(
                "success", true,
                "data", publicDto,
                "timestamp", java.time.Instant.now()
            )
        );
    }

    /**
     * Met à jour le profil utilisateur (prénom, nom, téléphone, avatar).
     * @param user utilisateur à mettre à jour
     * @param request données de mise à jour
     * @return UserDto mis à jour
     */
    @Transactional
    public UserDto updateProfile(User user, UpdateProfileRequest request) {
        boolean changed = false;
        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            user.setFirstName(request.getFirstName());
            changed = true;
        }
        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            user.setLastName(request.getLastName());
            changed = true;
        }
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            user.setPhone(request.getPhone());
            changed = true;
        }
        if (request.getAvatar() != null && !request.getAvatar().isBlank()) {
            // Suppression de l'ancien avatar si différent
            String oldAvatar = user.getAvatar();
            if (oldAvatar != null && !oldAvatar.isBlank() && !oldAvatar.equals(request.getAvatar())) {
                // On suppose que le champ avatar contient l'URL Cloudinary
                // On cherche le FileEntity correspondant à cette URL
                fileRepository.findAll().stream()
                    .filter(f -> oldAvatar.equals(f.getUrl()))
                    .findFirst()
                    .ifPresent(f -> {
                        f.setDeleted(true);
                        fileRepository.save(f);
                    });
            }
            user.setAvatar(request.getAvatar());
            System.out.println("Nouvel avatar : " + user.getAvatar());
            changed = true;
        }
        if (changed) {
            user.setUpdatedAt(Instant.now());
            userRepository.save(user);
        }
        return new UserDto(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            user.getPhone(),
            user.getAvatar(),
            user.getRole(),
            user.isActive(),
            user.isEmailVerified(),
            user.getCreatedAt(),
            user.getUpdatedAt(),
            user.getLastLoginAt()
        );
    }
}
