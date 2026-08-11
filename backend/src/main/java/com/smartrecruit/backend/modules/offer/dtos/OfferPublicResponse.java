package com.smartrecruit.backend.modules.offer.dtos;

import java.util.Map;
import java.util.UUID;

public record OfferPublicResponse(
    UUID id,
    String title,
    String descriptionMarkdown,
    Map<String, Object> categoryCriteria,
    Integer durationMonths,
    String contractType,
    java.time.OffsetDateTime createdAt) {}
