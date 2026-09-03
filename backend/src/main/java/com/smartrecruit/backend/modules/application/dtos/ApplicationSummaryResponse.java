package com.smartrecruit.backend.modules.application.dtos;

import com.smartrecruit.backend.modules.application.enums.ApplicationStatus;
import com.smartrecruit.backend.modules.application.enums.ExtractionStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ApplicationSummaryResponse(
    UUID id,
    CandidateResponse candidate,
    UUID offerId,
    String offerTitle,
    ApplicationStatus status,
    ExtractionStatus extractionStatus,
    BigDecimal totalScore,
    Boolean passedMinScore,
    OffsetDateTime appliedAt) {}
