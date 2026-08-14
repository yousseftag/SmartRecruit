package com.smartrecruit.backend.modules.auth.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartrecruit.backend.modules.auth.dtos.CreateUserRequest;
import com.smartrecruit.backend.modules.auth.dtos.UpdateProfileRequest;
import com.smartrecruit.backend.modules.auth.dtos.UserResponse;
import com.smartrecruit.backend.modules.auth.services.UserService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@Import({
  com.smartrecruit.backend.config.SecurityConfig.class,
  com.smartrecruit.backend.exceptions.GlobalExceptionHandler.class
})
class UserControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private UserService userService;

  @MockitoBean private com.smartrecruit.backend.security.JwtAuthConverter jwtAuthConverter;

  private ObjectMapper objectMapper =
      new com.fasterxml.jackson.databind.ObjectMapper()
          .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

  @Test
  void testGetAllUsers_WithHrAdminRole_ShouldReturn200() throws Exception {
    UserResponse mockUser =
        new UserResponse(
            UUID.randomUUID(),
            "john.doe",
            "John",
            "Doe",
            "john@example.com",
            "HR_ADMIN",
            java.time.Instant.now(),
            null);
    when(userService.getAllUsers()).thenReturn(List.of(mockUser));

    mockMvc
        .perform(
            get("/api/v1/users")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_HR_ADMIN"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].username").value("john.doe"));
  }

  @Test
  void testGetAllUsers_WithoutHrAdminRole_ShouldReturn403() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/users")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_RECRUITER"))))
        .andExpect(status().isForbidden());
  }

  @Test
  void testGetAllUsers_Unauthenticated_ShouldReturn401() throws Exception {
    mockMvc.perform(get("/api/v1/users")).andExpect(status().isUnauthorized());
  }

  @Test
  void testCreateUser_InvalidEmail_ShouldReturn400() throws Exception {
    CreateUserRequest request = new CreateUserRequest();
    request.setUsername("jane.doe");
    request.setEmail("invalid-email"); // Invalid
    request.setFirstName("Jane");
    request.setLastName("Doe");
    request.setRole("RECRUITER");

    mockMvc
        .perform(
            post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_HR_ADMIN"))))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testCreateUser_EmptyUsername_ShouldReturn400() throws Exception {
    CreateUserRequest request = new CreateUserRequest();
    request.setUsername(""); // Invalid
    request.setEmail("jane@example.com");
    request.setRole("RECRUITER");

    mockMvc
        .perform(
            post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_HR_ADMIN"))))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testGetMyProfile_Authenticated_ShouldReturn200() throws Exception {
    UserResponse mockUser =
        new UserResponse(
            UUID.randomUUID(),
            "me",
            "Me",
            "My",
            "me@example.com",
            "HR_ADMIN",
            java.time.Instant.now(),
            null);

    when(userService.getMyProfile(any())).thenReturn(mockUser);

    mockMvc
        .perform(get("/api/v1/users/me").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("me"));
  }

  @Test
  void testUpdateMyProfile_Authenticated_ShouldReturn200() throws Exception {
    UpdateProfileRequest request = new UpdateProfileRequest("NewMe", "NewMy", "newme@example.com");
    UserResponse mockUser =
        new UserResponse(
            UUID.randomUUID(),
            "me",
            "NewMe",
            "NewMy",
            "newme@example.com",
            "HR_ADMIN",
            java.time.Instant.now(),
            null);

    when(userService.updateMyProfile(any(), any())).thenReturn(mockUser);

    mockMvc
        .perform(
            put("/api/v1/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("newme@example.com"));
  }
}
