package com.smartrecruit.backend.modules.offer.repositories;

import com.smartrecruit.backend.modules.offer.entities.Offer;
import com.smartrecruit.backend.modules.offer.enums.OfferStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OfferRepository extends JpaRepository<Offer, UUID> {

  long countByStatus(OfferStatus status);

  @Query(
      """
      SELECT o FROM Offer o
      WHERE o.status = 'ACTIVE'
      ORDER BY (
          SELECT COUNT(a) FROM Application a
          WHERE a.offer = o AND a.status = 'NEW'
      ) DESC
      LIMIT 5
      """)
  List<Offer> findTop5ActiveByNewApplicationCount();
}
