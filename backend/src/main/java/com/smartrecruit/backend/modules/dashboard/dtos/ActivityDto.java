package com.smartrecruit.backend.modules.dashboard.dtos;

import java.time.Instant;

public record ActivityDto(
    String type,
    String user,
    String targetName,
    String fromStatus,
    String toStatus,
    Instant occurredAt) {}
