package com.smartrecruit.backend.modules.dashboard.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.smartrecruit.backend.modules.application.repositories.ApplicationRepository;
import com.smartrecruit.backend.modules.dashboard.dtos.ActivityDto;
import com.smartrecruit.backend.modules.dashboard.dtos.DashboardStatsDto;
import com.smartrecruit.backend.modules.dashboard.repositories.WorkflowStatusHistoryRepository;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

  @Mock private ApplicationRepository applicationRepository;
  @Mock private WorkflowStatusHistoryRepository workflowRepository;

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

    assertEquals(90.0, stats.cvExtractionRate()); // 9/10
    assertEquals(80.0, stats.aiValidationRate()); // 8/10
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
    assertEquals(now.atOffset(ZoneOffset.UTC), dto.occurredAt());
  }
}
