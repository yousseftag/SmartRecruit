package com.smartrecruit.backend.modules.reporting.services;

import com.smartrecruit.backend.modules.application.entities.Application;
import com.smartrecruit.backend.modules.application.entities.Candidate;
import com.smartrecruit.backend.modules.offer.entities.Offer;
import com.smartrecruit.backend.modules.reporting.dtos.CampaignStatsDto;
import com.smartrecruit.backend.modules.reporting.dtos.CandidateReportRowDto;
import com.smartrecruit.backend.modules.reporting.dtos.FunnelStageDto;
import com.smartrecruit.backend.modules.reporting.dtos.ReportingDashboardResponseDto;
import com.smartrecruit.backend.modules.reporting.dtos.ScoreDistributionDto;
import com.smartrecruit.backend.modules.reporting.enums.ReportingPeriod;
import com.smartrecruit.backend.modules.reporting.repositories.ReportingRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReportingService {

  private static final int DEFAULT_PREVIEW_LIMIT = 6;

  private final ReportingRepository reportingRepository;

  public ReportingService(ReportingRepository reportingRepository) {
    this.reportingRepository = reportingRepository;
  }

  /**
   * Generates the complete dashboard report payload including campaign KPIs,
   * recruitment funnel conversion, AI score distribution, and top ranked candidates.
   */
  public ReportingDashboardResponseDto getDashboardReport(UUID offerId, ReportingPeriod period) {
    OffsetDateTime startDate = period != null ? period.toStartDate(OffsetDateTime.now(ZoneOffset.UTC)) : null;

    CampaignStatsDto kpis = fetchCampaignStats(offerId, startDate);
    List<FunnelStageDto> funnel = fetchFunnelStages(offerId, startDate, kpis.totalApplications());
    ScoreDistributionDto scoreDist = fetchScoreDistribution(offerId, startDate);
    List<CandidateReportRowDto> topCandidates = getRankedCandidates(offerId, period, DEFAULT_PREVIEW_LIMIT);

    return new ReportingDashboardResponseDto(kpis, funnel, scoreDist, topCandidates);
  }

  /**
   * Fetches the ranked candidates list for preview or export generation.
   *
   * @param offerId Optional offer filter.
   * @param period Analytical period.
   * @param limit Maximum rows to fetch, or <= 0 for unlimited.
   */
  public List<CandidateReportRowDto> getRankedCandidates(UUID offerId, ReportingPeriod period, int limit) {
    OffsetDateTime startDate = period != null ? period.toStartDate(OffsetDateTime.now(ZoneOffset.UTC)) : null;
    Pageable pageable = limit > 0 ? PageRequest.of(0, limit) : Pageable.unpaged();

    List<Application> applications = reportingRepository.findRankedApplications(offerId, startDate, pageable);
    if (applications == null || applications.isEmpty()) {
      return Collections.emptyList();
    }

    List<CandidateReportRowDto> rankedList = new ArrayList<>(applications.size());
    for (int i = 0; i < applications.size(); i++) {
      Application app = applications.get(i);
      rankedList.add(mapToCandidateReportRow(app, i + 1));
    }
    return rankedList;
  }

  private CampaignStatsDto fetchCampaignStats(UUID offerId, OffsetDateTime startDate) {
    List<Object[]> rows = reportingRepository.fetchCampaignAggregates(offerId, startDate);
    if (rows == null || rows.isEmpty()) {
      return new CampaignStatsDto(0, 0, 0.0, 0.0, 0.0, 0, 0.0, 0, 0.0, 0);
    }

    Object[] row = rows.get(0);
    long totalApplications = ((Number) row[0]).longValue();
    long screenedApplications = ((Number) row[1]).longValue();
    double averageScore = ((Number) row[2]).doubleValue();
    double maxScore = ((Number) row[3]).doubleValue();
    long qualifiedCount = ((Number) row[4]).longValue();
    long hiredCount = ((Number) row[5]).longValue();
    long rejectedCount = ((Number) row[6]).longValue();

    double screenedRate = totalApplications > 0
        ? roundOneDecimal(((double) screenedApplications / totalApplications) * 100)
        : 0.0;
    double qualificationRate = totalApplications > 0
        ? roundOneDecimal(((double) qualifiedCount / totalApplications) * 100)
        : 0.0;
    double conversionRate = totalApplications > 0
        ? roundOneDecimal(((double) hiredCount / totalApplications) * 100)
        : 0.0;

    return new CampaignStatsDto(
        totalApplications,
        screenedApplications,
        screenedRate,
        averageScore,
        maxScore,
        qualifiedCount,
        qualificationRate,
        hiredCount,
        conversionRate,
        rejectedCount);
  }

  private List<FunnelStageDto> fetchFunnelStages(UUID offerId, OffsetDateTime startDate, long totalApplications) {
    List<Object[]> rows = reportingRepository.fetchFunnelCounts(offerId, startDate);
    if (rows == null || rows.isEmpty() || totalApplications == 0) {
      return List.of(
          new FunnelStageDto("Reçues", 0, 0.0),
          new FunnelStageDto("Admissibles IA", 0, 0.0),
          new FunnelStageDto("Présélectionnés", 0, 0.0),
          new FunnelStageDto("Entretiens", 0, 0.0),
          new FunnelStageDto("Recrutés", 0, 0.0));
    }

    Object[] row = rows.get(0);
    long totalReceived = ((Number) row[0]).longValue();
    long qualifiedAi = ((Number) row[1]).longValue();
    long shortlisted = ((Number) row[2]).longValue();
    long interviewing = ((Number) row[3]).longValue();
    long hired = ((Number) row[4]).longValue();

    return List.of(
        new FunnelStageDto("Reçues", totalReceived, 100.0),
        new FunnelStageDto("Admissibles IA", qualifiedAi, calculatePercentage(qualifiedAi, totalReceived)),
        new FunnelStageDto("Présélectionnés", shortlisted, calculatePercentage(shortlisted, totalReceived)),
        new FunnelStageDto("Entretiens", interviewing, calculatePercentage(interviewing, totalReceived)),
        new FunnelStageDto("Recrutés", hired, calculatePercentage(hired, totalReceived)));
  }

  private ScoreDistributionDto fetchScoreDistribution(UUID offerId, OffsetDateTime startDate) {
    List<Object[]> rows = reportingRepository.fetchScoreDistribution(offerId, startDate);
    if (rows == null || rows.isEmpty()) {
      return new ScoreDistributionDto(0, 0, 0, 0);
    }

    Object[] row = rows.get(0);
    long excellent = ((Number) row[0]).longValue();
    long qualified = ((Number) row[1]).longValue();
    long moderate = ((Number) row[2]).longValue();
    long insufficient = ((Number) row[3]).longValue();

    return new ScoreDistributionDto(excellent, qualified, moderate, insufficient);
  }

  private CandidateReportRowDto mapToCandidateReportRow(Application app, int rank) {
    Candidate candidate = app.getCandidate();
    Offer offer = app.getOffer();

    String fullName = "";
    String email = "";
    String phone = "";

    if (candidate != null) {
      String first = candidate.getFirstName() != null ? candidate.getFirstName().trim() : "";
      String last = candidate.getLastName() != null ? candidate.getLastName().trim() : "";
      fullName = (first + " " + last).trim();
      email = candidate.getEmail().trim();
      phone = candidate.getPhone();
    }

    String offerTitle = offer != null ? offer.getTitle() : null;
    Double totalScore = app.getTotalScore() != null ? app.getTotalScore().doubleValue() : null;
    boolean isAdmissible = Boolean.TRUE.equals(app.getPassedMinScore());

    return new CandidateReportRowDto(
        rank,
        candidate != null ? candidate.getId() : null,
        fullName,
        email,
        phone,
        offerTitle,
        totalScore,
        isAdmissible,
        app.getStatus(),
        app.getAppliedAt(),
        app.getCategoryScores());
  }

  private double calculatePercentage(long count, long total) {
    if (total <= 0) return 0.0;
    return roundOneDecimal(((double) count / total) * 100);
  }

  private double roundOneDecimal(double val) {
    return Math.round(val * 10.0) / 10.0;
  }
}
