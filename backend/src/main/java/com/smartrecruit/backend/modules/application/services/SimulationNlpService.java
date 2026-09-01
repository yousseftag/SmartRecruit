package com.smartrecruit.backend.modules.application.services;

import com.smartrecruit.backend.config.RabbitMQConfig;
import com.smartrecruit.backend.modules.application.dtos.SyncRequestDto;
import com.smartrecruit.backend.modules.application.enums.ExtractionStatus;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * TEMPORARY SIMULATION SERVICE FOR MVP This class simulates asynchronous AI NLP extraction. It
 * consumes tasks from cv.processing.queue, uses a deterministic 2-Success / 1-Fail cycle, and
 * publishes simulated results to cv.sync.queue.
 */
@Service
@Slf4j
@ConditionalOnProperty(
    name = "app.ai-simulation.enabled",
    havingValue = "true",
    matchIfMissing = true)
public class SimulationNlpService {

  private final RabbitTemplate rabbitTemplate;
  private final AtomicInteger requestCounter;

  public SimulationNlpService(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
    this.requestCounter = new AtomicInteger(0);
  }

  @RabbitListener(queues = RabbitMQConfig.CV_QUEUE)
  public void processCvTask(Map<String, Object> payload) {
    if (payload == null || !payload.containsKey("application_id")) {
      log.warn("Received invalid or empty CV processing task payload: {}", payload);
      return;
    }

    UUID applicationId =
        payload.get("application_id") instanceof UUID u
            ? u
            : UUID.fromString(payload.get("application_id").toString());
    UUID offerId =
        payload.get("offer_id") instanceof UUID u
            ? u
            : payload.get("offer_id") != null
                ? UUID.fromString(payload.get("offer_id").toString())
                : null;
    UUID cvId =
        payload.get("cv_id") instanceof UUID u
            ? u
            : payload.get("cv_id") != null
                ? UUID.fromString(payload.get("cv_id").toString())
                : null;

    try {
      Random rand = new Random();
      int delaySeconds = 6 + rand.nextInt(4); // 6 to 9 seconds realistic AI latency
      log.info(
          "Simulating realistic AI extraction for CV {}, processing for {} seconds...",
          cvId,
          delaySeconds);
      TimeUnit.SECONDS.sleep(delaySeconds);

      String[] firstNames = {
        "Ahmed", "Sara", "Youssef", "Fatima", "Omar", "Amina", "Mehdi", "Khadija"
      };
      String[] lastNames = {
        "Alaoui", "Bennani", "Tazi", "El Fassi", "Chraibi", "Amrani", "Benali", "El Idrissi"
      };
      String[] titles = {
        "Développeur Backend",
        "Développeur Full-Stack",
        "Ingénieur DevOps",
        "Tech Lead",
        "Data Scientist",
        "Software Engineer"
      };
      String[] skillsPool = {
        "java",
        "spring boot",
        "angular",
        "react",
        "postgresql",
        "docker",
        "kubernetes",
        "aws",
        "python",
        "fastapi"
      };

      String fName = firstNames[rand.nextInt(firstNames.length)];
      String lName = lastNames[rand.nextInt(lastNames.length)];
      String title = titles[rand.nextInt(titles.length)];

      // Pick 3 to 6 random skills
      int numSkills = 3 + rand.nextInt(4);
      List<String> selectedSkills = new ArrayList<>();
      while (selectedSkills.size() < numSkills) {
        String skill = skillsPool[rand.nextInt(skillsPool.length)];
        if (!selectedSkills.contains(skill)) {
          selectedSkills.add(skill);
        }
      }

      // 1. Candidate Info
      SyncRequestDto.CandidateInfoDto candidateInfo =
          new SyncRequestDto.CandidateInfoDto(
              fName,
              lName,
              fName.toLowerCase() + "." + lName.toLowerCase() + rand.nextInt(999) + "@example.com",
              "+2126" + (10000000 + rand.nextInt(90000000)),
              title);

      // 2. Extracted Data
      int expMonths = 12 + rand.nextInt(72);
      SyncRequestDto.ExtractedDataDto extractedData =
          new SyncRequestDto.ExtractedDataDto(
              candidateInfo,
              "Candidat simulé avec expérience en " + String.join(", ", selectedSkills) + ".",
              selectedSkills,
              expMonths,
              List.of("master", "bac+5"),
              List.of("English", "French"),
              "Maroc");

      // 3. Matched Criteria
      SyncRequestDto.MatchedCriteriaDto matchedCriteria =
          new SyncRequestDto.MatchedCriteriaDto(
              selectedSkills.subList(0, Math.max(1, selectedSkills.size() - 1)),
              expMonths >= 24,
              List.of("master"),
              List.of("French"),
              "Maroc");

      // 4. Extracted Matching
      SyncRequestDto.ExtractedMatchingDto extractedMatching =
          new SyncRequestDto.ExtractedMatchingDto(
              matchedCriteria,
              List.of("Bonne expérience technique", "Correspond au profil recherché"),
              List.of("Manque d'expérience sur certains outils cloud"));

      // 5. Category Scores: realistic spread between [5.0, 20.0]
      BigDecimal sSkills = BigDecimal.valueOf(6 + rand.nextInt(15)); // [6, 20]
      BigDecimal sExp = BigDecimal.valueOf(5 + rand.nextInt(16)); // [5, 20]
      BigDecimal sCourse = BigDecimal.valueOf(10 + rand.nextInt(11)); // [10, 20]
      BigDecimal sLang = BigDecimal.valueOf(8 + rand.nextInt(13)); // [8, 20]
      BigDecimal sLoc = BigDecimal.valueOf(rand.nextBoolean() ? 20 : 12); // 20 or 12

      SyncRequestDto.CategoryScoresDto categoryScores =
          new SyncRequestDto.CategoryScoresDto(sSkills, sExp, sCourse, sLang, sLoc);

      BigDecimal totalScore = sSkills.add(sExp).add(sCourse).add(sLang).add(sLoc);

      // 6. Deterministic 2-Success / 1-Fail Cycle
      int requestIndex = requestCounter.incrementAndGet();
      boolean isSuccess =
          (requestIndex % 3) != 0; // Hits 1, 2, 4, 5, 7... -> SUCCESS. Hits 3, 6, 9... -> FAILED.

      log.info(
          "Simulated AI Request #{} for CV {} -> Outcome: {}",
          requestIndex,
          cvId,
          isSuccess ? "SUCCESS (Score: " + totalScore + ")" : "FAILED");

      // Construct the exact request body FastAPI would send
      SyncRequestDto request = new SyncRequestDto();
      request.setApplicationId(applicationId);
      request.setOfferId(offerId);
      request.setCvId(cvId);

      if (isSuccess) {
        request.setExtractionStatus(ExtractionStatus.SUCCESS);
        request.setExtractedData(extractedData);
        request.setExtractedMatching(extractedMatching);
        request.setCategoryScores(categoryScores);
        request.setTotalScore(totalScore);
      } else {
        request.setExtractionStatus(ExtractionStatus.FAILED);
      }

      log.info(
          "Simulation publishing sync result to RabbitMQ queue '{}'", RabbitMQConfig.CV_SYNC_QUEUE);
      rabbitTemplate.convertAndSend(
          RabbitMQConfig.EXCHANGE, RabbitMQConfig.CV_SYNC_ROUTING_KEY, request);

    } catch (Exception e) {
      log.error("Simulation failed for CV {}", cvId, e);
    }
  }
}
