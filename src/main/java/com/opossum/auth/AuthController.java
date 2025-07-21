package com.opossum.auth;

import com.opossum.auth.dto.AuthResponse;
import com.opossum.auth.dto.LoginRequest;
import com.opossum.auth.dto.RefreshTokenRequest;
import com.opossum.auth.dto.RegisterRequest;
import com.opossum.common.exceptions.UnauthorizedException;
import com.opossum.user.User;
import com.opossum.user.UserRepository;
import com.opossum.token.RefreshTokenService;
import com.opossum.token.RefreshTokenRepository;
import com.opossum.token.RefreshToken;
import jakarta.validation.Valid;

import java.util.Optional;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.opossum.auth.dto.ForgotPasswordRequest;
import java.util.HashMap;
import com.opossum.auth.dto.ResetPasswordRequest;

/**
 * Contrôleur REST pour gérer l'authentification : - Inscription (register) -
 * Connexion (login)
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final RefreshTokenRepository refreshTokenRepository;

    private final AuthService authService;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;

    /**
     * Injection de dépendance par constructeur (manuellement). Pas de Lombok,
     * donc on écrit le constructeur à la main.
     */
    public AuthController(AuthService authService, UserRepository userRepository, RefreshTokenService refreshTokenService, RefreshTokenRepository refreshTokenRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.refreshTokenService = refreshTokenService;
        this.refreshTokenRepository = refreshTokenRepository;
        System.out.println(">>> AuthController instancié !");
    }

    /**
     * Route : POST /api/v1/auth/login ➤ Authentifie un utilisateur existant ➤
     * Retourne les tokens JWT si succès
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        System.out.println(">>> Reçu : " + request.getEmail() + " / " + request.getPassword());
        try {
            AuthResponse response = authService.login(request);
            // Mettre à jour lastLoginAt
            Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
            userOpt.ifPresent(user -> {
                user.setLastLoginAt(java.time.Instant.now());
                userRepository.save(user);
            });
            // Structure de réponse conforme à la spec
            java.util.Map<String, Object> userMap = new java.util.HashMap<>();
            userMap.put("id", response.getId());
            userMap.put("email", response.getEmail());
            userMap.put("firstName", response.getFirstName());
            userMap.put("lastName", response.getLastName());
            userMap.put("avatar", userOpt.map(User::getAvatar).orElse(null));
            userMap.put("role", response.getRole());

            java.util.Map<String, Object> tokensMap = new java.util.HashMap<>();
            tokensMap.put("accessToken", response.getAccessToken());
            tokensMap.put("refreshToken", response.getRefreshToken());
            tokensMap.put("expiresIn", response.getExpiresIn());

            java.util.Map<String, Object> data = java.util.Map.of(
                "user", userMap,
                "tokens", tokensMap
            );

            return ResponseEntity.ok(
                java.util.Map.of(
                    "success", true,
                    "data", data,
                    "message", "Connexion réussie",
                    "timestamp", java.time.Instant.now()
                )
            );
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

    /**
     * Route : POST /api/v1/auth/register ➤ Crée un nouveau compte utilisateur ➤
     * Retourne les tokens JWT + user info
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        System.out.println(">>> Reçu REGISTER: " + request.getEmail() + " / " + request.getPassword());

        try {
            AuthResponse response = authService.register(request);
            // Enveloppe la réponse selon la spec
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("id", response.getId());
            data.put("email", response.getEmail());
            data.put("firstName", response.getFirstName());
            data.put("lastName", response.getLastName());
            data.put("avatar", request.getAvatar());
            data.put("isEmailVerified", false);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                java.util.Map.of(
                    "success", true,
                    "data", data,
                    "message", "Compte créé avec succès. Vérifiez votre email.",
                    "timestamp", java.time.Instant.now()
                )
            );
        } catch (org.springframework.web.server.ResponseStatusException e) {
            // Gestion des erreurs de validation (email déjà utilisé, etc)
            return ResponseEntity.status(e.getStatusCode()).body(
                java.util.Map.of(
                    "success", false,
                    "error", java.util.Map.of(
                        "code", e.getStatusCode() == HttpStatus.CONFLICT ? "VALIDATION_ERROR" : "INTERNAL_ERROR",
                        "message", e.getReason() != null ? e.getReason() : "Erreur lors de la création du compte"
                    ),
                    "timestamp", java.time.Instant.now()
                )
            );
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                java.util.Map.of(
                    "success", false,
                    "error", java.util.Map.of(
                        "code", "INTERNAL_ERROR",
                        "message", "Erreur lors de la création du compte: " + ex.getMessage()
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
            Map<String, Object> data = Map.of(
                "accessToken", response.getAccessToken(),
                "refreshToken", response.getRefreshToken(),
                "expiresIn", response.getExpiresIn()
            );
            return ResponseEntity.ok(
                Map.of(
                    "success", true,
                    "data", data,
                    "timestamp", java.time.Instant.now()
                )
            );
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
        String email = request.getEmail();
        Map<String, Object> response = new HashMap<>();
        // Validation du format d'email
        if (email == null || email.isBlank() || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            response.put("success", false);
            response.put("error", Map.of(
                "code", "INVALID_EMAIL",
                "message", "Format d'email invalide"
            ));
            response.put("timestamp", java.time.Instant.now());
            return ResponseEntity.status(400).body(response);
        }
        // Appel du service (gère la logique et la sécurité)
        authService.forgotPassword(email);
        response.put("success", true);
        response.put("message", "Email de réinitialisation envoyé");
        response.put("timestamp", java.time.Instant.now());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody ResetPasswordRequest request) {
        Map<String, Object> response = new HashMap<>();
        String token = request.getToken();
        String newPassword = request.getNewPassword();
        // Validation du mot de passe
        if (newPassword == null || newPassword.length() < 8) {
            response.put("success", false);
            response.put("error", Map.of(
                "code", "INVALID_PASSWORD",
                "message", "Le mot de passe doit contenir au moins 8 caractères"
            ));
            response.put("timestamp", java.time.Instant.now());
            return ResponseEntity.status(400).body(response);
        }
        try {
            authService.resetPassword(token, newPassword);
            response.put("success", true);
            response.put("message", "Mot de passe réinitialisé avec succès");
            response.put("timestamp", java.time.Instant.now());
            return ResponseEntity.ok(response);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            response.put("success", false);
            response.put("error", Map.of(
                "code", "INVALID_OR_EXPIRED_TOKEN",
                "message", "Le lien de réinitialisation est invalide ou expiré"
            ));
            response.put("timestamp", java.time.Instant.now());
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", Map.of(
                "code", "INTERNAL_ERROR",
                "message", "Erreur lors de la réinitialisation: " + e.getMessage()
            ));
            response.put("timestamp", java.time.Instant.now());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/verify-email")
    public ResponseEntity<Map<String, Object>> verifyEmail(@RequestParam("token") String token) {
        Optional<User> optionalUser = userRepository.findByEmailVerificationToken(token);

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("timestamp", java.time.Instant.now());

        if (optionalUser.isEmpty()) {
            response.put("success", false);
            response.put("error", Map.of(
                "code", "INVALID_VERIFICATION_TOKEN",
                "message", "Token de vérification invalide ou expiré"
            ));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        User user = optionalUser.get();
        if (user.isEmailVerified()) {
            response.put("success", false);
            response.put("error", Map.of(
                "code", "EMAIL_ALREADY_VERIFIED",
                "message", "Cet email est déjà vérifié"
            ));
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        System.out.println(">>> [verifyEmail] Avant update: isEmailVerified=" + user.isEmailVerified());
        user.setIsEmailVerified(true);
        user.setEmailVerificationToken(null); // on supprime le token pour qu'il ne soit plus réutilisable
        userRepository.save(user);
        System.out.println(">>> [verifyEmail] Après update: isEmailVerified=" + user.isEmailVerified());

        response.put("success", true);
        response.put("message", "Email vérifié avec succès. Vous pouvez maintenant vous connecter.");
        return ResponseEntity.ok(response);
    }

@PostMapping("/logout")
public ResponseEntity<Map<String, Object>> logout(@RequestBody Map<String, String> body) {
    String refreshToken = body.get("refreshToken");
    if (refreshToken == null || refreshToken.isBlank()) {
        return ResponseEntity.badRequest().body(Map.of(
            "success", false,
            "message", "Le champ 'refreshToken' est requis.",
            "timestamp", java.time.Instant.now()
        ));
    }
    Optional<RefreshToken> optional = refreshTokenRepository.findByToken(refreshToken);
    if (optional.isEmpty() || optional.get().isRevoked() || optional.get().getExpiresAt().isBefore(java.time.Instant.now())) {
        return ResponseEntity.status(400).body(Map.of(
            "success", false,
            "message", "Token invalide ou déjà révoqué.",
            "timestamp", java.time.Instant.now()
        ));
    }
    refreshTokenService.revokeToken(refreshToken);
    return ResponseEntity.ok(Map.of(
        "success", true,
        "message", "Déconnexion réussie",
        "timestamp", java.time.Instant.now()
    ));
}


}
