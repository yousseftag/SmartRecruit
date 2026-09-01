package com.smartrecruit.backend.modules.application.dtos;

import com.smartrecruit.backend.modules.application.enums.ApplicationStatus;
import com.smartrecruit.backend.modules.application.enums.ExtractionStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ApplicationResponse(
    UUID id,
    CandidateResponse candidate,
    UUID offerId,
    String offerTitle,
    Integer offerMinScore,
    List<String> offerRequiredSkills,
    ApplicationStatus status,
    ExtractionStatus extractionStatus,
    UUID cvFileId,
    String cvOriginalFilename,
    BigDecimal totalScore,
    Boolean passedMinScore,
    Map<String, Object> categoryScores,
    Map<String, Object> extractedMatching,
    Map<String, Object> cvExtractedData,
    OffsetDateTime appliedAt,
    OffsetDateTime scoredAt) {}
