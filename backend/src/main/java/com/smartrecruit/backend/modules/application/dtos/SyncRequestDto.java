package com.smartrecruit.backend.modules.application.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class SyncRequestDto {

  @NotNull private UUID applicationId;

  @NotNull private UUID offerId;

  @NotNull private UUID cvId;

  @Valid private ExtractedDataDto extractedData;

  @Valid private ExtractedMatchingDto extractedMatching;

  @Valid private CategoryScoresDto categoryScores;

  @NotNull
  @Min(0)
  @Max(100)
  private BigDecimal totalScore;

  @AssertTrue(message = "Total score must equal the sum of category scores")
  public boolean isTotalScoreValid() {
    if (categoryScores == null || totalScore == null) {
      return true; // Let @NotNull handle missing fields
    }

    BigDecimal sum = BigDecimal.ZERO;
    if (categoryScores.skills() != null) sum = sum.add(categoryScores.skills());
    if (categoryScores.experience() != null) sum = sum.add(categoryScores.experience());
    if (categoryScores.coursework() != null) sum = sum.add(categoryScores.coursework());
    if (categoryScores.languages() != null) sum = sum.add(categoryScores.languages());
    if (categoryScores.localization() != null) sum = sum.add(categoryScores.localization());

    // We use compareTo to ignore scale differences (e.g. 100.0 vs 100)
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
      String phone) {}

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
