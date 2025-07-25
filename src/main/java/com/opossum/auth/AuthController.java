package com.opossum.auth;
import com.opossum.auth.dto.AuthResponse;
import com.opossum.auth.dto.LoginRequest;
import com.opossum.auth.dto.RefreshTokenRequest;
import com.opossum.common.exceptions.UnauthorizedException;
import jakarta.validation.Valid;
import java.util.Map;
import com.opossum.common.utils.ResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseCookie;
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
    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;
    private final PasswordResetService passwordResetService;
    private final LogoutService logoutService;

    /**
     * Route : GET /api/v1/auth
     * ➤ Vérifie la session à partir du cookie (refreshToken)
     * ➤ Retourne l'utilisateur et le statut d'authentification
     */
    @GetMapping("")
    public ResponseEntity<?> checkSession(@CookieValue(value = "refreshToken", required = false) String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseUtil.success(Map.of(
                "authenticated", false,
                "user", null
            ));
        }
        try {
            AuthResponse response = authService.refreshToken(refreshToken);
            // Ici, on suppose que AuthResponse contient l'utilisateur (à adapter si besoin)
            return ResponseUtil.success(Map.of(
                "authenticated", true,
                "user", response.getUserDTO() 
            ));
        } catch (Exception e) {
            return ResponseUtil.success(Map.of(
                "authenticated", false,
                "user", null
            ));
        }
    }

    /**
     * Injection de dépendance par constructeur (manuellement). Pas de Lombok,
     * donc on écrit le constructeur à la main.
     */
    public AuthController(AuthService authService, EmailVerificationService emailVerificationService, PasswordResetService passwordResetService, LogoutService logoutService) {
        this.authService = authService;
        this.emailVerificationService = emailVerificationService;
        this.passwordResetService = passwordResetService;
        this.logoutService = logoutService;
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
            return ResponseUtil.success(response);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            return ResponseUtil.error(e.getStatusCode().value(), "REGISTER_ERROR", e.getReason() != null ? e.getReason() : "Erreur lors de l'inscription");
        } catch (Exception ex) {
            return ResponseUtil.error(500, "INTERNAL_ERROR", "Erreur lors de l'inscription: " + ex.getMessage());
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
            return ResponseUtil.successWithCookie(data, cookie.toString());
        } catch (org.springframework.web.server.ResponseStatusException e) {
            if (e.getStatusCode() == org.springframework.http.HttpStatus.CONFLICT) {
                return ResponseUtil.error(401, "INVALID_CREDENTIALS", "Email ou mot de passe incorrect");
            }
            return ResponseUtil.error(e.getStatusCode().value(), "INTERNAL_ERROR", e.getReason() != null ? e.getReason() : "Erreur lors de la connexion");
        } catch (com.opossum.common.exceptions.UnauthorizedException e) {
            return ResponseUtil.error(401, "INVALID_CREDENTIALS", e.getMessage());
        } catch (Exception ex) {
            return ResponseUtil.error(500, "INTERNAL_ERROR", "Erreur lors de la connexion: " + ex.getMessage());
        }
    }


    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        if (request.getRefreshToken() == null || request.getRefreshToken().isBlank()) {
            return ResponseUtil.error(401, "INVALID_REFRESH_TOKEN", "Le champ 'refreshToken' est requis dans le body.");
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
            return ResponseUtil.successWithCookie(data, cookie.toString());
        } catch (UnauthorizedException | org.springframework.web.server.ResponseStatusException e) {
            return ResponseUtil.error(401, "INVALID_REFRESH_TOKEN", e.getMessage() != null ? e.getMessage() : "Refresh token invalide ou expiré");
        } catch (Exception e) {
            return ResponseUtil.error(500, "INTERNAL_ERROR", "Erreur lors du refresh token: " + e.getMessage());
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
        return logoutService.logout(refreshToken);
    }

}