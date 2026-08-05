package com.smartrecruit.backend.modules.application.repositories;

import com.smartrecruit.backend.modules.application.entities.Application;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {
  Optional<Application> findByCandidateIdAndOfferId(UUID candidateId, UUID offerId);
}
