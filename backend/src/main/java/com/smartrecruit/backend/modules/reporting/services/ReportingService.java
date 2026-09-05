package com.smartrecruit.backend.modules.reporting.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartrecruit.backend.modules.application.enums.ApplicationStatus;
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
import java.util.Map;
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
  private final ObjectMapper objectMapper = new ObjectMapper();

  public ReportingService(ReportingRepository reportingRepository) {
    this.reportingRepository = reportingRepository;
  }

  /**
   * Generates the complete dashboard report payload including campaign KPIs, recruitment funnel
   * conversion, AI score distribution, and top ranked candidates.
   */
  public ReportingDashboardResponseDto getDashboardReport(UUID offerId, ReportingPeriod period) {
    OffsetDateTime startDate =
        period != null ? period.toStartDate(OffsetDateTime.now(ZoneOffset.UTC)) : null;

    CampaignStatsDto kpis = fetchCampaignStats(offerId, startDate);
    List<FunnelStageDto> funnel = fetchFunnelStages(offerId, startDate, kpis.totalApplications());
    ScoreDistributionDto scoreDist = fetchScoreDistribution(offerId, startDate);
    List<CandidateReportRowDto> topCandidates =
        getRankedCandidates(offerId, period, DEFAULT_PREVIEW_LIMIT);

    return new ReportingDashboardResponseDto(kpis, funnel, scoreDist, topCandidates);
  }

  /**
   * Fetches the ranked candidates list for preview or export generation using direct column
   * projections to avoid heavy entity hydration.
   *
   * @param offerId Optional offer filter.
   * @param period Analytical period.
   * @param limit Maximum rows to fetch, or <= 0 for unlimited.
   */
  public List<CandidateReportRowDto> getRankedCandidates(
      UUID offerId, ReportingPeriod period, int limit) {
    OffsetDateTime startDate =
        period != null ? period.toStartDate(OffsetDateTime.now(ZoneOffset.UTC)) : null;
    Pageable pageable = limit > 0 ? PageRequest.of(0, limit) : Pageable.unpaged();

    List<Object[]> rows =
        reportingRepository.fetchRankedCandidateRows(offerId, startDate, pageable);
    if (rows == null || rows.isEmpty()) {
      return Collections.emptyList();
    }

    List<CandidateReportRowDto> rankedList = new ArrayList<>(rows.size());
    for (int i = 0; i < rows.size(); i++) {
      rankedList.add(mapToCandidateReportRow(rows.get(i), i + 1));
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

    double screenedRate =
        totalApplications > 0
            ? roundOneDecimal(((double) screenedApplications / totalApplications) * 100)
            : 0.0;
    double qualificationRate =
        totalApplications > 0
            ? roundOneDecimal(((double) qualifiedCount / totalApplications) * 100)
            : 0.0;
    double conversionRate =
        totalApplications > 0
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

  private List<FunnelStageDto> fetchFunnelStages(
      UUID offerId, OffsetDateTime startDate, long totalApplications) {
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
        new FunnelStageDto(
            "Admissibles IA", qualifiedAi, calculatePercentage(qualifiedAi, totalReceived)),
        new FunnelStageDto(
            "Présélectionnés", shortlisted, calculatePercentage(shortlisted, totalReceived)),
        new FunnelStageDto(
            "Entretiens", interviewing, calculatePercentage(interviewing, totalReceived)),
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

  private CandidateReportRowDto mapToCandidateReportRow(Object[] row, int rank) {
    UUID candidateId = null;
    if (row[0] instanceof UUID uuid) {
      candidateId = uuid;
    } else if (row[0] != null) {
      candidateId = UUID.fromString(row[0].toString());
    }

    String firstName = row[1] != null ? row[1].toString().trim() : "";
    String lastName = row[2] != null ? row[2].toString().trim() : "";
    String fullName = (firstName + " " + lastName).trim();
    String email = row[3] != null ? row[3].toString().trim() : "";
    String phone = row[4] != null ? row[4].toString().trim() : "";
    String offerTitle = row[5] != null ? row[5].toString() : null;
    Double totalScore = row[6] != null ? ((Number) row[6]).doubleValue() : null;
    boolean isAdmissible = Boolean.TRUE.equals(row[7]);

    ApplicationStatus status = ApplicationStatus.NEW;
    if (row[8] != null) {
      try {
        status = ApplicationStatus.valueOf(row[8].toString());
      } catch (IllegalArgumentException ignored) {
      }
    }

    OffsetDateTime appliedAt = null;
    if (row[9] instanceof OffsetDateTime odt) {
      appliedAt = odt;
    } else if (row[9] instanceof java.sql.Timestamp ts) {
      appliedAt = ts.toInstant().atOffset(ZoneOffset.UTC);
    }

    Map<String, Object> categoryScores = parseCategoryScores(row[10]);

    return new CandidateReportRowDto(
        rank,
        candidateId,
        fullName,
        email,
        phone,
        offerTitle,
        totalScore,
        isAdmissible,
        status,
        appliedAt,
        categoryScores);
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> parseCategoryScores(Object rawJson) {
    if (rawJson == null) {
      return Collections.emptyMap();
    }
    try {
      return objectMapper.readValue(rawJson.toString(), Map.class);
    } catch (Exception e) {
      return Collections.emptyMap();
    }
  }

  private double calculatePercentage(long count, long total) {
    if (total <= 0) return 0.0;
    return roundOneDecimal(((double) count / total) * 100);
  }

  private double roundOneDecimal(double val) {
    return Math.round(val * 10.0) / 10.0;
  }
}
