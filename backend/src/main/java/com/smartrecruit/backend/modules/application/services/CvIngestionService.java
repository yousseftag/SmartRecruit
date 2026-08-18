package com.smartrecruit.backend.modules.application.services;

import com.smartrecruit.backend.exceptions.DuplicateResourceException;
import com.smartrecruit.backend.exceptions.ResourceNotFoundException;
import com.smartrecruit.backend.integration.messaging.CvIngestionProducer;
import com.smartrecruit.backend.integration.storage.FileStorageService;
import com.smartrecruit.backend.modules.application.dtos.ApplyRequest;
import com.smartrecruit.backend.modules.application.dtos.ImportResponse;
import com.smartrecruit.backend.modules.application.entities.Application;
import com.smartrecruit.backend.modules.application.entities.Candidate;
import com.smartrecruit.backend.modules.application.entities.CvFile;
import com.smartrecruit.backend.modules.application.enums.ApplicationStatus;
import com.smartrecruit.backend.modules.application.enums.ExtractionStatus;
import com.smartrecruit.backend.modules.application.mappers.CandidateMapper;
import com.smartrecruit.backend.modules.application.repositories.ApplicationRepository;
import com.smartrecruit.backend.modules.application.repositories.CandidateRepository;
import com.smartrecruit.backend.modules.application.repositories.CvFileRepository;
import com.smartrecruit.backend.modules.offer.entities.Offer;
import com.smartrecruit.backend.modules.offer.repositories.OfferRepository;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class CvIngestionService {

  private static final List<String> ALLOWED_EXTENSIONS = List.of(".pdf", ".docx");

  private final OfferRepository offerRepository;
  private final CandidateRepository candidateRepository;
  private final ApplicationRepository applicationRepository;
  private final CvFileRepository cvFileRepository;
  private final FileStorageService fileStorageService;
  private final CvIngestionProducer cvIngestionProducer;
  private final SimulationNlpService simulationNlpService;

  /** Public Apply: Direct candidate application with known form data. */
  @Transactional
  public void applyToOffer(ApplyRequest request) {
    Offer offer =
        offerRepository
            .findById(request.offerId())
            .orElseThrow(() -> new ResourceNotFoundException("Job offer not found."));

    // 1. Validate file extension
    String originalFilename = request.file().getOriginalFilename();
    if (originalFilename == null
        || ALLOWED_EXTENSIONS.stream().noneMatch(originalFilename::endsWith)) {
      throw new IllegalArgumentException(
          "Format de fichier non supporté. Seuls les PDF et DOCX sont autorisés.");
    }

    // 2. Find existing candidate by email, or create a new one
    Candidate candidate =
        candidateRepository
            .findByEmail(request.email())
            .map(
                existing -> {
                  existing.setFirstName(request.firstName());
                  existing.setLastName(request.lastName());
                  if (request.phone() != null && !request.phone().isBlank()) {
                    existing.setPhone(request.phone());
                  }
                  return candidateRepository.save(existing);
                })
            .orElseGet(
                () -> {
                  Candidate newCandidate = CandidateMapper.toEntity(request);
                  return candidateRepository.save(newCandidate);
                });

    // 3. Process the CV file and link it to the candidate/offer
    try {
      processRawFile(
          offer,
          candidate,
          request.file().getBytes(),
          originalFilename,
          request.file().getContentType());
    } catch (Exception e) {
      log.error("Failed to process uploaded file", e);
      throw new RuntimeException("Failed to read file", e);
    }
  }

  /** HR Bulk Import: Processes unknown candidates via CVs or ZIPs for NLP extraction. */
  @Transactional
  public ImportResponse importCandidates(UUID offerId, List<MultipartFile> files) {
    Offer offer =
        offerRepository
            .findById(offerId)
            .orElseThrow(() -> new ResourceNotFoundException("Job offer not found."));

    List<UUID> applicationIds = new ArrayList<>();
    List<String> errors = new ArrayList<>();

    for (MultipartFile file : files) {
      String originalFilename = file.getOriginalFilename();

      // CASE 1: ZIP Archive Processing
      if (originalFilename != null && originalFilename.endsWith(".zip")) {
        try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
          ZipEntry entry;

          // Unpack the ZIP and process each valid CV file inside it
          while ((entry = zis.getNextEntry()) != null) {
            if (!entry.isDirectory()) {
              String entryName = entry.getName();
              if (ALLOWED_EXTENSIONS.stream().anyMatch(entryName::endsWith)) {
                try {
                  byte[] data = zis.readAllBytes();
                  String fileHash = calculateHash(data);

                  // Ghost Candidate Pattern: Create blank placeholder to be populated by NLP later.
                  Candidate candidate =
                      cvFileRepository
                          .findByChecksumSha256(fileHash)
                          .map(CvFile::getCandidate)
                          .orElseGet(() -> candidateRepository.save(Candidate.builder().build()));

                  Application app =
                      processRawFile(offer, candidate, data, entryName, "application/octet-stream");
                  applicationIds.add(app.getId());
                } catch (Exception e) {
                  log.error("Failed to process file inside ZIP: {}", entryName, e);
                  errors.add(
                      "Échec du traitement du fichier " + entryName + " : " + e.getMessage());
                }
              } else {
                errors.add("Format non supporté ignoré dans le ZIP : " + entryName);
              }
            }
          }
        } catch (Exception e) {
          log.error("Failed to extract zip file: {}", originalFilename, e);
          errors.add("Impossible d'extraire l'archive ZIP : " + originalFilename);
        }
      }
      // CASE 2: Direct CV Upload (PDF/DOCX)
      else if (originalFilename != null
          && ALLOWED_EXTENSIONS.stream().anyMatch(originalFilename::endsWith)) {
        try {
          byte[] data = file.getBytes();
          String fileHash = calculateHash(data);

          // Ghost Candidate Pattern: Create blank placeholder to be populated by NLP later.
          Candidate candidate =
              cvFileRepository
                  .findByChecksumSha256(fileHash)
                  .map(CvFile::getCandidate)
                  .orElseGet(() -> candidateRepository.save(Candidate.builder().build()));

          Application app =
              processRawFile(offer, candidate, data, originalFilename, file.getContentType());
          applicationIds.add(app.getId());
        } catch (Exception e) {
          log.error("Failed to read file: {}", originalFilename, e);
          errors.add("Échec du traitement de " + originalFilename + " : " + e.getMessage());
        }
      }
      // CASE 3: Invalid File Extension
      else {
        errors.add("Format non supporté ignoré : " + originalFilename);
      }
    }
    return new ImportResponse(applicationIds, errors);
  }

  /** Core workflow: Uploads to MinIO, saves DB state, and triggers RabbitMQ NLP processing. */
  private Application processRawFile(
      Offer offer,
      Candidate candidate,
      byte[] fileData,
      String originalFilename,
      String contentType) {

    // 1. Prevent applying twice to the same offer
    applicationRepository
        .findByCandidateIdAndOfferId(candidate.getId(), offer.getId())
        .ifPresent(
            app -> {
              log.warn(
                  "Candidate {} already applied to offer {}", candidate.getEmail(), offer.getId());
              throw new DuplicateResourceException("You have already applied to this job offer.");
            });

    String fileHash = calculateHash(fileData);

    // 2. Save the CV file to MinIO Storage and Database
    CvFile cvFile =
        cvFileRepository
            .findByChecksumSha256(fileHash)
            .orElseGet(
                () -> {
                  String extension = "";
                  if (originalFilename != null && originalFilename.lastIndexOf(".") > 0) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                  }
                  String storageKey = "resumes/" + UUID.randomUUID() + extension;

                  java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(fileData);
                  fileStorageService.uploadFile(bais, fileData.length, contentType, storageKey);

                  CvFile newCvFile =
                      CvFile.builder()
                          .candidate(candidate)
                          .storageKey(storageKey)
                          .originalFilename(originalFilename)
                          .checksumSha256(fileHash)
                          .extractionStatus(ExtractionStatus.PENDING) // Awaiting AI
                          .build();
                  return cvFileRepository.save(newCvFile);
                });

    // 3. Create the Workflow Application link
    Application application =
        Application.builder()
            .candidate(candidate)
            .offer(offer)
            .cvFile(cvFile)
            .status(ApplicationStatus.NEW)
            .build();
    application = applicationRepository.save(application);

    // 4. Fire-and-forget: Send event to RabbitMQ for AI Processing
    cvIngestionProducer.sendCvForProcessing(
        application.getId(), offer.getId(), cvFile.getId(), cvFile.getStorageKey());

    // 5. TEMPORARY MVP Simulation (Removes dependency on python worker for local dev)
    simulationNlpService.simulateNlpProcessing(application, cvFile);

    return application;
  }

  /** Helper: Calculates a SHA-256 hash of the file bytes to prevent duplicate storage. */
  private String calculateHash(byte[] fileData) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(digest.digest(fileData));
    } catch (Exception e) {
      throw new RuntimeException("Failed to calculate file hash", e);
    }
  }
}
