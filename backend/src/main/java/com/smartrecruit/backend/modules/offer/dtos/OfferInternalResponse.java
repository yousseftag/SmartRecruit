package com.smartrecruit.backend.modules.offer.dtos;

import com.fasterxml.jackson.databind.JsonNode;
import com.smartrecruit.backend.modules.auth.dtos.AppUserResponse;
import java.util.UUID;

public record OfferInternalResponse(
    UUID id,
    AppUserResponse createdBy,
    String title,
    String descriptionMarkdown,
    String status,
    JsonNode categoryWeights,
    JsonNode categoryCriteria,
    Integer minScore,
    Integer durationMonths,
    String contractType,
    JsonNode extractedRequirements,
    java.time.OffsetDateTime createdAt,
    java.time.OffsetDateTime updatedAt) {}
