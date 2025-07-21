package com.opossum.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Interface JPA pour gérer les utilisateurs dans la base de données. Hérite
 * automatiquement des méthodes CRUD de JpaRepository.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * *********** ✨ Windsurf Command ⭐  ************
     */
    /**
     * Recherche un utilisateur par son token de vérification d'email
     */
    /**
     * ***** 0d962529-6b18-4710-abff-aae19447e4e1  ******
     */
    @Query("SELECT u FROM User u WHERE u.emailVerificationToken = :token")
    Optional<User> findByEmailVerificationToken(@Param("token") String token);

    /**
     * Recherche un utilisateur par son email
     */
    Optional<User> findByEmail(String email);

    /**
     * Vérifie si un email est déjà utilisé
     */
    boolean existsByEmail(String email);

    /**
     * Recherche un utilisateur par son token de réinitialisation de mot de
     * passe
     */
    Optional<User> findByPasswordResetToken(String token);
}
