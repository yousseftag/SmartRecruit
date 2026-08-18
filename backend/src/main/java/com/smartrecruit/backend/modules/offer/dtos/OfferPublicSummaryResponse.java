package com.smartrecruit.backend.modules.offer.dtos;

import java.util.UUID;

public record OfferPublicSummaryResponse(
    UUID id,
    String title,
    String contractType,
    Integer durationMonths,
    java.time.OffsetDateTime createdAt) {}
