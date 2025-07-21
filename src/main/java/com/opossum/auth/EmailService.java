package com.opossum.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service

public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${MAIL_FROM:noreply@opossum.fr}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String toEmail, String token) {
        String verificationUrl = "http://localhost:8080/api/v1/auth/verify?token=" + token;
        String subject = "Vérification de votre adresse email";
        String text = "Bonjour,\n\nMerci de cliquer sur ce lien pour vérifier votre compte :\n" + verificationUrl + "\n\nL'équipe Opossum.";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }
        public void sendResetPasswordEmail(String toEmail, String token) {
        String resetUrl = "http://192.168.1.225:8080/reset-password.html?token=" + token;
        String subject = "Réinitialisation de votre mot de passe";
        String text = "Bonjour,\n\nPour réinitialiser votre mot de passe, cliquez sur ce lien :\n" + resetUrl +
            "\n\nSi le lien ne fonctionne pas, copiez ce code dans l'application :\n" + token +
            "\n\nCe lien et ce code sont valables 30 minutes.\n\nL'équipe Opossum.";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }
}