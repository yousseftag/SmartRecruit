package com.smartrecruit.backend.modules.application.controllers;

import com.smartrecruit.backend.modules.application.dtos.SyncRequestDto;
import com.smartrecruit.backend.modules.application.services.NlpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Internal webhook endpoint receiving asynchronous extraction results from the AI NLP worker. */
@RestController
@RequestMapping("/api/v1/internal/cv")
@RequiredArgsConstructor
public class NlpController {

  private final NlpService nlpService;

  /** Processes the asynchronous AI webhook payload to update CV extraction data and scores. */
  @PostMapping("/sync")
  public ResponseEntity<Void> syncCvData(@Valid @RequestBody SyncRequestDto requestDto) {
    nlpService.syncCvData(requestDto);
    return ResponseEntity.ok().build();
  }
}
