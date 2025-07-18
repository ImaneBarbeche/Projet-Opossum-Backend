package com.opossum.auth;

import com.opossum.user.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

/**
 * Utilitaire pour générer et valider des JWT (JSON Web Tokens).
 */
@Component
public class JwtUtil {
    // Inject secret from application.yml
    @Value("${jwt.secret}")
    private String jwtSecret;

    private Key key;

    @PostConstruct
    public void initKey() {
        key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // Durée de validité d’un accessToken : 30 minutes (en ms)
    private static final long EXPIRATION_TIME_MS = 30 * 60 * 1000;

    /**
     * Génère un token JWT pour un utilisateur
     */
    public String generateToken(User user) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + EXPIRATION_TIME_MS);

        return Jwts.builder()
                .setSubject(user.getId().toString()) // UUID en string
                .claim("email", user.getEmail())
                .claim("role", user.getRole())
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(key)
                .compact();
    }

    /**
     * Extrait l'UUID utilisateur depuis le token JWT
     */
    public UUID extractUserId(String token) {
        String subject = parseToken(token).getSubject();
        return UUID.fromString(subject);
    }

    /**
     * Vérifie si le token est valide (non expiré, bien signé, etc.)
     */
    public boolean isTokenValid(String token) {
        try {
            parseToken(token); // va lancer une exception si invalide
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Méthode interne qui parse le token en renvoyant les "claims"
     */
    private Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Retourne la date d’expiration du token (utile pour afficher sur front)
     */
    public Date getExpirationDate(String token) {
        return parseToken(token).getExpiration();
    }

    /**
     * Retourne le rôle utilisateur stocké dans le token
     */
    public String getRole(String token) {
        return parseToken(token).get("role", String.class);
    }

    /**
     * Retourne l’email stocké dans le token
     */
    public String getEmail(String token) {
        return parseToken(token).get("email", String.class);
    }
}
