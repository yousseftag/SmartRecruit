package com.smartrecruit.backend.modules.offer.controllers;

import com.smartrecruit.backend.modules.offer.dtos.OfferSyncCallbackDto;
import com.smartrecruit.backend.modules.offer.services.OfferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal webhook endpoint receiving asynchronous AI extraction results for job offers. Mirrors
 * the NlpController pattern for CV extraction callbacks.
 */
@RestController
@RequestMapping("/api/v1/internal/offers")
@RequiredArgsConstructor
@Slf4j
public class OfferSyncController {

  private final OfferService offerService;

  @PostMapping("/sync")
  public ResponseEntity<Void> syncOfferExtraction(@Valid @RequestBody OfferSyncCallbackDto dto) {
    log.info(
        "Received internal AI sync callback for Offer: {}, Status: {}",
        dto.offerId(),
        dto.aiStatus());
    offerService.syncExtractedRequirements(
        dto.offerId(), dto.aiStatus(), dto.extractedRequirements());
    return ResponseEntity.ok().build();
  }
}
