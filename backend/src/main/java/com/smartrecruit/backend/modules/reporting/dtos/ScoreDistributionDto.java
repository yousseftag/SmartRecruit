package com.smartrecruit.backend.modules.reporting.dtos;

/** Breakdown of candidate volume grouped into qualitative AI score tiers. */
public record ScoreDistributionDto(
    long excellentCount, long qualifiedCount, long moderateCount, long insufficientCount) {}
