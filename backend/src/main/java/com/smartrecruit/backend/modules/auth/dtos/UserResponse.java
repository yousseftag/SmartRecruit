package com.smartrecruit.backend.modules.auth.dtos;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String username,
    String firstName,
    String lastName,
    String email,
    String role,
    Instant createdAt,
    String warning) {}
