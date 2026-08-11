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
      if (userRepository.existsByRole(UserRole.HR_ADMIN)) {
        logger.info("Admin user already exists in PostgreSQL. Seeding skipped.");
        return;
      }

      logger.info("No Admin user found in PostgreSQL. Attempting to seed from Keycloak...");
      Optional<UserRepresentation> adminOpt = keycloakAdminService.getUserByUsername("admin");

      if (adminOpt.isPresent()) {
        UserRepresentation adminKC = adminOpt.get();

        AppUser adminUser =
            AppUser.builder()
                .keycloakSub(adminKC.getId())
                .username(adminKC.getUsername())
                .firstName(adminKC.getFirstName() != null ? adminKC.getFirstName() : "Admin")
                .lastName(adminKC.getLastName() != null ? adminKC.getLastName() : "Admin")
                .email(adminKC.getEmail() != null ? adminKC.getEmail() : "admin@example.com")
                .role(UserRole.HR_ADMIN)
                .build();

        userRepository.save(adminUser);
        logger.info("Admin user seeded in PostgreSQL with sub: {}", adminKC.getId());
      } else {
        logger.warn("Could not find 'admin' user in Keycloak. Seeding failed.");
      }
    } catch (Exception e) {
      logger.error("An error occurred while seeding admin user: {}", e.getMessage());
    }
  }
}
