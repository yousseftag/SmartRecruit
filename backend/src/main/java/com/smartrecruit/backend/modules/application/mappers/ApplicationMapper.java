package com.smartrecruit.backend.modules.application.mappers;

import com.smartrecruit.backend.modules.application.dtos.ApplicationResponse;
import com.smartrecruit.backend.modules.application.dtos.ApplicationSummaryResponse;
import com.smartrecruit.backend.modules.application.dtos.CandidateResponse;
import com.smartrecruit.backend.modules.application.entities.Application;
import com.smartrecruit.backend.modules.application.entities.Candidate;
import java.util.Map;

public class ApplicationMapper {

  public static ApplicationResponse toDto(Application application) {
    if (application == null) return null;

    Map<String, Object> cvExtractedData = null;
    if (application.getCvFile() != null) {
      cvExtractedData = application.getCvFile().getExtractedData();
    }

    String jobTitle = null;
    if (cvExtractedData != null) {
      @SuppressWarnings("unchecked")
      Map<String, Object> candidateInfo =
          (Map<String, Object>) cvExtractedData.get("candidate_info");
      if (candidateInfo != null && candidateInfo.get("current_job_title") != null) {
        jobTitle = candidateInfo.get("current_job_title").toString();
      }
    }

    CandidateResponse candidateResponse = null;
    Candidate candidate = application.getCandidate();
    if (candidate != null) {
      candidateResponse =
          new CandidateResponse(
              candidate.getId(),
              candidate.getFirstName(),
              candidate.getLastName(),
              candidate.getEmail(),
              candidate.getPhone(),
              jobTitle);
    }

    return new ApplicationResponse(
        application.getId(),
        candidateResponse,
        application.getOffer().getId(),
        application.getOffer().getMinScore(),
        application.getStatus(),
        application.getTotalScore(),
        application.getCategoryScores(),
        application.getExtractedMatching(),
        cvExtractedData,
        application.getAppliedAt(),
        application.getScoredAt());
  }

  public static ApplicationSummaryResponse toSummaryDto(Application application) {
    if (application == null) return null;

    String jobTitle = null;
    if (application.getCvFile() != null && application.getCvFile().getExtractedData() != null) {
      @SuppressWarnings("unchecked")
      Map<String, Object> candidateInfo =
          (Map<String, Object>) application.getCvFile().getExtractedData().get("candidate_info");
      if (candidateInfo != null && candidateInfo.get("current_job_title") != null) {
        jobTitle = candidateInfo.get("current_job_title").toString();
      }
    }

    CandidateResponse candidateResponse = null;
    Candidate candidate = application.getCandidate();
    if (candidate != null) {
      candidateResponse =
          new CandidateResponse(
              candidate.getId(),
              candidate.getFirstName(),
              candidate.getLastName(),
              candidate.getEmail(),
              candidate.getPhone(),
              jobTitle);
    }

    return new ApplicationSummaryResponse(
        application.getId(),
        candidateResponse,
        application.getOffer().getId(),
        application.getOffer().getTitle(),
        application.getOffer().getMinScore(),
        application.getStatus(),
        application.getTotalScore(),
        application.getAppliedAt());
  }
}
