package com.smartrecruit.backend.modules.reporting.enums;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

public enum ReportingPeriod {
  ALL,
  LAST_30_DAYS,
  LAST_90_DAYS,
  THIS_YEAR;

  /**
   * Computes the beginning timestamp in UTC for this reporting period.
   *
   * @param now Current reference timestamp.
   * @return The starting OffsetDateTime or null if the entire history (ALL) is requested.
   */
  public OffsetDateTime toStartDate(OffsetDateTime now) {
    if (now == null) {
      now = OffsetDateTime.now(ZoneOffset.UTC);
    }
    return switch (this) {
      case LAST_30_DAYS -> now.minusDays(30);
      case LAST_90_DAYS -> now.minusDays(90);
      case THIS_YEAR -> now.withDayOfYear(1).truncatedTo(ChronoUnit.DAYS);
      case ALL -> null;
    };
  }

  /**
   * Safe parser accepting string period identifiers such as "30d", "90d", "1y", "all", falling back
   * to ALL on null or unrecognized values.
   */
  public static ReportingPeriod fromString(String value) {
    if (value == null || value.isBlank()) {
      return ALL;
    }
    String normalized = value.trim().toUpperCase();
    return switch (normalized) {
      case "30D", "LAST_30_DAYS", "30_DAYS" -> LAST_30_DAYS;
      case "90D", "LAST_90_DAYS", "90_DAYS" -> LAST_90_DAYS;
      case "1Y", "THIS_YEAR", "YEAR" -> THIS_YEAR;
      default -> {
        try {
          yield ReportingPeriod.valueOf(normalized);
        } catch (IllegalArgumentException e) {
          yield ALL;
        }
      }
    };
  }
}
