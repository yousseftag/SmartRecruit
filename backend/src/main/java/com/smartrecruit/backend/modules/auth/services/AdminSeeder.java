package com.smartrecruit.backend.modules.auth.services;

import com.smartrecruit.backend.integration.keycloak.KeycloakAdminService;
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

  private final AppUserRepository userRepository;
  private final KeycloakAdminService keycloakAdminService;

  public AdminSeeder(AppUserRepository userRepository, KeycloakAdminService keycloakAdminService) {
    this.userRepository = userRepository;
    this.keycloakAdminService = keycloakAdminService;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void seedAdminUser() {
    try {
      String firstName = "Admin";
      String lastName = "RH";
      String email = "admin@smartrecruit.com";
      String keycloakSub;

      // 1. Ensure 'admin' exists in Keycloak with HR_ADMIN role
      Optional<UserRepresentation> adminOpt = keycloakAdminService.getUserByUsername("admin");
      if (adminOpt.isPresent()) {
        UserRepresentation adminKC = adminOpt.get();
        keycloakSub = adminKC.getId();
        if (adminKC.getFirstName() != null && !adminKC.getFirstName().isBlank()) {
          firstName = adminKC.getFirstName();
        }
        if (adminKC.getLastName() != null && !adminKC.getLastName().isBlank()) {
          lastName = adminKC.getLastName();
        }
        if (adminKC.getEmail() != null && !adminKC.getEmail().isBlank()) {
          email = adminKC.getEmail();
        }
        logger.info("Found existing 'admin' user in Keycloak with sub: {}", keycloakSub);
      } else {
        logger.info(
            "'admin' user not found in Keycloak. Auto-creating default admin in Keycloak...");
        keycloakSub =
            keycloakAdminService.createUser("admin", firstName, lastName, email, "admin", false);
        keycloakAdminService.assignRealmRole(keycloakSub, UserRole.HR_ADMIN.name());
        logger.info("Successfully created default 'admin' in Keycloak with sub: {}", keycloakSub);
      }

      // 2. Ensure 'admin' exists in PostgreSQL with the matching keycloakSub
      Optional<AppUser> existingAdmin = userRepository.findByUsername("admin");
      if (existingAdmin.isPresent()) {
        AppUser user = existingAdmin.get();
        if (!keycloakSub.equals(user.getKeycloakSub())) {
          user.setKeycloakSub(keycloakSub);
          userRepository.save(user);
          logger.info("Updated 'admin' PostgreSQL keycloakSub to: {}", keycloakSub);
        } else {
          logger.info("Admin user already synced in PostgreSQL with sub: {}", keycloakSub);
        }
      } else {
        AppUser adminUser =
            AppUser.builder()
                .keycloakSub(keycloakSub)
                .username("admin")
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .role(UserRole.HR_ADMIN)
                .build();

        userRepository.save(adminUser);
        logger.info("Admin user successfully seeded in PostgreSQL with sub: {}", keycloakSub);
      }
    } catch (Exception e) {
      logger.error("An error occurred while seeding admin user: {}", e.getMessage(), e);
    }
  }
}
