package com.smartrecruit.backend.modules.dashboard.services;

import com.smartrecruit.backend.modules.application.enums.ApplicationStatus;
import com.smartrecruit.backend.modules.application.repositories.ApplicationRepository;
import com.smartrecruit.backend.modules.dashboard.dtos.*;
import com.smartrecruit.backend.modules.dashboard.repositories.WorkflowStatusHistoryRepository;
import com.smartrecruit.backend.modules.offer.entities.Offer;
import com.smartrecruit.backend.modules.offer.repositories.OfferRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardService {

  private final ApplicationRepository applicationRepository;
  private final OfferRepository offerRepository;
  private final WorkflowStatusHistoryRepository workflowRepository;

  public DashboardService(
      ApplicationRepository applicationRepository,
      OfferRepository offerRepository,
      WorkflowStatusHistoryRepository workflowRepository) {
    this.applicationRepository = applicationRepository;
    this.offerRepository = offerRepository;
    this.workflowRepository = workflowRepository;
  }

  public DashboardStatsDto getStats() {
    List<Object[]> results = applicationRepository.fetchDashboardAggregates();
    if (results == null || results.isEmpty()) {
      return new DashboardStatsDto(0, 0, 0, 0, 0, 0, 0, 0);
    }

    Object[] row = results.get(0);
    long activeOffers = ((Number) row[0]).longValue();
    long newApplications = ((Number) row[1]).longValue();
    long newPassedAi = ((Number) row[2]).longValue();
    long newExtractedCvs = ((Number) row[3]).longValue();
    long activeCandidates = ((Number) row[4]).longValue();
    long hiredCandidates = ((Number) row[5]).longValue();
    long rejectedCandidates = ((Number) row[6]).longValue();

    double aiValidationRate =
        newApplications > 0 ? ((double) newPassedAi / newApplications) * 100 : 0.0;
    double cvExtractionRate =
        newApplications > 0 ? ((double) newExtractedCvs / newApplications) * 100 : 0.0;

    long totalFinished = hiredCandidates + rejectedCandidates;
    double hiringSuccessRate =
        totalFinished > 0 ? ((double) hiredCandidates / totalFinished) * 100 : 0.0;

    return new DashboardStatsDto(
        activeOffers,
        newApplications,
        aiValidationRate,
        cvExtractionRate,
        activeCandidates,
        hiredCandidates,
        hiringSuccessRate,
        rejectedCandidates);
  }

  public List<PriorityOfferDto> getPriorityOffers() {
    List<Offer> offers = offerRepository.findTop5ActiveByNewApplicationCount();
    return offers.stream()
        .map(
            offer -> {
              long newCount =
                  applicationRepository.countByOfferIdAndStatus(
                      offer.getId(), ApplicationStatus.NEW);
              long aiPassedCount =
                  applicationRepository.countByOfferIdAndStatusAndPassedMinScoreTrue(
                      offer.getId(), ApplicationStatus.NEW);
              return new PriorityOfferDto(
                  offer.getId(), offer.getTitle(), offer.getCreatedAt(), newCount, aiPassedCount);
            })
        .collect(Collectors.toList());
  }

  public List<ActivityDto> getRecentActivities() {
    List<Object[]> results = workflowRepository.fetchRecentActivities();
    return results.stream()
        .map(
            row -> {
              String type = (String) row[0];
              String user = (String) row[1];
              String targetName = (String) row[2];
              String fromStatus = (String) row[3];
              String toStatus = (String) row[4];
              LocalDateTime occurredAt =
                  ((Instant) row[5]).atZone(ZoneOffset.UTC).toLocalDateTime();
              return new ActivityDto(type, user, targetName, fromStatus, toStatus, occurredAt);
            })
        .collect(Collectors.toList());
  }

  public List<DailyApplicationStatsDto> getApplicationsByDay() {
    List<Object[]> results = applicationRepository.countApplicationsByDay();
    Map<String, Long> countsByDate =
        results.stream()
            .collect(
                Collectors.toMap(row -> (String) row[0], row -> ((Number) row[1]).longValue()));

    List<DailyApplicationStatsDto> fullWeek = new ArrayList<>();
    LocalDate today = LocalDate.now();
    for (int i = 6; i >= 0; i--) {
      String dateStr = today.minusDays(i).toString();
      fullWeek.add(new DailyApplicationStatsDto(dateStr, countsByDate.getOrDefault(dateStr, 0L)));
    }
    return fullWeek;
  }
}
