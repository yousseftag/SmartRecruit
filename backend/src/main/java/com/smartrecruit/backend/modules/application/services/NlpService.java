package com.smartrecruit.backend.modules.application.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartrecruit.backend.exceptions.ResourceNotFoundException;
import com.smartrecruit.backend.modules.application.dtos.SyncRequestDto;
import com.smartrecruit.backend.modules.application.entities.Application;
import com.smartrecruit.backend.modules.application.entities.Candidate;
import com.smartrecruit.backend.modules.application.entities.CvFile;
import com.smartrecruit.backend.modules.application.repositories.ApplicationRepository;
import com.smartrecruit.backend.modules.application.repositories.CandidateRepository;
import com.smartrecruit.backend.modules.application.repositories.CvFileRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NlpService {

  private final ApplicationRepository applicationRepository;
  private final CvFileRepository cvFileRepository;
  private final CandidateRepository candidateRepository;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Transactional
  public void syncCvData(SyncRequestDto requestDto) {
    Application application =
        applicationRepository
            .findById(requestDto.getApplicationId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Application not found with id: " + requestDto.getApplicationId()));

    CvFile cvFile =
        cvFileRepository
            .findById(requestDto.getCvId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "CvFile not found with id: " + requestDto.getCvId()));

    try {
      TypeReference<Map<String, Object>> mapType = new TypeReference<>() {};
      // Update Application
      BigDecimal totalScore = requestDto.getTotalScore();
      application.setTotalScore(totalScore);
      if (totalScore != null
          && application.getOffer() != null
          && application.getOffer().getMinScore() != null) {
        boolean passed =
            totalScore.compareTo(BigDecimal.valueOf(application.getOffer().getMinScore())) >= 0;
        application.setPassedMinScore(passed);
      } else if (totalScore != null) {
        application.setPassedMinScore(true);
      } else {
        application.setPassedMinScore(null);
      }
      application.setCategoryScores(
          objectMapper.convertValue(requestDto.getCategoryScores(), mapType));
      application.setExtractedMatching(
          objectMapper.convertValue(requestDto.getExtractedMatching(), mapType));
      application.setScoredAt(OffsetDateTime.now());

      // Update CvFile
      cvFile.setExtractedData(objectMapper.convertValue(requestDto.getExtractedData(), mapType));
      cvFile.setExtractionStatus(requestDto.getExtractionStatus());
      cvFile.setProcessedAt(OffsetDateTime.now());

      // Update Candidate if it has empty fields (null-only policy)
      Candidate candidate = application.getCandidate();
      if (candidate != null
          && requestDto.getExtractedData() != null
          && requestDto.getExtractedData().candidateInfo() != null) {
        SyncRequestDto.CandidateInfoDto info = requestDto.getExtractedData().candidateInfo();
        boolean updated = false;

        if ((candidate.getFirstName() == null || candidate.getFirstName().isBlank())
            && info.firstName() != null
            && !info.firstName().isBlank()) {
          candidate.setFirstName(info.firstName());
          updated = true;
        }
        if ((candidate.getLastName() == null || candidate.getLastName().isBlank())
            && info.lastName() != null
            && !info.lastName().isBlank()) {
          candidate.setLastName(info.lastName());
          updated = true;
        }
        if ((candidate.getEmail() == null || candidate.getEmail().isBlank())
            && info.email() != null
            && !info.email().isBlank()) {
          candidate.setEmail(info.email());
          updated = true;
        }
        if ((candidate.getPhone() == null || candidate.getPhone().isBlank())
            && info.phone() != null
            && !info.phone().isBlank()) {
          candidate.setPhone(info.phone());
          updated = true;
        }

        if (updated) {
          candidateRepository.save(candidate);
        }
      }
    } catch (IllegalArgumentException e) {
      throw new IllegalStateException("Failed to parse AI payload into JSONB structures", e);
    }

    applicationRepository.save(application);
    cvFileRepository.save(cvFile);
  }
}
