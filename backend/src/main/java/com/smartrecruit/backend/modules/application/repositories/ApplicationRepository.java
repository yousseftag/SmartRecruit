package com.smartrecruit.backend.modules.application.repositories;

import com.smartrecruit.backend.modules.application.dtos.ApplicationExtractionStatusResponse;
import com.smartrecruit.backend.modules.application.entities.Application;
import com.smartrecruit.backend.modules.application.enums.ApplicationStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {

  @Query(
      "SELECT a FROM Application a WHERE a.candidate.id = :candidateId AND a.offer.id = :offerId")
  Optional<Application> findByCandidateIdAndOfferId(
      @Param("candidateId") UUID candidateId, @Param("offerId") UUID offerId);

  @Query("SELECT a FROM Application a WHERE a.offer.id = :offerId")
  List<Application> findByOfferId(@Param("offerId") UUID offerId);

  /**
   * Optimized JPQL Projection: Instead of fetching the entire Application and CV entity (which
   * loads heavy JSONB data into memory), this directly creates a lightweight DTO containing only
   * the ID and NLP Extraction Status. Used by the UI for fast polling.
   */
  @Query(
      "SELECT new com.smartrecruit.backend.modules.application.dtos.ApplicationExtractionStatusResponse("
          + "a.id, "
          + "CASE "
          + "  WHEN c.extractionStatus = com.smartrecruit.backend.modules.application.enums.ExtractionStatus.FAILED THEN com.smartrecruit.backend.modules.application.enums.ExtractionStatus.FAILED "
          + "  WHEN c.extractionStatus = com.smartrecruit.backend.modules.application.enums.ExtractionStatus.STALLED THEN com.smartrecruit.backend.modules.application.enums.ExtractionStatus.STALLED "
          + "  WHEN a.totalScore IS NOT NULL OR a.scoredAt IS NOT NULL THEN com.smartrecruit.backend.modules.application.enums.ExtractionStatus.SUCCESS "
          + "  ELSE com.smartrecruit.backend.modules.application.enums.ExtractionStatus.PENDING "
          + "END) "
          + "FROM Application a JOIN a.cvFile c WHERE a.id = :id")
  Optional<ApplicationExtractionStatusResponse> findExtractionStatusById(@Param("id") UUID id);

  @Query(
      value =
          """
                    SELECT
                      (SELECT COUNT(*) FROM offer WHERE status = 'ACTIVE') as active_offers,
                      COUNT(*) FILTER (WHERE a.status = 'NEW') as new_applications,
                      COUNT(*) FILTER (WHERE a.status = 'NEW' AND a.passed_min_score = true) as new_passed_ai,
                      COUNT(c.id) FILTER (WHERE a.status = 'NEW' AND c.extraction_status = 'SUCCESS') as new_extracted_cvs,
                      COUNT(*) FILTER (WHERE a.status IN ('SHORTLISTED', 'INTERVIEWING', 'FOLLOW_UP')) as active_candidates,
                      COUNT(*) FILTER (WHERE a.status = 'HIRED') as hired_candidates,
                      COUNT(*) FILTER (WHERE a.status = 'REJECTED') as rejected_candidates
                    FROM application a
                    LEFT JOIN cv_file c ON a.cv_file_id = c.id
                    """,
      nativeQuery = true)
  List<Object[]> fetchDashboardAggregates();

  @Query(
      value =
          """
          SELECT TO_CHAR(applied_at AT TIME ZONE 'UTC', 'YYYY-MM-DD') as date_str, COUNT(*) as count
          FROM application
          WHERE (applied_at AT TIME ZONE 'UTC')::date >= (CURRENT_TIMESTAMP AT TIME ZONE 'UTC')::date - INTERVAL '6 days'
          GROUP BY TO_CHAR(applied_at AT TIME ZONE 'UTC', 'YYYY-MM-DD')
          ORDER BY date_str ASC
          """,
      nativeQuery = true)
  List<Object[]> countApplicationsByDay();

  @Query("SELECT COUNT(a) FROM Application a WHERE a.offer.id = :offerId AND a.status = :status")
  long countByOfferIdAndStatus(
      @Param("offerId") UUID offerId, @Param("status") ApplicationStatus status);

  @Query(
      "SELECT COUNT(a) FROM Application a WHERE a.offer.id = :offerId AND a.status = :status AND a.passedMinScore = true")
  long countByOfferIdAndStatusAndPassedMinScoreTrue(
      @Param("offerId") UUID offerId, @Param("status") ApplicationStatus status);
}
