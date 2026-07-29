package com.smartrecruit.backend.modules.auth.controllers;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/security-playground")
@Profile("!prod")
public class SecurityPlaygroundController {

  // 1. Requires ANY authenticated user (no specific role)
  @GetMapping("/authenticated")
  public Map<String, String> getAuthenticatedUser(Principal principal) {
    Map<String, String> response = new HashMap<>();
    response.put("message", "Success! You are authenticated.");
    response.put("user", principal.getName());
    return response;
  }

  // 2. Requires the ADMIN_RH role
  @GetMapping("/admin-only")
  @PreAuthorize("hasRole('ADMIN_RH')")
  public Map<String, String> getAdminData(Principal principal) {
    Map<String, String> response = new HashMap<>();
    response.put("message", "Success! You have the ADMIN_RH role.");
    response.put("user", principal.getName());
    return response;
  }
}
