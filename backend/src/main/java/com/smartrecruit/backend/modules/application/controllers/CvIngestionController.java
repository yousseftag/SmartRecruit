package com.smartrecruit.backend.modules.application.controllers;

import com.smartrecruit.backend.modules.application.dtos.ApplyRequest;
import com.smartrecruit.backend.modules.application.dtos.ImportResponse;
import com.smartrecruit.backend.modules.application.services.CvIngestionService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class CvIngestionController {

  private final CvIngestionService cvIngestionService;

  /** Handles public candidate applications via the careers portal (Single CV upload). */
  @PostMapping(value = "/public/applications/apply", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Void> applyToOffer(@Valid @ModelAttribute ApplyRequest request) {
    log.info("Received public application for offer {}", request.offerId());
    cvIngestionService.applyToOffer(request);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  /** Handles HR bulk imports from the dashboard (ZIP archives and direct CV files). */
  @PostMapping(value = "/applications/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasAnyRole('HR_ADMIN', 'RECRUITER')")
  public ResponseEntity<ImportResponse> importCandidates(
      @RequestParam("offerId") UUID offerId, @RequestParam("files") List<MultipartFile> files) {
    log.info("HR importing {} files for offer {}", files.size(), offerId);
    ImportResponse response = cvIngestionService.importCandidates(offerId, files);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
}
