package com.smartrecruit.backend.modules.application.entities;

import com.smartrecruit.backend.modules.application.enums.ExtractionStatus;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "cv_file")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CvFile {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "candidate_id", nullable = false)
  private Candidate candidate;

  @Column(name = "storage_key", nullable = false)
  private String storageKey;

  @Column(name = "original_filename", nullable = false)
  private String originalFilename;

  @Column(name = "checksum_sha256", nullable = false, unique = true)
  private String checksumSha256;

  @Enumerated(EnumType.STRING)
  @Column(name = "extraction_status", nullable = false)
  @Builder.Default
  private ExtractionStatus extractionStatus = ExtractionStatus.PENDING;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "extracted_data", columnDefinition = "jsonb")
  private Map<String, Object> extractedData;

  @CreationTimestamp
  @Column(name = "uploaded_at", nullable = false, updatable = false)
  private OffsetDateTime uploadedAt;

  @Column(name = "processed_at")
  private OffsetDateTime processedAt;
}
