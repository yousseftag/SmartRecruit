package com.smartrecruit.backend.modules.workflow.entities;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "email_template")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailTemplate {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "template_key", nullable = false)
  @Builder.Default
  private String templateKey = "OTHER";

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "subject", nullable = false)
  private String subject;

  @Column(name = "body_html", columnDefinition = "TEXT", nullable = false)
  private String bodyHtml;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;
}
