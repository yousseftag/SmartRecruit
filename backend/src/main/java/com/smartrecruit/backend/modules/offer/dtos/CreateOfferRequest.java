package com.smartrecruit.backend.modules.offer.dtos;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.Objects;

public record CreateOfferRequest(
    @NotBlank(message = "Le titre de l'offre est obligatoire") String title,
    String descriptionMarkdown,
    @NotNull(message = "Les poids des catégories sont obligatoires")
        Map<String, Integer> categoryWeights,
    Map<String, Object> categoryCriteria,
    Integer minScore,
    Integer durationMonths,
    String contractType) {

  @AssertTrue(message = "Les poids des catégories doivent totaliser exactement 100")
  public boolean isCategoryWeightsValid() {
    if (categoryWeights == null || categoryWeights.isEmpty()) {
      return false;
    }
    return categoryWeights.values().stream()
            .filter(Objects::nonNull)
            .mapToInt(Integer::intValue)
            .sum()
        == 100;
  }
}
