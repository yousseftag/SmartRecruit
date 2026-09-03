package com.smartrecruit.backend.security;

import com.smartrecruit.backend.modules.auth.entities.AppUser;
import com.smartrecruit.backend.modules.auth.repositories.AppUserRepository;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

  private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter =
      new JwtGrantedAuthoritiesConverter();
  private final AppUserRepository userRepository;

  public JwtAuthConverter(AppUserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
    Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
    return new JwtAuthenticationToken(jwt, authorities, getPrincipalClaimName(jwt));
  }

  private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
    String sub = jwt.getSubject();
    if (sub != null) {
      Optional<AppUser> userOpt = userRepository.findByKeycloakSub(sub);
      if (userOpt.isPresent() && userOpt.get().getRole() != null) {
        Set<GrantedAuthority> authorities =
            new HashSet<>(jwtGrantedAuthoritiesConverter.convert(jwt));
        authorities.add(new SimpleGrantedAuthority("ROLE_" + userOpt.get().getRole().name()));
        return authorities;
      }
    }

    // Fallback to token claims if user is not in PostgreSQL yet (e.g. initial login / JIT
    // provisioning)
    return Stream.concat(
            jwtGrantedAuthoritiesConverter.convert(jwt).stream(),
            extractResourceRoles(jwt).stream())
        .collect(Collectors.toSet());
  }

  private String getPrincipalClaimName(Jwt jwt) {
    String claimName = JwtClaimNames.SUB;
    if (jwt.hasClaim("preferred_username")) {
      claimName = "preferred_username";
    }
    return jwt.getClaimAsString(claimName);
  }

  @SuppressWarnings("unchecked")
  private Collection<? extends GrantedAuthority> extractResourceRoles(Jwt jwt) {
    Map<String, Object> realmAccess = jwt.getClaim("realm_access");
    if (realmAccess == null || realmAccess.isEmpty()) {
      return Set.of();
    }
    Collection<String> roles = (Collection<String>) realmAccess.get("roles");
    if (roles == null) {
      return Set.of();
    }
    return roles.stream()
        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
        .collect(Collectors.toSet());
  }
}
