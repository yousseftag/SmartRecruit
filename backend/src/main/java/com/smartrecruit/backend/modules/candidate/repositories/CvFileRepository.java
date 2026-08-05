package com.smartrecruit.backend.modules.candidate.repositories;

import com.smartrecruit.backend.modules.candidate.entities.CvFile;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CvFileRepository extends JpaRepository<CvFile, UUID> {
  Optional<CvFile> findByChecksumSha256(String checksumSha256);
}
