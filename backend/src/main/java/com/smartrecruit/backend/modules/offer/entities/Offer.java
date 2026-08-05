package com.smartrecruit.backend.modules.offer.entities;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

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

  // Minimal stub for Feature 3 to compile. Feature 2 dev will add the rest.
}
