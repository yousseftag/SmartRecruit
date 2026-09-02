package com.smartrecruit.backend.integration.modules.workflow;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartrecruit.backend.integration.email.EmailService;
import com.smartrecruit.backend.modules.application.repositories.ApplicationRepository;
import com.smartrecruit.backend.modules.workflow.dtos.CreateEmailTemplateRequest;
import com.smartrecruit.backend.modules.workflow.dtos.SendEmailRequest;
import com.smartrecruit.backend.modules.workflow.dtos.UpdateEmailTemplateRequest;
import com.smartrecruit.backend.modules.workflow.entities.EmailTemplate;
import com.smartrecruit.backend.modules.workflow.repositories.EmailTemplateRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class WorkflowControllerTest {

  @Autowired private MockMvc mockMvc;
  private final ObjectMapper objectMapper = new ObjectMapper();
  @Autowired private EmailTemplateRepository emailTemplateRepository;
  @Autowired private ApplicationRepository applicationRepository;
  @MockitoBean private EmailService emailService;

  @Test
  void unauthenticatedUserShouldGetUnauthorizedOnTemplates() throws Exception {
    mockMvc.perform(get("/api/v1/workflow/templates")).andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(roles = "RECRUITER")
  void shouldReturnAllTemplatesForRecruiter() throws Exception {
    mockMvc
        .perform(get("/api/v1/workflow/templates"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))))
        .andExpect(jsonPath("$[*].templateKey", hasItem("INTERVIEW_INVITATION")));
  }

  @Test
  @WithMockUser(roles = "HR_ADMIN")
  void shouldCreateNewTemplateWithDefaultOtherKey() throws Exception {
    CreateEmailTemplateRequest request =
        new CreateEmailTemplateRequest(
            "Offre d'embauche personnalisée",
            "Félicitations pour votre embauche",
            "<p>Bonjour {{nom_candidat}}, nous sommes ravis...</p>");

    mockMvc
        .perform(
            post("/api/v1/workflow/templates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNotEmpty())
        .andExpect(jsonPath("$.templateKey").value("OTHER"))
        .andExpect(jsonPath("$.name").value("Offre d'embauche personnalisée"))
        .andExpect(jsonPath("$.subject").value("Félicitations pour votre embauche"));
  }

  @Test
  @WithMockUser(roles = "RECRUITER")
  void shouldUpdateTemplateById() throws Exception {
    EmailTemplate existing =
        emailTemplateRepository.findFirstByTemplateKey("INTERVIEW_INVITATION").orElseThrow();

    UpdateEmailTemplateRequest updateRequest =
        new UpdateEmailTemplateRequest(
            "Convocation Entretien Modifiée",
            "Nouveau Sujet: Convocation {{titre_offre}}",
            "<p>Nouveau corps de message</p>");

    mockMvc
        .perform(
            put("/api/v1/workflow/templates/" + existing.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(existing.getId().toString()))
        .andExpect(jsonPath("$.name").value("Convocation Entretien Modifiée"))
        .andExpect(jsonPath("$.subject").value("Nouveau Sujet: Convocation {{titre_offre}}"));
  }

  @Test
  @WithMockUser(roles = "RECRUITER")
  void shouldDeleteCustomTemplateSuccessfully() throws Exception {
    EmailTemplate custom =
        emailTemplateRepository.save(
            EmailTemplate.builder()
                .templateKey("OTHER")
                .name("A supprimer")
                .subject("Sujet")
                .bodyHtml("<p>Corps</p>")
                .build());

    mockMvc
        .perform(delete("/api/v1/workflow/templates/" + custom.getId()))
        .andExpect(status().isNoContent());
  }

  @Test
  @WithMockUser(roles = "RECRUITER")
  void shouldRejectDeletingDefaultSystemTemplate() throws Exception {
    EmailTemplate defaultTemplate =
        emailTemplateRepository.findFirstByTemplateKey("INTERVIEW_INVITATION").orElseThrow();

    mockMvc
        .perform(delete("/api/v1/workflow/templates/" + defaultTemplate.getId()))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath(
                "$.message",
                containsString("Les modèles par défaut du système ne peuvent pas être supprimés")));
  }

  @Test
  @WithMockUser(roles = "VIEWER")
  void viewerShouldBeForbiddenToFetchTemplates() throws Exception {
    mockMvc.perform(get("/api/v1/workflow/templates")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "VIEWER")
  void viewerShouldBeForbiddenToCreateOrUpdateTemplate() throws Exception {
    CreateEmailTemplateRequest request =
        new CreateEmailTemplateRequest("Test", "Test Sujet", "<p>Test</p>");

    mockMvc
        .perform(
            post("/api/v1/workflow/templates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "RECRUITER")
  void shouldSendEmailSuccessfullyForValidCandidate() throws Exception {
    // Charlie from V3 seed data (has charlie@example.com)
    UUID applicationId = UUID.fromString("32000000-0000-0000-0000-000000000003");

    SendEmailRequest request =
        new SendEmailRequest(
            "Entretien pour {{titre_offre}}",
            "<p>Bonjour {{nom_candidat}}, RDV Vendredi à 14h</p>");

    doNothing().when(emailService).sendHtmlEmail(any(), any(), any());

    mockMvc
        .perform(
            post("/api/v1/workflow/applications/" + applicationId + "/send-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNoContent());
  }

  @Test
  @WithMockUser(roles = "RECRUITER")
  void shouldReturn400WhenCandidateHasNoEmail() throws Exception {
    UUID applicationId = UUID.fromString("52000000-0000-0000-0000-000000000005");
    var application = applicationRepository.findById(applicationId).orElseThrow();
    application.getCandidate().setEmail(null);
    applicationRepository.save(application);

    SendEmailRequest request = new SendEmailRequest("Sujet", "<p>Corps</p>");

    mockMvc
        .perform(
            post("/api/v1/workflow/applications/" + applicationId + "/send-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message", containsString("aucune adresse email")));
  }
}
