package com.smartrecruit.backend.security;

import com.smartrecruit.backend.modules.auth.entities.AppUser;
import com.smartrecruit.backend.modules.auth.repositories.AppUserRepository;
import com.smartrecruit.backend.modules.auth.services.UserService;
import java.security.SecureRandom;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  private final AppUserRepository userRepository;
  private final UserService userService;
  private final int passwordLength;
  private final String passwordChars;

  public SecurityUtils(
      AppUserRepository userRepository,
      @Lazy UserService userService,
      @Value("${app.security.password.length:12}") int passwordLength,
      @Value(
              "${app.security.password.chars:ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789}")
          String passwordChars) {
    this.userRepository = userRepository;
    this.userService = userService;
    this.passwordLength = passwordLength;
    this.passwordChars = passwordChars;
  }

  /**
   * Generates a cryptographically secure random password using configured length and character
   * pool.
   *
   * @return A randomly generated secure password string.
   */
  public String generateRandomPassword() {
    return generateRandomPassword(this.passwordLength, this.passwordChars);
  }

  /**
   * Generates a cryptographically secure random password with the given length and character pool.
   *
   * @param length The password length.
   * @param chars The character pool to draw from.
   * @return A randomly generated secure password string.
   */
  public static String generateRandomPassword(int length, String chars) {
    StringBuilder pwd = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      pwd.append(chars.charAt(SECURE_RANDOM.nextInt(chars.length())));
    }
    return pwd.toString();
  }

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
    return getCurrentJwt()
        .map(
            jwt ->
                userRepository
                    .findByKeycloakSub(jwt.getSubject())
                    .orElseGet(() -> userService.provisionOrLinkUser(jwt)));
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
