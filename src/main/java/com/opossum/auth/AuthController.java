package com.opossum.auth;

import com.opossum.auth.dto.AuthResponse;
import com.opossum.auth.dto.LoginRequest;
import com.opossum.auth.dto.RefreshTokenRequest;
import com.opossum.auth.dto.RegisterRequest;
import com.opossum.common.exceptions.UnauthorizedException;
import com.opossum.user.User;
import com.opossum.user.UserRepository;
import com.opossum.token.RefreshTokenService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.Optional;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.opossum.auth.dto.ForgotPasswordRequest;
import java.util.HashMap;
import java.util.Map;
import com.opossum.auth.dto.ResetPasswordRequest;
import com.opossum.token.RefreshTokenService;

/**
 * Contrôleur REST pour gérer l'authentification : - Inscription (register) -
 * Connexion (login)
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;

    /**
     * Injection de dépendance par constructeur (manuellement). Pas de Lombok,
     * donc on écrit le constructeur à la main.
     */
    public AuthController(AuthService authService, UserRepository userRepository, RefreshTokenService refreshTokenService) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.refreshTokenService = refreshTokenService;
        System.out.println(">>> AuthController instancié !");
    }

    /**
     * Route : POST /api/v1/auth/login ➤ Authentifie un utilisateur existant ➤
     * Retourne les tokens JWT si succès
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        System.out.println(">>> Reçu : " + request.getEmail() + " / " + request.getPassword());
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * Route : POST /api/v1/auth/register ➤ Crée un nouveau compte utilisateur ➤
     * Retourne les tokens JWT + user info
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        System.out.println(">>> Reçu REGISTER: " + request.getEmail() + " / " + request.getPassword());

        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response); // 201 Created
    }

    @PostMapping("/refresh-token")
    public AuthResponse refreshToken(@RequestBody RefreshTokenRequest request) {
        if (request.getRefreshToken() == null || request.getRefreshToken().isBlank()) {
            throw new UnauthorizedException("Le champ 'refreshToken' est requis dans le body.");
        }

        return authService.refreshToken(request.getRefreshToken());
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Si un compte existe, un email de réinitialisation a été envoyé.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<java.util.Map<String, Object>> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        authService.resetPassword(request.getToken(), request.getNewPassword());
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("success", true);
        response.put("message", "Mot de passe réinitialisé avec succès.");
        return ResponseEntity.ok(response);
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

@DeleteMapping("/logout")
public ResponseEntity<?> logout(@AuthenticationPrincipal User user) {
    refreshTokenService.deleteAllForUser(user.getId());

    return ResponseEntity.ok().body(
        Map.of("message", "Déconnexion réussie. Le refresh token a été supprimé.")
    );
}


}
