package com.smartrecruit.backend.exceptions;

import com.smartrecruit.backend.integration.keycloak.KeycloakIntegrationException;
import com.smartrecruit.backend.modules.auth.exceptions.UserAlreadyExistsException;
import com.smartrecruit.backend.modules.auth.exceptions.UserNotFoundException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleResourceNotFoundException(
      ResourceNotFoundException ex) {
    logger.warn("Resource Not Found", ex);
    return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  @ExceptionHandler(DuplicateResourceException.class)
  public ResponseEntity<ApiErrorResponse> handleDuplicateResourceException(
      DuplicateResourceException ex) {
    logger.warn("Conflict: Duplicate Resource", ex);
    return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
  }

  @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
  public ResponseEntity<ApiErrorResponse> handleValidationException(Exception ex) {
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

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorResponse> handleGlobalException(Exception ex) {
    logger.error("Internal Server Error", ex);
    return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
  }

  private ResponseEntity<ApiErrorResponse> buildResponse(HttpStatus status, String message) {
    ApiErrorResponse body =
        new ApiErrorResponse(
            LocalDateTime.now(), status.value(), status.getReasonPhrase(), message);
    return new ResponseEntity<>(body, status);
  }

  @ExceptionHandler(UserAlreadyExistsException.class)
  public ResponseEntity<ApiErrorResponse> handleUserAlreadyExistsException(
      UserAlreadyExistsException ex) {
    ApiErrorResponse body =
        new ApiErrorResponse(
            LocalDateTime.now(), HttpStatus.CONFLICT.value(), "Conflict", ex.getMessage());
    return new ResponseEntity<>(body, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleUserNotFoundException(UserNotFoundException ex) {
    ApiErrorResponse body =
        new ApiErrorResponse(
            LocalDateTime.now(), HttpStatus.NOT_FOUND.value(), "Not Found", ex.getMessage());
    return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(KeycloakIntegrationException.class)
  public ResponseEntity<ApiErrorResponse> handleKeycloakIntegrationException(
      KeycloakIntegrationException ex) {
    logger.error("Keycloak Integration Error", ex);
    ApiErrorResponse body =
        new ApiErrorResponse(
            LocalDateTime.now(), HttpStatus.BAD_GATEWAY.value(), "Bad Gateway", ex.getMessage());
    return new ResponseEntity<>(body, HttpStatus.BAD_GATEWAY);
  }
}
