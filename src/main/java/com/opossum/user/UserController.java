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
import java.util.Optional;

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

 
    /**
     * Mettre à jour son profil (prénom, nom, téléphone)
     */
    @PutMapping("/update-profile")
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
    @PutMapping("/update-password")
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
     * <p>
     * Cette méthode permet à l'utilisateur connecté de supprimer son compte après plusieurs vérifications :
     * - Authentification
     * - Existence du compte
     * - Mot de passe fourni et correct
     * - Confirmation explicite de la suppression
     * - Compte actif
     * <p>
     * Si toutes les conditions sont réunies, le compte est supprimé définitivement.
     * Les erreurs sont retournées avec un code et un message explicite pour le frontend.
     */
    @DeleteMapping("/delete-profile")
    public ResponseEntity<?> deleteProfile(
            @RequestBody DeleteProfileRequest request,
            java.security.Principal principal
    ) {
        // 1. Vérifier que l'utilisateur est bien authentifié
        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new DeleteProfileResponse(false, new ErrorResponse("UNAUTHORIZED", "Utilisateur non authentifié"), Instant.now())
            );
        }

        // 2. Récupérer l'utilisateur en base via son email
        Optional<User> userOpt = userRepository.findByEmail(principal.getName());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new DeleteProfileResponse(false, new ErrorResponse("USER_NOT_FOUND", "Utilisateur introuvable"), Instant.now())
            );
        }
        User user = userOpt.get();

        // 3. Vérifier que le mot de passe est bien fourni
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new DeleteProfileResponse(false, new ErrorResponse("INVALID_PASSWORD", "Mot de passe requis"), Instant.now())
            );
        }

        // 4. Vérifier que l'utilisateur a explicitement confirmé la suppression (ex : case à cocher côté frontend)
        if (!request.isConfirmDeletion()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new DeleteProfileResponse(false, new ErrorResponse("CONFIRMATION_REQUIRED", "Confirmation de suppression requise"), Instant.now())
            );
        }

        // 5. Vérifier que le mot de passe fourni est correct
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new DeleteProfileResponse(false, new ErrorResponse("INVALID_PASSWORD", "Mot de passe incorrect"), Instant.now())
            );
        }

        // 6. Vérifier que le compte est actif
        if (!user.isActive()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new DeleteProfileResponse(false, new ErrorResponse("USER_INACTIVE", "Utilisateur inactif"), Instant.now())
            );
        }

        // 7. Suppression des données utilisateur (et potentiellement des tokens associés)
        userService.deleteUser(user.getId());
        // TODO: Supprimer les refresh tokens associés, envoyer email de confirmation

        // 8. Retourner une réponse de succès
        return ResponseEntity.ok(
            new DeleteProfileResponse(true, null, Instant.now(), "Compte supprimé définitivement")
        );
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