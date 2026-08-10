package com.smartrecruit.backend.modules.offer.repositories;

import com.smartrecruit.backend.modules.offer.dtos.OfferSummaryResponse;
import com.smartrecruit.backend.modules.offer.entities.Offer;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OfferRepository extends JpaRepository<Offer, UUID> {
  List<Offer> findByStatus(String status);

  @Query(
      "SELECT new com.smartrecruit.backend.modules.offer.dtos.OfferSummaryResponse(o.id, o.title) FROM Offer o WHERE o.status = 'ACTIVE'")
  List<OfferSummaryResponse> findActiveOfferSummaries();
}
