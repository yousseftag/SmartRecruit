package com.smartrecruit.backend.modules.reporting.repositories;

import com.smartrecruit.backend.modules.application.entities.Application;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportingRepository extends JpaRepository<Application, UUID> {

  /**
   * highly-performant single-pass query aggregating core campaign KPIs using PostgreSQL FILTER
   * clauses to avoid in-memory loops.
   */
  @Query(
      value =
          """
          SELECT
            COUNT(*) as total_apps,
            COUNT(*) FILTER (WHERE a.total_score IS NOT NULL) as screened_apps,
            COALESCE(ROUND(AVG(a.total_score) FILTER (WHERE a.total_score IS NOT NULL), 1), 0) as avg_score,
            COALESCE(MAX(a.total_score), 0) as max_score,
            COUNT(*) FILTER (WHERE a.passed_min_score = true) as qualified_apps,
            COUNT(*) FILTER (WHERE a.status = 'HIRED') as hired_apps,
            COUNT(*) FILTER (WHERE a.status = 'REJECTED') as rejected_apps
          FROM application a
          WHERE (:offerId IS NULL OR a.offer_id = :offerId)
            AND (CAST(:startDate AS TIMESTAMPTZ) IS NULL OR a.applied_at >= :startDate)
          """,
      nativeQuery = true)
  List<Object[]> fetchCampaignAggregates(
      @Param("offerId") UUID offerId, @Param("startDate") OffsetDateTime startDate);

  /**
   * Aggregates conversion milestones across the recruitment pipeline in a single round-trip query.
   */
  @Query(
      value =
          """
          SELECT
            COUNT(*) as total_received,
            COUNT(*) FILTER (WHERE a.passed_min_score = true) as qualified_ai,
            COUNT(DISTINCT a.id) FILTER (
              WHERE a.status IN ('SHORTLISTED', 'INTERVIEWING', 'HIRED')
                 OR EXISTS (SELECT 1 FROM workflow_status_history w WHERE w.application_id = a.id AND w.to_status = 'SHORTLISTED')
            ) as shortlisted_count,
            COUNT(DISTINCT a.id) FILTER (
              WHERE a.status IN ('INTERVIEWING', 'HIRED')
                 OR EXISTS (SELECT 1 FROM workflow_status_history w WHERE w.application_id = a.id AND w.to_status = 'INTERVIEWING')
            ) as interviewing_count,
            COUNT(*) FILTER (WHERE a.status = 'HIRED') as hired_count
          FROM application a
          WHERE (:offerId IS NULL OR a.offer_id = :offerId)
            AND (CAST(:startDate AS TIMESTAMPTZ) IS NULL OR a.applied_at >= :startDate)
          """,
      nativeQuery = true)
  List<Object[]> fetchFunnelCounts(
      @Param("offerId") UUID offerId, @Param("startDate") OffsetDateTime startDate);

  /** Groups candidates into 4 AI score tiers in a single query pass. */
  @Query(
      value =
          """
          SELECT
            COUNT(*) FILTER (WHERE a.total_score >= 85) as excellent_count,
            COUNT(*) FILTER (WHERE a.total_score >= 70 AND a.total_score < 85) as qualified_count,
            COUNT(*) FILTER (WHERE a.total_score >= 50 AND a.total_score < 70) as moderate_count,
            COUNT(*) FILTER (WHERE a.total_score < 50) as insufficient_count
          FROM application a
          WHERE (:offerId IS NULL OR a.offer_id = :offerId)
            AND (CAST(:startDate AS TIMESTAMPTZ) IS NULL OR a.applied_at >= :startDate)
          """,
      nativeQuery = true)
  List<Object[]> fetchScoreDistribution(
      @Param("offerId") UUID offerId, @Param("startDate") OffsetDateTime startDate);

  /**
   * Fetches only the 12 columns required for candidate ranking and exports via a native projection,
   * completely bypassing heavy entity hydration and unused JSONB columns.
   */
  @Query(
      value =
          """
          SELECT
            a.id as application_id,
            c.id as candidate_id,
            c.first_name,
            c.last_name,
            c.email,
            c.phone,
            o.title as offer_title,
            a.total_score,
            a.passed_min_score,
            a.status,
            a.applied_at,
            a.category_scores::text as category_scores_json
          FROM application a
          JOIN candidate c ON a.candidate_id = c.id
          JOIN offer o ON a.offer_id = o.id
          WHERE (:offerId IS NULL OR a.offer_id = :offerId)
            AND (CAST(:startDate AS TIMESTAMPTZ) IS NULL OR a.applied_at >= :startDate)
          ORDER BY a.total_score DESC NULLS LAST, a.applied_at ASC
          """,
      nativeQuery = true)
  List<Object[]> fetchRankedCandidateRows(
      @Param("offerId") UUID offerId,
      @Param("startDate") OffsetDateTime startDate,
      Pageable pageable);
}
