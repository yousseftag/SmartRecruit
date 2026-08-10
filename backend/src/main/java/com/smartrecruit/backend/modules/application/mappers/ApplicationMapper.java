package com.smartrecruit.backend.modules.application.mappers;

import com.smartrecruit.backend.modules.application.dtos.ApplicationResponse;
import com.smartrecruit.backend.modules.application.dtos.CandidateResponse;
import com.smartrecruit.backend.modules.application.entities.Application;
import com.smartrecruit.backend.modules.application.entities.Candidate;

public class ApplicationMapper {

  public static ApplicationResponse toDto(Application application) {
    if (application == null) return null;

    CandidateResponse candidateResponse = null;
    Candidate candidate = application.getCandidate();
    if (candidate != null) {
      candidateResponse =
          new CandidateResponse(
              candidate.getId(),
              candidate.getFirstName(),
              candidate.getLastName(),
              candidate.getEmail(),
              candidate.getPhone());
    }

    com.fasterxml.jackson.databind.JsonNode cvExtractedData = null;
    if (application.getCvFile() != null) {
      cvExtractedData = application.getCvFile().getExtractedData();
    }

    return new ApplicationResponse(
        application.getId(),
        candidateResponse,
        application.getOffer() != null ? application.getOffer().getId() : null,
        application.getStatus(),
        application.getTotalScore(),
        application.getCategoryScores(),
        application.getExtractedMatching(),
        cvExtractedData,
        application.getAppliedAt(),
        application.getScoredAt());
  }
}
