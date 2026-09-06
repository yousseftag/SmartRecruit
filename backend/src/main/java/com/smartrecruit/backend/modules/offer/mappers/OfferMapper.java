package com.smartrecruit.backend.modules.offer.mappers;

import com.smartrecruit.backend.modules.offer.dtos.OfferInternalResponse;
import com.smartrecruit.backend.modules.offer.dtos.OfferPublicResponse;
import com.smartrecruit.backend.modules.offer.dtos.OfferPublicSummaryResponse;
import com.smartrecruit.backend.modules.offer.entities.Offer;
import java.util.UUID;

public class OfferMapper {

  public static OfferPublicSummaryResponse toPublicSummaryDto(Offer offer) {
    if (offer == null) return null;
    return new OfferPublicSummaryResponse(
        offer.getId(),
        offer.getTitle(),
        offer.getContractType(),
        offer.getDurationMonths(),
        offer.getLocalization(),
        offer.getExperience(),
        offer.getCreatedAt());
  }

  public static OfferPublicResponse toPublicDto(Offer offer) {
    if (offer == null) return null;
    return new OfferPublicResponse(
        offer.getId(),
        offer.getTitle(),
        offer.getDescriptionMarkdown(),
        offer.getCategoryCriteria(),
        offer.getDurationMonths(),
        offer.getContractType(),
        offer.getCreatedAt());
  }

  public static OfferInternalResponse toInternalDto(Offer offer) {
    if (offer == null) return null;

    UUID createdBy = offer.getCreatedBy() != null ? offer.getCreatedBy().getId() : null;
    UUID updatedBy = offer.getUpdatedBy() != null ? offer.getUpdatedBy().getId() : null;

    return new OfferInternalResponse(
        offer.getId(),
        createdBy,
        updatedBy,
        offer.getTitle(),
        offer.getDescriptionMarkdown(),
        offer.getStatus(),
        offer.getOfferAiStatus(),
        offer.getCategoryWeights(),
        offer.getCategoryCriteria(),
        offer.getMinScore(),
        offer.getDurationMonths(),
        offer.getContractType(),
        offer.getExtractedRequirements(),
        offer.getCreatedAt(),
        offer.getUpdatedAt());
  }
}
