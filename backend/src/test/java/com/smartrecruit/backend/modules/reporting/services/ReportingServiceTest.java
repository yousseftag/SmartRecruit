package com.smartrecruit.backend.modules.reporting.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.smartrecruit.backend.modules.application.entities.Application;
import com.smartrecruit.backend.modules.application.entities.Candidate;
import com.smartrecruit.backend.modules.offer.entities.Offer;
import com.smartrecruit.backend.modules.application.enums.ApplicationStatus;
import com.smartrecruit.backend.modules.reporting.dtos.CandidateReportRowDto;
import com.smartrecruit.backend.modules.reporting.dtos.ReportingDashboardResponseDto;
import com.smartrecruit.backend.modules.reporting.enums.ReportingPeriod;
import com.smartrecruit.backend.modules.reporting.repositories.ReportingRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ReportingServiceTest {

  @Mock
  private ReportingRepository reportingRepository;

  @InjectMocks
  private ReportingService reportingService;

  @Test
  void getDashboardReport_WhenNoDataExists_ShouldReturnZeroStatsSafely() {
    when(reportingRepository.fetchCampaignAggregates(any(), any())).thenReturn(Collections.emptyList());
    when(reportingRepository.fetchFunnelCounts(any(), any())).thenReturn(Collections.emptyList());
    when(reportingRepository.fetchScoreDistribution(any(), any())).thenReturn(Collections.emptyList());
    when(reportingRepository.findRankedApplications(any(), any(), any())).thenReturn(Collections.emptyList());

    ReportingDashboardResponseDto report = reportingService.getDashboardReport(null, ReportingPeriod.ALL);

    assertNotNull(report);
    assertEquals(0, report.kpis().totalApplications());
    assertEquals(0.0, report.kpis().screenedRate());
    assertEquals(0.0, report.kpis().qualificationRate());
    assertEquals(0.0, report.kpis().conversionRate());
    assertEquals(5, report.funnel().size());
    assertEquals(0, report.funnel().get(0).count());
    assertEquals(0, report.scoreDistribution().excellentCount());
    assertTrue(report.topCandidates().isEmpty());
  }

  @Test
  void getDashboardReport_WhenDataExists_ShouldCalculateAllMetricsAccurately() {
    UUID offerId = UUID.randomUUID();
    // Row mapping: total, screened, avgScore, maxScore, qualified, hired, rejected
    Object[] kpiRow = new Object[] {50L, 45L, 78.4, 95.0, 20L, 5L, 10L};
    when(reportingRepository.fetchCampaignAggregates(eq(offerId), any()))
        .thenReturn(List.<Object[]>of(kpiRow));

    // Funnel mapping: totalReceived, qualifiedAi, shortlisted, interviewing, hired
    Object[] funnelRow = new Object[] {50L, 20L, 12L, 8L, 5L};
    when(reportingRepository.fetchFunnelCounts(eq(offerId), any()))
        .thenReturn(List.<Object[]>of(funnelRow));

    // Distribution mapping: excellent, qualified, moderate, insufficient
    Object[] distRow = new Object[] {8L, 15L, 18L, 4L};
    when(reportingRepository.fetchScoreDistribution(eq(offerId), any()))
        .thenReturn(List.<Object[]>of(distRow));

    when(reportingRepository.findRankedApplications(eq(offerId), any(), any()))
        .thenReturn(Collections.emptyList());

    ReportingDashboardResponseDto report = reportingService.getDashboardReport(offerId, ReportingPeriod.LAST_30_DAYS);

    assertNotNull(report);
    assertEquals(50, report.kpis().totalApplications());
    assertEquals(45, report.kpis().screenedApplications());
    assertEquals(90.0, report.kpis().screenedRate()); // (45 / 50) * 100
    assertEquals(78.4, report.kpis().averageScore());
    assertEquals(95.0, report.kpis().maxScore());
    assertEquals(20, report.kpis().qualifiedCount());
    assertEquals(40.0, report.kpis().qualificationRate()); // (20 / 50) * 100
    assertEquals(5, report.kpis().hiredCount());
    assertEquals(10.0, report.kpis().conversionRate()); // (5 / 50) * 100
    assertEquals(10, report.kpis().rejectedCount());

    // Funnel verification
    assertEquals(5, report.funnel().size());
    assertEquals("Reçues", report.funnel().get(0).stage());
    assertEquals(50, report.funnel().get(0).count());
    assertEquals(100.0, report.funnel().get(0).percentage());

    assertEquals("Admissibles IA", report.funnel().get(1).stage());
    assertEquals(20, report.funnel().get(1).count());
    assertEquals(40.0, report.funnel().get(1).percentage());

    assertEquals("Recrutés", report.funnel().get(4).stage());
    assertEquals(5, report.funnel().get(4).count());
    assertEquals(10.0, report.funnel().get(4).percentage());

    // Score distribution verification
    assertEquals(8, report.scoreDistribution().excellentCount());
    assertEquals(15, report.scoreDistribution().qualifiedCount());
    assertEquals(18, report.scoreDistribution().moderateCount());
    assertEquals(4, report.scoreDistribution().insufficientCount());
  }

  @Test
  void getRankedCandidates_ShouldMapCandidatesWithRankAndAdmissibility() {
    UUID candidateId = UUID.randomUUID();
    Candidate candidate = Candidate.builder()
        .id(candidateId)
        .firstName("Karim")
        .lastName("Benjelloun")
        .email("k.benjelloun@email.com")
        .phone("+212611223344")
        .build();

    Offer offer = Offer.builder()
        .id(UUID.randomUUID())
        .title("Senior Java Developer")
        .build();

    Application app = Application.builder()
        .id(UUID.randomUUID())
        .candidate(candidate)
        .offer(offer)
        .totalScore(new BigDecimal("92.50"))
        .passedMinScore(true)
        .status(ApplicationStatus.INTERVIEWING)
        .appliedAt(OffsetDateTime.now(ZoneOffset.UTC))
        .categoryScores(Map.of("technical", 95, "experience", 90))
        .build();

    when(reportingRepository.findRankedApplications(any(), any(), any(Pageable.class)))
        .thenReturn(List.of(app));

    List<CandidateReportRowDto> results = reportingService.getRankedCandidates(null, ReportingPeriod.ALL, 10);

    assertEquals(1, results.size());
    CandidateReportRowDto row = results.get(0);
    assertEquals(1, row.rank());
    assertEquals(candidateId, row.candidateId());
    assertEquals("Karim Benjelloun", row.fullName());
    assertEquals("k.benjelloun@email.com", row.email());
    assertEquals("+212611223344", row.phone());
    assertEquals("Senior Java Developer", row.offerTitle());
    assertEquals(92.50, row.totalScore());
    assertTrue(row.isAdmissible());
    assertEquals(ApplicationStatus.INTERVIEWING, row.status());
    assertNotNull(row.categoryScores());
  }
}
