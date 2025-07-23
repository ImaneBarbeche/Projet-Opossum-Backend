package com.opossum.auth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

@Service

public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${MAIL_FROM:noreply@opossum.fr}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    private String generateQrCodeBase64(String data) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, 200, 200);
            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            byte[] pngData = pngOutputStream.toByteArray();
            return Base64.getEncoder().encodeToString(pngData);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du QR code", e);
        }
    }

    public void sendVerificationEmail(String toEmail, String token) {
        String verificationUrl = "opossum://verify-email/" + token;
        String subject = "Vérification de votre adresse email";
        String qrBase64 = generateQrCodeBase64(verificationUrl);
        String html = "<html><body>Bonjour,<br><br>Pour vérifier votre compte, scannez ce QR code avec l'application Opossum :<br><br>" +
            "<img src='data:image/png;base64," + qrBase64 + "' alt='QR Code' style='width:200px;height:200px;'/>" +
            "<br><br>L'équipe Opossum.</body></html>";
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email de vérification", e);
        }
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
        String resetUrl = "opossum://reset-password/" + token;
        String subject = "Réinitialisation de votre mot de passe";
        String qrBase64 = generateQrCodeBase64(resetUrl);
        String html = "<html><body>Bonjour,<br><br>Pour réinitialiser votre mot de passe, scannez ce QR code avec l'application Opossum :<br><br>" +
            "<img src='data:image/png;base64," + qrBase64 + "' alt='QR Code' style='width:200px;height:200px;'/>" +
            "<br><br>Ou cliquez sur le bouton ci-dessous :<br><br>" +
            "<a href='" + resetUrl + "' style='display:inline-block;padding:10px 20px;background:#1976d2;color:#fff;text-decoration:none;border-radius:4px;'>Réinitialiser mon mot de passe</a>" +
            "<br><br>Ce lien et ce code sont valables 30 minutes.<br><br>L'équipe Opossum.</body></html>";
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email de réinitialisation", e);
        }
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