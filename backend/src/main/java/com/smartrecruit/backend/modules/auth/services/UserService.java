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
import com.smartrecruit.backend.security.SecurityUtils;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class UserService {

  private final AppUserRepository userRepository;
  private final KeycloakAdminService keycloakAdminService;
  private final EmailService emailService;
  private final SecurityUtils securityUtils;

  public UserService(
      AppUserRepository userRepository,
      KeycloakAdminService keycloakAdminService,
      EmailService emailService,
      SecurityUtils securityUtils) {
    this.userRepository = userRepository;
    this.keycloakAdminService = keycloakAdminService;
    this.emailService = emailService;
    this.securityUtils = securityUtils;
  }

  @Transactional(readOnly = true)
  public UserResponse getMyProfile(Jwt jwt) {
    String sub = jwt.getSubject();
    AppUser user = userRepository.findByKeycloakSub(sub).orElseGet(() -> provisionOrLinkUser(jwt));
    return UserMapper.toResponse(user);
  }

  public UserResponse updateMyProfile(Jwt jwt, UpdateProfileRequest request) {
    String sub = jwt.getSubject();
    AppUser user = userRepository.findByKeycloakSub(sub).orElseGet(() -> provisionOrLinkUser(jwt));

    String previousFirstName = user.getFirstName();
    String previousLastName = user.getLastName();
    String previousEmail = user.getEmail();

    String newFirstName = (request.firstName() != null) ? request.firstName() : previousFirstName;
    String newLastName = (request.lastName() != null) ? request.lastName() : previousLastName;
    String newEmail =
        (request.email() != null && !request.email().isBlank()) ? request.email() : previousEmail;

    if (request.email() != null
        && !request.email().isBlank()
        && !user.getEmail().equalsIgnoreCase(request.email())) {
      if (userRepository.existsByEmailAndIdNot(newEmail, user.getId())) {
        throw new UserAlreadyExistsException("Email is already in use by another account.");
      }
    }

    // 1. Update Keycloak external identity provider
    keycloakAdminService.updateUser(sub, newFirstName, newLastName, newEmail);

    // 2. Persist to PostgreSQL database with Compensating Rollback if DB write fails
    try {
      user.setFirstName(newFirstName);
      user.setLastName(newLastName);
      user.setEmail(newEmail);
      AppUser savedUser = userRepository.save(user);
      return UserMapper.toResponse(savedUser);
    } catch (Exception dbException) {
      log.error(
          "Failed to persist user profile update in PostgreSQL. Initiating compensating rollback to Keycloak for sub: {}",
          sub,
          dbException);
      try {
        keycloakAdminService.updateUser(sub, previousFirstName, previousLastName, previousEmail);
        log.info("Successfully rolled back Keycloak state for user sub: {}", sub);
      } catch (Exception kcRollbackEx) {
        log.error(
            "CRITICAL: Failed to rollback Keycloak state after PostgreSQL error for user sub: {}",
            sub,
            kcRollbackEx);
      }
      throw dbException;
    }
  }

  public List<UserResponse> getAllUsers() {
    return userRepository.findAll().stream()
        .map(UserMapper::toResponse)
        .collect(Collectors.toList());
  }

  @Transactional
  public UserResponse createUser(CreateUserRequest request) {
    String username = request.getUsername() != null ? request.getUsername().trim() : "";
    if (username.isBlank()) {
      throw new IllegalArgumentException("Username is required.");
    }
    if (username.contains(" ")) {
      throw new IllegalArgumentException("Username must not contain spaces.");
    }
    if (userRepository.existsByUsername(username)) {
      throw new UserAlreadyExistsException("Username is already taken.");
    }
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new UserAlreadyExistsException("Email is already in use.");
    }

    String password = securityUtils.generateRandomPassword();
    String firstName = request.getFirstName() != null ? request.getFirstName().trim() : "";
    String lastName = request.getLastName() != null ? request.getLastName().trim() : "";

    String sub =
        keycloakAdminService.createTemporaryUser(
            username, firstName, lastName, request.getEmail().trim(), password);

    try {
      keycloakAdminService.assignRealmRole(sub, request.getRole());
      try {
        keycloakAdminService.assignClientRole(sub, "account", "view-profile");
        keycloakAdminService.assignClientRole(sub, "account", "manage-account");
      } catch (Exception e) {
        log.debug("Account client roles assignment for new user: {}", e.getMessage());
      }

      AppUser user =
          AppUser.builder()
              .keycloakSub(sub)
              .username(username)
              .email(request.getEmail().trim())
              .firstName(firstName)
              .lastName(lastName)
              .role(UserRole.valueOf(request.getRole()))
              .build();

      AppUser savedUser = userRepository.save(user);

      String warning = null;
      try {
        emailService.sendWelcomeEmail(request.getEmail().trim(), username, password);
      } catch (Exception e) {
        log.warn("Welcome email failed for user {}: {}", username, e.getMessage());
        warning = "User created successfully, but sending welcome email failed.";
      }

      return UserMapper.toResponse(savedUser, warning);
    } catch (Exception ex) {
      log.error(
          "Failed to complete user creation for sub {}, initiating compensating deletion in Keycloak",
          sub,
          ex);
      try {
        keycloakAdminService.deleteUser(sub);
      } catch (Exception cleanupEx) {
        log.error(
            "Failed to delete Keycloak user {} during rollback: {}", sub, cleanupEx.getMessage());
      }
      throw ex;
    }
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

  @Transactional
  public AppUser provisionOrLinkUser(Jwt jwt) {
    String sub = jwt.getSubject();
    String username = jwt.getClaimAsString("preferred_username");
    if (username == null || username.isBlank()) {
      username = sub;
    }
    String email = jwt.getClaimAsString("email");
    if (email == null || email.isBlank()) {
      email = username + "@smartrecruit.com";
    }
    String firstName = jwt.getClaimAsString("given_name");
    String lastName = jwt.getClaimAsString("family_name");

    // 1. Try to link by username or email if already exists in PostgreSQL
    Optional<AppUser> existing = userRepository.findByUsername(username);
    if (existing.isEmpty()) {
      existing = userRepository.findByEmail(email);
    }

    UserRole role = extractRoleFromJwt(jwt).orElse(UserRole.VIEWER);

    if (existing.isPresent()) {
      AppUser user = existing.get();
      log.info(
          "Linking existing PostgreSQL user '{}' to Keycloak sub '{}'", user.getUsername(), sub);
      user.setKeycloakSub(sub);
      if (firstName != null && !firstName.isBlank()) {
        user.setFirstName(firstName);
      }
      if (lastName != null && !lastName.isBlank()) {
        user.setLastName(lastName);
      }
      user.setEmail(email);
      extractRoleFromJwt(jwt).ifPresent(user::setRole);
      return userRepository.save(user);
    }

    // 2. Otherwise JIT provision new user in PostgreSQL
    log.info(
        "JIT Provisioning new user in PostgreSQL: username={}, sub={}, role={}",
        username,
        sub,
        role);
    AppUser newUser =
        AppUser.builder()
            .keycloakSub(sub)
            .username(username)
            .firstName(firstName != null && !firstName.isBlank() ? firstName : username)
            .lastName(lastName != null && !lastName.isBlank() ? lastName : "")
            .email(email)
            .role(role)
            .build();

    return userRepository.save(newUser);
  }

  @SuppressWarnings("unchecked")
  private Optional<UserRole> extractRoleFromJwt(Jwt jwt) {
    if (jwt == null) {
      return Optional.empty();
    }
    Map<String, Object> realmAccess = jwt.getClaim("realm_access");
    if (realmAccess != null && !realmAccess.isEmpty()) {
      Collection<String> roles = (Collection<String>) realmAccess.get("roles");
      if (roles != null) {
        if (roles.contains("HR_ADMIN")) {
          return Optional.of(UserRole.HR_ADMIN);
        }
        if (roles.contains("RECRUITER")) {
          return Optional.of(UserRole.RECRUITER);
        }
        if (roles.contains("VIEWER")) {
          return Optional.of(UserRole.VIEWER);
        }
      }
    }
    return Optional.empty();
  }
}
