package com.smartrecruit.backend.modules.offer.services;

import com.smartrecruit.backend.config.RabbitMQConfig;
import com.smartrecruit.backend.exceptions.ResourceNotFoundException;
import com.smartrecruit.backend.modules.offer.entities.Offer;
import com.smartrecruit.backend.modules.offer.enums.OfferAiStatus;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
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
 *
 * <p>Implements a deterministic 2-Success / 1-Fail cycle matching CV processing, and generates
 * dynamic, context-aware secondary skills and AI insights based on the offer domain.
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

  // Domain-specific skills pools for realistic NLP extraction
  private static final List<String> CLOUD_DEVOPS_SKILLS =
      List.of(
          "Docker",
          "Kubernetes",
          "Terraform",
          "Ansible",
          "CI/CD (GitHub Actions / GitLab CI)",
          "Prometheus & Grafana",
          "Helm",
          "AWS CloudWatch",
          "Linux Shell");

  private static final List<String> BACKEND_JAVA_SKILLS =
      List.of(
          "Apache Kafka",
          "Redis",
          "PostgreSQL",
          "Architecture Microservices",
          "Spring Cloud",
          "GraphQL",
          "Keycloak / OAuth2",
          "RabbitMQ",
          "JUnit 5 & Testcontainers");

  private static final List<String> FRONTEND_WEB_SKILLS =
      List.of(
          "TypeScript",
          "Tailwind CSS",
          "State Management (NgRx / Signals)",
          "Next.js",
          "Jest & Cypress",
          "Vite",
          "Performance Web & SEO");

  private static final List<String> DATA_AI_SKILLS =
      List.of(
          "PyTorch",
          "LangChain",
          "HuggingFace Transformers",
          "FastAPI",
          "Bases Vectorielles (Chroma/Pinecone)",
          "MLflow",
          "Apache Spark",
          "Pandas & NumPy");

  private static final List<String> METHODOLOGY_SKILLS =
      List.of(
          "Méthodologie Agile / Scrum",
          "Clean Code & TDD",
          "Code Review",
          "Architecture Hexagonale",
          "Conception d'APIs REST",
          "SonarQube");

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

      // Deterministic 2-Success / 1-Fail cycle matching CV processing (SimulationNlpService)
      // Hits 1, 2 -> SUCCESS. Hit 3 -> FAILED. Hits 4, 5 -> SUCCESS. Hit 6 -> FAILED.
      boolean isSuccess = (reqNum % 3) != 0;
      OfferAiStatus aiStatus = isSuccess ? OfferAiStatus.SUCCESS : OfferAiStatus.FAILED;

      Map<String, Object> extractedRequirements = null;
      if (isSuccess) {
        extractedRequirements = generateContextualRequirements(offerId);
      }

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

  /**
   * Generates dynamic, domain-aware secondary skills and AI insights based on the offer title,
   * description, and existing criteria (excluding skills already entered as mandatory).
   */
  private Map<String, Object> generateContextualRequirements(UUID offerId) {
    try {
      Offer offer = offerService.getOfferById(offerId);
      String title = (offer.getTitle() != null ? offer.getTitle() : "").toLowerCase();
      String desc =
          (offer.getDescriptionMarkdown() != null ? offer.getDescriptionMarkdown() : "")
              .toLowerCase();
      String combinedText = title + " " + desc;

      // Identify already selected skills to avoid duplicates
      Set<String> existingSkills = new HashSet<>();
      if (offer.getCategoryCriteria() != null
          && offer.getCategoryCriteria().containsKey("skills")) {
        Object skillsObj = offer.getCategoryCriteria().get("skills");
        if (skillsObj instanceof List<?> list) {
          for (Object s : list) {
            if (s != null) {
              existingSkills.add(s.toString().toLowerCase().trim());
            }
          }
        }
      }

      // Assemble domain-relevant candidate pool based on keywords
      List<String> pool = new ArrayList<>();
      if (combinedText.contains("devops")
          || combinedText.contains("cloud")
          || combinedText.contains("infra")
          || combinedText.contains("sre")
          || combinedText.contains("kubernetes")
          || combinedText.contains("aws")) {
        pool.addAll(CLOUD_DEVOPS_SKILLS);
      }

      if (combinedText.contains("java")
          || combinedText.contains("spring")
          || combinedText.contains("backend")
          || combinedText.contains("microservice")
          || combinedText.contains("api")) {
        pool.addAll(BACKEND_JAVA_SKILLS);
      }

      if (combinedText.contains("angular")
          || combinedText.contains("react")
          || combinedText.contains("frontend")
          || combinedText.contains("front")
          || combinedText.contains("web")
          || combinedText.contains("full-stack")
          || combinedText.contains("fullstack")) {
        pool.addAll(FRONTEND_WEB_SKILLS);
      }

      if (combinedText.contains("data")
          || combinedText.contains("ia")
          || combinedText.contains("ai")
          || combinedText.contains("nlp")
          || combinedText.contains("llm")
          || combinedText.contains("python")
          || combinedText.contains("machine learning")) {
        pool.addAll(DATA_AI_SKILLS);
      }

      // Always include methodology skills as secondary candidate options
      pool.addAll(METHODOLOGY_SKILLS);

      // Filter out skills already in mandatory criteria
      List<String> available = new ArrayList<>();
      for (String candidate : pool) {
        if (!existingSkills.contains(candidate.toLowerCase().trim())
            && !available.contains(candidate)) {
          available.add(candidate);
        }
      }

      // Fallback if pool emptied
      if (available.isEmpty()) {
        available.addAll(List.of("Git / GitHub", "Docker", "Agile / Scrum", "Clean Code"));
      }

      // Pick 2 to 4 randomized skills
      Collections.shuffle(available, random);
      int pickCount = Math.min(available.size(), 2 + random.nextInt(3)); // 2 to 4
      List<String> selectedSkills = new ArrayList<>(available.subList(0, Math.max(1, pickCount)));

      // Randomized professional NLP analytical insights
      String[] insightTemplates = {
        "Analyse sémantique finalisée : Détection d'exigences implicites d'outillage ("
            + selectedSkills.size()
            + " compétences secondaires identifiées). Espace vectoriel calibré à 95.4% de confiance pour le matching des candidatures.",
        "Modélisation vectorielle validée : La description du poste met en exergue des pratiques d'ingénierie collaborative et de rigueur méthodologique. Compétences d'accompagnement suggérées : "
            + String.join(", ", selectedSkills)
            + ".",
        "Extraction NLP complétée : Pondération optimisée des critères clés. Des compétences périphériques non renseignées dans les critères obligatoires ont été relevées dans le texte ("
            + String.join(", ", selectedSkills)
            + ").",
        "Analyse contextuelle du poste réussie : Profil d'ingénierie orienté production et scalabilité. Le pipeline d'inférence sémantique est calibré pour évaluer la pertinence des candidatures.",
        "Prétraitement IA achevé : Cartographie des compétences secondaires alignée avec le descriptif du poste. Indexation sémantique opérationnelle."
      };
      String selectedInsight = insightTemplates[random.nextInt(insightTemplates.length)];

      return Map.of(
          "missing_from_criteria", selectedSkills,
          "insights", selectedInsight);

    } catch (Exception e) {
      log.warn(
          "Error generating contextual requirements, falling back to default pool: {}",
          e.getMessage());
      return Map.of(
          "missing_from_criteria",
          List.of("Docker", "CI/CD", "Agile / Scrum"),
          "insights",
          "L'analyse NLP a identifié des compétences secondaires dans la description du poste.");
    }
  }
}
