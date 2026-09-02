package com.smartrecruit.backend.modules.offer.entities;

import com.smartrecruit.backend.modules.auth.entities.AppUser;
import com.smartrecruit.backend.modules.offer.enums.OfferAiStatus;
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
import jakarta.persistence.Table;
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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "updated_by")
  private AppUser updatedBy;

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

  @Enumerated(EnumType.STRING)
  @Column(name = "offer_ai_status", nullable = false)
  @Builder.Default
  private OfferAiStatus offerAiStatus = OfferAiStatus.PENDING;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "extracted_requirements", columnDefinition = "jsonb")
  private Map<String, Object> extractedRequirements;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  public String getLocalization() {
    if (categoryCriteria != null && categoryCriteria.get("localization") instanceof String s) {
      return s;
    }
    return null;
  }

  public Integer getExperience() {
    if (categoryCriteria != null && categoryCriteria.get("experience") instanceof Number n) {
      return n.intValue();
    }
    return null;
  }

  public List<String> getRequiredSkills() {
    if (categoryCriteria != null && categoryCriteria.get("skills") instanceof List<?> list) {
      return list.stream().map(Object::toString).toList();
    }
    return Collections.emptyList();
  }
}
