package com.opossum.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO utilisé pour mettre à jour le prénom, nom et téléphone de l'utilisateur connecté.
 * Sécurisé : on ne peut pas modifier l'email, le mot de passe, etc.
 */
public class UpdateProfileRequest {

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 50)
    private String firstName;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 50)
    private String lastName;

    private String phone; // facultatif

    // Constructeur vide requis
    public UpdateProfileRequest() {}

    public UpdateProfileRequest(String firstName, String lastName, String phone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
    }

    // === Getters et Setters ===

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
