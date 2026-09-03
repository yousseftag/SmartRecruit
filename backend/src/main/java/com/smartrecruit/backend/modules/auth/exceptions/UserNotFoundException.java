package com.smartrecruit.backend.modules.auth.exceptions;

import com.smartrecruit.backend.exceptions.ResourceNotFoundException;

public class UserNotFoundException extends ResourceNotFoundException {
  public UserNotFoundException(String message) {
    super(message);
  }
}
