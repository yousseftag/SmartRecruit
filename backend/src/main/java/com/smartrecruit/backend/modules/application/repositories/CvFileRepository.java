package com.smartrecruit.backend.modules.application.repositories;

import com.smartrecruit.backend.modules.application.entities.CvFile;
import com.smartrecruit.backend.modules.application.enums.ExtractionStatus;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CvFileRepository extends JpaRepository<CvFile, UUID> {
  Optional<CvFile> findByChecksumSha256(String checksumSha256);

  @Modifying
  @Query(
      "UPDATE CvFile c SET c.extractionStatus = :stalledStatus "
          + "WHERE c.extractionStatus = 'PENDING' AND c.uploadedAt < :cutoff")
  int markPendingJobsAsStalled(
      @Param("stalledStatus") ExtractionStatus stalledStatus,
      @Param("cutoff") OffsetDateTime cutoff);
}
