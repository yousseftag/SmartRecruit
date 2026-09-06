package com.smartrecruit.backend.modules.dashboard.controllers;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.smartrecruit.backend.config.SecurityConfig;
import com.smartrecruit.backend.exceptions.GlobalExceptionHandler;
import com.smartrecruit.backend.modules.dashboard.dtos.ActivityDto;
import com.smartrecruit.backend.modules.dashboard.dtos.DailyApplicationStatsDto;
import com.smartrecruit.backend.modules.dashboard.dtos.DashboardStatsDto;
import com.smartrecruit.backend.modules.dashboard.dtos.PriorityOfferDto;
import com.smartrecruit.backend.modules.dashboard.services.DashboardService;
import com.smartrecruit.backend.security.JwtAuthConverter;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DashboardController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class DashboardControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private DashboardService dashboardService;

  @MockitoBean private JwtAuthConverter jwtAuthConverter;

  @Test
  void getStats_WithHrAdminRole_ShouldReturn200AndStatsJson() throws Exception {
    DashboardStatsDto mockStats = new DashboardStatsDto(5L, 12L, 75.0, 85.0, 25L, 3L, 30.0, 7L);
    when(dashboardService.getStats()).thenReturn(mockStats);

    mockMvc
        .perform(
            get("/api/v1/dashboard/stats")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_HR_ADMIN"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.activeOffers").value(5))
        .andExpect(jsonPath("$.newApplications").value(12))
        .andExpect(jsonPath("$.activeCandidates").value(25))
        .andExpect(jsonPath("$.cvExtractionRate").value(85.0))
        .andExpect(jsonPath("$.aiValidationRate").value(75.0))
        .andExpect(jsonPath("$.hiringSuccessRate").value(30.0));
  }

  @Test
  void getPriorityOffers_WithRecruiterRole_ShouldReturn200AndListJson() throws Exception {
    UUID offerId = UUID.randomUUID();
    OffsetDateTime now = OffsetDateTime.now();
    PriorityOfferDto mockOffer =
        new PriorityOfferDto(offerId, "Lead Backend Engineer", now, 6L, 3L);
    when(dashboardService.getPriorityOffers()).thenReturn(List.of(mockOffer));

    mockMvc
        .perform(
            get("/api/v1/dashboard/priority-offers")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_RECRUITER"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(offerId.toString()))
        .andExpect(jsonPath("$[0].title").value("Lead Backend Engineer"))
        .andExpect(jsonPath("$[0].newCount").value(6))
        .andExpect(jsonPath("$[0].aiPassedCount").value(3));
  }

  @Test
  void getRecentActivities_WithViewerRole_ShouldReturn200AndListJson() throws Exception {
    LocalDateTime now = LocalDateTime.now();
    ActivityDto mockActivity =
        new ActivityDto(
            "STATUS_CHANGE", "Sarah Recruiter", "Alice Green", "NEW", "SHORTLISTED", now);
    when(dashboardService.getRecentActivities()).thenReturn(List.of(mockActivity));

    mockMvc
        .perform(
            get("/api/v1/dashboard/recent-activities")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_VIEWER"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].type").value("STATUS_CHANGE"))
        .andExpect(jsonPath("$[0].user").value("Sarah Recruiter"))
        .andExpect(jsonPath("$[0].targetName").value("Alice Green"))
        .andExpect(jsonPath("$[0].fromStatus").value("NEW"))
        .andExpect(jsonPath("$[0].toStatus").value("SHORTLISTED"));
  }

  @Test
  void getApplicationsByDay_WithAuthorizedRole_ShouldReturn200AndChartJson() throws Exception {
    List<DailyApplicationStatsDto> mockChart =
        List.of(
            new DailyApplicationStatsDto("2026-08-28", 5L),
            new DailyApplicationStatsDto("2026-08-29", 5L),
            new DailyApplicationStatsDto("2026-08-30", 6L),
            new DailyApplicationStatsDto("2026-08-31", 3L),
            new DailyApplicationStatsDto("2026-09-01", 4L),
            new DailyApplicationStatsDto("2026-09-02", 8L),
            new DailyApplicationStatsDto("2026-09-03", 7L));
    when(dashboardService.getApplicationsByDay()).thenReturn(mockChart);

    mockMvc
        .perform(
            get("/api/v1/dashboard/applications-by-day")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_HR_ADMIN"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(7))
        .andExpect(jsonPath("$[1].date").value("2026-08-29"))
        .andExpect(jsonPath("$[1].count").value(5))
        .andExpect(jsonPath("$[2].date").value("2026-08-30"))
        .andExpect(jsonPath("$[2].count").value(6));
  }

  @Test
  void getStats_WithUnauthorizedRole_ShouldReturn403Forbidden() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/dashboard/stats")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CANDIDATE"))))
        .andExpect(status().isForbidden());
  }

  @Test
  void getStats_WithoutAuthentication_ShouldReturn401Unauthorized() throws Exception {
    mockMvc.perform(get("/api/v1/dashboard/stats")).andExpect(status().isUnauthorized());
  }
}
