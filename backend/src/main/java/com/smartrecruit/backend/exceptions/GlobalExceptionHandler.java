package com.smartrecruit.backend.exceptions;

import com.smartrecruit.backend.integration.keycloak.KeycloakIntegrationException;
import com.smartrecruit.backend.modules.auth.exceptions.UserAlreadyExistsException;
import com.smartrecruit.backend.modules.auth.exceptions.UserNotFoundException;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorResponse> handleGlobalException(Exception ex) {
    logger.error("Internal Server Error", ex);
    ApiErrorResponse body =
        new ApiErrorResponse(
            LocalDateTime.now(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            "An unexpected error occurred");
    return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
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
