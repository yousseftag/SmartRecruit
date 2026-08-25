package com.smartrecruit.backend.modules.dashboard.services;

import com.smartrecruit.backend.modules.application.enums.ApplicationStatus;
import com.smartrecruit.backend.modules.application.repositories.ApplicationRepository;
import com.smartrecruit.backend.modules.dashboard.dtos.*;
import com.smartrecruit.backend.modules.dashboard.entities.WorkflowStatusHistory;
import com.smartrecruit.backend.modules.dashboard.repositories.WorkflowStatusHistoryRepository;
import com.smartrecruit.backend.modules.offer.entities.Offer;
import com.smartrecruit.backend.modules.offer.repositories.OfferRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
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
    List<ActivityDto> activities = new ArrayList<>();

    // Fetch workflow status changes
    List<WorkflowStatusHistory> workflows = workflowRepository.findLatest(PageRequest.of(0, 10));
    for (WorkflowStatusHistory w : workflows) {
      String user =
          w.getChangedBy() != null
              ? w.getChangedBy().getFirstName() + " " + w.getChangedBy().getLastName()
              : "System";
      activities.add(
          new ActivityDto(
              "STATUS_CHANGE",
              user,
              "Changed status from " + w.getFromStatus() + " to " + w.getToStatus(),
              w.getChangedAt()));
    }

    // Fetch recent offer modifications
    List<Offer> recentOffers = offerRepository.findLatestOffers(10);
    for (Offer o : recentOffers) {
      String user =
          o.getUpdatedBy() != null
              ? o.getUpdatedBy().getFirstName() + " " + o.getUpdatedBy().getLastName()
              : "System";
      String type = o.getCreatedAt().equals(o.getUpdatedAt()) ? "CREATE_OFFER" : "UPDATE_OFFER";
      String action = type.equals("CREATE_OFFER") ? "Created offer" : "Updated offer";
      activities.add(new ActivityDto(type, user, action + " " + o.getTitle(), o.getUpdatedAt()));
    }

    // Sort and limit to 10 entries total
    return activities.stream()
        .sorted(Comparator.comparing(ActivityDto::occurredAt).reversed())
        .limit(10)
        .collect(Collectors.toList());
  }

  public List<DailyApplicationStatsDto> getApplicationsByDay() {
    List<Object[]> results = applicationRepository.countApplicationsByDay();
    return results.stream()
        .map(
            row -> {
              String date = (String) row[0];
              long count = ((Number) row[1]).longValue();
              return new DailyApplicationStatsDto(date, count);
            })
        .collect(Collectors.toList());
  }
}
