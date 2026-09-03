package com.smartrecruit.backend.modules.offer.services;

import com.smartrecruit.backend.config.RabbitMQConfig;
import com.smartrecruit.backend.exceptions.ResourceNotFoundException;
import com.smartrecruit.backend.modules.offer.enums.OfferAiStatus;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Development & testing simulation service for asynchronous AI offer pre-processing. Consumes tasks
 * from offer.processing.queue, simulates NLP vectorization latency (2-3s), and syncs results back
 * directly to OfferService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
    name = "app.ai-simulation.enabled",
    havingValue = "true",
    matchIfMissing = true)
public class SimulationOfferService {

  private final OfferService offerService;
  private final AtomicInteger requestCounter = new AtomicInteger(0);
  private final Random random = new Random();

  @RabbitListener(queues = RabbitMQConfig.OFFER_QUEUE)
  public void processOfferTask(Map<String, Object> payload) {
    if (payload == null || !payload.containsKey("offer_id")) {
      log.warn("Received invalid offer processing task payload: {}", payload);
      return;
    }

    UUID offerId =
        payload.get("offer_id") instanceof UUID u
            ? u
            : UUID.fromString(payload.get("offer_id").toString());

    int reqNum = requestCounter.incrementAndGet();
    try {
      int delaySeconds = 2 + random.nextInt(2); // 2 to 3 seconds simulation delay
      log.info(
          "Simulating AI analysis for Offer ID: {} ({}s delay, request #{})",
          offerId,
          delaySeconds,
          reqNum);
      TimeUnit.SECONDS.sleep(delaySeconds);

      // Deterministic simulation: 9 out of 10 succeed, 10th fails to allow testing failure flow
      boolean isSuccess = (reqNum % 10) != 0;
      OfferAiStatus aiStatus = isSuccess ? OfferAiStatus.SUCCESS : OfferAiStatus.FAILED;

      Map<String, Object> extractedRequirements =
          isSuccess
              ? Map.of(
                  "missing_from_criteria",
                  List.of("Docker", "CI/CD", "Agile / Scrum"),
                  "insights",
                  "L'analyse NLP a identifié des compétences secondaires dans la description du poste non renseignées dans les critères obligatoires.")
              : null;

      offerService.syncExtractedRequirements(offerId, aiStatus, extractedRequirements);
      log.info("Offer simulation callback complete -> Offer: {}, Outcome: {}", offerId, aiStatus);

    } catch (ResourceNotFoundException e) {
      log.warn(
          "Offer {} no longer exists during AI simulation processing: {}", offerId, e.getMessage());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.error("Simulation sleep interrupted for Offer: {}", offerId, e);
    } catch (Exception e) {
      log.error("Simulation failed for Offer: {}", offerId, e);
    }
  }
}
