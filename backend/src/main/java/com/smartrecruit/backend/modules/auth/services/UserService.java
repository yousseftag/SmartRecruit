package com.smartrecruit.backend.modules.auth.services;

import com.smartrecruit.backend.integration.keycloak.KeycloakAdminService;
import com.smartrecruit.backend.modules.auth.dtos.UpdateProfileRequest;
import com.smartrecruit.backend.modules.auth.dtos.UserResponse;
import com.smartrecruit.backend.modules.auth.entities.AppUser;
import com.smartrecruit.backend.modules.auth.exceptions.UserAlreadyExistsException;
import com.smartrecruit.backend.modules.auth.exceptions.UserNotFoundException;
import com.smartrecruit.backend.modules.auth.mappers.UserMapper;
import com.smartrecruit.backend.modules.auth.repositories.AppUserRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

  private final AppUserRepository userRepository;
  private final KeycloakAdminService keycloakAdminService;

  public UserService(AppUserRepository userRepository, KeycloakAdminService keycloakAdminService) {
    this.userRepository = userRepository;
    this.keycloakAdminService = keycloakAdminService;
  }

  @Transactional(readOnly = true)
  public UserResponse getMyProfile(Jwt jwt) {
    String sub = jwt.getSubject();
    AppUser user =
        userRepository
            .findByKeycloakSub(sub)
            .orElseThrow(() -> new UserNotFoundException("User not found for sub: " + sub));

    return UserMapper.toResponse(user);
  }

  @Transactional
  public UserResponse updateMyProfile(Jwt jwt, UpdateProfileRequest request) {
    String sub = jwt.getSubject();
    AppUser user =
        userRepository
            .findByKeycloakSub(sub)
            .orElseThrow(() -> new UserNotFoundException("User not found for sub: " + sub));

    if (!user.getEmail().equalsIgnoreCase(request.email())) {
      if (userRepository.existsByEmailAndIdNot(request.email(), user.getId())) {
        throw new UserAlreadyExistsException("Email is already in use by another account.");
      }
    }

    // Update PostgreSQL
    user.setFirstName(request.firstName());
    user.setLastName(request.lastName());
    user.setEmail(request.email());
    AppUser savedUser = userRepository.save(user);

    // Update Keycloak
    keycloakAdminService.updateUser(sub, request.firstName(), request.lastName(), request.email());

    return UserMapper.toResponse(savedUser);
  }
}
