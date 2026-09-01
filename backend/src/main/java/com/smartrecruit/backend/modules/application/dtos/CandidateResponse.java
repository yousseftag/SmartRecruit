package com.smartrecruit.backend.modules.application.dtos;

import java.util.UUID;

public record CandidateResponse(
    UUID id,
    String firstName,
    String lastName,
    String email,
    String phone,
    String currentJobTitle) {}
