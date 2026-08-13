package com.smartrecruit.backend.modules.auth.services;

import com.smartrecruit.backend.integration.email.EmailService;
import com.smartrecruit.backend.integration.keycloak.KeycloakAdminService;
import com.smartrecruit.backend.modules.auth.dtos.CreateUserRequest;
import com.smartrecruit.backend.modules.auth.dtos.UpdateProfileRequest;
import com.smartrecruit.backend.modules.auth.dtos.UpdateUserRequest;
import com.smartrecruit.backend.modules.auth.dtos.UserResponse;
import com.smartrecruit.backend.modules.auth.entities.AppUser;
import com.smartrecruit.backend.modules.auth.entities.UserRole;
import com.smartrecruit.backend.modules.auth.exceptions.UserAlreadyExistsException;
import com.smartrecruit.backend.modules.auth.exceptions.UserNotFoundException;
import com.smartrecruit.backend.modules.auth.mappers.UserMapper;
import com.smartrecruit.backend.modules.auth.repositories.AppUserRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

  private static final Logger log = LoggerFactory.getLogger(UserService.class);

  private final AppUserRepository userRepository;
  private final KeycloakAdminService keycloakAdminService;
  private final EmailService emailService;

  public UserService(
      AppUserRepository userRepository,
      KeycloakAdminService keycloakAdminService,
      EmailService emailService) {
    this.userRepository = userRepository;
    this.keycloakAdminService = keycloakAdminService;
    this.emailService = emailService;
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

  public List<UserResponse> getAllUsers() {
    return userRepository.findAll().stream()
        .map(UserMapper::toResponse)
        .collect(Collectors.toList());
  }

  @Transactional
  public UserResponse createUser(CreateUserRequest request) {
    if (userRepository.existsByUsername(request.getUsername())) {
      throw new UserAlreadyExistsException("Username is already taken.");
    }
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new UserAlreadyExistsException("Email is already in use.");
    }

    String password = generateRandomPassword();
    String firstName = request.getFirstName() != null ? request.getFirstName() : "";
    String lastName = request.getLastName() != null ? request.getLastName() : "";

    String sub =
        keycloakAdminService.createUser(
            request.getUsername(), firstName, lastName, request.getEmail(), password);
    keycloakAdminService.assignRealmRole(sub, request.getRole());

    AppUser user =
        AppUser.builder()
            .keycloakSub(sub)
            .username(request.getUsername())
            .email(request.getEmail())
            .firstName(firstName)
            .lastName(lastName)
            .role(UserRole.valueOf(request.getRole()))
            .build();

    AppUser savedUser = userRepository.save(user);

    try {
      emailService.sendWelcomeEmail(request.getEmail(), request.getUsername(), password);
    } catch (Exception e) {
      log.warn("Welcome email failed for user {}: {}", request.getUsername(), e.getMessage());
    }

    return UserMapper.toResponse(savedUser);
  }

  @Transactional
  public UserResponse updateUser(UUID id, UpdateUserRequest request, Jwt jwt) {
    AppUser user =
        userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));

    String currentSub = jwt.getSubject();
    if (user.getKeycloakSub().equals(currentSub)) {
      if (!user.getRole().name().equals(request.getRole())) {
        throw new IllegalArgumentException("You cannot change your own role.");
      }
    }

    if (!user.getEmail().equalsIgnoreCase(request.getEmail())) {
      if (userRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
        throw new UserAlreadyExistsException("Email is already in use by another account.");
      }
    }

    String firstName = request.getFirstName() != null ? request.getFirstName() : "";
    String lastName = request.getLastName() != null ? request.getLastName() : "";

    keycloakAdminService.updateUser(user.getKeycloakSub(), firstName, lastName, request.getEmail());

    if (!user.getRole().name().equals(request.getRole())) {
      keycloakAdminService.removeRealmRole(user.getKeycloakSub(), user.getRole().name());
      keycloakAdminService.assignRealmRole(user.getKeycloakSub(), request.getRole());
      user.setRole(UserRole.valueOf(request.getRole()));
    }

    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.setEmail(request.getEmail());

    return UserMapper.toResponse(userRepository.save(user));
  }

  @Transactional
  public void deleteUser(UUID id, Jwt jwt) {
    AppUser user =
        userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));

    String currentSub = jwt.getSubject();
    if (user.getKeycloakSub().equals(currentSub)) {
      throw new IllegalArgumentException("You cannot delete your own account.");
    }

    keycloakAdminService.deleteUser(user.getKeycloakSub());
    userRepository.delete(user);
  }

  private String generateRandomPassword() {
    int length = 12;
    String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    StringBuilder pwd = new StringBuilder();
    java.security.SecureRandom random = new java.security.SecureRandom();
    for (int i = 0; i < length; i++) {
      pwd.append(chars.charAt(random.nextInt(chars.length())));
    }
    return pwd.toString();
  }
}
