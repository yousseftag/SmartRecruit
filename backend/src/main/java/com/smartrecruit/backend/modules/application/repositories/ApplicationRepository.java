package com.smartrecruit.backend.modules.application.repositories;

import com.smartrecruit.backend.modules.application.dtos.ApplicationStatusResponse;
import com.smartrecruit.backend.modules.application.entities.Application;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {
  Optional<Application> findByCandidateIdAndOfferId(UUID candidateId, UUID offerId);

  @Query(
      "SELECT new com.smartrecruit.backend.modules.application.dtos.ApplicationStatusResponse(a.id, c.extractionStatus) "
          + "FROM Application a JOIN a.cvFile c WHERE a.id = :id")
  Optional<ApplicationStatusResponse> findStatusById(@Param("id") UUID id);
}
