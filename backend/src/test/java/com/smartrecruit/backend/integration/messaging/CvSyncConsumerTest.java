package com.smartrecruit.backend.integration.messaging;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.smartrecruit.backend.exceptions.ResourceNotFoundException;
import com.smartrecruit.backend.modules.application.dtos.SyncRequestDto;
import com.smartrecruit.backend.modules.application.enums.ExtractionStatus;
import com.smartrecruit.backend.modules.application.services.NlpService;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CvSyncConsumerTest {

  private NlpService nlpService;
  private CvSyncConsumer cvSyncConsumer;

  @BeforeEach
  void setUp() {
    nlpService = mock(NlpService.class);
    cvSyncConsumer = new CvSyncConsumer(nlpService);
  }

  @Test
  void shouldDelegateToNlpServiceWhenMessageReceived() {
    SyncRequestDto requestDto = new SyncRequestDto();
    requestDto.setApplicationId(UUID.randomUUID());
    requestDto.setOfferId(UUID.randomUUID());
    requestDto.setCvId(UUID.randomUUID());
    requestDto.setExtractionStatus(ExtractionStatus.SUCCESS);
    requestDto.setTotalScore(BigDecimal.valueOf(85));

    cvSyncConsumer.consumeCvSyncResult(requestDto);

    verify(nlpService).syncCvData(requestDto);
  }

  @Test
  void shouldGracefullyHandleResourceNotFoundExceptionWithoutThrowing() {
    SyncRequestDto requestDto = new SyncRequestDto();
    UUID nonExistentAppId = UUID.randomUUID();
    requestDto.setApplicationId(nonExistentAppId);

    doThrow(new ResourceNotFoundException("Application not found with id: " + nonExistentAppId))
        .when(nlpService)
        .syncCvData(requestDto);

    assertDoesNotThrow(() -> cvSyncConsumer.consumeCvSyncResult(requestDto));
    verify(nlpService).syncCvData(requestDto);
  }

  @Test
  void shouldIgnoreNullOrEmptyPayload() {
    assertDoesNotThrow(() -> cvSyncConsumer.consumeCvSyncResult(null));
    assertDoesNotThrow(() -> cvSyncConsumer.consumeCvSyncResult(new SyncRequestDto()));

    verify(nlpService, never()).syncCvData(org.mockito.ArgumentMatchers.any());
  }
}
