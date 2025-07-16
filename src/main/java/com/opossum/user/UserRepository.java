package com.opossum.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Interface JPA pour gérer les utilisateurs dans la base de données.
 * Hérite automatiquement des méthodes CRUD de JpaRepository.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Recherche un utilisateur par son email (login)
     */
    Optional<User> findByEmail(String email);

    /**
     * Vérifie si un email est déjà utilisé
     */
    
    boolean existsByEmail(String email);
}
