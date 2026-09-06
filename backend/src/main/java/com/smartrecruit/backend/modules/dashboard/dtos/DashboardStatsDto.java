package com.smartrecruit.backend.modules.dashboard.dtos;

public record DashboardStatsDto(
    long activeOffers,
    long newApplications,
    double aiValidationRate,
    double cvExtractionRate,
    long activeCandidates,
    long hiredCandidates,
    double hiringSuccessRate,
    long rejectedCandidates) {}
