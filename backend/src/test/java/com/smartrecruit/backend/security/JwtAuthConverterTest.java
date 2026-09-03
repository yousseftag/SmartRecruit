package com.smartrecruit.backend.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.smartrecruit.backend.modules.auth.entities.AppUser;
import com.smartrecruit.backend.modules.auth.entities.UserRole;
import com.smartrecruit.backend.modules.auth.repositories.AppUserRepository;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
class JwtAuthConverterTest {

  @Mock private AppUserRepository userRepository;

  @InjectMocks private JwtAuthConverter jwtAuthConverter;

  private final String sub = "test-sub-123";

  private Jwt createMockJwt(String subject, String preferredUsername, List<String> realmRoles) {
    Map<String, Object> headers = Map.of("alg", "RS256");
    Map<String, Object> claims = new HashMap<>();
    claims.put("sub", subject);
    if (preferredUsername != null) {
      claims.put("preferred_username", preferredUsername);
    }
    claims.put("scope", "read write");

    if (realmRoles != null) {
      claims.put("realm_access", Map.of("roles", realmRoles));
    }

    return new Jwt(
        "mock-token-value", Instant.now(), Instant.now().plusSeconds(3600), headers, claims);
  }

  @Test
  void testConvert_WhenUserInPostgres_ResolvesRoleFromPostgres() {
    Jwt jwt = createMockJwt(sub, "bob", List.of("HR_ADMIN"));

    AppUser user =
        AppUser.builder()
            .id(UUID.randomUUID())
            .keycloakSub(sub)
            .username("bob")
            .email("bob@example.com")
            .firstName("Bob")
            .lastName("Smith")
            .role(UserRole.VIEWER)
            .build();

    when(userRepository.findByKeycloakSub(sub)).thenReturn(Optional.of(user));

    AbstractAuthenticationToken authToken = jwtAuthConverter.convert(jwt);

    assertNotNull(authToken);
    Set<String> authorities =
        authToken.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toSet());

    assertTrue(authorities.contains("ROLE_VIEWER"));
    assertFalse(authorities.contains("ROLE_HR_ADMIN"));
    assertEquals("bob", authToken.getName());
  }

  @Test
  void testConvert_WhenUserNotInPostgres_FallsBackToJwtRoles() {
    Jwt jwt = createMockJwt(sub, "alice", List.of("RECRUITER"));

    when(userRepository.findByKeycloakSub(sub)).thenReturn(Optional.empty());

    AbstractAuthenticationToken authToken = jwtAuthConverter.convert(jwt);

    assertNotNull(authToken);
    Set<String> authorities =
        authToken.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toSet());

    assertTrue(authorities.contains("ROLE_RECRUITER"));
    assertEquals("alice", authToken.getName());
  }

  @Test
  void testConvert_WithoutPreferredUsername_UsesSubAsPrincipal() {
    Jwt jwt = createMockJwt(sub, null, null);

    when(userRepository.findByKeycloakSub(sub)).thenReturn(Optional.empty());

    AbstractAuthenticationToken authToken = jwtAuthConverter.convert(jwt);

    assertNotNull(authToken);
    assertEquals(sub, authToken.getName());
  }
}
