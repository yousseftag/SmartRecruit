package com.smartrecruit.backend.modules.auth.dtos;

import jakarta.validation.constraints.Email;

public record UpdateProfileRequest(
    String firstName, String lastName, @Email(message = "Email must be valid") String email) {}
