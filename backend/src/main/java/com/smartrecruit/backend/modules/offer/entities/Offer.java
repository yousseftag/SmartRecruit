package com.smartrecruit.backend.modules.offer.entities;

import com.smartrecruit.backend.modules.auth.entities.AppUser;
import jakarta.persistence.*;
import java.util.Map;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
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

  @Column(name = "status", nullable = false)
  @Builder.Default
  private String status = "DRAFT";

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "category_weights", columnDefinition = "jsonb", nullable = false)
  private Map<String, Object> categoryWeights;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "category_criteria", columnDefinition = "jsonb")
  private Map<String, Object> categoryCriteria;

  @Column(name = "min_score")
  private Integer minScore;

  @Column(name = "duration_months")
  private Integer durationMonths;

  @Column(name = "contract_type")
  private String contractType;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "extracted_requirements", columnDefinition = "jsonb")
  private Map<String, Object> extractedRequirements;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private java.time.OffsetDateTime createdAt;

  @org.hibernate.annotations.UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private java.time.OffsetDateTime updatedAt;
}
