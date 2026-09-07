package com.smartrecruit.backend.modules.dashboard.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.smartrecruit.backend.modules.application.enums.ApplicationStatus;
import com.smartrecruit.backend.modules.application.repositories.ApplicationRepository;
import com.smartrecruit.backend.modules.application.repositories.WorkflowStatusHistoryRepository;
import com.smartrecruit.backend.modules.dashboard.dtos.ActivityDto;
import com.smartrecruit.backend.modules.dashboard.dtos.DailyApplicationStatsDto;
import com.smartrecruit.backend.modules.dashboard.dtos.DashboardStatsDto;
import com.smartrecruit.backend.modules.dashboard.dtos.PriorityOfferDto;
import com.smartrecruit.backend.modules.offer.entities.Offer;
import com.smartrecruit.backend.modules.offer.repositories.OfferRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

  @Mock private ApplicationRepository applicationRepository;
  @Mock private WorkflowStatusHistoryRepository workflowRepository;
  @Mock private OfferRepository offerRepository;

  @InjectMocks private DashboardService dashboardService;

  @Test
  void getStats_ShouldHandleDivisionByZero_WhenNoApplicationsExist() {
    // Edge Case: Zero applications for KPI extraction and hiring rates
    Object[] row = new Object[] {0L, 0L, 0L, 0L, 0L, 0L, 0L};
    List<Object[]> queryResult = Collections.singletonList(row);

    when(applicationRepository.fetchDashboardAggregates()).thenReturn(queryResult);

    DashboardStatsDto stats = dashboardService.getStats();

    // Rates should gracefully default to 0.0 without throwing ArithmeticException
    assertEquals(0.0, stats.cvExtractionRate());
    assertEquals(0.0, stats.hiringSuccessRate());
    assertEquals(0.0, stats.aiValidationRate());
  }

  @Test
  void getStats_ShouldCalculateRatesProperly_WhenDataExists() {
    // Row mapping: activeOffers, newApplications, newPassedAi, newExtractedCvs, activeCandidates,
    // hiredCandidates, rejectedCandidates
    Object[] row =
        new Object[] {
          5L, 10L, 8L, 9L, 20L, 2L, 3L
        }; // 10 new applications, 9 extracted (90%), 2 hired out of 5 finished (40%)
    List<Object[]> queryResult = Collections.singletonList(row);

    when(applicationRepository.fetchDashboardAggregates()).thenReturn(queryResult);

    DashboardStatsDto stats = dashboardService.getStats();

    assertEquals(5, stats.activeOffers());
    assertEquals(10, stats.newApplications());
    assertEquals(20, stats.activeCandidates());
    assertEquals(90.0, stats.cvExtractionRate()); // 9/10
    assertEquals((8.0 / 9.0) * 100, stats.aiValidationRate(), 0.001); // 8/9
    assertEquals(40.0, stats.hiringSuccessRate()); // 2/(2+3)
  }

  @Test
  void getStats_ShouldReturnZeroes_WhenDbReturnsEmpty() {
    // Edge case: empty result
    when(applicationRepository.fetchDashboardAggregates()).thenReturn(Collections.emptyList());

    DashboardStatsDto stats = dashboardService.getStats();

    assertEquals(0, stats.activeOffers());
    assertEquals(0.0, stats.cvExtractionRate());
  }

  @Test
  void getRecentActivities_ShouldMapNativeResultCorrectly_WithInstantAndNullUsers() {
    // Edge Case: Native query returning Instant and null user handled by COALESCE in SQL
    Instant now = Instant.now();
    Object[] nativeRow =
        new Object[] {
          "STATUS_CHANGE",
          "System", // Mocking the COALESCE result
          "John Doe",
          "new",
          "interview",
          now // Postgres native type mapping returns Instant
        };

    List<Object[]> queryResult = Collections.singletonList(nativeRow);

    when(workflowRepository.fetchRecentActivities()).thenReturn(queryResult);

    List<ActivityDto> activities = dashboardService.getRecentActivities();

    assertEquals(1, activities.size());
    ActivityDto dto = activities.get(0);
    assertEquals("STATUS_CHANGE", dto.type());
    assertEquals("System", dto.user());
    assertEquals(now, dto.occurredAt());
  }

  @Test
  void getPriorityOffers_ShouldMapOffersAndCountsCorrectly() {
    UUID offerId = UUID.randomUUID();
    OffsetDateTime now = OffsetDateTime.now();
    Offer offer =
        Offer.builder()
            .id(offerId)
            .title("Senior Full-Stack Developer")
            .status("ACTIVE")
            .createdAt(now)
            .build();

    when(offerRepository.findTop5ActiveByNewApplicationCount())
        .thenReturn(Collections.singletonList(offer));
    when(applicationRepository.countByOfferIdAndStatus(offerId, ApplicationStatus.NEW))
        .thenReturn(7L);
    when(applicationRepository.countByOfferIdAndStatusAndPassedMinScoreTrue(
            offerId, ApplicationStatus.NEW))
        .thenReturn(4L);

    List<PriorityOfferDto> results = dashboardService.getPriorityOffers();

    assertEquals(1, results.size());
    PriorityOfferDto dto = results.get(0);
    assertEquals(offerId, dto.id());
    assertEquals("Senior Full-Stack Developer", dto.title());
    assertEquals(now, dto.createdAt());
    assertEquals(7L, dto.newCount());
    assertEquals(4L, dto.aiPassedCount());
  }

  @Test
  void getApplicationsByDay_ShouldGenerateCompleteSevenDaySeries_AndMapDailyCounts() {
    LocalDate today = LocalDate.now(ZoneOffset.UTC);
    String day1 = today.minusDays(4).toString();
    String day2 = today.minusDays(1).toString();
    Object[] row1 = new Object[] {day1, 5L};
    Object[] row2 = new Object[] {day2, 8L};

    when(applicationRepository.countApplicationsByDay()).thenReturn(List.of(row1, row2));

    List<DailyApplicationStatsDto> stats = dashboardService.getApplicationsByDay();

    assertEquals(7, stats.size());
    // Chronological order from today.minusDays(6) up to today
    assertEquals(today.minusDays(6).toString(), stats.get(0).date());
    assertEquals(0L, stats.get(0).count()); // missing day defaults to 0

    assertEquals(day1, stats.get(2).date());
    assertEquals(5L, stats.get(2).count()); // populated day

    assertEquals(day2, stats.get(5).date());
    assertEquals(8L, stats.get(5).count()); // populated day

    assertEquals(today.toString(), stats.get(6).date());
    assertEquals(0L, stats.get(6).count()); // today defaults to 0 when not in DB
  }

  @Test
  void getRecentActivities_ShouldReturnMultipleActivities_WithAccurateUtcConversion() {
    Instant now = Instant.now();
    Instant tenMinutesAgo = now.minusSeconds(600);

    Object[] row1 =
        new Object[] {"STATUS_CHANGE", "Jane Recruiter", "Alice Smith", "NEW", "INTERVIEWING", now};
    Object[] row2 =
        new Object[] {
          "APPLICATION_CREATED", "Candidate Portal", "Bob Johnson", null, "NEW", tenMinutesAgo
        };

    when(workflowRepository.fetchRecentActivities()).thenReturn(List.of(row1, row2));

    List<ActivityDto> activities = dashboardService.getRecentActivities();

    assertEquals(2, activities.size());

    ActivityDto first = activities.get(0);
    assertEquals("STATUS_CHANGE", first.type());
    assertEquals("Jane Recruiter", first.user());
    assertEquals("Alice Smith", first.targetName());
    assertEquals("NEW", first.fromStatus());
    assertEquals("INTERVIEWING", first.toStatus());
    assertEquals(now, first.occurredAt());

    ActivityDto second = activities.get(1);
    assertEquals("APPLICATION_CREATED", second.type());
    assertEquals("Candidate Portal", second.user());
    assertEquals("Bob Johnson", second.targetName());
    assertEquals(null, second.fromStatus());
    assertEquals("NEW", second.toStatus());
    assertEquals(tenMinutesAgo, second.occurredAt());
  }
}
