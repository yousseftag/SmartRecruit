package com.smartrecruit.backend.modules.auth.repositories;

import com.smartrecruit.backend.modules.auth.entities.AppUser;
import com.smartrecruit.backend.modules.auth.entities.UserRole;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, UUID> {
  Optional<AppUser> findByKeycloakSub(String keycloakSub);

  Optional<AppUser> findByUsername(String username);

  Optional<AppUser> findByEmail(String email);

  boolean existsByEmail(String email);

  boolean existsByUsername(String username);

  boolean existsByEmailAndIdNot(String email, UUID id);

  boolean existsByRole(UserRole role);
}
