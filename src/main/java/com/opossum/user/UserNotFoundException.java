
// Déclaration du package dans lequel se trouve cette classe d'exception
package com.opossum.user;


// Importation de la classe UUID pour identifier de façon unique un utilisateur
import java.util.UUID;


// Exception personnalisée pour signaler qu'un utilisateur n'a pas été trouvé
public class UserNotFoundException extends RuntimeException {

    /**
     * Construit une exception UserNotFoundException avec un message explicite
     * @param id l'identifiant unique de l'utilisateur recherché
     */
    public UserNotFoundException(UUID id) {
        // Appelle le constructeur de RuntimeException avec un message personnalisé
        super("User not found with id: " + id);
    }
}