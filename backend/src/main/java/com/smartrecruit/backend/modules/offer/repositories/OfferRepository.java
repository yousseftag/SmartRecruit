package com.smartrecruit.backend.modules.offer.repositories;

import com.smartrecruit.backend.modules.offer.dtos.OfferTitleResponse;
import com.smartrecruit.backend.modules.offer.entities.Offer;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OfferRepository extends JpaRepository<Offer, UUID> {
  List<Offer> findByStatus(String status);

  Optional<Offer> findByIdAndStatus(UUID id, String status);

  List<Offer> findAllByOrderByCreatedAtDesc();

  List<Offer> findByStatusInOrderByCreatedAtDesc(List<String> statuses);

  @Query(
      "SELECT new com.smartrecruit.backend.modules.offer.dtos.OfferTitleResponse(o.id, o.title) FROM Offer o WHERE o.status = 'ACTIVE'")
  List<OfferTitleResponse> findActiveOfferTitles();
}
