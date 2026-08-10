package com.smartrecruit.backend.modules.application.mappers;

import com.smartrecruit.backend.modules.application.dtos.ApplyRequest;
import com.smartrecruit.backend.modules.application.entities.Candidate;

public class CandidateMapper {

  public static Candidate toEntity(ApplyRequest request) {
    if (request == null) return null;
    return Candidate.builder()
        .firstName(request.firstName())
        .lastName(request.lastName())
        .email(request.email())
        .phone(request.phone())
        .build();
  }
}
