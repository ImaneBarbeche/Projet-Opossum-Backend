package com.opossum.auth.dto;

import java.util.UUID;

/**
 * DTO simple pour exposer les infos utilisateur côté frontend
 */
public class UserDTO {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String role;

    public UserDTO(UUID id, String email, String firstName, String lastName, String role) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }

    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getRole() { return role; }
}
