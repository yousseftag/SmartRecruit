package com.smartrecruit.backend.modules.auth.services;

import com.smartrecruit.backend.integration.keycloak.KeycloakAdminService;
import com.smartrecruit.backend.integration.keycloak.KeycloakIntegrationException;
import com.smartrecruit.backend.modules.auth.entities.AppUser;
import com.smartrecruit.backend.modules.auth.entities.UserRole;
import com.smartrecruit.backend.modules.auth.repositories.AppUserRepository;
import java.util.Optional;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AdminSeeder {

  private static final Logger logger = LoggerFactory.getLogger(AdminSeeder.class);

  private static final String DEFAULT_ADMIN_USERNAME = "admin";
  private static final String DEFAULT_ADMIN_FIRSTNAME = "Admin";
  private static final String DEFAULT_ADMIN_LASTNAME = "RH";
  private static final String DEFAULT_ADMIN_EMAIL = "admin@smartrecruit.com";

  private final AppUserRepository userRepository;
  private final KeycloakAdminService keycloakAdminService;

  public AdminSeeder(AppUserRepository userRepository, KeycloakAdminService keycloakAdminService) {
    this.userRepository = userRepository;
    this.keycloakAdminService = keycloakAdminService;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void seedAdminUser() {
    try {
      AdminBootstrapData adminData = ensureKeycloakAdminUser();
      assignAdminRoles(adminData.keycloakSub());
      syncPostgresAdminUser(adminData);
    } catch (Exception e) {
      logger.error("An error occurred while seeding admin user: {}", e.getMessage(), e);
    }
  }

  private AdminBootstrapData ensureKeycloakAdminUser() {
    Optional<UserRepresentation> adminOpt =
        keycloakAdminService.getUserByUsername(DEFAULT_ADMIN_USERNAME);

    if (adminOpt.isPresent()) {
      UserRepresentation adminKC = adminOpt.get();
      String sub = adminKC.getId();
      String firstName = defaultIfBlank(adminKC.getFirstName(), DEFAULT_ADMIN_FIRSTNAME);
      String lastName = defaultIfBlank(adminKC.getLastName(), DEFAULT_ADMIN_LASTNAME);
      String email = defaultIfBlank(adminKC.getEmail(), DEFAULT_ADMIN_EMAIL);
      logger.info("Found existing 'admin' user in Keycloak with sub: {}", sub);
      return new AdminBootstrapData(sub, firstName, lastName, email);
    }

    logger.info("'admin' user not found in Keycloak. Auto-creating default admin in Keycloak...");
    String sub =
        keycloakAdminService.createUser(
            DEFAULT_ADMIN_USERNAME,
            DEFAULT_ADMIN_FIRSTNAME,
            DEFAULT_ADMIN_LASTNAME,
            DEFAULT_ADMIN_EMAIL,
            "admin",
            false);
    logger.info("Successfully created default 'admin' in Keycloak with sub: {}", sub);
    return new AdminBootstrapData(
        sub, DEFAULT_ADMIN_FIRSTNAME, DEFAULT_ADMIN_LASTNAME, DEFAULT_ADMIN_EMAIL);
  }

  private void assignAdminRoles(String keycloakSub) {
    try {
      keycloakAdminService.assignRealmRole(keycloakSub, UserRole.HR_ADMIN.name());
      keycloakAdminService.assignClientRole(keycloakSub, "account", "view-profile");
      keycloakAdminService.assignClientRole(keycloakSub, "account", "manage-account");
    } catch (KeycloakIntegrationException e) {
      logger.debug(
          "Admin roles already assigned or Keycloak role assignment notice: {}", e.getMessage());
    }
  }

  private void syncPostgresAdminUser(AdminBootstrapData adminData) {
    Optional<AppUser> existingAdmin = userRepository.findByUsername(DEFAULT_ADMIN_USERNAME);
    if (existingAdmin.isPresent()) {
      AppUser user = existingAdmin.get();
      boolean changed = false;
      if (!adminData.keycloakSub().equals(user.getKeycloakSub())) {
        user.setKeycloakSub(adminData.keycloakSub());
        changed = true;
      }
      if (user.getRole() != UserRole.HR_ADMIN) {
        user.setRole(UserRole.HR_ADMIN);
        changed = true;
      }
      if (changed) {
        userRepository.save(user);
        logger.info(
            "Updated 'admin' PostgreSQL record to sub: {} and role: HR_ADMIN",
            adminData.keycloakSub());
      } else {
        logger.info(
            "Admin user already synced in PostgreSQL with sub: {}", adminData.keycloakSub());
      }
    } else {
      AppUser adminUser =
          AppUser.builder()
              .keycloakSub(adminData.keycloakSub())
              .username(DEFAULT_ADMIN_USERNAME)
              .firstName(adminData.firstName())
              .lastName(adminData.lastName())
              .email(adminData.email())
              .role(UserRole.HR_ADMIN)
              .build();

      userRepository.save(adminUser);
      logger.info(
          "Admin user successfully seeded in PostgreSQL with sub: {}", adminData.keycloakSub());
    }
  }

  private String defaultIfBlank(String value, String defaultValue) {
    return (value != null && !value.isBlank()) ? value : defaultValue;
  }

  private record AdminBootstrapData(
      String keycloakSub, String firstName, String lastName, String email) {}
}
