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

    @Query("SELECT u FROM User u WHERE u.emailVerificationToken = :token")
    Optional<User> findByEmailVerificationToken(@Param("token") String token);

    /**
     * Recherche un utilisateur par son email (login)
     */
    Optional<User> findByEmail(String email);

    /**
     * Vérifie si un email est déjà utilisé
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.email = :email")
    long countByEmail(@Param("email") String email);

    default boolean existsByEmail(String email) {
        return countByEmail(email) > 0;
    }
}