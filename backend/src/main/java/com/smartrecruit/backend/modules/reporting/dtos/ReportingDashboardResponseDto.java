package com.smartrecruit.backend.modules.reporting.dtos;

import java.util.List;

/** Composite payload returning all analytical views required by the Reporting page. */
public record ReportingDashboardResponseDto(
    CampaignStatsDto kpis,
    List<FunnelStageDto> funnel,
    ScoreDistributionDto scoreDistribution,
    List<CandidateReportRowDto> topCandidates) {}
