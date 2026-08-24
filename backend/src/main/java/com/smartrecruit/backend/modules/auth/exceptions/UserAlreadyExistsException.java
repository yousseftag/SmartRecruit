package com.smartrecruit.backend.modules.auth.exceptions;

import com.smartrecruit.backend.exceptions.DuplicateResourceException;

public class UserAlreadyExistsException extends DuplicateResourceException {
  public UserAlreadyExistsException(String message) {
    super(message);
  }
}
