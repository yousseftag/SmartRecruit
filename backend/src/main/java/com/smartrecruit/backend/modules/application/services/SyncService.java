package com.smartrecruit.backend.modules.application.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartrecruit.backend.exceptions.ResourceNotFoundException;
import com.smartrecruit.backend.modules.application.dtos.SyncRequestDto;
import com.smartrecruit.backend.modules.application.entities.Application;
import com.smartrecruit.backend.modules.application.entities.Candidate;
import com.smartrecruit.backend.modules.application.entities.CvFile;
import com.smartrecruit.backend.modules.application.enums.ExtractionStatus;
import com.smartrecruit.backend.modules.application.repositories.ApplicationRepository;
import com.smartrecruit.backend.modules.application.repositories.CandidateRepository;
import com.smartrecruit.backend.modules.application.repositories.CvFileRepository;
import java.time.OffsetDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SyncService {

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
      application.setTotalScore(requestDto.getTotalScore());
      application.setCategoryScores(
          objectMapper.convertValue(requestDto.getCategoryScores(), mapType));
      application.setExtractedMatching(
          objectMapper.convertValue(requestDto.getExtractedMatching(), mapType));
      application.setScoredAt(OffsetDateTime.now());

      // Update CvFile
      cvFile.setExtractedData(objectMapper.convertValue(requestDto.getExtractedData(), mapType));
      cvFile.setExtractionStatus(ExtractionStatus.SUCCESS);
      cvFile.setProcessedAt(OffsetDateTime.now());

      // Update Candidate if it's a stub
      Candidate candidate = application.getCandidate();
      if (candidate.getEmail() == null
          && requestDto.getExtractedData() != null
          && requestDto.getExtractedData().candidateInfo() != null) {
        SyncRequestDto.CandidateInfoDto info = requestDto.getExtractedData().candidateInfo();
        if (info.email() != null) {
          candidateRepository
              .findByEmail(info.email())
              .ifPresentOrElse(
                  existing -> {
                    application.setCandidate(existing);
                    cvFile.setCandidate(existing);
                    if (existing.getPhone() == null && info.phone() != null) {
                      existing.setPhone(info.phone());
                    }
                    candidateRepository.save(existing);
                    // We leave the empty stub candidate orphaned rather than risking a
                    // ConstraintViolation deleting it here
                  },
                  () -> {
                    candidate.setFirstName(info.firstName());
                    candidate.setLastName(info.lastName());
                    candidate.setEmail(info.email());
                    candidate.setPhone(info.phone());
                    candidateRepository.save(candidate);
                  });
        }
      }
    } catch (IllegalArgumentException e) {
      throw new RuntimeException("Failed to serialize AI sync payload", e);
    }

    applicationRepository.save(application);
    cvFileRepository.save(cvFile);
  }
}
