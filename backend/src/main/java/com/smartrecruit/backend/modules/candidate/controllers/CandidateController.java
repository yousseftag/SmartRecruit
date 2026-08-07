package com.smartrecruit.backend.modules.candidate.controllers;

import com.smartrecruit.backend.modules.application.dtos.ApplyRequest;
import com.smartrecruit.backend.modules.application.services.CandidateApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/candidates")
@RequiredArgsConstructor
@Slf4j
public class CandidateController {

  private final CandidateApplicationService applicationService;

  @PostMapping(value = "/apply", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Void> apply(@Valid @ModelAttribute ApplyRequest request) {
    log.info("Received application from {} for offer {}", request.email(), request.offerId());
    applicationService.applyToOffer(request);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }
}
