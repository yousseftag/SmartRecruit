package com.smartrecruit.backend.modules.application.entities;

import com.fasterxml.jackson.databind.JsonNode;
import com.smartrecruit.backend.modules.application.enums.ApplicationStatus;
import com.smartrecruit.backend.modules.offer.entities.Offer;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;
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

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "category_scores", columnDefinition = "jsonb")
  private JsonNode categoryScores;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "extracted_matching", columnDefinition = "jsonb")
  private JsonNode extractedMatching;

  @Column(name = "scored_at")
  private OffsetDateTime scoredAt;

  @CreationTimestamp
  @Column(name = "applied_at", nullable = false, updatable = false)
  private OffsetDateTime appliedAt;
}
