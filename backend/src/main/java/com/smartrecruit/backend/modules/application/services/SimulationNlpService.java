package com.smartrecruit.backend.modules.application.services;

import com.smartrecruit.backend.modules.application.dtos.SyncRequestDto;
import com.smartrecruit.backend.modules.application.entities.Application;
import com.smartrecruit.backend.modules.application.entities.CvFile;
import com.smartrecruit.backend.modules.application.enums.ExtractionStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * TEMPORARY SIMULATION SERVICE FOR MVP This class simulates asynchronous AI NLP extraction. It uses
 * a deterministic 2-Success / 1-Fail cycle (Hit 1: Success, Hit 2: Success, Hit 3: Failed) for
 * predictable testing of ingestion, failure states, and retry logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SimulationNlpService {

  private final AtomicInteger requestCounter = new AtomicInteger(0);

  public void simulateNlpProcessing(Application application, CvFile cvFile) {
    CompletableFuture.runAsync(
        () -> {
          try {
            int delaySeconds = 4 + new java.util.Random().nextInt(4); // 4 to 7 seconds
            log.info(
                "Simulating NLP extraction for CV {}, waiting {} seconds...",
                cvFile.getId(),
                delaySeconds);
            TimeUnit.SECONDS.sleep(delaySeconds);

            java.util.Random rand = new java.util.Random();

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
            List<String> selectedSkills = new java.util.ArrayList<>();
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
                    fName.toLowerCase()
                        + "."
                        + lName.toLowerCase()
                        + rand.nextInt(999)
                        + "@example.com",
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
                (requestIndex % 3)
                    != 0; // Hits 1, 2, 4, 5, 7... -> SUCCESS. Hits 3, 6, 9... -> FAILED.

            log.info(
                "Simulated AI Request #{} for CV {} -> Outcome: {}",
                requestIndex,
                cvFile.getId(),
                isSuccess ? "SUCCESS (Score: " + totalScore + ")" : "FAILED");

            // Construct the exact request body FastAPI would send
            SyncRequestDto request = new SyncRequestDto();
            request.setApplicationId(application.getId());
            request.setOfferId(application.getOffer().getId());
            request.setCvId(cvFile.getId());

            if (isSuccess) {
              request.setExtractionStatus(ExtractionStatus.SUCCESS);
              request.setExtractedData(extractedData);
              request.setExtractedMatching(extractedMatching);
              request.setCategoryScores(categoryScores);
              request.setTotalScore(totalScore);
            } else {
              request.setExtractionStatus(ExtractionStatus.FAILED);
            }

            log.info("Simulation sending webhook to POST /api/v1/internal/cv/sync");
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.postForEntity(
                "http://localhost:8080/api/v1/internal/cv/sync", request, Void.class);

          } catch (Exception e) {
            log.error("Simulation failed", e);
          }
        });
  }
}
