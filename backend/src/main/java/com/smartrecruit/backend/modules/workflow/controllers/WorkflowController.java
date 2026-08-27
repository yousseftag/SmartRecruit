package com.smartrecruit.backend.modules.workflow.controllers;

import com.smartrecruit.backend.modules.workflow.dtos.CreateEmailTemplateRequest;
import com.smartrecruit.backend.modules.workflow.dtos.EmailTemplateResponse;
import com.smartrecruit.backend.modules.workflow.dtos.SendEmailRequest;
import com.smartrecruit.backend.modules.workflow.dtos.UpdateEmailTemplateRequest;
import com.smartrecruit.backend.modules.workflow.services.EmailTemplateService;
import com.smartrecruit.backend.modules.workflow.services.WorkflowEmailService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/workflow")
@RequiredArgsConstructor
@Slf4j
public class WorkflowController {

  private final EmailTemplateService emailTemplateService;
  private final WorkflowEmailService workflowEmailService;

  /** Fetches all configured email templates. */
  @GetMapping("/templates")
  @PreAuthorize("hasAnyRole('HR_ADMIN', 'RECRUITER')")
  public ResponseEntity<List<EmailTemplateResponse>> getAllTemplates() {
    log.info("Fetching all email templates");
    return ResponseEntity.ok(emailTemplateService.getAllTemplates());
  }

  /** Fetches a single email template by its UUID. */
  @GetMapping("/templates/{id}")
  @PreAuthorize("hasAnyRole('HR_ADMIN', 'RECRUITER')")
  public ResponseEntity<EmailTemplateResponse> getTemplateById(@PathVariable UUID id) {
    log.info("Fetching email template with ID: {}", id);
    return ResponseEntity.ok(emailTemplateService.getTemplateById(id));
  }

  /** Creates a new email template (accessible to HR Admin & Recruiter). */
  @PostMapping("/templates")
  @PreAuthorize("hasAnyRole('HR_ADMIN', 'RECRUITER')")
  public ResponseEntity<EmailTemplateResponse> createTemplate(
      @Valid @RequestBody CreateEmailTemplateRequest request) {
    log.info("Creating new email template: {}", request.name());
    EmailTemplateResponse response = emailTemplateService.createTemplate(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /** Updates an existing email template by its UUID. */
  @PutMapping("/templates/{id}")
  @PreAuthorize("hasAnyRole('HR_ADMIN', 'RECRUITER')")
  public ResponseEntity<EmailTemplateResponse> updateTemplate(
      @PathVariable UUID id, @Valid @RequestBody UpdateEmailTemplateRequest request) {
    log.info("Updating email template with ID: {}", id);
    return ResponseEntity.ok(emailTemplateService.updateTemplate(id, request));
  }

  /** Deletes an email template by its UUID. */
  @DeleteMapping("/templates/{id}")
  @PreAuthorize("hasAnyRole('HR_ADMIN', 'RECRUITER')")
  public ResponseEntity<Void> deleteTemplate(@PathVariable UUID id) {
    log.info("Deleting email template with ID: {}", id);
    emailTemplateService.deleteTemplate(id);
    return ResponseEntity.noContent().build();
  }

  /** Dispatches an email to the candidate linked to the given application. */
  @PostMapping("/applications/{id}/send-email")
  @PreAuthorize("hasAnyRole('HR_ADMIN', 'RECRUITER')")
  public ResponseEntity<Void> sendEmail(
      @PathVariable UUID id, @Valid @RequestBody SendEmailRequest request) {
    log.info("Sending workflow email for application ID: {}", id);
    workflowEmailService.sendWorkflowEmail(id, request);
    return ResponseEntity.noContent().build();
  }
}
