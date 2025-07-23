package com.opossum.user;

import com.opossum.user.dto.DeleteProfileRequest;
import com.opossum.user.dto.DeleteProfileResponse;
import com.opossum.user.dto.ErrorResponse;
import com.opossum.auth.EmailService;
import com.opossum.token.RefreshTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.Optional;

@Service
public class AccountDeletionService {
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final RefreshTokenService refreshTokenService;

    public AccountDeletionService(UserRepository userRepository, EmailService emailService, RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.refreshTokenService = refreshTokenService;
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

        if (!user.getPasswordHash().equals(request.getPassword()) && !user.getPasswordHash().isBlank()) {
            // Pour la sécurité, il faudrait utiliser un PasswordEncoder ici
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
        refreshTokenService.deleteAllForUser(user.getId());
        userRepository.deleteById(user.getId());
        // Envoi d'un email de confirmation de suppression de compte
        emailService.sendAccountDeletedConfirmation(user.getEmail(), user.getFirstName());

        return ResponseEntity.ok(
            new DeleteProfileResponse(true, null, Instant.now(), "Compte supprimé définitivement")
        );
    }
}
