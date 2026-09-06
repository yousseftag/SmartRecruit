package com.smartrecruit.backend.modules.offer.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.smartrecruit.backend.modules.offer.enums.OfferAiStatus;
import com.smartrecruit.backend.modules.offer.repositories.OfferRepository;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OfferExtractionStallDetectorTest {

  @Mock private OfferRepository offerRepository;

  @InjectMocks private OfferExtractionStallDetector stallDetector;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(stallDetector, "stallThresholdMinutes", 5L);
  }

  @Test
  void testMarkStalledOffers_ShouldQueryRepositoryWithCutoff() {
    when(offerRepository.markPendingOffersAsStalled(
            eq(OfferAiStatus.STALLED), any(OffsetDateTime.class)))
        .thenReturn(2);

    stallDetector.markStalledOffers();

    verify(offerRepository)
        .markPendingOffersAsStalled(eq(OfferAiStatus.STALLED), any(OffsetDateTime.class));
  }
}
