package com.smartrecruit.backend.integration.messaging;

import com.smartrecruit.backend.config.RabbitMQConfig;
import com.smartrecruit.backend.modules.offer.entities.Offer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OfferIngestionProducer {

  private final RabbitTemplate rabbitTemplate;

  public void sendOfferForProcessing(Offer offer) {
    Map<String, Object> criterias = new HashMap<>();
    criterias.put("min_score", offer.getMinScore() != null ? offer.getMinScore() : 0);
    criterias.put(
        "category_weights",
        offer.getCategoryWeights() != null ? offer.getCategoryWeights() : Collections.emptyMap());
    criterias.put(
        "category_criteria",
        offer.getCategoryCriteria() != null ? offer.getCategoryCriteria() : Collections.emptyMap());
    criterias.put(
        "description_markdown",
        offer.getDescriptionMarkdown() != null ? offer.getDescriptionMarkdown() : "");
    criterias.put(
        "duration_months", offer.getDurationMonths() != null ? offer.getDurationMonths() : 0);
    criterias.put("contract_type", offer.getContractType() != null ? offer.getContractType() : "");

    Map<String, Object> payload = Map.of("offer_id", offer.getId(), "criterias", criterias);

    log.info("Dispatching Offer AI pre-processing for Offer ID: {}", offer.getId());
    rabbitTemplate.convertAndSend(
        RabbitMQConfig.EXCHANGE, RabbitMQConfig.OFFER_ROUTING_KEY, payload);
  }
}
