package com.opossum.user;
import com.opossum.user.dto.ChangePasswordRequest;
import com.opossum.auth.EmailService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.opossum.token.RefreshTokenService;
import com.opossum.user.dto.UpdateProfileRequest;
import com.opossum.user.dto.UserDto;
import com.opossum.user.dto.DeleteProfileRequest;
import com.opossum.user.dto.DeleteProfileResponse;
import com.opossum.user.dto.ErrorResponse;

@Service
@Transactional(readOnly = true)

public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final EmailService emailService;

    // @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, RefreshTokenService refreshTokenService, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
        this.emailService = emailService;
    }

    /**
     * Invalide tous les refresh tokens d'un utilisateur (après changement de mot de passe)
     */
    @Transactional
    public void invalidateAllTokensForUser(UUID userId) {
        refreshTokenService.deleteAllForUser(userId);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(UUID id) {
        return userRepository.findById(id);
    }

    @Transactional
    public Optional<User> editPassword(UUID id, String newPassword) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return Optional.of(user);
    }
    

    @Transactional
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        // Delete all refresh tokens for this user first
        refreshTokenService.deleteAllForUser(id);
        userRepository.deleteById(id);
    }


    @Transactional
    public User updateProfile(UUID id, UpdateProfileRequest dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (dto.getFirstName() != null && !dto.getFirstName().isBlank()) {
            user.setFirstName(dto.getFirstName());
        }

        if (dto.getLastName() != null && !dto.getLastName().isBlank()) {
            user.setLastName(dto.getLastName());
        }

        if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
            user.setPhone(dto.getPhone());
        }

        if (dto.getAvatar() != null && !dto.getAvatar().isBlank()) {
            user.setAvatar(dto.getAvatar());
        }

        user.setUpdatedAt(Instant.now());
        return userRepository.save(user);
    }

    @Transactional
    public boolean verifyEmailToken(String token) {
        Optional<User> userOpt = userRepository.findByEmailVerificationToken(token);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setEmailVerified(true);
            user.setEmailVerificationToken(null);
            user.setUpdatedAt(Instant.now());
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Transactional
    public void updateLastLogin(UUID userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setLastLoginAt(Instant.now());
            userRepository.save(user);
        });
    }

    public UserDto mapToDto(User user) {
        if (user == null) {
            return null;
        }

        return new UserDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getAvatar(), // avatarUrl = avatar
                user.getRole() != null ? user.getRole().name() : null, // enum to string
                user.isActive(),
                user.isEmailVerified(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getLastLoginAt()
        );
    }

    /**
     * Gère le changement de mot de passe d'un utilisateur, avec toutes les vérifications et notifications.
     * Retourne une ResponseEntity adaptée au contrôleur.
     */
    @Transactional
    public ResponseEntity<?> changePassword(User user, ChangePasswordRequest request) {
        if (user == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).body(
                java.util.Map.of(
                    "success", false,
                    "error", java.util.Map.of(
                        "code", "UNAUTHORIZED",
                        "message", "Utilisateur non connecté"
                    ),
                    "timestamp", java.time.Instant.now()
                )
            );
        }
        // Vérifier l'ancien mot de passe
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).body(
                java.util.Map.of(
                    "success", false,
                    "error", java.util.Map.of(
                        "code", "INVALID_CURRENT_PASSWORD",
                        "message", "Mot de passe actuel incorrect"
                    ),
                    "timestamp", java.time.Instant.now()
                )
            );
        }
        // Vérifier que le nouveau mot de passe est différent
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST).body(
                java.util.Map.of(
                    "success", false,
                    "error", java.util.Map.of(
                        "code", "PASSWORD_UNCHANGED",
                        "message", "Le nouveau mot de passe doit être différent de l'actuel"
                    ),
                    "timestamp", java.time.Instant.now()
                )
            );
        }
        // Mettre à jour le mot de passe
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(java.time.Instant.now());
        userRepository.save(user);
        // Invalider tous les refresh tokens de l'utilisateur
        invalidateAllTokensForUser(user.getId());
        // Envoyer un email de notification à l'utilisateur après modification du mot de passe
        emailService.sendPasswordChangedNotification(user.getEmail(), user.getFirstName());
        return ResponseEntity.ok(
            java.util.Map.of(
                "success", true,
                "message", "Mot de passe modifié avec succès",
                "timestamp", java.time.Instant.now()
            )
        );
    }

    /**
     * Gère la mise à jour du profil utilisateur (prénom, nom, téléphone, avatar).
     * Retourne une ResponseEntity adaptée au contrôleur.
     */
    @Transactional
    public ResponseEntity<?> updateProfile(User user, UpdateProfileRequest request) {
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
            user.setAvatar(request.getAvatar());
            changed = true;
        }
        if (changed) {
            user.setUpdatedAt(Instant.now());
            userRepository.save(user);
        }
        UserDto dto = mapToDto(user);
        return ResponseEntity.ok(
            java.util.Map.of(
                "success", true,
                "data", dto,
                "message", "Profil mis à jour avec succès",
                "timestamp", Instant.now()
            )
        );
    }

    /**
     * Gère la suppression du profil utilisateur avec toutes les vérifications nécessaires.
     * Retourne une ResponseEntity adaptée au contrôleur.
     */
    @Transactional
    public ResponseEntity<?> deleteProfile(DeleteProfileRequest request, java.security.Principal principal) {
        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).body(
                new DeleteProfileResponse(false, new ErrorResponse("UNAUTHORIZED", "Utilisateur non authentifié"), Instant.now())
            );
        }

        Optional<User> userOpt = userRepository.findByEmail(principal.getName());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND).body(
                new DeleteProfileResponse(false, new ErrorResponse("USER_NOT_FOUND", "Utilisateur introuvable"), Instant.now())
            );
        }
        User user = userOpt.get();

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST).body(
                new DeleteProfileResponse(false, new ErrorResponse("INVALID_PASSWORD", "Mot de passe requis"), Instant.now())
            );
        }

        if (!request.isConfirmDeletion()) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST).body(
                new DeleteProfileResponse(false, new ErrorResponse("CONFIRMATION_REQUIRED", "Confirmation de suppression requise"), Instant.now())
            );
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).body(
                new DeleteProfileResponse(false, new ErrorResponse("INVALID_PASSWORD", "Mot de passe incorrect"), Instant.now())
            );
        }

        if (!user.isActive()) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).body(
                new DeleteProfileResponse(false, new ErrorResponse("USER_INACTIVE", "Utilisateur inactif"), Instant.now())
            );
        }

        // Suppression des données utilisateur (et potentiellement des tokens associés)
        deleteUser(user.getId());
        // Envoi d'un email de confirmation de suppression de compte
        emailService.sendAccountDeletedConfirmation(user.getEmail(), user.getFirstName());

        return ResponseEntity.ok(
            new DeleteProfileResponse(true, null, Instant.now(), "Compte supprimé définitivement")
        );
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
            if (!currentUser.isActive()) {
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
            UserDto dto = mapToDto(currentUser);
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
        java.util.Optional<User> userOpt = getUserById(uuid);
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
}
