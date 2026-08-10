package com.smartrecruit.backend.modules.application.controllers;

import com.smartrecruit.backend.modules.application.dtos.SyncRequestDto;
import com.smartrecruit.backend.modules.application.services.SyncService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/internal/cv")
@RequiredArgsConstructor
public class SyncController {

  private final SyncService syncService;

  @PostMapping("/sync")
  public ResponseEntity<Void> syncCvData(@Valid @RequestBody SyncRequestDto requestDto) {
    syncService.syncCvData(requestDto);
    return ResponseEntity.ok().build();
  }
}
