package com.opossum.auth;
import com.opossum.auth.dto.AuthResponse;
import com.opossum.auth.dto.LoginRequest;
import com.opossum.auth.dto.RegisterRequest;
import com.opossum.common.exceptions.UnauthorizedException;
import com.opossum.user.User;
import com.opossum.user.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.opossum.auth.dto.ForgotPasswordRequest;
import com.opossum.auth.dto.ResetPasswordRequest;
/**
 * Contrôleur REST pour gérer l'authentification : - Inscription (register) -
 * Connexion (login)
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    /**
     * Injection de dépendance par constructeur (manuellement). Pas de Lombok,
     * donc on écrit le constructeur à la main.
     */
    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
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
public ResponseEntity<AuthResponse> refreshToken(HttpServletRequest request) {
    String authHeader = request.getHeader("Authorization");

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        throw new UnauthorizedException("Token manquant");
    }

    String refreshToken = authHeader.substring(7);
    AuthResponse response = authService.refreshToken(refreshToken);
    return ResponseEntity.ok(response);
}

@PostMapping("/forgot-password")
public ResponseEntity<Object> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
    // Appel du service (ne divulgue pas si l'email existe ou non)
    authService.forgotPassword(request.getEmail());
    // Réponse générique
    return ResponseEntity.ok(
        java.util.Map.of(
            "success", true,
            "message", "Email de réinitialisation envoyé",
            "timestamp", java.time.Instant.now().toString()
        )
    );
}

@PostMapping("/reset-password")
public ResponseEntity<Object> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
    // Validation du mot de passe
    if (request.getNewPassword() == null || request.getNewPassword().length() < 8) {
        return ResponseEntity.badRequest().body(
            java.util.Map.of(
                "success", false,
                "error", java.util.Map.of(
                    "code", "INVALID_PASSWORD",
                    "message", "Le mot de passe doit contenir au moins 8 caractères"
                ),
                "timestamp", java.time.Instant.now().toString()
            )
        );
    }
    try {
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(
            java.util.Map.of(
                "success", true,
                "message", "Mot de passe réinitialisé avec succès",
                "timestamp", java.time.Instant.now().toString()
            )
        );
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(
            java.util.Map.of(
                "success", false,
                "error", java.util.Map.of(
                    "code", "INVALID_OR_EXPIRED_TOKEN",
                    "message", "Le lien de réinitialisation est invalide ou expiré"
                ),
                "timestamp", java.time.Instant.now().toString()
            )
        );
    }
}

@GetMapping("/verify-email")
public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
    Optional<User> optionalUser = userRepository.findByEmailVerificationToken(token);

    if (optionalUser.isEmpty()) {
        return ResponseEntity.badRequest().body("Lien de vérification invalide ou expiré.");
    }

    User user = optionalUser.get();
    user.setIsEmailVerified(true);
    user.setEmailVerificationToken(null); // on supprime le token pour qu'il ne soit plus réutilisable
    userRepository.save(user);

    return ResponseEntity.ok("Email vérifié avec succès !");
}

}
