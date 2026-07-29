package com.smartrecruit.backend.exceptions;

import java.time.LocalDateTime;

public record ApiErrorResponse(LocalDateTime timestamp, int status, String error, String message) {}
