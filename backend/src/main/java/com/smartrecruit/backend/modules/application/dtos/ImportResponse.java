package com.smartrecruit.backend.modules.application.dtos;

import java.util.List;
import java.util.UUID;

public record ImportResponse(List<UUID> applicationIds, List<String> errors) {}
