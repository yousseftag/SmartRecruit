package com.smartrecruit.backend.integration.keycloak;

import jakarta.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class KeycloakAdminService {

  private final Keycloak keycloak;
  private final String realm;

  public KeycloakAdminService(
      @Value("${keycloak.admin.server-url}") String serverUrl,
      @Value("${keycloak.admin.client-id}") String clientId,
      @Value("${keycloak.admin.username}") String username,
      @Value("${keycloak.admin.password}") String password,
      @Value("${keycloak.realm}") String realm) {

    this.realm = realm;
    this.keycloak =
        KeycloakBuilder.builder()
            .serverUrl(serverUrl)
            .realm(realm)
            .clientId(clientId)
            .username(username)
            .password(password)
            .build();
  }

  private RealmResource getRealmResource() {
    return keycloak.realm(realm);
  }

  public void updateUser(String userId, String firstName, String lastName, String email) {
    try {
      UserResource userResource = getRealmResource().users().get(userId);
      UserRepresentation user = userResource.toRepresentation();

      user.setFirstName(firstName);
      user.setLastName(lastName);
      user.setEmail(email);

      userResource.update(user);
    } catch (Exception e) {
      throw new KeycloakIntegrationException(
          "Failed to update user in Keycloak: " + e.getMessage(), e);
    }
  }

  public Optional<UserRepresentation> getUserByUsername(String username) {
    try {
      UsersResource usersResource = getRealmResource().users();
      List<UserRepresentation> searchResults = usersResource.searchByUsername(username, true);
      if (searchResults != null && !searchResults.isEmpty()) {
        return Optional.of(searchResults.get(0));
      }
      return Optional.empty();
    } catch (Exception e) {
      throw new KeycloakIntegrationException(
          "Failed to fetch user by username from Keycloak: " + e.getMessage(), e);
    }
  }

  public String createUser(
      String username, String firstName, String lastName, String email, String password) {
    try {
      UserRepresentation user = new UserRepresentation();
      user.setUsername(username);
      user.setFirstName(firstName);
      user.setLastName(lastName);
      user.setEmail(email);
      user.setEnabled(true);
      user.setEmailVerified(true);

      CredentialRepresentation credential = new CredentialRepresentation();
      credential.setType(CredentialRepresentation.PASSWORD);
      credential.setValue(password);
      credential.setTemporary(true);
      user.setCredentials(Collections.singletonList(credential));

      Response response = getRealmResource().users().create(user);

      if (response.getStatus() != 201) {
        throw new KeycloakIntegrationException(
            "Failed to create user in Keycloak, status: " + response.getStatus(), null);
      }

      String path = response.getLocation().getPath();
      return path.substring(path.lastIndexOf('/') + 1);
    } catch (Exception e) {
      if (e instanceof KeycloakIntegrationException) throw e;
      throw new KeycloakIntegrationException(
          "Failed to create user in Keycloak: " + e.getMessage(), e);
    }
  }

  public void assignRealmRole(String userId, String roleName) {
    try {
      RoleRepresentation role = getRealmResource().roles().get(roleName).toRepresentation();
      getRealmResource()
          .users()
          .get(userId)
          .roles()
          .realmLevel()
          .add(Collections.singletonList(role));
    } catch (Exception e) {
      throw new KeycloakIntegrationException(
          "Failed to assign role in Keycloak: " + e.getMessage(), e);
    }
  }

  public void removeRealmRole(String userId, String roleName) {
    try {
      RoleRepresentation role = getRealmResource().roles().get(roleName).toRepresentation();
      getRealmResource()
          .users()
          .get(userId)
          .roles()
          .realmLevel()
          .remove(Collections.singletonList(role));
    } catch (Exception e) {
      throw new KeycloakIntegrationException(
          "Failed to remove role in Keycloak: " + e.getMessage(), e);
    }
  }

  public void deleteUser(String userId) {
    try {
      Response response = getRealmResource().users().delete(userId);
      if (response.getStatus() != 204 && response.getStatus() != 200) {
        throw new KeycloakIntegrationException(
            "Failed to delete user in Keycloak, status: " + response.getStatus(), null);
      }
    } catch (Exception e) {
      if (e instanceof KeycloakIntegrationException) throw e;
      throw new KeycloakIntegrationException(
          "Failed to delete user in Keycloak: " + e.getMessage(), e);
    }
  }
}
