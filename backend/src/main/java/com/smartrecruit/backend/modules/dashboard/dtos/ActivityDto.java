package com.smartrecruit.backend.modules.dashboard.dtos;

import java.time.OffsetDateTime;

public record ActivityDto(
    String type, String user, String description, OffsetDateTime occurredAt) {}
