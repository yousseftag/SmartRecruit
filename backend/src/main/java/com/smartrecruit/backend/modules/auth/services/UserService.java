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

  public UserService(
      AppUserRepository userRepository,
      KeycloakAdminService keycloakAdminService,
      EmailService emailService) {
    this.userRepository = userRepository;
    this.keycloakAdminService = keycloakAdminService;
    this.emailService = emailService;
  }

  @Transactional
  public UserResponse getMyProfile(Jwt jwt) {
    String sub = jwt.getSubject();
    AppUser user = userRepository.findByKeycloakSub(sub).orElseGet(() -> provisionOrLinkUser(jwt));

    // Self-healing: auto-sync local database with any direct updates from Keycloak token claims
    boolean updated = false;
    String jwtEmail = jwt.getClaimAsString("email");
    String jwtFirstName = jwt.getClaimAsString("given_name");
    String jwtLastName = jwt.getClaimAsString("family_name");

    if (jwtEmail != null && !jwtEmail.equalsIgnoreCase(user.getEmail())) {
      user.setEmail(jwtEmail);
      updated = true;
    }
    if (jwtFirstName != null && !jwtFirstName.equals(user.getFirstName())) {
      user.setFirstName(jwtFirstName);
      updated = true;
    }
    if (jwtLastName != null && !jwtLastName.equals(user.getLastName())) {
      user.setLastName(jwtLastName);
      updated = true;
    }

    if (updated) {
      log.info("Self-healing: Updated PostgreSQL user {} from Keycloak token claims", sub);
      user = userRepository.save(user);
    }

    return UserMapper.toResponse(user);
  }

  public UserResponse updateMyProfile(Jwt jwt, UpdateProfileRequest request) {
    String sub = jwt.getSubject();
    AppUser user = userRepository.findByKeycloakSub(sub).orElseGet(() -> provisionOrLinkUser(jwt));

    if (!user.getEmail().equalsIgnoreCase(request.email())) {
      if (userRepository.existsByEmailAndIdNot(request.email(), user.getId())) {
        throw new UserAlreadyExistsException("Email is already in use by another account.");
      }
    }

    String previousFirstName = user.getFirstName();
    String previousLastName = user.getLastName();
    String previousEmail = user.getEmail();

    // 1. Update Keycloak external identity provider
    keycloakAdminService.updateUser(sub, request.firstName(), request.lastName(), request.email());

    // 2. Persist to PostgreSQL database with Compensating Rollback if DB write fails
    try {
      user.setFirstName(request.firstName());
      user.setLastName(request.lastName());
      user.setEmail(request.email());
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

    String warning = null;
    try {
      emailService.sendWelcomeEmail(request.getEmail(), request.getUsername(), password);
    } catch (Exception e) {
      log.warn("Welcome email failed for user {}: {}", request.getUsername(), e.getMessage());
      warning = "L'utilisateur a été créé, mais l'envoi de l'email a échoué.";
    }

    return UserMapper.toResponse(savedUser, warning);
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
      return userRepository.save(user);
    }

    // 2. Otherwise JIT provision new user in PostgreSQL
    UserRole role = extractRoleFromJwt(jwt);
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
  private UserRole extractRoleFromJwt(Jwt jwt) {
    Map<String, Object> realmAccess = jwt.getClaim("realm_access");
    if (realmAccess != null && !realmAccess.isEmpty()) {
      Collection<String> roles = (Collection<String>) realmAccess.get("roles");
      if (roles != null) {
        if (roles.contains("HR_ADMIN")) {
          return UserRole.HR_ADMIN;
        }
        if (roles.contains("RECRUITER")) {
          return UserRole.RECRUITER;
        }
      }
    }
    return UserRole.VIEWER;
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
