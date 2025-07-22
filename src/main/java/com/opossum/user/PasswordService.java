package com.opossum.user;

import com.opossum.user.dto.ChangePasswordRequest;
import com.opossum.auth.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.opossum.token.RefreshTokenService;

@Service
public class PasswordService {
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;

    public PasswordService(PasswordEncoder passwordEncoder, EmailService emailService, UserRepository userRepository, RefreshTokenService refreshTokenService) {
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.refreshTokenService = refreshTokenService;
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
        refreshTokenService.deleteAllForUser(user.getId());
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
}
