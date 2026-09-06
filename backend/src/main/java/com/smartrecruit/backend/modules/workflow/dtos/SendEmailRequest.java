package com.smartrecruit.backend.modules.workflow.dtos;

import jakarta.validation.constraints.NotBlank;

public record SendEmailRequest(
    @NotBlank(message = "Le sujet est obligatoire.") String subject,
    @NotBlank(message = "Le corps du message est obligatoire.") String bodyHtml) {}
