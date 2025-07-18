package com.opossum.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO utilisé pour changer le mot de passe d'un utilisateur. Contient
 * uniquement le champ `newPassword`, minimum 8 caractères.
 */
public class UpdatePasswordRequest {

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String newPassword;

    // Constructeur vide (requis par Spring)
    public UpdatePasswordRequest() {
    }

    public UpdatePasswordRequest(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}