package com.opossum.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO utilisé pour mettre à jour les informations de profil de l'utilisateur
 * connecté. Sécurisé : on ne peut pas modifier l'email, le mot de passe, etc.
 */
public class UpdateProfileRequest {

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 50, message = "Le prénom doit contenir entre 2 et 50 caractères")
    private String firstName; // CamelCase cohérent avec le premier fichier

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères")
    private String lastName; // CamelCase cohérent avec le premier fichier

    @NotBlank(message = "L'email est obligatoire")
    @Size(max = 100, message = "L'email ne peut excéder 100 caractères")
    private String email;

    @Size(max = 20, message = "Le numéro de téléphone ne peut excéder 20 caractères")
    private String phone; // facultatif

    @Size(max = 255, message = "L'URL de l'avatar ne peut excéder 255 caractères")
    private String avatarUrl;

    // Constructeur vide requis pour JPA/Jackson
    public UpdateProfileRequest() {
    }

    // Constructeur complet cohérent avec le deuxième fichier
    public UpdateProfileRequest(String firstName, String lastName, String email, String phone, String avatarUrl) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.avatarUrl = avatarUrl;
    }

    // === Getters et Setters ===
    // Version améliorée avec nommage cohérent (CamelCase)
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatar() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
