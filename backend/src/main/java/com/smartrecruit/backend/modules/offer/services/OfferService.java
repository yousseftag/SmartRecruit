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

    // Reset AI extraction state and re-dispatch for AI processing;
    // previous extractedRequirements are preserved as a safe backup until new results arrive.
    offer.setOfferAiStatus(OfferAiStatus.PENDING);

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

  /** Publishes an offer to ACTIVE status. Only allowed from DRAFT with SUCCESS AI status. */
  public Offer publishOffer(UUID id, Jwt jwt) {
    Offer offer = getOfferById(id);

    if (!"DRAFT".equals(offer.getStatus())) {
      throw new IllegalStateException("Seule une offre en brouillon peut être publiée.");
    }

    if (offer.getOfferAiStatus() == OfferAiStatus.PENDING) {
      throw new IllegalStateException(
          "L'offre est en cours d'analyse par l'IA. Veuillez patienter avant de la publier.");
    }

    if (offer.getOfferAiStatus() != OfferAiStatus.SUCCESS) {
      throw new IllegalStateException(
          "L'analyse IA de l'offre n'a pas réussi. Veuillez relancer l'analyse avant de publier.");
    }

    offer.setStatus("ACTIVE");
    offer.setUpdatedBy(resolveUser(jwt));
    log.info("Published offer '{}' (id: {})", offer.getTitle(), id);
    return offerRepository.save(offer);
  }

  /** Closes an ACTIVE offer. */
  public Offer closeOffer(UUID id, Jwt jwt) {
    Offer offer = getOfferById(id);

    if (!"ACTIVE".equals(offer.getStatus())) {
      throw new IllegalStateException("Seule une offre active peut être fermée.");
    }

    offer.setStatus("CLOSED");
    offer.setUpdatedBy(resolveUser(jwt));
    log.info("Closed offer '{}' (id: {})", offer.getTitle(), id);
    return offerRepository.save(offer);
  }

  /** Reopens a CLOSED offer back to ACTIVE. */
  public Offer reopenOffer(UUID id, Jwt jwt) {
    Offer offer = getOfferById(id);

    if (!"CLOSED".equals(offer.getStatus())) {
      throw new IllegalStateException("Seule une offre fermée peut être réouverte.");
    }

    offer.setStatus("ACTIVE");
    offer.setUpdatedBy(resolveUser(jwt));
    log.info("Reopened offer '{}' (id: {})", offer.getTitle(), id);
    return offerRepository.save(offer);
  }

  /**
   * Re-triggers AI analysis on a DRAFT offer. Allowed when AI status is SUCCESS, FAILED, or
   * STALLED.
   */
  public Offer reprocessOffer(UUID id, Jwt jwt) {
    Offer offer = getOfferById(id);

    if (!"DRAFT".equals(offer.getStatus())) {
      throw new IllegalStateException("Seules les offres en brouillon peuvent être réanalysées.");
    }

    if (offer.getOfferAiStatus() == OfferAiStatus.PENDING) {
      throw new IllegalStateException("L'analyse IA est déjà en cours.");
    }

    // Reset AI extraction state and re-dispatch for AI processing;
    // previous extractedRequirements are preserved as a safe backup until new results arrive.
    offer.setOfferAiStatus(OfferAiStatus.PENDING);
    offer.setUpdatedBy(resolveUser(jwt));

    log.info("Triggered AI reprocessing for offer '{}' (id: {})", offer.getTitle(), id);
    Offer saved = offerRepository.save(offer);
    offerIngestionProducer.sendOfferForProcessing(saved);
    return saved;
  }

  private AppUser resolveUser(Jwt jwt) {
    if (jwt == null) {
      return null;
    }
    String sub = jwt.getSubject();
    return appUserRepository.findByKeycloakSub(sub).orElse(null);
  }
}
