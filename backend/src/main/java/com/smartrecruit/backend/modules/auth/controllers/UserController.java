package com.smartrecruit.backend.modules.auth.controllers;

import com.smartrecruit.backend.modules.auth.dtos.CreateUserRequest;
import com.smartrecruit.backend.modules.auth.dtos.UpdateProfileRequest;
import com.smartrecruit.backend.modules.auth.dtos.UpdateUserRequest;
import com.smartrecruit.backend.modules.auth.dtos.UserResponse;
import com.smartrecruit.backend.modules.auth.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Management", description = "Endpoints for managing users")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/me")
  @Operation(
      summary = "Get my profile",
      description = "Retrieve the profile of the currently authenticated user")
  public ResponseEntity<UserResponse> getMyProfile(@AuthenticationPrincipal Jwt jwt) {
    return ResponseEntity.ok(userService.getMyProfile(jwt));
  }

  @PutMapping("/me")
  @Operation(
      summary = "Update my profile",
      description = "Update the profile of the currently authenticated user")
  public ResponseEntity<UserResponse> updateMyProfile(
      @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody UpdateProfileRequest request) {
    return ResponseEntity.ok(userService.updateMyProfile(jwt, request));
  }

  @GetMapping
  @PreAuthorize("hasRole('HR_ADMIN')")
  @Operation(
      summary = "Get all users",
      description = "Retrieve a list of all users (HR_ADMIN only)")
  public ResponseEntity<List<UserResponse>> getAllUsers() {
    return ResponseEntity.ok(userService.getAllUsers());
  }

  @PostMapping
  @PreAuthorize("hasRole('HR_ADMIN')")
  @Operation(
      summary = "Create a new user",
      description = "Create a new user account and send welcome email (HR_ADMIN only)")
  public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('HR_ADMIN')")
  @Operation(summary = "Update user", description = "Update user profile and role (HR_ADMIN only)")
  public ResponseEntity<UserResponse> updateUser(
      @PathVariable UUID id,
      @Valid @RequestBody UpdateUserRequest request,
      @AuthenticationPrincipal Jwt jwt) {
    return ResponseEntity.ok(userService.updateUser(id, request, jwt));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('HR_ADMIN')")
  @Operation(summary = "Delete user", description = "Delete a user account (HR_ADMIN only)")
  public ResponseEntity<Void> deleteUser(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
    userService.deleteUser(id, jwt);
    return ResponseEntity.noContent().build();
  }
}
