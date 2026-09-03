package com.smartrecruit.backend.modules.dashboard.controllers;

import com.smartrecruit.backend.modules.dashboard.dtos.ActivityDto;
import com.smartrecruit.backend.modules.dashboard.dtos.DailyApplicationStatsDto;
import com.smartrecruit.backend.modules.dashboard.dtos.DashboardStatsDto;
import com.smartrecruit.backend.modules.dashboard.dtos.PriorityOfferDto;
import com.smartrecruit.backend.modules.dashboard.services.DashboardService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@PreAuthorize("hasAnyRole('HR_ADMIN', 'RECRUITER', 'VIEWER')")
public class DashboardController {

  private final DashboardService dashboardService;

  public DashboardController(DashboardService dashboardService) {
    this.dashboardService = dashboardService;
  }

  @GetMapping("/stats")
  public ResponseEntity<DashboardStatsDto> getStats() {
    return ResponseEntity.ok(dashboardService.getStats());
  }

  @GetMapping("/priority-offers")
  public ResponseEntity<List<PriorityOfferDto>> getPriorityOffers() {
    return ResponseEntity.ok(dashboardService.getPriorityOffers());
  }

  @GetMapping("/recent-activities")
  public ResponseEntity<List<ActivityDto>> getRecentActivities() {
    return ResponseEntity.ok(dashboardService.getRecentActivities());
  }

  @GetMapping("/applications-by-day")
  public ResponseEntity<List<DailyApplicationStatsDto>> getApplicationsByDay() {
    return ResponseEntity.ok(dashboardService.getApplicationsByDay());
  }
}
