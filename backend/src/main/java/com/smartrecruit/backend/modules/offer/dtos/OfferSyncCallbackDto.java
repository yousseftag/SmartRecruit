package com.smartrecruit.backend.modules.offer.dtos;

import com.smartrecruit.backend.modules.offer.enums.OfferAiStatus;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;

public record OfferSyncCallbackDto(
    @NotNull(message = "L'identifiant de l'offre est obligatoire") UUID offerId,
    @NotNull(message = "Le statut IA est obligatoire") OfferAiStatus aiStatus,
    Map<String, Object> extractedRequirements) {}
