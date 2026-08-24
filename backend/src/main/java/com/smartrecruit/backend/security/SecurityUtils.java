package com.smartrecruit.backend.security;

import com.smartrecruit.backend.modules.auth.entities.AppUser;
import com.smartrecruit.backend.modules.auth.repositories.AppUserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

  private final AppUserRepository userRepository;

  /**
   * Retrieves the Keycloak Subject (UUID) of the currently authenticated user.
   *
   * @return Optional containing the Keycloak sub if authenticated via JWT, empty otherwise.
   */
  public Optional<String> getCurrentUserSub() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication instanceof JwtAuthenticationToken jwtToken) {
      return Optional.ofNullable(jwtToken.getToken().getSubject());
    }
    return Optional.empty();
  }

  /**
   * Retrieves the raw {@link Jwt} of the currently authenticated request.
   *
   * @return Optional containing the Jwt, empty otherwise.
   */
  public Optional<Jwt> getCurrentJwt() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication instanceof JwtAuthenticationToken jwtToken) {
      return Optional.ofNullable(jwtToken.getToken());
    }
    return Optional.empty();
  }

  /**
   * Retrieves the {@link AppUser} entity corresponding to the currently authenticated user.
   *
   * @return Optional containing the AppUser from the local PostgreSQL database.
   */
  public Optional<AppUser> getCurrentUser() {
    return getCurrentUserSub().flatMap(userRepository::findByKeycloakSub);
  }

  /**
   * Retrieves the preferred username of the currently authenticated user.
   *
   * @return Optional containing the username if available.
   */
  public Optional<String> getCurrentUsername() {
    return getCurrentJwt()
        .map(jwt -> jwt.getClaimAsString("preferred_username"))
        .or(() -> getCurrentUser().map(AppUser::getUsername));
  }
}
