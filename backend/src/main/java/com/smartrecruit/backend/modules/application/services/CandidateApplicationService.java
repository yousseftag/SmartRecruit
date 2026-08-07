package com.smartrecruit.backend.modules.application.services;

import com.smartrecruit.backend.exceptions.DuplicateResourceException;
import com.smartrecruit.backend.exceptions.ResourceNotFoundException;
import com.smartrecruit.backend.integration.messaging.CvIngestionProducer;
import com.smartrecruit.backend.integration.storage.FileStorageService;
import com.smartrecruit.backend.modules.application.dtos.ApplyRequest;
import com.smartrecruit.backend.modules.application.entities.Application;
import com.smartrecruit.backend.modules.application.enums.ApplicationStatus;
import com.smartrecruit.backend.modules.application.repositories.ApplicationRepository;
import com.smartrecruit.backend.modules.candidate.entities.Candidate;
import com.smartrecruit.backend.modules.candidate.entities.CvFile;
import com.smartrecruit.backend.modules.candidate.enums.ExtractionStatus;
import com.smartrecruit.backend.modules.candidate.repositories.CandidateRepository;
import com.smartrecruit.backend.modules.candidate.repositories.CvFileRepository;
import com.smartrecruit.backend.modules.offer.entities.Offer;
import com.smartrecruit.backend.modules.offer.repositories.OfferRepository;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class CandidateApplicationService {

  private final OfferRepository offerRepository;
  private final CandidateRepository candidateRepository;
  private final ApplicationRepository applicationRepository;
  private final CvFileRepository cvFileRepository;
  private final FileStorageService fileStorageService;
  private final CvIngestionProducer cvIngestionProducer;

  @Transactional
  public void applyToOffer(ApplyRequest request) {
    // 1. Validate Offer
    Offer offer =
        offerRepository
            .findById(request.offerId())
            .orElseThrow(() -> new ResourceNotFoundException("Job offer not found."));

    // 2. Find or Create Candidate
    Candidate candidate =
        candidateRepository
            .findByEmail(request.email())
            .map(
                existing -> {
                  // Candidate exists: overwrite personal info with the latest submission (if not
                  // null)
                  existing.setFirstName(request.firstName());
                  existing.setLastName(request.lastName());
                  if (request.phone() != null && !request.phone().isBlank()) {
                    existing.setPhone(request.phone());
                  }
                  return candidateRepository.save(existing);
                })
            .orElseGet(
                () -> {
                  Candidate newCandidate =
                      Candidate.builder()
                          .firstName(request.firstName())
                          .lastName(request.lastName())
                          .email(request.email())
                          .phone(request.phone())
                          .build();
                  return candidateRepository.save(newCandidate);
                });

    // 3. Check for Duplicate Application
    applicationRepository
        .findByCandidateIdAndOfferId(candidate.getId(), offer.getId())
        .ifPresent(
            app -> {
              log.warn(
                  "Candidate {} already applied to offer {}", candidate.getEmail(), offer.getId());
              throw new DuplicateResourceException("You have already applied to this job offer.");
            });

    // 4. Calculate SHA-256 Hash of CV
    String fileHash = calculateHash(request.file());

    // 5. Find or Create CvFile
    CvFile cvFile =
        cvFileRepository
            .findByChecksumSha256(fileHash)
            .orElseGet(
                () -> {
                  String originalFilename = request.file().getOriginalFilename();
                  String extension = "";
                  if (originalFilename != null && originalFilename.lastIndexOf(".") > 0) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                  }
                  String storageKey = "resumes/" + UUID.randomUUID() + extension;
                  fileStorageService.uploadFile(request.file(), storageKey);

                  CvFile newCvFile =
                      CvFile.builder()
                          .candidate(candidate)
                          .storageKey(storageKey)
                          .originalFilename(request.file().getOriginalFilename())
                          .checksumSha256(fileHash)
                          .extractionStatus(ExtractionStatus.PENDING)
                          .build();
                  return cvFileRepository.save(newCvFile);
                });

    // 6. Create Application
    Application application =
        Application.builder()
            .candidate(candidate)
            .offer(offer)
            .cvFile(cvFile)
            .status(ApplicationStatus.NEW)
            .build();
    application = applicationRepository.save(application);

    // 7. Dispatch to RabbitMQ for AI Scoring & Extraction
    cvIngestionProducer.sendCvForProcessing(
        application.getId(), offer.getId(), cvFile.getId(), cvFile.getStorageKey());
  }

  private String calculateHash(MultipartFile file) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      try (InputStream is = file.getInputStream()) {
        byte[] buffer = new byte[8192];
        int read;
        while ((read = is.read(buffer)) > 0) {
          digest.update(buffer, 0, read);
        }
      }
      return HexFormat.of().formatHex(digest.digest());
    } catch (Exception e) {
      throw new RuntimeException("Failed to calculate file hash", e);
    }
  }
}
