package com.opossum.auth;
import com.opossum.token.RefreshTokenRepository;
import com.opossum.token.RefreshTokenService;
import com.opossum.token.RefreshToken;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class LogoutService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenService refreshTokenService;

    public LogoutService(RefreshTokenRepository refreshTokenRepository, RefreshTokenService refreshTokenService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenService = refreshTokenService;
    }

    public ResponseEntity<Map<String, Object>> logout(String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
            .httpOnly(true)
            .secure(true)
            .sameSite("None")
            .path("/")
            .maxAge(0)
            .build();
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest()
                .header("Set-Cookie", cookie.toString())
                .body(Map.of(
                    "success", false,
                    "message", "Le champ 'refreshToken' est requis.",
                    "timestamp", Instant.now()
                ));
        }
        Optional<RefreshToken> optional = refreshTokenRepository.findByToken(refreshToken);
        if (optional.isEmpty() || optional.get().isRevoked() || optional.get().getExpiresAt().isBefore(Instant.now())) {
            return ResponseEntity.status(400)
                .header("Set-Cookie", cookie.toString())
                .body(Map.of(
                    "success", false,
                    "message", "Token invalide ou déjà révoqué.",
                    "timestamp", Instant.now()
                ));
        }
        refreshTokenService.revokeToken(refreshToken);
        return ResponseEntity.ok()
            .header("Set-Cookie", cookie.toString())
            .body(Map.of(
                "success", true,
                "message", "Déconnexion réussie",
                "timestamp", Instant.now()
            ));
    }
}
