package com.smartrecruit.backend.modules.application.entities;

import com.smartrecruit.backend.modules.application.enums.ApplicationStatus;
import com.smartrecruit.backend.modules.application.enums.ExtractionStatus;
import com.smartrecruit.backend.modules.offer.entities.Offer;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(
    name = "application",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_application_candidate_offer",
          columnNames = {"candidate_id", "offer_id"})
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Application {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "candidate_id", nullable = false)
  private Candidate candidate;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "offer_id", nullable = false)
  private Offer offer;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cv_file_id", nullable = false)
  private CvFile cvFile;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  @Builder.Default
  private ApplicationStatus status = ApplicationStatus.NEW;

  @Column(name = "total_score", precision = 5, scale = 2)
  private BigDecimal totalScore;

  @Column(name = "passed_min_score")
  private Boolean passedMinScore;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "category_scores", columnDefinition = "jsonb")
  private Map<String, Object> categoryScores;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "extracted_matching", columnDefinition = "jsonb")
  private Map<String, Object> extractedMatching;

  @Column(name = "scored_at")
  private OffsetDateTime scoredAt;

  @CreationTimestamp
  @Column(name = "applied_at", nullable = false, updatable = false)
  private OffsetDateTime appliedAt;

  public String getOfferTitle() {
    return offer != null ? offer.getTitle() : null;
  }

  public UUID getOfferId() {
    return offer != null ? offer.getId() : null;
  }

  public Integer getOfferMinScore() {
    return offer != null ? offer.getMinScore() : null;
  }

  public List<String> getOfferRequiredSkills() {
    return offer != null ? offer.getRequiredSkills() : Collections.emptyList();
  }

  public ExtractionStatus getExtractionStatus() {
    if (cvFile == null) return null;
    if (cvFile.getExtractionStatus() == ExtractionStatus.FAILED) return ExtractionStatus.FAILED;
    if (cvFile.getExtractionStatus() == ExtractionStatus.STALLED) return ExtractionStatus.STALLED;
    if (this.totalScore != null || this.scoredAt != null) return ExtractionStatus.SUCCESS;
    return ExtractionStatus.PENDING;
  }

  public UUID getCvFileId() {
    return cvFile != null ? cvFile.getId() : null;
  }

  public String getCvOriginalFilename() {
    return cvFile != null ? cvFile.getOriginalFilename() : null;
  }

  public Map<String, Object> getCvExtractedData() {
    return cvFile != null ? cvFile.getExtractedData() : null;
  }

  public String getCandidateJobTitle() {
    return cvFile != null ? cvFile.getCurrentJobTitle() : null;
  }
}
