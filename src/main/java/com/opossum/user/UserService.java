package com.opossum.user;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opossum.user.dto.UpdateProfileRequest;
import com.opossum.user.dto.UserDto;
import com.opossum.user.UserNotFoundException;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(UUID id) {
        return userRepository.findById(id);
    }

    Transactional
    ic Optional<User> editPassword(UUID id, String newPassword) {
         user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        .setPasswordHash(passwordEncoder.encode(newPassword));
        Repository.save(user);
        rn Optional.ofNullable(user);
    

    @Transactional
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
    }

    /**
     ************ ✨ Windsurf Command ⭐  ************
     */ *
     * Mettre à jour son profil (prénom, nom, téléphone)
     *
     * Si l'email est mis à jour, la vérification de l'email est réinitialisée.
     *
     * @param id Identifiant de l'utilisateur
     * @param dto Informations de profil à mettre à jour
     * @return L'utilisateur mis à jour
     */
     *
     * ***** b01a42b5-e1f0-4999-b6d2-0de03a848ee4  ******
     */
    @Transactional
     *
     User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (dto.getFirstName() != null && !dto.getFirstName().isBlank()) {
            user.setFirstName(dto.getFirstName());
        }

        if (dto.getLastName() != null && !dto.getLastName().isBlank()) {
            user.setLastName(dto.getLastName());
        }

        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            if (!user.getEmail().equals(dto.getEmail())) {
                user.setEmail(dto.getEmail());
                user.setEmailVerified(false); // Réinitialiser la vérification si email changé
            }
        }

        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }

        if (dto.getAvatar() != null) {
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
                user.getLastLogi
                
                