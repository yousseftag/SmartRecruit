package com.smartrecruit.backend.modules.offer.repositories;

import com.smartrecruit.backend.modules.offer.dtos.OfferTitleResponse;
import com.smartrecruit.backend.modules.offer.entities.Offer;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OfferRepository extends JpaRepository<Offer, UUID> {

  List<Offer> findByStatus(String status);

  Optional<Offer> findByIdAndStatus(UUID id, String status);

  @Query(
      "SELECT new com.smartrecruit.backend.modules.offer.dtos.OfferTitleResponse(o.id, o.title) FROM Offer o WHERE o.status = 'ACTIVE'")
  List<OfferTitleResponse> findActiveOfferTitles();

  long countByStatus(String status);

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

  @Query("SELECT o FROM Offer o ORDER BY o.updatedAt DESC LIMIT :limit")
  List<Offer> findLatestOffers(@Param("limit") int limit);
}
