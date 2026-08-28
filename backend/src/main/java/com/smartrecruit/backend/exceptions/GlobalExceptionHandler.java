package com.smartrecruit.backend.exceptions;

import com.smartrecruit.backend.integration.keycloak.KeycloakIntegrationException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  private ResponseEntity<ApiErrorResponse> buildResponse(HttpStatus status, String message) {
    ApiErrorResponse body =
        new ApiErrorResponse(
            LocalDateTime.now(), status.value(), status.getReasonPhrase(), message);
    return new ResponseEntity<>(body, status);
  }

  // --- 400 Bad Request (Validation & Business Arguments) ---
  @ExceptionHandler({
    MethodArgumentNotValidException.class,
    BindException.class,
    IllegalArgumentException.class
  })
  public ResponseEntity<ApiErrorResponse> handleValidationException(Exception ex) {
    if (ex instanceof IllegalArgumentException iae) {
      log.warn("Illegal argument / business constraint: {}", iae.getMessage());
      return buildResponse(HttpStatus.BAD_REQUEST, iae.getMessage());
    }

    BindingResult bindingResult = null;
    if (ex instanceof MethodArgumentNotValidException manve) {
      bindingResult = manve.getBindingResult();
    } else if (ex instanceof BindException be) {
      bindingResult = be.getBindingResult();
    }

    String message = "Validation failed";
    if (bindingResult != null && bindingResult.hasFieldErrors()) {
      message =
          bindingResult.getFieldErrors().stream()
              .map(error -> error.getField() + ": " + error.getDefaultMessage())
              .collect(Collectors.joining(", "));
    }

    return buildResponse(HttpStatus.BAD_REQUEST, message);
  }

  // --- 401 Unauthorized ---
  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ApiErrorResponse> handleAuthenticationException(
      AuthenticationException ex) {
    log.warn("Authentication failed: {}", ex.getMessage());
    return buildResponse(HttpStatus.UNAUTHORIZED, "Authentication failed");
  }

  // --- 403 Forbidden ---
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
    log.warn("Access denied: {}", ex.getMessage());
    return buildResponse(HttpStatus.FORBIDDEN, "You don't have permission to access this resource");
  }

  // --- 404 Not Found (Polymorphic: handles ResourceNotFoundException, UserNotFoundException, etc.)
  // ---
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleResourceNotFoundException(
      ResourceNotFoundException ex) {
    log.warn("Resource not found: {}", ex.getMessage());
    return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  // --- 409 Conflict (Polymorphic: handles DuplicateResourceException, UserAlreadyExistsException,
  // etc.) ---
  @ExceptionHandler(DuplicateResourceException.class)
  public ResponseEntity<ApiErrorResponse> handleDuplicateResourceException(
      DuplicateResourceException ex) {
    log.warn("Conflict: {}", ex.getMessage());
    return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
  }

  // --- 502 Bad Gateway / Keycloak Integration Mapping ---
  @ExceptionHandler(KeycloakIntegrationException.class)
  public ResponseEntity<ApiErrorResponse> handleKeycloakIntegrationException(
      KeycloakIntegrationException ex) {
    log.error("Keycloak Integration Error: {}", ex.getMessage(), ex);
    String msg = ex.getMessage();
    if (msg != null && msg.contains("error-username-invalid-character")) {
      return buildResponse(
          HttpStatus.BAD_REQUEST,
          "Le nom d'utilisateur contient des caractères non autorisés ou des espaces.");
    }
    if (msg != null && msg.contains("User exists with same username")) {
      return buildResponse(
          HttpStatus.CONFLICT, "Un utilisateur avec ce nom d'utilisateur existe déjà.");
    }
    if (msg != null && msg.contains("User exists with same email")) {
      return buildResponse(
          HttpStatus.CONFLICT, "Un utilisateur avec cette adresse email existe déjà.");
    }
    return buildResponse(HttpStatus.BAD_GATEWAY, ex.getMessage());
  }

  // --- 500 Internal Server Error (Fallback) ---
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorResponse> handleGlobalException(Exception ex) {
    log.error("Unexpected Internal Server Error", ex);
    return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
  }
}
