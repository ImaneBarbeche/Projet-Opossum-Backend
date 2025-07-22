package com.opossum.user;

import com.opossum.user.dto.UserDto;

public class UserMapper {
    private UserMapper() {}

    public static UserDto mapToDto(User user) {
        if (user == null) {
            return null;
        }
        return new UserDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getAvatar(), // avatarUrl = avatar
                user.getRole() != null ? user.getRole().name() : null, // enum to string
                user.isActive(),
                user.isEmailVerified(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getLastLoginAt()
        );
    }
}
