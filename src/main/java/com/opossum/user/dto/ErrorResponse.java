
// DTO utilisé pour représenter une erreur dans les réponses API
package com.opossum.user.dto;


/**
 * Représente une erreur renvoyée par l'API.
 * Contient un code d'erreur et un message explicatif.
 */
public class ErrorResponse {
    /**
     * Code d'erreur unique (ex: INVALID_PASSWORD, USER_NOT_FOUND, etc.).
     */
    private String code;

    /**
     * Message explicatif destiné à l'utilisateur ou au frontend.
     */
    private String message;

    /**
     * Constructeur pour initialiser le code et le message d'erreur.
     * @param code Code d'erreur unique
     * @param message Message explicatif
     */
    public ErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * Getter pour le code d'erreur.
     */
    public String getCode() { return code; }

    /**
     * Getter pour le message d'erreur.
     */
    public String getMessage() { return message; }
}
