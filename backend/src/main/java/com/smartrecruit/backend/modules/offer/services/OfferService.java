package com.smartrecruit.backend.modules.offer.services;

import com.smartrecruit.backend.exceptions.ResourceNotFoundException;
import com.smartrecruit.backend.integration.messaging.OfferIngestionProducer;
import com.smartrecruit.backend.modules.auth.entities.AppUser;
import com.smartrecruit.backend.modules.auth.repositories.AppUserRepository;
import com.smartrecruit.backend.modules.offer.dtos.CreateOfferRequest;
import com.smartrecruit.backend.modules.offer.entities.Offer;
import com.smartrecruit.backend.modules.offer.enums.OfferAiStatus;
import com.smartrecruit.backend.modules.offer.repositories.OfferRepository;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OfferService {

  private final OfferRepository offerRepository;
  private final AppUserRepository appUserRepository;
  private final OfferIngestionProducer offerIngestionProducer;

  /** Retrieves all job offers ordered by creation date descending for internal HR view. */
  @Transactional(readOnly = true)
  public List<Offer> getAllOffers() {
    return offerRepository.findAllByOrderByCreatedAtDesc();
  }

  /** Finds a specific job offer by UUID or throws ResourceNotFoundException. */
  @Transactional(readOnly = true)
  public Offer getOfferById(UUID id) {
    return offerRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Offre introuvable avec l'id : " + id));
  }

  /** Creates a new job offer in DRAFT state with PENDING AI status. */
  public Offer createOffer(CreateOfferRequest request, Jwt jwt) {
    AppUser user = resolveUser(jwt);

    Offer offer =
        Offer.builder()
            .title(request.title())
            .descriptionMarkdown(request.descriptionMarkdown())
            .status("DRAFT")
            .offerAiStatus(OfferAiStatus.PENDING)
            .categoryWeights(Map.copyOf(request.categoryWeights()))
            .categoryCriteria(
                request.categoryCriteria() != null
                    ? request.categoryCriteria()
                    : Collections.emptyMap())
            .minScore(request.minScore())
            .durationMonths(request.durationMonths())
            .contractType(request.contractType())
            .createdBy(user)
            .updatedBy(user)
            .build();

    log.info("Creating new offer '{}' in DRAFT state", offer.getTitle());
    Offer saved = offerRepository.save(offer);
    offerIngestionProducer.sendOfferForProcessing(saved);
    return saved;
  }

  /** Updates an existing offer. Allowed only if the offer is in DRAFT status. */
  public Offer updateOffer(UUID id, CreateOfferRequest request, Jwt jwt) {
    Offer offer = getOfferById(id);

    if (!"DRAFT".equals(offer.getStatus())) {
      throw new IllegalStateException(
          "Seules les offres en brouillon peuvent être modifiées. Fermez l'offre d'abord pour la modifier.");
    }

    offer.setTitle(request.title());
    offer.setDescriptionMarkdown(request.descriptionMarkdown());
    offer.setCategoryWeights(Map.copyOf(request.categoryWeights()));
    offer.setCategoryCriteria(
        request.categoryCriteria() != null ? request.categoryCriteria() : Collections.emptyMap());
    offer.setMinScore(request.minScore());
    offer.setDurationMonths(request.durationMonths());
    offer.setContractType(request.contractType());
    offer.setUpdatedBy(resolveUser(jwt));

    // Reset AI extraction state and re-dispatch for AI processing
    offer.setOfferAiStatus(OfferAiStatus.PENDING);
    offer.setExtractedRequirements(null);

    log.info("Updated offer '{}' (id: {})", offer.getTitle(), id);
    Offer saved = offerRepository.save(offer);
    offerIngestionProducer.sendOfferForProcessing(saved);
    return saved;
  }

  /** Persists asynchronous AI extraction results and updates AI processing status. */
  public void syncExtractedRequirements(
      UUID offerId, OfferAiStatus aiStatus, Map<String, Object> extractedRequirements) {
    Offer offer = getOfferById(offerId);
    offer.setOfferAiStatus(aiStatus);
    if (extractedRequirements != null) {
      offer.setExtractedRequirements(extractedRequirements);
    }
    offerRepository.save(offer);
    log.info(
        "Persisted AI extraction sync for Offer {}: Status={}, Requirements={}",
        offerId,
        aiStatus,
        extractedRequirements != null ? "present" : "null");
  }

  private AppUser resolveUser(Jwt jwt) {
    if (jwt == null) {
      return null;
    }
    String sub = jwt.getSubject();
    return appUserRepository.findByKeycloakSub(sub).orElse(null);
  }
}
