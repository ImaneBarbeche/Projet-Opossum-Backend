package com.opossum.auth;
import com.opossum.auth.dto.AuthResponse;
import com.opossum.auth.dto.LoginRequest;
import com.opossum.auth.dto.RefreshTokenRequest;
import com.opossum.common.exceptions.UnauthorizedException;
import com.opossum.token.RefreshTokenService;
import com.opossum.token.RefreshTokenRepository;
import com.opossum.token.RefreshToken;
import jakarta.validation.Valid;
import java.util.Optional;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import com.opossum.auth.dto.ForgotPasswordRequest;
import com.opossum.auth.dto.ResetPasswordRequest;

import com.opossum.auth.dto.RegisterRequest;

/**
 * Contrôleur REST pour gérer l'authentification : - Inscription (register) -
 * Connexion (login)
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final RefreshTokenRepository refreshTokenRepository;

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final EmailVerificationService emailVerificationService;
    private final PasswordResetService passwordResetService;

    /**
     * Injection de dépendance par constructeur (manuellement). Pas de Lombok,
     * donc on écrit le constructeur à la main.
     */
    public AuthController(AuthService authService, RefreshTokenService refreshTokenService, RefreshTokenRepository refreshTokenRepository, EmailVerificationService emailVerificationService, PasswordResetService passwordResetService) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.emailVerificationService = emailVerificationService;
        this.passwordResetService = passwordResetService;
    }

    /**
     * Route : POST /api/v1/auth/register
     * ➤ Inscrit un nouvel utilisateur
     * ➤ Retourne un message de succès ou d'erreur
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.ok(
                java.util.Map.of(
                    "success", true,
                    "data", response,
                    "timestamp", java.time.Instant.now()
                )
            );
        } catch (org.springframework.web.server.ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(
                java.util.Map.of(
                    "success", false,
                    "error", java.util.Map.of(
                        "code", "REGISTER_ERROR",
                        "message", e.getReason() != null ? e.getReason() : "Erreur lors de l'inscription"
                    ),
                    "timestamp", java.time.Instant.now()
                )
            );
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(
                java.util.Map.of(
                    "success", false,
                    "error", java.util.Map.of(
                        "code", "INTERNAL_ERROR",
                        "message", "Erreur lors de l'inscription: " + ex.getMessage()
                    ),
                    "timestamp", java.time.Instant.now()
                )
            );
        }
    }

    /**
     * Route : POST /api/v1/auth/login ➤ Authentifie un utilisateur existant ➤
     * Retourne les tokens JWT si succès
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            // Cookie sécurisé pour le web (Angular)
            ResponseCookie cookie = ResponseCookie.from("refreshToken", response.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(60 * 60 * 24 * 7) // 7 jours
                .build();
            // Body pour le mobile (React Native)
            return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(java.util.Map.of(
                    "success", true,
                    "data", java.util.Map.of(
                        "accessToken", response.getAccessToken(),
                        "refreshToken", response.getRefreshToken(),
                        "expiresIn", response.getExpiresIn()
                    ),
                    "timestamp", java.time.Instant.now()
                ));
        } catch (org.springframework.web.server.ResponseStatusException e) {
            if (e.getStatusCode() == org.springframework.http.HttpStatus.CONFLICT) {
                return ResponseEntity.status(401).body(
                    java.util.Map.of(
                        "success", false,
                        "error", java.util.Map.of(
                            "code", "INVALID_CREDENTIALS",
                            "message", "Email ou mot de passe incorrect"
                        ),
                        "timestamp", java.time.Instant.now()
                    )
                );
            }
            return ResponseEntity.status(e.getStatusCode()).body(
                java.util.Map.of(
                    "success", false,
                    "error", java.util.Map.of(
                        "code", "INTERNAL_ERROR",
                        "message", e.getReason() != null ? e.getReason() : "Erreur lors de la connexion"
                    ),
                    "timestamp", java.time.Instant.now()
                )
            );
        } catch (com.opossum.common.exceptions.UnauthorizedException e) {
            return ResponseEntity.status(401).body(
                java.util.Map.of(
                    "success", false,
                    "error", java.util.Map.of(
                        "code", "INVALID_CREDENTIALS",
                        "message", e.getMessage()
                    ),
                    "timestamp", java.time.Instant.now()
                )
            );
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(
                java.util.Map.of(
                    "success", false,
                    "error", java.util.Map.of(
                        "code", "INTERNAL_ERROR",
                        "message", "Erreur lors de la connexion: " + ex.getMessage()
                    ),
                    "timestamp", java.time.Instant.now()
                )
            );
        }
    }


    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        if (request.getRefreshToken() == null || request.getRefreshToken().isBlank()) {
            return ResponseEntity.status(401).body(
                Map.of(
                    "success", false,
                    "error", Map.of(
                        "code", "INVALID_REFRESH_TOKEN",
                        "message", "Le champ 'refreshToken' est requis dans le body."
                    ),
                    "timestamp", java.time.Instant.now()
                )
            );
        }
        try {
            AuthResponse response = authService.refreshToken(request.getRefreshToken());
            ResponseCookie cookie = ResponseCookie.from("refreshToken", response.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(60 * 60 * 24 * 7)
                .build();
            Map<String, Object> data = Map.of(
                "accessToken", response.getAccessToken(),
                "refreshToken", response.getRefreshToken(),
                "expiresIn", response.getExpiresIn()
            );
            return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of(
                    "success", true,
                    "data", data,
                    "timestamp", java.time.Instant.now()
                ));
        } catch (UnauthorizedException | org.springframework.web.server.ResponseStatusException e) {
            return ResponseEntity.status(401).body(
                Map.of(
                    "success", false,
                    "error", Map.of(
                        "code", "INVALID_REFRESH_TOKEN",
                        "message", e.getMessage() != null ? e.getMessage() : "Refresh token invalide ou expiré"
                    ),
                    "timestamp", java.time.Instant.now()
                )
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                Map.of(
                    "success", false,
                    "error", Map.of(
                        "code", "INTERNAL_ERROR",
                        "message", "Erreur lors du refresh token: " + e.getMessage()
                    ),
                    "timestamp", java.time.Instant.now()
                )
            );
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        return passwordResetService.forgotPassword(request.getEmail());
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody ResetPasswordRequest request) {
        return passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
    }

    @GetMapping("/verify-email")
    public ResponseEntity<Map<String, Object>> verifyEmail(@RequestParam("token") String token) {
        return emailVerificationService.verifyEmail(token);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null || refreshToken.isBlank()) {
            // On supprime le cookie côté client même si le body est vide
            ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(0)
                .build();
            return ResponseEntity.badRequest()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of(
                    "success", false,
                    "message", "Le champ 'refreshToken' est requis.",
                    "timestamp", java.time.Instant.now()
                ));
        }
        Optional<RefreshToken> optional = refreshTokenRepository.findByToken(refreshToken);
        if (optional.isEmpty() || optional.get().isRevoked() || optional.get().getExpiresAt().isBefore(java.time.Instant.now())) {
            ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(0)
                .build();
            return ResponseEntity.status(400)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of(
                    "success", false,
                    "message", "Token invalide ou déjà révoqué.",
                    "timestamp", java.time.Instant.now()
                ));
        }
        refreshTokenService.revokeToken(refreshToken);
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
            .httpOnly(true)
            .secure(true)
            .sameSite("None")
            .path("/")
            .maxAge(0)
            .build();
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, cookie.toString())
            .body(Map.of(
                "success", true,
                "message", "Déconnexion réussie",
                "timestamp", java.time.Instant.now()
            ));
    }


}
