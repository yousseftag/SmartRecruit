package com.smartrecruit.backend.modules.offer.entities;

import com.smartrecruit.backend.modules.auth.entities.AppUser;
import com.smartrecruit.backend.modules.offer.enums.OfferStatus;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "offer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Offer {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by")
  private AppUser createdBy;

  @Column(name = "title", nullable = false)
  private String title;

  @Column(name = "description_markdown")
  private String descriptionMarkdown;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  @Builder.Default
  private OfferStatus status = OfferStatus.DRAFT;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "category_weights", columnDefinition = "jsonb", nullable = false)
  private String categoryWeights;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "category_criteria", columnDefinition = "jsonb")
  private String categoryCriteria;

  @Column(name = "min_score")
  private Integer minScore;

  @Column(name = "duration_months")
  private Integer durationMonths;

  @Column(name = "contract_type")
  private String contractType;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "extracted_requirements", columnDefinition = "jsonb")
  private String extractedRequirements;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "updated_by")
  private AppUser updatedBy;
}
