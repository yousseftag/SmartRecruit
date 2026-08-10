package com.smartrecruit.backend.modules.application.dtos;

import com.fasterxml.jackson.databind.JsonNode;
import com.smartrecruit.backend.modules.application.enums.ApplicationStatus;
import java.math.BigDecimal;
import java.util.UUID;

public record ApplicationResponse(
    UUID id,
    CandidateResponse candidate,
    UUID offerId,
    ApplicationStatus status,
    BigDecimal totalScore,
    JsonNode categoryScores,
    JsonNode extractedMatching,
    JsonNode cvExtractedData,
    java.time.OffsetDateTime appliedAt,
    java.time.OffsetDateTime scoredAt) {}
