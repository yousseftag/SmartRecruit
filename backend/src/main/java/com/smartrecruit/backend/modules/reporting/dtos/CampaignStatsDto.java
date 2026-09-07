package com.smartrecruit.backend.modules.reporting.dtos;

/** Analytical KPI metrics for a recruitment campaign or consolidated offers. */
public record CampaignStatsDto(
    long totalApplications,
    long screenedApplications,
    double screenedRate,
    double averageScore,
    double maxScore,
    long qualifiedCount,
    double qualificationRate,
    long hiredCount,
    double conversionRate,
    long rejectedCount) {}
