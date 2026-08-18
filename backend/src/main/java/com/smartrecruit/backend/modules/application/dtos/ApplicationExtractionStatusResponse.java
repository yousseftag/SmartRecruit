package com.smartrecruit.backend.modules.application.dtos;

import com.smartrecruit.backend.modules.application.enums.ExtractionStatus;
import java.util.UUID;

public record ApplicationExtractionStatusResponse(UUID id, ExtractionStatus extractionStatus) {}
