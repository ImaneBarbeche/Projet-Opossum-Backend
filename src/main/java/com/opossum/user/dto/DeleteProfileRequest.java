
// DTO utilisé pour la requête de suppression de profil utilisateur
package com.opossum.user.dto;


// Annotation pour valider que le champ password n'est pas vide
import jakarta.validation.constraints.NotBlank;


/**
 * Représente la requête envoyée par l'utilisateur pour supprimer son compte.
 * Contient le mot de passe pour vérification et une confirmation explicite.
 */
public class DeleteProfileRequest {
    /**
     * Mot de passe de l'utilisateur, requis pour confirmer l'identité avant suppression.
     * Annoté avec @NotBlank pour garantir qu'il n'est pas vide.
     */
    @NotBlank
    private String password;

    /**
     * Confirmation explicite de la suppression (ex: case à cocher côté frontend).
     */
    private boolean confirmDeletion;

    /**
     * Getter pour le mot de passe.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Setter pour le mot de passe.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Getter pour la confirmation de suppression.
     */
    public boolean isConfirmDeletion() {
        return confirmDeletion;
    }

    /**
     * Setter pour la confirmation de suppression.
     */
    public void setConfirmDeletion(boolean confirmDeletion) {
        this.confirmDeletion = confirmDeletion;
    }
}
