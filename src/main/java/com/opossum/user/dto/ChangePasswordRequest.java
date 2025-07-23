
// Package contenant les DTO liés à l'utilisateur
package com.opossum.user.dto;


// Importation des annotations de validation pour garantir l'intégrité des données reçues
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


// DTO utilisé pour transporter les informations lors d'une demande de changement de mot de passe
public class ChangePasswordRequest {

    // Mot de passe actuel de l'utilisateur (obligatoire)
    @NotBlank(message = "Le mot de passe actuel est obligatoire")
    private String currentPassword;


    // Nouveau mot de passe souhaité (obligatoire, au moins 8 caractères)
    @NotBlank(message = "Le nouveau mot de passe est obligatoire")
    @Size(min = 8, message = "Le nouveau mot de passe doit contenir au moins 8 caractères")
    private String newPassword;


    // Constructeur par défaut requis pour la désérialisation
    public ChangePasswordRequest() {}


    /**
     * Constructeur avec paramètres
     * @param currentPassword le mot de passe actuel
     * @param newPassword le nouveau mot de passe souhaité
     */
    public ChangePasswordRequest(String currentPassword, String newPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }


    // Getter pour le mot de passe actuel
    public String getCurrentPassword() {
        return currentPassword;
    }


    // Setter pour le mot de passe actuel
    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }


    // Getter pour le nouveau mot de passe
    public String getNewPassword() {
        return newPassword;
    }


    // Setter pour le nouveau mot de passe
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
