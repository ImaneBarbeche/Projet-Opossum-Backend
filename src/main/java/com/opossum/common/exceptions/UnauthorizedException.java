package com.opossum.common.exceptions;

/**
 * Exception pour les accès non autorisés
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
    // Exception UnauthorizedException à implémenter
}