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

    @Value("${FRONTEND_URL:http://localhost:8080}")
    private String frontendUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

       public void sendVerificationEmail(String toEmail, String token) {
        String verificationUrl = frontendUrl + "/api/v1/auth/verify?token=" + token;
        String subject = "Vérification de votre adresse email";
        String text = "Bonjour,\n\nMerci de cliquer sur ce lien pour vérifier votre compte :\n" + verificationUrl + "\n\nL'équipe Opossum.";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }

    /**
     * Envoie un email de notification à l'utilisateur après un changement de mot de passe
     * @param toEmail l'adresse email du destinataire
     * @param firstName le prénom de l'utilisateur (optionnel pour personnaliser le message)
     */
    public void sendPasswordChangedNotification(String toEmail, String firstName) {
        String subject = "Votre mot de passe a été modifié";
        String text = "Bonjour" + (firstName != null && !firstName.isBlank() ? " " + firstName : "") + ",\n\n" +
                "Votre mot de passe vient d'être modifié avec succès.\n" +
                "Si vous n'êtes pas à l'origine de ce changement, merci de contacter immédiatement le support.\n\n" +
                "L'équipe Opossum.";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }

    /**
     * Envoie un email de confirmation après suppression du compte utilisateur
     * @param toEmail l'adresse email du destinataire
     * @param firstName le prénom de l'utilisateur (optionnel)
     */
    public void sendAccountDeletedConfirmation(String toEmail, String firstName) {
        String subject = "Votre compte a bien été supprimé";
        String text = "Bonjour" + (firstName != null && !firstName.isBlank() ? " " + firstName : "") + ",\n\n" +
                "Votre compte Opossum a bien été supprimé.\n" +
                "Si vous n'êtes pas à l'origine de cette action, merci de contacter immédiatement le support.\n\n" +
                "L'équipe Opossum.";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }

       public void sendResetPasswordEmail(String toEmail, String token) {
        String resetUrl = frontendUrl + "/reset-password.html?token=" + token;
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

        /**
     * Envoie un email à l'auteur lorsqu'une annonce est archivée/bloquée par l'admin
     */
    public void sendAnnouncementBlockedNotification(String toEmail, String firstName, String title, String reason, String moderationNotes) {
        String subject = "Votre annonce a été archivée par l'équipe de modération";
        String text = "Bonjour" + (firstName != null && !firstName.isBlank() ? " " + firstName : "") + ",\n\n" +
                "Votre annonce \"" + title + "\" a été archivée par l'équipe de modération.\n" +
                "Raison : " + reason + (moderationNotes != null && !moderationNotes.isBlank() ? "\nNotes de modération : " + moderationNotes : "") +
                "\n\nSi vous pensez qu'il s'agit d'une erreur, contactez le support.\n\nL'équipe Opossum.";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }
}