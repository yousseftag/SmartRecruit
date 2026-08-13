package com.smartrecruit.backend.integration.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

  private final JavaMailSender mailSender;
  private final String fromAddress;

  public EmailService(
      JavaMailSender mailSender,
      @Value("${spring.mail.from:noreply@smartrecruit.com}") String fromAddress) {
    this.mailSender = mailSender;
    this.fromAddress = fromAddress;
  }

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
}
