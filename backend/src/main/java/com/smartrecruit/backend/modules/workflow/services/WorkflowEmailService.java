package com.smartrecruit.backend.modules.workflow.services;

import com.smartrecruit.backend.exceptions.ResourceNotFoundException;
import com.smartrecruit.backend.integration.email.EmailService;
import com.smartrecruit.backend.modules.application.entities.Application;
import com.smartrecruit.backend.modules.application.entities.Candidate;
import com.smartrecruit.backend.modules.application.repositories.ApplicationRepository;
import com.smartrecruit.backend.modules.workflow.dtos.SendEmailRequest;
import com.smartrecruit.backend.security.SecurityUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowEmailService {

  private final ApplicationRepository applicationRepository;
  private final EmailTemplateService emailTemplateService;
  private final EmailService emailService;
  private final SecurityUtils securityUtils;

  /**
   * Dispatches an email to the candidate associated with the given application. Interpolates
   * dynamic candidate, offer, and recruiter context into the email subject and HTML body.
   *
   * @param applicationId the target application ID
   * @param request the email sending payload containing subject, bodyHtml, and optional extra
   *     variables
   */
  @Transactional(readOnly = true)
  public void sendWorkflowEmail(UUID applicationId, SendEmailRequest request) {
    Application application =
        applicationRepository
            .findById(applicationId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Candidature introuvable pour l'ID : " + applicationId));

    Candidate candidate = application.getCandidate();
    if (candidate == null || candidate.getEmail() == null || candidate.getEmail().isBlank()) {
      throw new IllegalArgumentException(
          "Impossible d'envoyer l'email : aucune adresse email n'est associée à ce candidat.");
    }

    String candidateName = "Candidat";
    if (candidate.getFirstName() != null && !candidate.getFirstName().isBlank()) {
      candidateName = candidate.getFirstName();
      if (candidate.getLastName() != null && !candidate.getLastName().isBlank()) {
        candidateName += " " + candidate.getLastName();
      }
    }

    String offerTitle =
        application.getOfferTitle() != null ? application.getOfferTitle() : "Poste chez Norsys";

    String recruiterName =
        securityUtils
            .getCurrentUser()
            .map(user -> user.getFirstName() + " " + user.getLastName())
            .orElse("L'équipe Norsys");

    Map<String, String> variables = new HashMap<>();
    variables.put("nom_candidat", candidateName);
    variables.put("titre_offre", offerTitle);
    variables.put("nom_recruteur", recruiterName);
    variables.put("email_candidat", candidate.getEmail());

    String finalSubject = emailTemplateService.interpolate(request.subject(), variables);
    String finalBodyHtml = emailTemplateService.interpolate(request.bodyHtml(), variables);

    log.info(
        "Dispatching workflow email to candidate '{}' ({}) for offer '{}'",
        candidateName,
        candidate.getEmail(),
        offerTitle);

    emailService.sendHtmlEmail(candidate.getEmail(), finalSubject, finalBodyHtml);
  }
}
