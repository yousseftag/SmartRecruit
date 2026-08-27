package com.smartrecruit.backend.integration.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

  private final JavaMailSender mailSender;
  private final String fromAddress;

  public EmailService(
      JavaMailSender mailSender,
      @Value("${spring.mail.from:noreply@smartrecruit.com}") String fromAddress) {
    this.mailSender = mailSender;
    this.fromAddress = fromAddress;
  }

  /**
   * Sends a plain-text welcome email containing initial login credentials.
   *
   * @param to recipient email address
   * @param username created username
   * @param generatedPassword temporary generated password
   */
  public void sendWelcomeEmail(String to, String username, String generatedPassword) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(fromAddress);
    message.setTo(to);
    message.setSubject("Bienvenue sur SmartRecruit !");
    message.setText(
        "Bonjour,\n\n"
            + "Votre compte SmartRecruit a été créé avec succès.\n\n"
            + "Voici vos informations de connexion :\n"
            + "Nom d'utilisateur : "
            + username
            + "\n"
            + "Mot de passe temporaire : "
            + generatedPassword
            + "\n\n"
            + "Veuillez vous connecter à l'application et changer votre mot de passe dès que possible.\n\n"
            + "Cordialement,\n"
            + "L'équipe SmartRecruit");

    mailSender.send(message);
  }

  /**
   * Sends an HTML formatted email with UTF-8 encoding.
   *
   * @param to recipient email address
   * @param subject email subject line
   * @param htmlContent HTML body content
   */
  public void sendHtmlEmail(String to, String subject, String htmlContent) {
    try {
      MimeMessage mimeMessage = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
      helper.setFrom(fromAddress);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setText(htmlContent, true);
      mailSender.send(mimeMessage);
      log.info("HTML email successfully sent to: {}", to);
    } catch (MessagingException e) {
      log.error("Failed to send HTML email to: {}", to, e);
      throw new RuntimeException("Erreur lors de l'envoi de l'email à " + to, e);
    }
  }
}
