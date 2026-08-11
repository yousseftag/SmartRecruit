package com.smartrecruit.backend.modules.auth.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateProfileRequest(
    @NotBlank String firstName, @NotBlank String lastName, @NotBlank @Email String email) {}
