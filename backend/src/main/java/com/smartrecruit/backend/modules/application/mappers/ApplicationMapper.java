package com.smartrecruit.backend.modules.application.mappers;

import com.smartrecruit.backend.modules.application.dtos.ApplicationResponse;
import com.smartrecruit.backend.modules.application.dtos.ApplicationSummaryResponse;
import com.smartrecruit.backend.modules.application.dtos.CandidateResponse;
import com.smartrecruit.backend.modules.application.entities.Application;
import com.smartrecruit.backend.modules.application.entities.Candidate;

public class ApplicationMapper {

  public static ApplicationResponse toDto(Application application) {
    if (application == null) return null;

    return new ApplicationResponse(
        application.getId(),
        toCandidateResponse(application.getCandidate(), application.getCandidateJobTitle()),
        application.getOfferId(),
        application.getOfferTitle(),
        application.getOfferMinScore(),
        application.getOfferRequiredSkills(),
        application.getStatus(),
        application.getExtractionStatus(),
        application.getCvFileId(),
        application.getCvOriginalFilename(),
        application.getTotalScore(),
        application.getCategoryScores(),
        application.getExtractedMatching(),
        application.getCvExtractedData(),
        application.getAppliedAt(),
        application.getScoredAt());
  }

  public static ApplicationSummaryResponse toSummaryDto(Application application) {
    if (application == null) return null;

    return new ApplicationSummaryResponse(
        application.getId(),
        toCandidateResponse(application.getCandidate(), application.getCandidateJobTitle()),
        application.getOfferId(),
        application.getOfferTitle(),
        application.getOfferMinScore(),
        application.getStatus(),
        application.getExtractionStatus(),
        application.getTotalScore(),
        application.getAppliedAt());
  }

  private static CandidateResponse toCandidateResponse(Candidate candidate, String jobTitle) {
    if (candidate == null) return null;
    return new CandidateResponse(
        candidate.getId(),
        candidate.getFirstName(),
        candidate.getLastName(),
        candidate.getEmail(),
        candidate.getPhone(),
        jobTitle);
  }
}
