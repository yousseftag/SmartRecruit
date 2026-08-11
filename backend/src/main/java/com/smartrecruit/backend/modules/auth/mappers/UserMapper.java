package com.smartrecruit.backend.modules.auth.mappers;

import com.smartrecruit.backend.modules.auth.dtos.UserResponse;
import com.smartrecruit.backend.modules.auth.entities.AppUser;
import java.util.List;
import java.util.stream.Collectors;

public class UserMapper {

  private UserMapper() {}

  public static UserResponse toResponse(AppUser user) {
    if (user == null) {
      return null;
    }
    return new UserResponse(
        user.getId(),
        user.getUsername(),
        user.getFirstName(),
        user.getLastName(),
        user.getEmail(),
        user.getRole() != null ? user.getRole().name() : null,
        user.getCreatedAt());
  }

  public static List<UserResponse> toResponseList(List<AppUser> users) {
    if (users == null) {
      return null;
    }
    return users.stream().map(UserMapper::toResponse).collect(Collectors.toList());
  }
}
