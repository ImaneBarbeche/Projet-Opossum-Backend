package com.opossum.user;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import com.opossum.user.dto.DeleteProfileRequest;
import com.opossum.user.dto.DeleteProfileResponse;
import com.opossum.user.dto.ErrorResponse;
import com.opossum.user.dto.UpdatePasswordRequest;
import com.opossum.user.dto.UpdateProfileRequest;
import com.opossum.user.dto.UserDto;

import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import java.time.Instant;

@RestController
@RequestMapping("api/v1/user")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @PostConstruct
public void init() {
    System.out.println(">>> UserController instancié !");
}
  
    /**
     * Voir les infos de son propre profil
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non connecté.");
        }

        return ResponseEntity.ok(
                new UserProfileResponse(
                        user.getId(),
                        user.getEmail(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getPhone(),
                        user.getRole()
                )
        );
    }

@PutMapping("/me")
public ResponseEntity<String> updateMyProfile(@AuthenticationPrincipal User user,
                                              @Valid @RequestBody UpdateProfileRequest request) {
    if (user == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non connecté.");
    }

    user.setFirstName(request.getFirstName());
    user.setLastName(request.getLastName());
    user.setPhone(request.getPhone());
    user.setAvatar(request.getAvatar());

    userRepository.save(user);

    return ResponseEntity.ok("✅ Profil mis à jour avec succès !");
}



    @PutMapping("/edit-password")
    public ResponseEntity<UserDto> editPassword(@PathVariable UUID id, @RequestBody String newPassword) {
        Optional<User> userOptional = userService.editPassword(id, newPassword);
        return userOptional.map(userService::mapToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Mettre à jour son profil (prénom, nom, téléphone)
     */
    @PutMapping("/edit")
    public ResponseEntity<User> updateProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setUpdatedAt(Instant.now());

        User updated = userRepository.save(user);
        return ResponseEntity.ok(updated);
    }

    /**
     * Changer son mot de passe
     */
    @PutMapping("/me/password")
    public ResponseEntity<Void> updatePassword(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdatePasswordRequest request
    ) {
        // Tu peux ajouter ici une vérification de l'ancien mot de passe
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    /**
     * Supprimer définitivement son propre compte
     */
    @DeleteMapping("/deleteProfile")
    public ResponseEntity<?> deleteProfile(
            @RequestBody DeleteProfileRequest request,
            java.security.Principal principal
    ) {
        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new DeleteProfileResponse(false, new ErrorResponse("UNAUTHORIZED", "Utilisateur non authentifié"), Instant.now())
            );
        }
        Optional<User> userOpt = userRepository.findByEmail(principal.getName());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new DeleteProfileResponse(false, new ErrorResponse("USER_NOT_FOUND", "Utilisateur introuvable"), Instant.now())
            );
        }
        User user = userOpt.get();
        // Validation : mot de passe requis
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new DeleteProfileResponse(false, new ErrorResponse("INVALID_PASSWORD", "Mot de passe requis"), Instant.now())
            );
        }
        // Validation : confirmation requise
        if (!request.isConfirmDeletion()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new DeleteProfileResponse(false, new ErrorResponse("CONFIRMATION_REQUIRED", "Confirmation de suppression requise"), Instant.now())
            );
        }
        // Vérification du mot de passe
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new DeleteProfileResponse(false, new ErrorResponse("INVALID_PASSWORD", "Mot de passe incorrect"), Instant.now())
            );
        }
        // Vérification utilisateur actif
        if (!user.isActive()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new DeleteProfileResponse(false, new ErrorResponse("USER_INACTIVE", "Utilisateur inactif"), Instant.now())
            );
        }
        // Suppression des données utilisateur
        userService.deleteUser(user.getId());
        // TODO: Supprimer les refresh tokens associés, envoyer email de confirmation
        return ResponseEntity.ok(
            new DeleteProfileResponse(true, null, Instant.now(), "Compte supprimé définitivement")
        );
    }

    @GetMapping("/auth/verify-email/{token}")
    public ResponseEntity<Void> verifyEmail(@PathVariable String token) {
         System.out.println(">>> Vérification token reçu : " + token);
        if (userService.verifyEmailToken(token)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/profiles/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable UUID id) {
        return userService.getUserById(id)
                .map(userService::mapToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<User> users = userService.getAllUsers()
                .stream()
                .collect(Collectors.toList());
        List<UserDto> userDtos = users.stream()
                .map(userService::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDtos);
    }

}