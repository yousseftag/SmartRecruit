package com.smartrecruit.backend.modules.application.dtos;

import com.smartrecruit.backend.modules.application.enums.ApplicationStatus;
import java.math.BigDecimal;
import java.util.UUID;

public record ApplicationSummaryResponse(
    UUID id,
    CandidateResponse candidate,
    UUID offerId,
    String offerTitle,
    Integer offerMinScore,
    ApplicationStatus status,
    BigDecimal totalScore,
    java.time.OffsetDateTime appliedAt) {}
