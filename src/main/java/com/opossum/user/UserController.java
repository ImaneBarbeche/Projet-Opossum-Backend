package com.opossum.user;

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
import com.opossum.user.dto.ChangePasswordRequest;
import com.opossum.user.dto.UpdateProfileRequest;
import com.opossum.user.dto.UserDto;
import com.opossum.user.dto.PublicUserDto;

import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import java.time.Instant;
import java.util.Optional;

@RestController
@RequestMapping("api/v1/users")
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
        UserDto dto = userService.mapToDto(user);
        return ResponseEntity.ok(dto);
    }

 
    /**
     * Mettre à jour son profil (prénom, nom, téléphone)
     */
    @PutMapping("/update-profile")
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        boolean changed = false;
        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            user.setFirstName(request.getFirstName());
            changed = true;
        }
        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            user.setLastName(request.getLastName());
            changed = true;
        }
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            user.setPhone(request.getPhone());
            changed = true;
        }
        if (request.getAvatar() != null && !request.getAvatar().isBlank()) {
            user.setAvatar(request.getAvatar());
            changed = true;
        }
        if (changed) {
            user.setUpdatedAt(Instant.now());
            userRepository.save(user);
        }
        UserDto dto = userService.mapToDto(user);
        return ResponseEntity.ok(
            java.util.Map.of(
                "success", true,
                "data", dto,
                "message", "Profil mis à jour avec succès",
                "timestamp", Instant.now()
            )
        );
    }

    /**
     * Changer son mot de passe (avec vérification de l'ancien)
     */
    @PutMapping("/update-password")
    public ResponseEntity<?> updatePassword(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                java.util.Map.of(
                    "success", false,
                    "error", java.util.Map.of(
                        "code", "UNAUTHORIZED",
                        "message", "Utilisateur non connecté"
                    ),
                    "timestamp", Instant.now()
                )
            );
        }
        // Vérifier l'ancien mot de passe
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                java.util.Map.of(
                    "success", false,
                    "error", java.util.Map.of(
                        "code", "INVALID_CURRENT_PASSWORD",
                        "message", "Mot de passe actuel incorrect"
                    ),
                    "timestamp", Instant.now()
                )
            );
        }
        // Vérifier que le nouveau mot de passe est différent
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                java.util.Map.of(
                    "success", false,
                    "error", java.util.Map.of(
                        "code", "PASSWORD_UNCHANGED",
                        "message", "Le nouveau mot de passe doit être différent de l'actuel"
                    ),
                    "timestamp", Instant.now()
                )
            );
        }
        // Mettre à jour le mot de passe
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
        // Invalider tous les refresh tokens de l'utilisateur
        userService.invalidateAllTokensForUser(user.getId());
        // TODO: Envoyer un email de notification
        return ResponseEntity.ok(
            java.util.Map.of(
                "success", true,
                "message", "Mot de passe modifié avec succès",
                "timestamp", Instant.now()
            )
        );
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



    /**
     * Voir un profil utilisateur (privé si 'me', public sinon)
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserProfile(
            @PathVariable String id,
            @AuthenticationPrincipal User currentUser
    ) {
        if ("me".equalsIgnoreCase(id)) {
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    java.util.Map.of(
                        "success", false,
                        "error", java.util.Map.of(
                            "code", "UNAUTHORIZED",
                            "message", "Token JWT invalide ou expiré"
                        ),
                        "timestamp", Instant.now()
                    )
                );
            }
            if (!currentUser.isActive()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    java.util.Map.of(
                        "success", false,
                        "error", java.util.Map.of(
                            "code", "USER_BLOCKED",
                            "message", "Ce profil n’est pas accessible"
                        ),
                        "timestamp", Instant.now()
                    )
                );
            }
            UserDto dto = userService.mapToDto(currentUser);
            return ResponseEntity.ok(
                java.util.Map.of(
                    "success", true,
                    "data", dto,
                    "timestamp", Instant.now()
                )
            );
        }
        // UUID demandé
        UUID uuid;
        try {
            uuid = UUID.fromString(id);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                java.util.Map.of(
                    "success", false,
                    "error", java.util.Map.of(
                        "code", "USER_NOT_FOUND",
                        "message", "Utilisateur introuvable"
                    ),
                    "timestamp", Instant.now()
                )
            );
        }
        Optional<User> userOpt = userService.getUserById(uuid);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                java.util.Map.of(
                    "success", false,
                    "error", java.util.Map.of(
                        "code", "USER_NOT_FOUND",
                        "message", "Utilisateur introuvable"
                    ),
                    "timestamp", Instant.now()
                )
            );
        }
        User user = userOpt.get();
        if (!user.isActive()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                java.util.Map.of(
                    "success", false,
                    "error", java.util.Map.of(
                        "code", "USER_BLOCKED",
                        "message", "Ce profil n’est pas accessible"
                    ),
                    "timestamp", Instant.now()
                )
            );
        }
        PublicUserDto publicDto = new PublicUserDto(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getAvatar(),
            user.getCreatedAt()
        );
        return ResponseEntity.ok(
            java.util.Map.of(
                "success", true,
                "data", publicDto,
                "timestamp", Instant.now()
            )
        );
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