package com.smartrecruit.backend.modules.application.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public record ApplyRequest(
    @NotBlank(message = "First name is required") String firstName,
    @NotBlank(message = "Last name is required") String lastName,
    @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,
    String phone,
    @NotNull(message = "Offer ID is required") UUID offerId,
    @NotNull(message = "CV file is required") MultipartFile file) {}
