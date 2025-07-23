package com.opossum.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Requête envoyée pour créer un nouveau compte utilisateur.
 */
public class RegisterRequest {

    // ==== Champs ====

    /**
     * Email de l'utilisateur
     * - Obligatoire
     * - Format email valide requis
     */
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    /**
     * Mot de passe
     * - Obligatoire
     * - Minimum 8 caractères
     */
    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String password;

    /**
     * Prénom
     * - Obligatoire
     * - Entre 2 et 50 caractères
     */
    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 50, message = "Le prénom doit contenir entre 2 et 50 caractères")
    private String firstName;

    /**
     * Nom
     * - Obligatoire
     * - Entre 2 et 50 caractères
     */
    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères")
    private String lastName;

    /**
     * Numéro de téléphone (optionnel)
     * - Peut être vide
     * - Format libre (contrôle à ajouter si besoin)
     */
    private String phone;


    /**
     * Avatar (optionnel)
     * - Peut être null
     * - URL directe vers une image
     * - 500 caractères max
     */
    @Size(max = 500, message = "L'avatar ne peut excéder 500 caractères")
    private String avatar;

    // ==== Constructeurs ====

    public RegisterRequest() {
        // Obligatoire pour la désérialisation automatique
    }

    public RegisterRequest(String email, String password, String firstName, String lastName, String phone, String avatar) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.avatar = avatar;
    }

    // ==== Getters et Setters ====

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

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

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}