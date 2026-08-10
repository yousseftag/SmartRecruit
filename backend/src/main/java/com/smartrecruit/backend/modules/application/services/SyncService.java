package com.smartrecruit.backend.modules.application.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartrecruit.backend.exceptions.ResourceNotFoundException;
import com.smartrecruit.backend.modules.application.dtos.SyncRequestDto;
import com.smartrecruit.backend.modules.application.entities.Application;
import com.smartrecruit.backend.modules.application.repositories.ApplicationRepository;
import com.smartrecruit.backend.modules.candidate.entities.CvFile;
import com.smartrecruit.backend.modules.candidate.enums.ExtractionStatus;
import com.smartrecruit.backend.modules.candidate.repositories.CvFileRepository;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SyncService {

  private final ApplicationRepository applicationRepository;
  private final CvFileRepository cvFileRepository;
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
      // Update Application
      application.setTotalScore(requestDto.getTotalScore());
      application.setCategoryScores(
          objectMapper.writeValueAsString(requestDto.getCategoryScores()));
      application.setExtractedMatching(
          objectMapper.writeValueAsString(requestDto.getExtractedMatching()));
      application.setScoredAt(OffsetDateTime.now());

      // Update CvFile
      cvFile.setExtractedData(objectMapper.writeValueAsString(requestDto.getExtractedData()));
      cvFile.setExtractionStatus(ExtractionStatus.SUCCESS);
      cvFile.setProcessedAt(OffsetDateTime.now());
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to serialize AI sync payload", e);
    }

    applicationRepository.save(application);
    cvFileRepository.save(cvFile);
  }
}
