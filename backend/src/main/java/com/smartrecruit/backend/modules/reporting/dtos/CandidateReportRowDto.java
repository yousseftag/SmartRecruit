package com.smartrecruit.backend.modules.reporting.dtos;

import com.smartrecruit.backend.modules.application.enums.ApplicationStatus;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/** Structured candidate details for ranking display and Excel/PDF export generation. */
public record CandidateReportRowDto(
    int rank,
    UUID candidateId,
    String fullName,
    String email,
    String phone,
    String offerTitle,
    Double totalScore,
    boolean isAdmissible,
    ApplicationStatus status,
    OffsetDateTime appliedAt,
    Map<String, Object> categoryScores) {}
