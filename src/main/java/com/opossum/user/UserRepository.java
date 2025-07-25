// Package contenant les classes liées à l'utilisateur
package com.opossum.user;

import com.opossum.common.enums.UserStatus;
import java.time.Instant;
import java.util.List;

// Importations nécessaires pour l'utilisation de Spring Data JPA
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.opossum.common.enums.Role;

import java.util.Optional;
import java.util.UUID;

/**
 * Interface JPA pour gérer les utilisateurs dans la base de données.
 * Hérite automatiquement des méthodes CRUD de JpaRepository (findAll, findById,
 * save, delete, etc.).
 * Permet aussi de définir des requêtes personnalisées pour des besoins
 * spécifiques.
 */

@Repository // Indique à Spring que cette interface est un bean de type repository
public interface UserRepository
        extends JpaRepository<User, UUID>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<User> {

    long countByRole(Role role);

    /**
     * Recherche un utilisateur par son token de vérification d'email
     * 
     * @param token le token de vérification
     * @return un Optional<User> correspondant
     */
    @Query("SELECT u FROM User u WHERE u.emailVerificationToken = :token")
    Optional<User> findByEmailVerificationToken(@Param("token") String token);

    /**
     * Recherche un utilisateur par son email (login)
     * 
     * @param email l'email à rechercher
     * @return un Optional<User> correspondant
     */
    Optional<User> findByEmail(String email);

    /**
     * Compte le nombre d'utilisateurs ayant un email donné (pour vérifier
     * l'unicité)
     * 
     * @param email l'email à vérifier
     * @return le nombre d'utilisateurs avec cet email
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.email = :email")
    long countByEmail(@Param("email") String email);

    /**
     * Vérifie si un utilisateur existe avec un email donné
     * 
     * @param email l'email à vérifier
     * @return true si l'email existe, false sinon
     */
    default boolean existsByEmail(String email) {
        return countByEmail(email) > 0;
    }

    /**
     * Recherche un utilisateur par son token de réinitialisation de mot de passe
     * 
     * @param token le token de reset
     * @return un Optional<User> correspondant
     */
    Optional<User> findByPasswordResetToken(String token);

    List<User> findByStatusAndUpdatedAtBefore(UserStatus status, Instant updatedAt);
}
