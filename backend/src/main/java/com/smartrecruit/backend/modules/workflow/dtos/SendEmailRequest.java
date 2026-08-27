package com.smartrecruit.backend.modules.workflow.dtos;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record SendEmailRequest(
    String templateKey,
    @NotBlank(message = "Le sujet est obligatoire.") String subject,
    @NotBlank(message = "Le corps du message est obligatoire.") String bodyHtml,
    Map<String, String> extraVariables) {}
