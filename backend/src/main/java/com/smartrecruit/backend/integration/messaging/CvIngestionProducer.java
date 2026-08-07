package com.smartrecruit.backend.integration.messaging;

import com.smartrecruit.backend.config.RabbitMQConfig;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CvIngestionProducer {

  private final RabbitTemplate rabbitTemplate;

  public void sendCvForProcessing(UUID applicationId, UUID offerId, UUID cvId, String storageKey) {
    Map<String, Object> payload =
        Map.of(
            "application_id", applicationId,
            "offer_id", offerId,
            "cv_id", cvId,
            "storage_key", storageKey);

    log.info("Dispatching CV processing job for Application: {}", applicationId);
    rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.CV_ROUTING_KEY, payload);
  }
}
