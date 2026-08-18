package com.smartrecruit.backend.modules.application.repositories;

import com.smartrecruit.backend.modules.application.dtos.ApplicationExtractionStatusResponse;
import com.smartrecruit.backend.modules.application.entities.Application;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {
  Optional<Application> findByCandidateIdAndOfferId(UUID candidateId, UUID offerId);

  List<Application> findByOfferId(UUID offerId);

  /**
   * Optimized JPQL Projection: Instead of fetching the entire Application and CV entity (which
   * loads heavy JSONB data into memory), this directly creates a lightweight DTO containing only
   * the ID and NLP Extraction Status. Used by the UI for fast polling.
   */
  @Query(
      "SELECT new com.smartrecruit.backend.modules.application.dtos.ApplicationExtractionStatusResponse(a.id, c.extractionStatus) "
          + "FROM Application a JOIN a.cvFile c WHERE a.id = :id")
  Optional<ApplicationExtractionStatusResponse> findExtractionStatusById(@Param("id") UUID id);
}
