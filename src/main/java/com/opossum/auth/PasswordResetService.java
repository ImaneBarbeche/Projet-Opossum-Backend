package com.opossum.auth;
import com.opossum.user.User;
import com.opossum.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetService(UserRepository userRepository, EmailService emailService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    public ResponseEntity<Map<String, Object>> forgotPassword(String email) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", Instant.now());
        if (email == null || email.isBlank() || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            response.put("success", false);
            response.put("error", Map.of(
                "code", "INVALID_EMAIL",
                "message", "Format d'email invalide"
            ));
            return ResponseEntity.status(400).body(response);
        }
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            // Pour ne pas divulguer si un compte existe ou pas : on ne dit rien
            response.put("success", true);
            response.put("message", "Email de réinitialisation envoyé");
            return ResponseEntity.ok(response);
        }
        User user = optionalUser.get();
        String resetToken = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plus(Duration.ofMinutes(30));
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetExpiresAt(expiresAt);
        userRepository.save(user);
        emailService.sendResetPasswordEmail(user.getEmail(), resetToken);
        response.put("success", true);
        response.put("message", "Email de réinitialisation envoyé");
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Map<String, Object>> resetPassword(String token, String newPassword) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", Instant.now());
        if (newPassword == null || newPassword.length() < 8) {
            response.put("success", false);
            response.put("error", Map.of(
                "code", "INVALID_PASSWORD",
                "message", "Le mot de passe doit contenir au moins 8 caractères"
            ));
            return ResponseEntity.status(400).body(response);
        }
        Optional<User> optionalUser = userRepository.findByPasswordResetToken(token);
        if (optionalUser.isEmpty()) {
            response.put("success", false);
            response.put("error", Map.of(
                "code", "INVALID_OR_EXPIRED_TOKEN",
                "message", "Le lien de réinitialisation est invalide ou expiré"
            ));
            return ResponseEntity.status(400).body(response);
        }
        User user = optionalUser.get();
        Instant now = Instant.now();
        Instant expiresAt = user.getPasswordResetExpiresAt();
        if (expiresAt == null || expiresAt.isBefore(now)) {
            response.put("success", false);
            response.put("error", Map.of(
                "code", "INVALID_OR_EXPIRED_TOKEN",
                "message", "Le lien de réinitialisation est invalide ou expiré"
            ));
            return ResponseEntity.status(400).body(response);
        }
        String hashedPassword = passwordEncoder.encode(newPassword);
        user.setPasswordHash(hashedPassword);
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiresAt(null);
        userRepository.save(user);
        response.put("success", true);
        response.put("message", "Mot de passe réinitialisé avec succès");
        return ResponseEntity.ok(response);
    }
}
