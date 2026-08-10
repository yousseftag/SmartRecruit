package com.smartrecruit.backend.modules.offer.controllers;

import com.smartrecruit.backend.exceptions.ResourceNotFoundException;
import com.smartrecruit.backend.modules.offer.dtos.OfferInternalResponse;
import com.smartrecruit.backend.modules.offer.dtos.OfferPublicResponse;
import com.smartrecruit.backend.modules.offer.dtos.OfferSummaryResponse;
import com.smartrecruit.backend.modules.offer.entities.Offer;
import com.smartrecruit.backend.modules.offer.mappers.OfferMapper;
import com.smartrecruit.backend.modules.offer.repositories.OfferRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class OfferController {

  private final OfferRepository offerRepository;

  // --- PUBLIC ENDPOINTS ---

  @GetMapping("/public/offers")
  public ResponseEntity<List<OfferPublicResponse>> getAllPublicOffers() {
    List<OfferPublicResponse> offers =
        offerRepository.findByStatus("ACTIVE").stream().map(OfferMapper::toPublicDto).toList();
    return ResponseEntity.ok(offers);
  }

  @GetMapping("/public/offers/{id}")
  public ResponseEntity<OfferPublicResponse> getPublicOfferById(@PathVariable UUID id) {
    Offer offer =
        offerRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Job offer not found."));
    return ResponseEntity.ok(OfferMapper.toPublicDto(offer));
  }

  // --- INTERNAL HR ENDPOINTS ---

  /*
  @GetMapping("/offers")
  public ResponseEntity<List<OfferInternalResponse>> getAllInternalOffers() {
    List<OfferInternalResponse> offers =
        offerRepository.findAll().stream().map(OfferMapper::toInternalDto).toList();
    return ResponseEntity.ok(offers);
  }
  */

  @GetMapping("/offers/summary")
  public ResponseEntity<List<OfferSummaryResponse>> getActiveOfferSummaries() {
    return ResponseEntity.ok(offerRepository.findActiveOfferSummaries());
  }

  @GetMapping("/offers/{id}")
  public ResponseEntity<OfferInternalResponse> getInternalOfferById(@PathVariable UUID id) {
    Offer offer =
        offerRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Job offer not found."));
    return ResponseEntity.ok(OfferMapper.toInternalDto(offer));
  }
}
