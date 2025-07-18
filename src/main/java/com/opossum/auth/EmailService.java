package com.opossum.auth;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

    public void sendVerificationEmail(String toEmail, String token) {
        String verificationUrl = "http://localhost:8080/api/v1/auth/verify?token=" + token;

        // Simuler l'envoi de l'email
        System.out.println("=== EMAIL DE VÉRIFICATION ===");
        System.out.println("À : " + toEmail);
        System.out.println("Merci de cliquer sur ce lien pour vérifier votre compte :");
        System.out.println(verificationUrl);
        System.out.println("=== FIN DU MAIL ===");
    }
}
