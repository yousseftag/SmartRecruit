package com.smartrecruit.backend.modules.application.dtos;

import com.smartrecruit.backend.modules.application.enums.ApplicationStatus;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public record ApplicationResponse(
    UUID id,
    CandidateResponse candidate,
    UUID offerId,
    ApplicationStatus status,
    BigDecimal totalScore,
    Map<String, Object> categoryScores,
    Map<String, Object> extractedMatching,
    Map<String, Object> cvExtractedData,
    java.time.OffsetDateTime appliedAt,
    java.time.OffsetDateTime scoredAt) {}
