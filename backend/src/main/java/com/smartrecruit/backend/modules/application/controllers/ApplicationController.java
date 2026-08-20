package com.smartrecruit.backend.modules.application.controllers;

import com.smartrecruit.backend.exceptions.ResourceNotFoundException;
import com.smartrecruit.backend.integration.storage.FileStorageService;
import com.smartrecruit.backend.modules.application.dtos.ApplicationExtractionStatusResponse;
import com.smartrecruit.backend.modules.application.dtos.ApplicationResponse;
import com.smartrecruit.backend.modules.application.dtos.ApplicationSummaryResponse;
import com.smartrecruit.backend.modules.application.dtos.UpdateApplicationStatusRequest;
import com.smartrecruit.backend.modules.application.entities.Application;
import com.smartrecruit.backend.modules.application.entities.CvFile;
import com.smartrecruit.backend.modules.application.mappers.ApplicationMapper;
import com.smartrecruit.backend.modules.application.repositories.ApplicationRepository;
import com.smartrecruit.backend.modules.application.services.ApplicationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
@Slf4j
public class ApplicationController {

  private final ApplicationRepository applicationRepository;
  private final ApplicationService applicationService;
  private final FileStorageService fileStorageService;

  /** Fetches all applications (summary), optionally filtered by a specific job offer ID. */
  @GetMapping
  @PreAuthorize("hasAnyRole('HR_ADMIN', 'RECRUITER', 'VIEWER')")
  public ResponseEntity<List<ApplicationSummaryResponse>> getAllApplications(
      @RequestParam(value = "offerId", required = false) UUID offerId) {
    log.info("Fetching all applications{}", offerId != null ? " for offer " + offerId : "");

    List<Application> applications =
        (offerId != null)
            ? applicationRepository.findByOfferId(offerId)
            : applicationRepository.findAll();

    List<ApplicationSummaryResponse> response =
        applications.stream().map(ApplicationMapper::toSummaryDto).collect(Collectors.toList());
    return ResponseEntity.ok(response);
  }

  /** Retrieves the full, detailed profile of a single application (including AI extracted data). */
  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('HR_ADMIN', 'RECRUITER', 'VIEWER')")
  public ResponseEntity<ApplicationResponse> getApplicationById(@PathVariable UUID id) {
    Application application =
        applicationRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Application not found."));
    return ResponseEntity.ok(ApplicationMapper.toDto(application));
  }

  /** Polls the current NLP extraction status of an application to update the UI loaders. */
  @GetMapping("/{id}/extraction-status")
  @PreAuthorize("hasAnyRole('HR_ADMIN', 'RECRUITER', 'VIEWER')")
  public ResponseEntity<ApplicationExtractionStatusResponse> getApplicationExtractionStatus(
      @PathVariable("id") UUID id) {
    log.info("Checking extraction status for application {}", id);
    ApplicationExtractionStatusResponse statusResponse =
        applicationRepository
            .findExtractionStatusById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Application not found."));
    return ResponseEntity.ok(statusResponse);
  }

  /** Updates the workflow stage in candidate page or kanban board. */
  @PutMapping("/{id}/status")
  @PreAuthorize("hasAnyRole('HR_ADMIN', 'RECRUITER')")
  public ResponseEntity<Void> updateApplicationStatus(
      @PathVariable("id") UUID id, @Valid @RequestBody UpdateApplicationStatusRequest request) {
    log.info("Updating application {} status to {}", id, request.status());
    applicationService.updateApplicationStatus(id, request.status());
    return ResponseEntity.noContent().build();
  }

  /** Re-triggers AI extraction and scoring for an existing candidate. */
  @PostMapping("/{id}/re-extract")
  @PreAuthorize("hasAnyRole('HR_ADMIN', 'RECRUITER')")
  public ResponseEntity<Void> reExtractCv(@PathVariable("id") UUID id) {
    log.info("Request to re-extract CV for application {}", id);
    applicationService.reExtractCv(id);
    return ResponseEntity.noContent().build();
  }

  /** Deletes an application. Only HR and Recruiters can do this. */
  @DeleteMapping("/{id}")
  @PreAuthorize("hasAnyRole('HR_ADMIN', 'RECRUITER')")
  public ResponseEntity<Void> deleteApplication(@PathVariable("id") UUID id) {
    log.info("Deleting application {}", id);
    applicationService.deleteApplication(id);
    return ResponseEntity.noContent().build();
  }

  /** Previews or downloads the original CV file. */
  @GetMapping("/{id}/cv")
  @PreAuthorize("hasAnyRole('HR_ADMIN', 'RECRUITER', 'VIEWER')")
  public ResponseEntity<byte[]> getApplicationCvFile(@PathVariable("id") UUID id) {
    log.info("Fetching CV file for application {}", id);
    Application application =
        applicationRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Application not found."));

    CvFile cvFile = application.getCvFile();
    if (cvFile == null || cvFile.getStorageKey() == null) {
      throw new ResourceNotFoundException("No CV file found for this application.");
    }

    byte[] fileBytes = fileStorageService.downloadFile(cvFile.getStorageKey());
    String filename =
        cvFile.getOriginalFilename() != null ? cvFile.getOriginalFilename() : "cv.pdf";
    String contentType =
        filename.toLowerCase().endsWith(".pdf") ? "application/pdf" : "application/octet-stream";

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
        .contentType(MediaType.parseMediaType(contentType))
        .body(fileBytes);
  }
}
