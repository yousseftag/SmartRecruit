package com.smartrecruit.backend.modules.dashboard.entities;

import com.smartrecruit.backend.modules.application.entities.Application;
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

  @Column(name = "from_status")
  private String fromStatus;

  @Column(name = "to_status", nullable = false)
  private String toStatus;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "changed_by")
  private AppUser changedBy;

  @CreationTimestamp
  @Column(name = "changed_at", nullable = false, updatable = false)
  private OffsetDateTime changedAt;
}
