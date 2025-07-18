package com.opossum.token;

import com.opossum.common.exceptions.UnauthorizedException;
import com.opossum.user.User;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    private static final long EXPIRATION_DAYS = 7;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /**
     * Crée et enregistre un nouveau refresh token pour un utilisateur.
     */
    @Transactional
    public String createRefreshToken(User user) {
        // Supprime l’ancien token si existant
        refreshTokenRepository.deleteByUser_Id(user.getId());

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

    /**
     * Vérifie qu’un refresh token est authentique et retourne l'utilisateur s'il est valide.
     */
    public User verifyRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenWithUser(token)
                .orElseThrow(() -> new UnauthorizedException("Token invalide"));

        if (refreshToken.isRevoked() || refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new UnauthorizedException("Token expiré ou révoqué");
        }

        return refreshToken.getUser();
    }

    /**
     * Supprime tous les refresh tokens d'un utilisateur (avant suppression du compte)
     */
    @Transactional
    public void deleteAllForUser(UUID userId) {
        refreshTokenRepository.deleteByUser_Id(userId);
    }
}