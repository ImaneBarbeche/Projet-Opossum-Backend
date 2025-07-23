package com.opossum.auth;

import com.opossum.user.User;
import com.opossum.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Optional;

@Service
public class EmailVerificationService {
    private final UserRepository userRepository;

    public EmailVerificationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Vérifie le token de vérification d'email et met à jour l'utilisateur si valide.
     * Retourne une Map prête à être utilisée dans la réponse du contrôleur.
     */
    public ResponseEntity<Map<String, Object>> verifyEmail(String token) {
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

        user.setIsEmailVerified(true);
        user.setEmailVerificationToken(null);
        userRepository.save(user);

        response.put("success", true);
        response.put("message", "Email vérifié avec succès. Vous pouvez maintenant vous connecter.");
        return ResponseEntity.ok(response);
    }
}
