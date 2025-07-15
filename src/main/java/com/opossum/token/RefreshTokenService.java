package com.opossum.token;

import com.opossum.user.User;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

/**
 * Service métier pour la gestion des refresh tokens :
 * - création
 * - vérification
 * - révocation
 */
@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    // Expiration par défaut : 7 jours
    private static final long EXPIRATION_DAYS = 7;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /**
     * Crée et enregistre un nouveau refresh token pour un utilisateur.
     */
    public String createRefreshToken(User user) {
        // Supprime l’ancien token si existant
        refreshTokenRepository.deleteByUserId(user.getId());

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(generateSecureToken());
        refreshToken.setCreatedAt(Instant.now());
        refreshToken.setExpiresAt(Instant.now().plus(EXPIRATION_DAYS, ChronoUnit.DAYS));
        refreshToken.setRevoked(false);

        refreshTokenRepository.save(refreshToken);
        return refreshToken.getToken();
    }

    /**
     * Vérifie qu’un refresh token est valide et non expiré.
     */
    public boolean isValid(String token) {
        Optional<RefreshToken> optionalToken = refreshTokenRepository.findByToken(token);

        if (optionalToken.isEmpty()) {
            return false;
        }

        RefreshToken rt = optionalToken.get();
        return !rt.isRevoked() && rt.getExpiresAt().isAfter(Instant.now());
    }

    /**
     * Révoque manuellement un refresh token (ex: logout).
     */
    @Transactional
    public void revokeToken(String token) {
        Optional<RefreshToken> optional = refreshTokenRepository.findByToken(token);
        optional.ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }

    /**
     * Génère une chaîne aléatoire sécurisée pour le token.
     */
    private String generateSecureToken() {
        byte[] randomBytes = new byte[64];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
