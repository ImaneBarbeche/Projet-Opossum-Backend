package com.opossum.auth;

import com.opossum.auth.dto.AuthResponse;
import com.opossum.auth.dto.LoginRequest;
import com.opossum.auth.dto.RegisterRequest;
import com.opossum.common.exceptions.UnauthorizedException;
import com.opossum.token.RefreshTokenService;
import com.opossum.user.User;
import com.opossum.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Service métier responsable de la logique d'authentification.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    /**
     * Constructeur manuel (pas de Lombok ici)
     */
    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtUtil jwtUtil,
                       RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
    }

    /**
     * Authentifie un utilisateur avec email + mot de passe
     */
    public AuthResponse login(LoginRequest request) {
        // Création d’un token d’authentification
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
        );

        try {
            authentication = authenticationManager.authenticate(authentication);
        } catch (Exception ex) {
            throw new UnauthorizedException();
        }

        User user = (User) authentication.getPrincipal();
        return buildAuthResponse(user);
    }

    /**
     * Enregistre un nouvel utilisateur
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email déjà utilisé");
        }

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setRole("USER");
        user.setActive(true);
        user.setEmailVerified(false);
        user.setCreatedAt(Instant.now());

        userRepository.save(user);

        return buildAuthResponse(user);
    }

    /**
     * Construit la réponse AuthResponse avec tokens + infos user
     */
    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtUtil.generateToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                accessToken,
                refreshToken,
                1800, // expiresIn: 30 minutes
                Instant.now()
        );
    }
}
