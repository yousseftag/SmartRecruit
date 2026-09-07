package com.smartrecruit.backend.modules.reporting.dtos;

/** Metric milestone representation for the recruitment conversion funnel. */
public record FunnelStageDto(String stage, long count, double percentage) {}
