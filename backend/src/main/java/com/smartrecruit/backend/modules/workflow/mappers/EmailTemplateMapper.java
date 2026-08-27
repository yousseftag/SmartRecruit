package com.smartrecruit.backend.modules.workflow.mappers;

import com.smartrecruit.backend.modules.workflow.dtos.EmailTemplateResponse;
import com.smartrecruit.backend.modules.workflow.entities.EmailTemplate;

public final class EmailTemplateMapper {

  private EmailTemplateMapper() {}

  public static EmailTemplateResponse toDto(EmailTemplate entity) {
    if (entity == null) {
      return null;
    }
    return new EmailTemplateResponse(
        entity.getId(),
        entity.getTemplateKey(),
        entity.getName(),
        entity.getSubject(),
        entity.getBodyHtml(),
        entity.getUpdatedAt());
  }
}
