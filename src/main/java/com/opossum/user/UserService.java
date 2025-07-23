

package com.opossum.user;

import java.util.List;
import com.opossum.user.dto.ChangePasswordRequest;
import com.opossum.auth.EmailService;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.opossum.token.RefreshTokenService;
import com.opossum.user.dto.UpdateProfileRequest;
import com.opossum.user.dto.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import com.opossum.user.dto.DeleteProfileRequest;

@Service
@Transactional(readOnly = true)

public class UserService {
    public com.opossum.user.dto.UserProfileResponse mapToUserProfileResponse(User user) {
        if (user == null) {
            return null;
        }
        return new com.opossum.user.dto.UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getAvatar(),
                user.getRole() != null ? user.getRole().name() : null
        );
    }
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final ProfileService profileService;
    private final AccountDeletionService accountDeletionService;
    private final PasswordService passwordService;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, RefreshTokenService refreshTokenService, EmailService emailService, ProfileService profileService, AccountDeletionService accountDeletionService, PasswordService passwordService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
        this.profileService = profileService;
        this.accountDeletionService = accountDeletionService;
        this.passwordService = passwordService;
    }

    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public Optional<User> getUserById(UUID id) {
        return userRepository.findById(id);
    }

        /**
     * Récupère le profil utilisateur (privé si 'me', public sinon)
     * @param id identifiant ou "me"
     * @param currentUser utilisateur authentifié (peut être null)
     * @return ResponseEntity avec le profil ou une erreur
     */
    public ResponseEntity<?> getUserProfile(String id, User currentUser) {
        return profileService.getUserProfile(id, currentUser);
    }

    @Transactional
    public User updatePassword(User user, String newPassword) {
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(Instant.now());
        return userRepository.save(user);
    }
    
    /**
     * Gère le changement de mot de passe d'un utilisateur, avec toutes les vérifications et notifications.
     * Retourne une ResponseEntity adaptée au contrôleur.
     */
    @Transactional
    public ResponseEntity<?> changePassword(User user, ChangePasswordRequest request) {
        return passwordService.changePassword(user, request);
    }
    
    /**
     * Invalide tous les refresh tokens d'un utilisateur (après changement de mot de passe)
     */
    @Transactional
    public void invalidateAllTokensForUser(UUID userId) {
        refreshTokenService.deleteAllForUser(userId);
    }

    // Removed duplicate updateProfile(UUID, UpdateProfileRequest) method. Use updateProfile(User, UpdateProfileRequest) instead.

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
    /**
     * Gère la mise à jour du profil utilisateur (prénom, nom, téléphone, avatar).
     * Retourne une ResponseEntity adaptée au contrôleur.
     */
    @Transactional
    public ResponseEntity<?> updateProfile(User user, UpdateProfileRequest request) {
        UserDto dto = profileService.updateProfile(user, request);
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
        return accountDeletionService.deleteProfile(request, principal);
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


}
