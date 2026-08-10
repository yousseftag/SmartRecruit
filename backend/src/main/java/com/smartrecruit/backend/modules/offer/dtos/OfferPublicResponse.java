package com.smartrecruit.backend.modules.offer.dtos;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.UUID;

public record OfferPublicResponse(
    UUID id,
    String title,
    String descriptionMarkdown,
    JsonNode categoryCriteria,
    Integer durationMonths,
    String contractType,
    java.time.OffsetDateTime createdAt) {}
