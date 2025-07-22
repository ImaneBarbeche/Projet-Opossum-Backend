package com.opossum.user;
import java.util.List;
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
import com.opossum.user.dto.ChangePasswordRequest;
import com.opossum.user.dto.UpdateProfileRequest;
import com.opossum.user.dto.UserDto;
import com.opossum.auth.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("api/v1/users")


public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService, UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userService = userService;
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
        return userService.updateProfile(user, request);
    }

    /**
     * Changer son mot de passe (avec vérification de l'ancien)
     */
    @PutMapping("/update-password")
    public ResponseEntity<?> updatePassword(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        return userService.changePassword(user, request);
    }

    @DeleteMapping("/delete-profile")
    public ResponseEntity<?> deleteProfile(
            @RequestBody DeleteProfileRequest request,
            java.security.Principal principal
    ) {
        return userService.deleteProfile(request, principal);
    }

    /**
     * Voir un profil utilisateur (privé si 'me', public sinon)
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserProfile(
            @PathVariable String id,
            @AuthenticationPrincipal User currentUser
    ) {
        return userService.getUserProfile(id, currentUser);
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