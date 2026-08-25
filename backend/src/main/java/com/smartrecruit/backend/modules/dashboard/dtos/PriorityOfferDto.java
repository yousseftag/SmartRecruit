package com.smartrecruit.backend.modules.dashboard.dtos;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PriorityOfferDto(
    UUID id, String title, OffsetDateTime createdAt, long newCount, long aiPassedCount) {}
