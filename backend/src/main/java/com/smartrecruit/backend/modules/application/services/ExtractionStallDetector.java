package com.smartrecruit.backend.modules.application.services;

import com.smartrecruit.backend.modules.application.enums.ExtractionStatus;
import com.smartrecruit.backend.modules.application.repositories.CvFileRepository;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExtractionStallDetector {

  @Value("${app.cv-ingestion.stall-threshold-minutes:5}")
  private long stallThresholdMinutes;

  private final CvFileRepository cvFileRepository;

  /**
   * Sweeps for CV files stuck in PENDING extraction state beyond the configured threshold and marks
   * them as STALLED. Runs every 1 minute.
   */
  @Scheduled(fixedDelayString = "PT1M")
  @Transactional
  public void markStalledJobs() {
    OffsetDateTime cutoff = OffsetDateTime.now().minusMinutes(stallThresholdMinutes);
    int updated = cvFileRepository.markPendingJobsAsStalled(ExtractionStatus.STALLED, cutoff);
    if (updated > 0) {
      log.warn(
          "ExtractionStallDetector: Marked {} CV file(s) pending for > {} min as STALLED",
          updated,
          stallThresholdMinutes);
    }
  }
}
