package com.opossum.auth;

import com.opossum.auth.dto.AuthResponse;
import com.opossum.auth.dto.LoginRequest;
import com.opossum.auth.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST pour gérer l'authentification :
 * - Inscription (register)
 * - Connexion (login)
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * Injection de dépendance par constructeur (manuellement).
     * Pas de Lombok, donc on écrit le constructeur à la main.
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Route : POST /api/v1/auth/login
     * ➤ Authentifie un utilisateur existant
     * ➤ Retourne les tokens JWT si succès
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        // Délègue à AuthService pour valider les identifiants
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response); // 200 OK
    }

    /**
     * Route : POST /api/v1/auth/register
     * ➤ Crée un nouveau compte utilisateur
     * ➤ Retourne les tokens JWT + user info
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response); // 201 Created
    }
}
