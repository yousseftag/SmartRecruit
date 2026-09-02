package com.smartrecruit.backend.integration.messaging;

import com.smartrecruit.backend.config.RabbitMQConfig;
import com.smartrecruit.backend.exceptions.ResourceNotFoundException;
import com.smartrecruit.backend.modules.application.dtos.SyncRequestDto;
import com.smartrecruit.backend.modules.application.services.NlpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ consumer for asynchronous AI CV extraction and scoring sync results. Ensures resilient
 * processing even if the backend was temporarily unavailable.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CvSyncConsumer {

  private final NlpService nlpService;

  @RabbitListener(queues = RabbitMQConfig.CV_SYNC_QUEUE)
  public void consumeCvSyncResult(SyncRequestDto requestDto) {
    if (requestDto == null || requestDto.getApplicationId() == null) {
      log.warn("Received empty or invalid CV sync payload: {}", requestDto);
      return;
    }

    log.info(
        "Received asynchronous CV sync result via RabbitMQ for Application ID: {}",
        requestDto.getApplicationId());

    try {
      nlpService.syncCvData(requestDto);
    } catch (ResourceNotFoundException e) {
      log.warn(
          "Dropping orphan CV sync result: Application or CV file no longer exists in database ({})",
          e.getMessage());
    }
  }
}
