package com.opossum.user.dto;

import jakarta.validation.constraints.Size;

/**
 * DTO utilisé pour mettre à jour les informations de profil de l'utilisateur
 * connecté. Sécurisé : on ne peut pas modifier l'email, le mot de passe, etc.
 */
public class UpdateProfileRequest {
    @Size(min = 2, max = 50, message = "Le prénom doit contenir entre 2 et 50 caractères")
    private String firstName;

    @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères")
    private String lastName;

    @Size(max = 20, message = "Le numéro de téléphone ne peut excéder 20 caractères")
    private String phone;

    @Size(max = 255, message = "L'URL de l'avatar ne peut excéder 255 caractères")
    private String avatar;

    public UpdateProfileRequest() {}

    public UpdateProfileRequest(String firstName, String lastName, String phone, String avatar) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.avatar = avatar;
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
}