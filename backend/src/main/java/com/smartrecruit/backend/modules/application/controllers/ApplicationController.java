package com.smartrecruit.backend.modules.application.controllers;

import com.smartrecruit.backend.exceptions.ResourceNotFoundException;
import com.smartrecruit.backend.modules.application.dtos.ApplicationResponse;
import com.smartrecruit.backend.modules.application.dtos.ApplicationStatusResponse;
import com.smartrecruit.backend.modules.application.dtos.ApplyRequest;
import com.smartrecruit.backend.modules.application.entities.Application;
import com.smartrecruit.backend.modules.application.mappers.ApplicationMapper;
import com.smartrecruit.backend.modules.application.repositories.ApplicationRepository;
import com.smartrecruit.backend.modules.application.services.CandidateApplicationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ApplicationController {

  private final ApplicationRepository applicationRepository;
  private final CandidateApplicationService applicationService;

  @GetMapping("/api/v1/applications/{id}")
  public ResponseEntity<ApplicationResponse> getApplicationById(@PathVariable UUID id) {
    Application application =
        applicationRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Application not found."));
    return ResponseEntity.ok(ApplicationMapper.toDto(application));
  }

  @GetMapping("/api/v1/applications/{id}/status")
  public ResponseEntity<ApplicationStatusResponse> getApplicationStatus(@PathVariable UUID id) {
    ApplicationStatusResponse statusResponse =
        applicationRepository
            .findStatusById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Application not found."));
    return ResponseEntity.ok(statusResponse);
  }

  @PostMapping(
      value = "/api/v1/public/applications/apply",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Void> apply(@Valid @ModelAttribute ApplyRequest request) {
    log.info("Received application from {} for offer {}", request.email(), request.offerId());
    applicationService.applyToOffer(request);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @PostMapping(
      value = "/api/v1/applications/import",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<List<UUID>> importCandidates(
      @RequestParam("offerId") UUID offerId, @RequestParam("files") List<MultipartFile> files) {
    log.info("HR importing {} files for offer {}", files.size(), offerId);
    List<UUID> applicationIds = applicationService.importCandidates(offerId, files);
    return ResponseEntity.status(HttpStatus.CREATED).body(applicationIds);
  }
}
