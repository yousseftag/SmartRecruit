package com.smartrecruit.backend.modules.offer.repositories;

import com.smartrecruit.backend.modules.offer.entities.Offer;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OfferRepository extends JpaRepository<Offer, UUID> {}
