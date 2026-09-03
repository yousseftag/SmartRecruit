package com.smartrecruit.backend.modules.offer.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.smartrecruit.backend.exceptions.ResourceNotFoundException;
import com.smartrecruit.backend.integration.messaging.OfferIngestionProducer;
import com.smartrecruit.backend.modules.auth.entities.AppUser;
import com.smartrecruit.backend.modules.auth.entities.UserRole;
import com.smartrecruit.backend.modules.auth.repositories.AppUserRepository;
import com.smartrecruit.backend.modules.offer.dtos.CreateOfferRequest;
import com.smartrecruit.backend.modules.offer.entities.Offer;
import com.smartrecruit.backend.modules.offer.enums.OfferAiStatus;
import com.smartrecruit.backend.modules.offer.repositories.OfferRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
class OfferServiceTest {

  @Mock private OfferRepository offerRepository;
  @Mock private AppUserRepository appUserRepository;
  @Mock private OfferIngestionProducer offerIngestionProducer;
  @Mock private Jwt jwt;

  @InjectMocks private OfferService offerService;

  private AppUser mockUser;
  private CreateOfferRequest validRequest;
  private final UUID offerId = UUID.randomUUID();
  private final String keycloakSub = "sub-recruiter-123";

  @BeforeEach
  void setUp() {
    mockUser =
        AppUser.builder()
            .id(UUID.randomUUID())
            .keycloakSub(keycloakSub)
            .username("recruiter")
            .email("recruiter@smartrecruit.com")
            .firstName("Jane")
            .lastName("Recruiter")
            .role(UserRole.RECRUITER)
            .build();

    validRequest =
        new CreateOfferRequest(
            "Senior Backend Engineer",
            "Job description markdown",
            Map.of(
                "skills",
                40,
                "experience",
                30,
                "coursework",
                10,
                "languages",
                10,
                "localization",
                10),
            Map.of("skills", List.of("Java", "Spring Boot")),
            75,
            12,
            "CDI");
  }

  // --- CREATE OFFER TESTS ---

