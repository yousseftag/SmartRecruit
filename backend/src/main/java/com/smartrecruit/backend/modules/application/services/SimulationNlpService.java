package com.smartrecruit.backend.modules.application.services;

import com.smartrecruit.backend.modules.application.dtos.SyncRequestDto;
import com.smartrecruit.backend.modules.application.entities.Application;
import com.smartrecruit.backend.modules.application.entities.CvFile;
import com.smartrecruit.backend.modules.application.enums.ExtractionStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * TEMPORARY SIMULATION SERVICE FOR MVP This class exists solely to fake the delayed processing of
 * CVs so the frontend can demonstrate its polling and loading states. Once the real Python NLP
 * worker is attached to the RabbitMQ queue, this entire file can be safely deleted.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SimulationNlpService {

  public void simulateNlpProcessing(Application application, CvFile cvFile) {
    CompletableFuture.runAsync(
        () -> {
          try {
            int delaySeconds = 5 + new java.util.Random().nextInt(6); // 5 to 10 seconds
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

            // Pick 3 to 5 random skills
            int numSkills = 3 + rand.nextInt(3);
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

            // 5. Category Scores (randomize between 5.0 and 20.0)
            BigDecimal sSkills = BigDecimal.valueOf(10 + rand.nextInt(11));
            BigDecimal sExp = BigDecimal.valueOf(10 + rand.nextInt(11));
            BigDecimal sCourse = BigDecimal.valueOf(15 + rand.nextInt(6));
            BigDecimal sLang = BigDecimal.valueOf(10 + rand.nextInt(11));
            BigDecimal sLoc = BigDecimal.valueOf(20); // usually matched

            SyncRequestDto.CategoryScoresDto categoryScores =
                new SyncRequestDto.CategoryScoresDto(sSkills, sExp, sCourse, sLang, sLoc);

            BigDecimal totalScore = sSkills.add(sExp).add(sCourse).add(sLang).add(sLoc);

            // Construct the exact request body FastAPI would send
            SyncRequestDto request = new SyncRequestDto();
            request.setApplicationId(application.getId());
            request.setOfferId(application.getOffer().getId());
            request.setCvId(cvFile.getId());
            request.setExtractionStatus(ExtractionStatus.SUCCESS);
            request.setExtractedData(extractedData);
            request.setExtractedMatching(extractedMatching);
            request.setCategoryScores(categoryScores);
            request.setTotalScore(totalScore);

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
