package com.smartrecruit.backend.modules.workflow.dtos;

import java.time.OffsetDateTime;
import java.util.UUID;

public record EmailTemplateResponse(
    UUID id,
    String templateKey,
    String name,
    String subject,
    String bodyHtml,
    OffsetDateTime updatedAt) {}
