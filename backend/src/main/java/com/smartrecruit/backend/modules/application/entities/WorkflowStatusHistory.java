package com.smartrecruit.backend.modules.application.entities;

import com.smartrecruit.backend.modules.application.enums.ApplicationStatus;
import com.smartrecruit.backend.modules.auth.entities.AppUser;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "workflow_status_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowStatusHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "application_id", nullable = false)
  private Application application;

  @Enumerated(EnumType.STRING)
  @Column(name = "from_status")
  private ApplicationStatus fromStatus;

  @Enumerated(EnumType.STRING)
  @Column(name = "to_status", nullable = false)
  private ApplicationStatus toStatus;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "changed_by")
  private AppUser changedBy;

  @CreationTimestamp
  @Column(name = "changed_at", nullable = false, updatable = false)
  private OffsetDateTime changedAt;
}
