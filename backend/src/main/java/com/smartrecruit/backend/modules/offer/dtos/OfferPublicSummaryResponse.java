package com.smartrecruit.backend.modules.offer.dtos;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OfferPublicSummaryResponse(
    UUID id,
    String title,
    String contractType,
    Integer durationMonths,
    String localization,
    Integer experience,
    OffsetDateTime createdAt) {}