  @Test
  void testCreateOffer_WithAuthenticatedUser_ShouldSaveDraftWithPendingAi() {
    when(jwt.getSubject()).thenReturn(keycloakSub);
    when(appUserRepository.findByKeycloakSub(keycloakSub)).thenReturn(Optional.of(mockUser));
    when(offerRepository.save(any(Offer.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Offer created = offerService.createOffer(validRequest, jwt);

    assertNotNull(created);
    assertEquals("Senior Backend Engineer", created.getTitle());
    assertEquals("DRAFT", created.getStatus());
    assertEquals(OfferAiStatus.PENDING, created.getOfferAiStatus());
    assertEquals(mockUser, created.getCreatedBy());
    assertEquals(mockUser, created.getUpdatedBy());
    assertEquals(75, created.getMinScore());
    verify(offerRepository).save(any(Offer.class));
    verify(offerIngestionProducer).sendOfferForProcessing(any(Offer.class));
  }

  @Test
  void testCreateOffer_WithoutJwt_ShouldSaveDraftWithNullCreator() {
    when(offerRepository.save(any(Offer.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Offer created = offerService.createOffer(validRequest, null);

    assertNotNull(created);
    assertEquals("DRAFT", created.getStatus());
    assertEquals(OfferAiStatus.PENDING, created.getOfferAiStatus());
    assertNull(created.getCreatedBy());
    assertNull(created.getUpdatedBy());
    verify(offerRepository).save(any(Offer.class));
    verify(offerIngestionProducer).sendOfferForProcessing(any(Offer.class));
  }

  // --- GET OFFERS TESTS ---

  @Test
  void testGetAllOffers_ShouldReturnListFromRepository() {
    Offer offer1 = Offer.builder().id(UUID.randomUUID()).title("Offer 1").build();
    Offer offer2 = Offer.builder().id(UUID.randomUUID()).title("Offer 2").build();
    when(offerRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(offer1, offer2));

    List<Offer> results = offerService.getAllOffers();

    assertEquals(2, results.size());
    verify(offerRepository).findAllByOrderByCreatedAtDesc();
  }

  @Test
  void testGetOfferById_WhenExists_ShouldReturnOffer() {
    Offer offer = Offer.builder().id(offerId).title("Offer 1").build();
    when(offerRepository.findById(offerId)).thenReturn(Optional.of(offer));

    Offer result = offerService.getOfferById(offerId);

    assertNotNull(result);
    assertEquals(offerId, result.getId());
    verify(offerRepository).findById(offerId);
  }

  @Test
  void testGetOfferById_WhenNotFound_ShouldThrowResourceNotFoundException() {
    when(offerRepository.findById(offerId)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> offerService.getOfferById(offerId));
    verify(offerRepository).findById(offerId);
  }

  // --- UPDATE OFFER TESTS ---

  @Test
  void testUpdateOffer_WhenDraft_ShouldUpdateFieldsAndSave() {
    Offer existingOffer =
        Offer.builder()
            .id(offerId)
            .title("Old Title")
            .status("DRAFT")
            .offerAiStatus(OfferAiStatus.PENDING)
            .build();

    when(offerRepository.findById(offerId)).thenReturn(Optional.of(existingOffer));
    when(jwt.getSubject()).thenReturn(keycloakSub);
    when(appUserRepository.findByKeycloakSub(keycloakSub)).thenReturn(Optional.of(mockUser));
    when(offerRepository.save(any(Offer.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Offer updated = offerService.updateOffer(offerId, validRequest, jwt);

    assertNotNull(updated);
    assertEquals("Senior Backend Engineer", updated.getTitle());
    assertEquals(mockUser, updated.getUpdatedBy());
    assertEquals(OfferAiStatus.PENDING, updated.getOfferAiStatus());
    verify(offerRepository).save(existingOffer);
    verify(offerIngestionProducer).sendOfferForProcessing(existingOffer);
  }

  @Test
  void testUpdateOffer_WhenActive_ShouldThrowIllegalStateException() {
    Offer activeOffer = Offer.builder().id(offerId).title("Active Offer").status("ACTIVE").build();
    when(offerRepository.findById(offerId)).thenReturn(Optional.of(activeOffer));

    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class,
            () -> offerService.updateOffer(offerId, validRequest, jwt));

    assertEquals(
        "Seules les offres en brouillon peuvent être modifiées. Fermez l'offre d'abord pour la modifier.",
        exception.getMessage());
  }

  @Test
  void testUpdateOffer_WhenClosed_ShouldThrowIllegalStateException() {
    Offer closedOffer = Offer.builder().id(offerId).title("Closed Offer").status("CLOSED").build();
    when(offerRepository.findById(offerId)).thenReturn(Optional.of(closedOffer));

    assertThrows(
        IllegalStateException.class, () -> offerService.updateOffer(offerId, validRequest, jwt));
  }

  // --- SYNC EXTRACTED REQUIREMENTS TESTS ---

  @Test
  void testSyncExtractedRequirements_WhenSuccess_ShouldUpdateStatusAndRequirements() {
    Offer offer = Offer.builder().id(offerId).offerAiStatus(OfferAiStatus.PENDING).build();
    when(offerRepository.findById(offerId)).thenReturn(Optional.of(offer));
    when(offerRepository.save(any(Offer.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Map<String, Object> requirements = Map.of("missing_from_criteria", List.of("Docker"));
    offerService.syncExtractedRequirements(offerId, OfferAiStatus.SUCCESS, requirements);

    assertEquals(OfferAiStatus.SUCCESS, offer.getOfferAiStatus());
    assertEquals(requirements, offer.getExtractedRequirements());
    verify(offerRepository).save(offer);
  }

  @Test
  void testSyncExtractedRequirements_WhenFailed_ShouldUpdateStatus() {
    Offer offer = Offer.builder().id(offerId).offerAiStatus(OfferAiStatus.PENDING).build();
    when(offerRepository.findById(offerId)).thenReturn(Optional.of(offer));
    when(offerRepository.save(any(Offer.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    offerService.syncExtractedRequirements(offerId, OfferAiStatus.FAILED, null);

    assertEquals(OfferAiStatus.FAILED, offer.getOfferAiStatus());
    assertNull(offer.getExtractedRequirements());
    verify(offerRepository).save(offer);
  }
}
