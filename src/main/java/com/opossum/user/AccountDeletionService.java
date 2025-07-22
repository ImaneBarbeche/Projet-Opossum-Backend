package com.opossum.user;

import com.opossum.user.dto.DeleteProfileRequest;
import com.opossum.user.dto.DeleteProfileResponse;
import com.opossum.user.dto.ErrorResponse;
import com.opossum.auth.EmailService;
import com.opossum.token.RefreshTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.Optional;

@Service
public class AccountDeletionService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;

    public AccountDeletionService(UserRepository userRepository, EmailService emailService, RefreshTokenService refreshTokenService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.refreshTokenService = refreshTokenService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Gère la suppression du profil utilisateur avec toutes les vérifications nécessaires.
     * Retourne une ResponseEntity adaptée au contrôleur.
     */
    @Transactional
    public ResponseEntity<?> deleteProfile(DeleteProfileRequest request, java.security.Principal principal) {
        // DEBUG: log le mot de passe reçu (à retirer en prod)
        System.out.println("[DEBUG] Password reçu pour suppression: '" + request.getPassword() + "'");
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
        System.out.println("[DEBUG] Password hash stocké: '" + userOpt.get().getPasswordHash() + "'");
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

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash()) && !user.getPasswordHash().isBlank()) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).body(
                new DeleteProfileResponse(false, new ErrorResponse("INVALID_PASSWORD", "Mot de passe incorrect"), Instant.now())
            );
        }

        if (!user.isActive()) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).body(
                new DeleteProfileResponse(false, new ErrorResponse("USER_INACTIVE", "Utilisateur inactif"), Instant.now())
            );
        }

        // Log de sécurité : suppression exécutée
        System.out.println("[DEBUG] Suppression exécutée pour l'utilisateur : " + user.getEmail());
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
