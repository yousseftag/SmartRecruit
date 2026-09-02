package com.smartrecruit.backend.modules.offer.dtos;

import com.smartrecruit.backend.modules.auth.dtos.AppUserResponse;
import java.util.Map;
import java.util.UUID;

public record OfferInternalResponse(
    UUID id,
    AppUserResponse createdBy,
    String title,
    String descriptionMarkdown,
    String status,
    Map<String, Object> categoryWeights,
    Map<String, Object> categoryCriteria,
    Integer minScore,
    Integer durationMonths,
    String contractType,
    Map<String, Object> extractedRequirements,
    java.time.OffsetDateTime createdAt,
    java.time.OffsetDateTime updatedAt) {}
