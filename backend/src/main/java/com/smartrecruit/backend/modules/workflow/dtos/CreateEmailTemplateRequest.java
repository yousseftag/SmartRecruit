package com.smartrecruit.backend.modules.workflow.dtos;

import jakarta.validation.constraints.NotBlank;

public record CreateEmailTemplateRequest(
    String templateKey,
    @NotBlank(message = "Le nom du modèle est obligatoire.") String name,
    @NotBlank(message = "Le sujet est obligatoire.") String subject,
    @NotBlank(message = "Le corps du message est obligatoire.") String bodyHtml) {}
