package com.smartrecruit.backend.modules.offer.services;

import com.smartrecruit.backend.modules.offer.enums.OfferAiStatus;
import com.smartrecruit.backend.modules.offer.repositories.OfferRepository;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Sweeps for Offer AI analysis jobs stuck in PENDING status beyond the configured threshold and
 * marks them as STALLED. Runs every 1 minute.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OfferExtractionStallDetector {

  @Value("${app.offer-ai.stall-threshold-minutes:5}")
  private long stallThresholdMinutes;

  private final OfferRepository offerRepository;

  @Scheduled(fixedDelayString = "PT1M")
  @Transactional
  public void markStalledOffers() {
    OffsetDateTime cutoff = OffsetDateTime.now().minusMinutes(stallThresholdMinutes);
    int updated = offerRepository.markPendingOffersAsStalled(OfferAiStatus.STALLED, cutoff);
    if (updated > 0) {
      log.warn(
          "OfferExtractionStallDetector: Marked {} offer(s) pending AI analysis for > {} min as STALLED",
          updated,
          stallThresholdMinutes);
    }
  }
}
