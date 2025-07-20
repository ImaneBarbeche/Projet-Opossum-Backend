
// DTO utilisé pour la réponse à la suppression de profil utilisateur
package com.opossum.user.dto;


// Permet de stocker la date et l'heure de la réponse
import java.time.Instant;


/**
 * Représente la réponse renvoyée après une tentative de suppression de profil utilisateur.
 * Contient le statut, un éventuel message d'erreur, un timestamp et un message informatif.
 */
public class DeleteProfileResponse {
    /**
     * Indique si la suppression a réussi ou non.
     */
    private boolean success;

    /**
     * Détail de l'erreur en cas d'échec (null si succès).
     */
    private ErrorResponse error;

    /**
     * Date et heure de la réponse (utile pour le frontend).
     */
    private Instant timestamp;

    /**
     * Message informatif à afficher à l'utilisateur.
     */
    private String message;

    /**
     * Constructeur pour une réponse sans message personnalisé.
     * @param success Indique le succès ou l'échec
     * @param error Détail de l'erreur (null si succès)
     * @param timestamp Date et heure de la réponse
     */
    public DeleteProfileResponse(boolean success, ErrorResponse error, Instant timestamp) {
        this.success = success;
        this.error = error;
        this.timestamp = timestamp;
    }

    /**
     * Constructeur pour une réponse avec message personnalisé.
     * @param success Indique le succès ou l'échec
     * @param error Détail de l'erreur (null si succès)
     * @param timestamp Date et heure de la réponse
     * @param message Message informatif à afficher
     */
    public DeleteProfileResponse(boolean success, ErrorResponse error, Instant timestamp, String message) {
        this.success = success;
        this.error = error;
        this.timestamp = timestamp;
        this.message = message;
    }

    /**
     * Getter pour le statut de succès.
     */
    public boolean isSuccess() { return success; }

    /**
     * Getter pour l'erreur éventuelle.
     */
    public ErrorResponse getError() { return error; }

    /**
     * Getter pour le timestamp.
     */
    public Instant getTimestamp() { return timestamp; }

    /**
     * Getter pour le message informatif.
     */
    public String getMessage() { return message; }
}
