package com.smartrecruit.backend.modules.application.repositories;

import com.smartrecruit.backend.modules.application.entities.Application;
import com.smartrecruit.backend.modules.application.enums.ApplicationStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {
  Optional<Application> findByCandidateIdAndOfferId(UUID candidateId, UUID offerId);

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
      SELECT TO_CHAR(applied_at, 'YYYY-MM-DD') as date_str, COUNT(*) as count
      FROM application
      WHERE applied_at >= CURRENT_DATE - INTERVAL '6 days'
      GROUP BY TO_CHAR(applied_at, 'YYYY-MM-DD')
      ORDER BY date_str ASC
      """,
      nativeQuery = true)
  List<Object[]> countApplicationsByDay();

  long countByOfferIdAndStatus(UUID offerId, ApplicationStatus status);

  long countByOfferIdAndStatusAndPassedMinScoreTrue(UUID offerId, ApplicationStatus status);
}
