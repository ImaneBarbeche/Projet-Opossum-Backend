package com.opossum.auth;

import com.opossum.auth.dto.AuthResponse;
import com.opossum.auth.dto.LoginRequest;
import com.opossum.auth.dto.RegisterRequest;
import com.opossum.common.exceptions.UnauthorizedException;
import com.opossum.token.RefreshTokenService;
import com.opossum.user.User;
import com.opossum.user.UserRepository;
import jakarta.transaction.Transactional;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;
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
     * Constructeur sans Lombok
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
        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());

        if (optionalUser.isEmpty()) {
            throw new UnauthorizedException("Identifiants incorrects");
        }

        User user = optionalUser.get();

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Identifiants incorrects");

        }

        System.out.println("Connexion réussie pour : " + user.getEmail());

        return buildAuthResponse(user);
    }

    /**
     * Enregistre un nouvel utilisateur
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        System.out.println(">> check email en BDD : " + request.getEmail());
        System.out.println(">> user trouvé : " + userRepository.findByEmail(request.getEmail()));
        System.out.println(">> existsByEmail : " + userRepository.existsByEmail(request.getEmail()));

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email déjà utilisé");

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

    public AuthResponse refreshToken(String refreshToken) {
        User user = refreshTokenService.verifyRefreshToken(refreshToken);
        return buildAuthResponse(user);
    }

}
