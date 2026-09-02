package com.smartrecruit.backend.modules.application.dtos;

import java.util.List;
import java.util.UUID;

public record ImportResponse(List<FileImportStatus> fileStatuses) {

  public record FileImportStatus(
      String filename,
      UUID applicationId,
      int extractedCount,
      String errorCode,
      String message,
      List<String> subErrors) {}
}
