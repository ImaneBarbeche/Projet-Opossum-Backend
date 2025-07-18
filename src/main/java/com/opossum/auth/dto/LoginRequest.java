package com.opossum.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Représente la requête envoyée par l'utilisateur
 * lorsqu'il se connecte via l'API.
 */
public class LoginRequest {

    /**
     * Email de l'utilisateur
     * - doit être non vide
     * - doit avoir un format email valide
     */
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    /**
     * Mot de passe saisi
     * - doit être non vide
     */
    @NotBlank(message = "Le mot de passe est obligatoire")
    private String password;

    // ==== Constructeur vide pour la désérialisation automatique ====
    public LoginRequest() {
    }

    // ==== Constructeur pratique pour les tests ou manuellement ====
    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
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
}