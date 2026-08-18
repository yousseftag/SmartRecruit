package com.smartrecruit.backend.modules.offer.mappers;

import com.smartrecruit.backend.modules.auth.dtos.AppUserResponse;
import com.smartrecruit.backend.modules.offer.dtos.OfferInternalResponse;
import com.smartrecruit.backend.modules.offer.dtos.OfferPublicResponse;
import com.smartrecruit.backend.modules.offer.dtos.OfferPublicSummaryResponse;
import com.smartrecruit.backend.modules.offer.entities.Offer;

public class OfferMapper {

  public static OfferPublicSummaryResponse toPublicSummaryDto(Offer offer) {
    if (offer == null) return null;
    return new OfferPublicSummaryResponse(
        offer.getId(),
        offer.getTitle(),
        offer.getContractType(),
        offer.getDurationMonths(),
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

    AppUserResponse createdByResponse = null;
    if (offer.getCreatedBy() != null) {
      createdByResponse = new AppUserResponse(offer.getCreatedBy().getId());
    }
    return new OfferInternalResponse(
        offer.getId(),
        createdByResponse,
        offer.getTitle(),
        offer.getDescriptionMarkdown(),
        offer.getStatus(),
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
