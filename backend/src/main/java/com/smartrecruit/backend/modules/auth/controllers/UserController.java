package com.smartrecruit.backend.modules.auth.controllers;

import com.smartrecruit.backend.modules.auth.dtos.UpdateProfileRequest;
import com.smartrecruit.backend.modules.auth.dtos.UserResponse;
import com.smartrecruit.backend.modules.auth.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
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
}
