package com.smartrecruit.backend.modules.dashboard.dtos;

import java.time.LocalDateTime;

public record ActivityDto(
    String type,
    String user,
    String targetName,
    String fromStatus,
    String toStatus,
    LocalDateTime occurredAt) {}
