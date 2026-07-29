package com.smartrecruit.backend.test.integration.modules.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityPlaygroundIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Test
  public void unauthenticatedUserShouldGetUnauthorized() throws Exception {
    mockMvc
        .perform(get("/api/v1/security-playground/authenticated"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(username = "testuser")
  public void authenticatedUserShouldAccessAuthenticatedEndpoint() throws Exception {
    mockMvc
        .perform(get("/api/v1/security-playground/authenticated"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Success! You are authenticated."))
        .andExpect(jsonPath("$.user").value("testuser"));
  }

  @Test
  @WithMockUser(
      username = "admin",
      roles = {"ADMIN_RH"})
  public void adminRhShouldAccessAdminEndpoint() throws Exception {
    mockMvc
        .perform(get("/api/v1/security-playground/admin-only"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Success! You have the ADMIN_RH role."))
        .andExpect(jsonPath("$.user").value("admin"));
  }

  @Test
  @WithMockUser(
      username = "user",
      roles = {"CANDIDATE"})
  public void nonAdminUserShouldGetForbiddenOnAdminEndpoint() throws Exception {
    mockMvc
        .perform(get("/api/v1/security-playground/admin-only"))
        .andExpect(status().isForbidden());
  }
}
