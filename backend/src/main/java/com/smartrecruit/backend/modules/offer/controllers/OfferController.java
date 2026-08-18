package com.smartrecruit.backend.modules.offer.controllers;

import com.smartrecruit.backend.exceptions.ResourceNotFoundException;
import com.smartrecruit.backend.modules.offer.dtos.OfferInternalResponse;
import com.smartrecruit.backend.modules.offer.dtos.OfferPublicResponse;
import com.smartrecruit.backend.modules.offer.dtos.OfferPublicSummaryResponse;
import com.smartrecruit.backend.modules.offer.dtos.OfferTitleResponse;
import com.smartrecruit.backend.modules.offer.entities.Offer;
import com.smartrecruit.backend.modules.offer.mappers.OfferMapper;
import com.smartrecruit.backend.modules.offer.repositories.OfferRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class OfferController {

  private final OfferRepository offerRepository;

  // --- PUBLIC ENDPOINTS (Careers Page) ---

  /**
   * Purpose: Renders the public Careers page list. Optimized to return a lightweight summary DTO
   * (drops heavy markdown and internal AI weights).
   */
  @GetMapping("/public/offers")
  public ResponseEntity<List<OfferPublicSummaryResponse>> getAllPublicOffers() {
    List<OfferPublicSummaryResponse> offers =
        offerRepository.findByStatus("ACTIVE").stream()
            .map(OfferMapper::toPublicSummaryDto)
            .toList();
    return ResponseEntity.ok(offers);
  }

  /**
   * Purpose: Renders the public deep-dive view for a specific job offer. Security: Only allows
   * viewing if the offer is explicitly marked as 'ACTIVE'.
   */
  @GetMapping("/public/offers/{id}")
  public ResponseEntity<OfferPublicResponse> getPublicOfferById(@PathVariable UUID id) {
    Offer offer =
        offerRepository
            .findByIdAndStatus(id, "ACTIVE")
            .orElseThrow(
                () -> new ResourceNotFoundException("Job offer not found or is no longer active."));
    return ResponseEntity.ok(OfferMapper.toPublicDto(offer));
  }

  // --- INTERNAL HR ENDPOINTS ---

  /**
   * Purpose: Internal HR lightweight endpoint. Optimized to populate dropdowns (e.g., "Select an
   * Offer" in the Candidate Import UI).
   */
  @GetMapping("/offers/titles")
  @PreAuthorize("hasAnyRole('HR_ADMIN', 'RECRUITER', 'VIEWER')")
  public ResponseEntity<List<OfferTitleResponse>> getActiveOfferTitles() {
    return ResponseEntity.ok(offerRepository.findActiveOfferTitles());
  }

  /**
   * Purpose: Internal HR deep-dive. (not used in feature 3!) Exposes hidden fields (like AI
   * extracted_requirements) when HR views or edits an offer.
   */
  @GetMapping("/offers/{id}")
  @PreAuthorize("hasAnyRole('HR_ADMIN', 'RECRUITER', 'VIEWER')")
  public ResponseEntity<OfferInternalResponse> getInternalOfferById(@PathVariable UUID id) {
    Offer offer =
        offerRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Job offer not found."));
    return ResponseEntity.ok(OfferMapper.toInternalDto(offer));
  }
}
