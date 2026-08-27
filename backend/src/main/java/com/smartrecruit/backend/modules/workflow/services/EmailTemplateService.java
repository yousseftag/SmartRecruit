package com.smartrecruit.backend.modules.workflow.services;

import com.smartrecruit.backend.exceptions.ResourceNotFoundException;
import com.smartrecruit.backend.modules.workflow.dtos.CreateEmailTemplateRequest;
import com.smartrecruit.backend.modules.workflow.dtos.EmailTemplateResponse;
import com.smartrecruit.backend.modules.workflow.dtos.UpdateEmailTemplateRequest;
import com.smartrecruit.backend.modules.workflow.entities.EmailTemplate;
import com.smartrecruit.backend.modules.workflow.mappers.EmailTemplateMapper;
import com.smartrecruit.backend.modules.workflow.repositories.EmailTemplateRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailTemplateService {

  private final EmailTemplateRepository emailTemplateRepository;

  /** Fetches all configured email templates. */
  @Transactional(readOnly = true)
  public List<EmailTemplateResponse> getAllTemplates() {
    return emailTemplateRepository.findAll().stream()
        .map(EmailTemplateMapper::toDto)
        .collect(Collectors.toList());
  }

  /** Fetches an email template by its UUID. */
  @Transactional(readOnly = true)
  public EmailTemplateResponse getTemplateById(UUID id) {
    EmailTemplate template =
        emailTemplateRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException("Modèle d'email introuvable avec l'ID : " + id));
    return EmailTemplateMapper.toDto(template);
  }

  /** Fetches an email template by its template type/key (e.g. INTERVIEW_INVITATION). */
  @Transactional(readOnly = true)
  public EmailTemplateResponse getTemplateByKey(String templateKey) {
    EmailTemplate template =
        emailTemplateRepository
            .findFirstByTemplateKey(templateKey)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Modèle d'email introuvable pour le type : " + templateKey));
    return EmailTemplateMapper.toDto(template);
  }

  /** Creates a new custom email template. */
  @Transactional
  public EmailTemplateResponse createTemplate(CreateEmailTemplateRequest request) {
    EmailTemplate template =
        EmailTemplate.builder()
            .name(request.name().trim())
            .subject(request.subject().trim())
            .bodyHtml(request.bodyHtml().trim())
            .build();

    EmailTemplate saved = emailTemplateRepository.save(template);
    log.info("Created new custom email template '{}' with ID: {}", saved.getName(), saved.getId());
    return EmailTemplateMapper.toDto(saved);
  }

  /** Updates an existing email template by its unique UUID ID. */
  @Transactional
  public EmailTemplateResponse updateTemplate(UUID id, UpdateEmailTemplateRequest request) {
    EmailTemplate template =
        emailTemplateRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException("Modèle d'email introuvable avec l'ID : " + id));

    template.setName(request.name().trim());
    template.setSubject(request.subject().trim());
    template.setBodyHtml(request.bodyHtml().trim());

    EmailTemplate updated = emailTemplateRepository.save(template);
    log.info("Updated email template '{}' (ID: {})", updated.getName(), id);
    return EmailTemplateMapper.toDto(updated);
  }

  /** Deletes a custom template by its UUID. System default templates cannot be deleted. */
  @Transactional
  public void deleteTemplate(UUID id) {
    EmailTemplate template =
        emailTemplateRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException("Modèle d'email introuvable avec l'ID : " + id));

    if (!"OTHER".equalsIgnoreCase(template.getTemplateKey())) {
      throw new IllegalArgumentException(
          "Les modèles par défaut du système ne peuvent pas être supprimés.");
    }

    emailTemplateRepository.delete(template);
    log.info("Deleted custom email template '{}' with ID: {}", template.getName(), id);
  }

  /**
   * Replaces dynamic variable placeholders in template text.
   *
   * @param text raw template text with {{variable}} placeholders
   * @param variables map of variable names to replacement values
   * @return interpolated text
   */
  public String interpolate(String text, Map<String, String> variables) {
    if (text == null || variables == null || variables.isEmpty()) {
      return text != null ? text : "";
    }

    String result = text;
    for (Map.Entry<String, String> entry : variables.entrySet()) {
      String placeholder = "{{" + entry.getKey() + "}}";
      String value = entry.getValue() != null ? entry.getValue() : "";
      result = result.replace(placeholder, value);
    }
    return result;
  }
}
