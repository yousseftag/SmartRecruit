package com.smartrecruit.backend.modules.offer.repositories;

import com.smartrecruit.backend.modules.offer.dtos.OfferTitleResponse;
import com.smartrecruit.backend.modules.offer.entities.Offer;
import com.smartrecruit.backend.modules.offer.enums.OfferAiStatus;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

  @Modifying
  @Query(
      "UPDATE Offer o SET o.offerAiStatus = :stalledStatus "
          + "WHERE o.offerAiStatus = 'PENDING' AND o.updatedAt < :cutoff")
  int markPendingOffersAsStalled(
      @Param("stalledStatus") OfferAiStatus stalledStatus, @Param("cutoff") OffsetDateTime cutoff);
}
