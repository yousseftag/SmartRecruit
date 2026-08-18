package com.smartrecruit.backend.modules.application.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartrecruit.backend.modules.application.enums.ExtractionStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.Data;

/**
 * Webhook payload from the FastAPI NLP worker.
 *
 * <p>NOTE: Strict validation is omitted for extracted JSON fields. AI processing is
 * non-deterministic; missing/extra fields will PASS and be saved as partial JSONB. Only core IDs,
 * score bounds (0-100), and exact score math (sum match) will trigger a 400 Bad Request.
 */
@Data
public class SyncRequestDto {

  @NotNull private UUID applicationId;

  @NotNull private UUID offerId;

  @NotNull private UUID cvId;

  @NotNull private ExtractionStatus extractionStatus;

  @Valid private ExtractedDataDto extractedData;

  @Valid private ExtractedMatchingDto extractedMatching;

  @Valid private CategoryScoresDto categoryScores;

  @Min(0)
  @Max(100)
  private BigDecimal totalScore;

  @AssertTrue(
      message =
          "extractionStatus must be either SUCCESS or FAILED for sync callback (cannot be PENDING)")
  public boolean isExtractionStatusTerminal() {
    if (extractionStatus == null) {
      return true; // handled by @NotNull
    }
    return extractionStatus == ExtractionStatus.SUCCESS
        || extractionStatus == ExtractionStatus.FAILED;
  }

  @AssertTrue(message = "Total score must equal the sum of category scores when fully provided")
  public boolean isTotalScoreValid() {
    // If the AI failed to extract the total score or category scores, we accept the partial payload
    if (categoryScores == null || totalScore == null) {
      return true;
    }

    BigDecimal sum = BigDecimal.ZERO;
    if (categoryScores.skills() != null) sum = sum.add(categoryScores.skills());
    if (categoryScores.experience() != null) sum = sum.add(categoryScores.experience());
    if (categoryScores.coursework() != null) sum = sum.add(categoryScores.coursework());
    if (categoryScores.languages() != null) sum = sum.add(categoryScores.languages());
    if (categoryScores.localization() != null) sum = sum.add(categoryScores.localization());

    // If the AI generated SOME category scores, check if they add up to the total score
    return sum.compareTo(totalScore) == 0;
  }

  public record ExtractedDataDto(
      @JsonProperty("candidate_info") CandidateInfoDto candidateInfo,
      @JsonProperty("description_markdown") String descriptionMarkdown,
      List<String> skills,
      Integer experience,
      List<String> coursework,
      List<String> languages,
      String localization) {}

  public record CandidateInfoDto(
      @JsonProperty("first_name") String firstName,
      @JsonProperty("last_name") String lastName,
      String email,
      String phone,
      @JsonProperty("current_job_title") String currentJobTitle) {}

  public record ExtractedMatchingDto(
      @JsonProperty("matched_criteria") MatchedCriteriaDto matchedCriteria,
      List<String> strengths,
      List<String> weaknesses) {}

  public record MatchedCriteriaDto(
      List<String> skills,
      Boolean experience,
      List<String> coursework,
      List<String> languages,
      String localization) {}

  public record CategoryScoresDto(
      BigDecimal skills,
      BigDecimal experience,
      BigDecimal coursework,
      BigDecimal languages,
      BigDecimal localization) {}
}
