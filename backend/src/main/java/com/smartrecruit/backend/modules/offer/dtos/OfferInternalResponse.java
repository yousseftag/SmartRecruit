package com.smartrecruit.backend.modules.offer.dtos;

import com.smartrecruit.backend.modules.auth.dtos.AppUserResponse;
import com.smartrecruit.backend.modules.offer.enums.OfferAiStatus;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record OfferInternalResponse(
    UUID id,
    AppUserResponse createdBy,
    String title,
    String descriptionMarkdown,
    String status,
    OfferAiStatus offerAiStatus,
    Map<String, Object> categoryWeights,
    Map<String, Object> categoryCriteria,
    Integer minScore,
    Integer durationMonths,
    String contractType,
    Map<String, Object> extractedRequirements,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt) {}
