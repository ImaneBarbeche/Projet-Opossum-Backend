
package com.opossum.user;

import com.opossum.user.dto.UserProfileResponse;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

// import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opossum.token.RefreshTokenService;
import com.opossum.user.dto.UpdateProfileRequest;
import com.opossum.user.dto.UserDto;
// import com.opossum.user.UserNotFoundException;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    public UserProfileResponse mapToUserProfileResponse(User user) {
        if (user == null) {
            return null;
        }
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getAvatar(),
                user.getRole() != null ? user.getRole().name() : null
        );
    }

    // @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
    }

    /**
     * Invalide tous les refresh tokens d'un utilisateur (après changement de mot de passe)
     */
    @Transactional
    public void invalidateAllTokensForUser(UUID userId) {
        refreshTokenService.deleteAllForUser(userId);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(UUID id) {
        return userRepository.findById(id);
    }

    @Transactional
    public User updatePassword(User user, String newPassword) {
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(Instant.now());
        return userRepository.save(user);
    }

    @Transactional
    public void deleteProfile(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        // Delete all refresh tokens for this user first
        refreshTokenService.deleteAllForUser(id);
        userRepository.deleteById(id);
    }

    @Transactional
    public User updateProfile(User user, UpdateProfileRequest dto) {

        if (dto.getFirstName() != null && !dto.getFirstName().isBlank()) {
            user.setFirstName(dto.getFirstName());
        }
        if (dto.getLastName() != null && !dto.getLastName().isBlank()) {
            user.setLastName(dto.getLastName());
        }

        if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
            user.setPhone(dto.getPhone());
        }

        if (dto.getAvatar() != null && !dto.getAvatar().isBlank()) {
            user.setAvatar(dto.getAvatar());
        }

        user.setUpdatedAt(Instant.now());
        return userRepository.save(user);
    }

    @Transactional
    public boolean verifyEmailToken(String token) {
        Optional<User> userOpt = userRepository.findByEmailVerificationToken(token);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setEmailVerified(true);
            user.setEmailVerificationToken(null);
            user.setUpdatedAt(Instant.now());
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Transactional
    public void updateLastLogin(UUID userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setLastLoginAt(Instant.now());
            userRepository.save(user);
        });
    }

    public UserDto mapToDto(User user) {
        if (user == null) {
            return null;
        }
        return new UserDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getAvatar(),
                user.getRole(),
                user.isActive(),
                user.isEmailVerified(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getLastLoginAt()
        );
    }
}
