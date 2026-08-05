package com.smartrecruit.backend.modules.candidate.repositories;

import com.smartrecruit.backend.modules.candidate.entities.Candidate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, UUID> {
  Optional<Candidate> findByEmail(String email);
}
