package com.opossum.user;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.opossum.user.dto.UpdateUserRequest;
import com.opossum.user.dto.UserDto;

import jakarta.transaction.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(UUID id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByConnected(UUID id) {
        return userRepository.findById(id);
    }

    public Optional<User> editPassword(UUID id, String newPassword) {
        return userRepository.findById(id).map(user -> {
            user.setPasswordHash(newPassword);
            return userRepository.save(user);
        });
    }

    public void deleteUser(UUID id) {
        userRepository.deleteById(id);
    }

    @Transactional
    public Optional<User> updateUser(UUID id, UpdateUserRequest dto) {
        return userRepository.findById(id).map(user -> {
            if (dto.getFirstname() != null) {
                user.setFirstname(dto.getFirstname());
            }
            if (dto.getLastname() != null) {
                user.setLastname(dto.getLastname());
            }
            if (dto.getEmail() != null) {
                user.setEmail(dto.getEmail());
            }
            if (dto.getPhone() != null) {
                user.setPhone(dto.getPhone());
            }
            if (dto.getAvatarUrl() != null) {
                user.setAvatarUrl(dto.getAvatarUrl());
            }
            user.setUpdatedAt(Instant.now());
            return userRepository.save(user);
        });
    }

    public boolean verifyEmailToken(String token) {
        Optional<User> userOpt = userRepository.findByEmailVerificationToken(token);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setEmailVerified(true);
            user.setEmailVerificationToken(null);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public UserDto mapToDto(User user) {
        return new UserDto(
                user.getId(),
                user.getFirstname(),
                user.getLastname(),
                user.getEmail(),
                user.getPhone(),
                user.getAvatarUrl(),
                user.getRole(),
                user.isActive(),
                user.isEmailVerified(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getLastLoginAt()
        );

    }

}
