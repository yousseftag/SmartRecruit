package com.smartrecruit.backend.modules.auth.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.smartrecruit.backend.integration.email.EmailService;
import com.smartrecruit.backend.integration.keycloak.KeycloakAdminService;
import com.smartrecruit.backend.modules.auth.dtos.CreateUserRequest;
import com.smartrecruit.backend.modules.auth.dtos.UpdateProfileRequest;
import com.smartrecruit.backend.modules.auth.dtos.UpdateUserRequest;
import com.smartrecruit.backend.modules.auth.dtos.UserResponse;
import com.smartrecruit.backend.modules.auth.entities.AppUser;
import com.smartrecruit.backend.modules.auth.entities.UserRole;
import com.smartrecruit.backend.modules.auth.exceptions.UserAlreadyExistsException;
import com.smartrecruit.backend.modules.auth.repositories.AppUserRepository;
import com.smartrecruit.backend.security.SecurityUtils;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private AppUserRepository userRepository;
  @Mock private KeycloakAdminService keycloakAdminService;
  @Mock private EmailService emailService;
  @Mock private SecurityUtils securityUtils;

  @InjectMocks private UserService userService;

  private Jwt mockJwt;
  private AppUser mockUser;
  private final UUID userId = UUID.randomUUID();
  private final String keycloakSub = "test-sub-123";

  @BeforeEach
  void setUp() {
    mockJwt = mock(Jwt.class);

    mockUser =
        AppUser.builder()
            .id(userId)
            .keycloakSub(keycloakSub)
            .username("john.doe")
            .email("john@example.com")
            .firstName("John")
            .lastName("Doe")
            .role(UserRole.HR_ADMIN)
            .build();
  }

  // --- CREATE USER TESTS ---

  @Test
  void testCreateUser_Success() {
    CreateUserRequest request = new CreateUserRequest();
    request.setUsername("jane.doe");
    request.setEmail("jane@example.com");
    request.setFirstName("Jane");
    request.setLastName("Doe");
    request.setRole("RECRUITER");

    when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
    when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
    when(securityUtils.generateRandomPassword()).thenReturn("generated-temp-pwd");
    when(keycloakAdminService.createTemporaryUser(
            eq("jane.doe"),
            eq("Jane"),
            eq("Doe"),
            eq("jane@example.com"),
            eq("generated-temp-pwd")))
        .thenReturn("new-sub");

    AppUser savedUser =
        AppUser.builder()
            .id(UUID.randomUUID())
            .keycloakSub("new-sub")
            .username("jane.doe")
            .email("jane@example.com")
            .firstName("Jane")
            .lastName("Doe")
            .role(UserRole.RECRUITER)
            .build();

    when(userRepository.save(any(AppUser.class))).thenReturn(savedUser);

    UserResponse response = userService.createUser(request);

    assertNotNull(response);
    assertEquals("jane.doe", response.username());
    assertNull(response.warning()); // No warning expected

    verify(keycloakAdminService, times(1)).assignRealmRole("new-sub", "RECRUITER");
    verify(emailService, times(1))
        .sendWelcomeEmail(eq("jane@example.com"), eq("jane.doe"), eq("generated-temp-pwd"));
  }

  @Test
  void testCreateUser_EmailFailure_ShouldNotRollback() {
    CreateUserRequest request = new CreateUserRequest();
    request.setUsername("jane.doe");
    request.setEmail("jane@example.com");
    request.setRole("RECRUITER");

    when(userRepository.existsByUsername(anyString())).thenReturn(false);
    when(userRepository.existsByEmail(anyString())).thenReturn(false);
    when(securityUtils.generateRandomPassword()).thenReturn("generated-temp-pwd");
    when(keycloakAdminService.createTemporaryUser(
            anyString(), anyString(), anyString(), anyString(), anyString()))
        .thenReturn("new-sub");
    when(userRepository.save(any(AppUser.class))).thenReturn(mockUser);

    doThrow(new RuntimeException("Mail server down"))
        .when(emailService)
        .sendWelcomeEmail(anyString(), anyString(), anyString());

    UserResponse response = userService.createUser(request);

    assertNotNull(response);
    assertNotNull(response.warning()); // Warning expected
    assertEquals(
        "User created successfully, but sending welcome email failed.", response.warning());

    verify(userRepository, times(1)).save(any(AppUser.class)); // Verifies save was called
  }

  @Test
  void testCreateUser_UsernameExists() {
    CreateUserRequest request = new CreateUserRequest();
    request.setUsername("john.doe");
    when(userRepository.existsByUsername("john.doe")).thenReturn(true);

    assertThrows(UserAlreadyExistsException.class, () -> userService.createUser(request));
    verify(userRepository, never()).save(any());
  }

  // --- GET / UPDATE MY PROFILE TESTS ---

  @Test
  void testGetMyProfile_Success() {
    when(mockJwt.getSubject()).thenReturn(keycloakSub);
    when(userRepository.findByKeycloakSub(keycloakSub)).thenReturn(Optional.of(mockUser));

    UserResponse response = userService.getMyProfile(mockJwt);

    assertNotNull(response);
    assertEquals(mockUser.getUsername(), response.username());
  }

  @Test
  void testUpdateMyProfile_Success() {
    UpdateProfileRequest request =
        new UpdateProfileRequest("JohnNew", "DoeNew", "newjohn@example.com");

    when(mockJwt.getSubject()).thenReturn(keycloakSub);
    when(userRepository.findByKeycloakSub(keycloakSub)).thenReturn(Optional.of(mockUser));
    when(userRepository.existsByEmailAndIdNot(request.email(), mockUser.getId())).thenReturn(false);
    when(userRepository.save(any(AppUser.class))).thenAnswer(i -> i.getArguments()[0]);

    UserResponse response = userService.updateMyProfile(mockJwt, request);

    assertEquals("newjohn@example.com", response.email());
    assertEquals("JohnNew", response.firstName());
    verify(keycloakAdminService, times(1))
        .updateUser(keycloakSub, "JohnNew", "DoeNew", "newjohn@example.com");
  }

  @Test
  void testUpdateMyProfile_DuplicateEmail() {
    UpdateProfileRequest request =
        new UpdateProfileRequest("JohnNew", "DoeNew", "existing@example.com");

    when(mockJwt.getSubject()).thenReturn(keycloakSub);
    when(userRepository.findByKeycloakSub(keycloakSub)).thenReturn(Optional.of(mockUser));
    when(userRepository.existsByEmailAndIdNot(request.email(), mockUser.getId())).thenReturn(true);

    assertThrows(
        UserAlreadyExistsException.class, () -> userService.updateMyProfile(mockJwt, request));
    verify(keycloakAdminService, never())
        .updateUser(anyString(), anyString(), anyString(), anyString());
  }

  // --- UPDATE USER TESTS (HR_ADMIN) ---

  @Test
  void testUpdateUser_Success() {
    UpdateUserRequest request = new UpdateUserRequest();
    request.setEmail("new@example.com");
    request.setFirstName("New");
    request.setLastName("User");
    request.setRole("RECRUITER");

    when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
    when(mockJwt.getSubject()).thenReturn("different-sub"); // Current user is someone else
    when(userRepository.existsByEmailAndIdNot(request.getEmail(), userId)).thenReturn(false);
    when(userRepository.save(any(AppUser.class))).thenAnswer(i -> i.getArguments()[0]);

    UserResponse response = userService.updateUser(userId, request, mockJwt);

    assertEquals("RECRUITER", response.role());
    verify(keycloakAdminService).removeRealmRole(keycloakSub, "HR_ADMIN");
    verify(keycloakAdminService).assignRealmRole(keycloakSub, "RECRUITER");
    verify(keycloakAdminService).updateUser(keycloakSub, "New", "User", "new@example.com");
  }

  @Test
  void testUpdateUser_CannotChangeOwnRole() {
    UpdateUserRequest request = new UpdateUserRequest();
    request.setEmail("john@example.com");
    request.setRole("RECRUITER"); // Changing from HR_ADMIN to RECRUITER

    when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
    when(mockJwt.getSubject()).thenReturn(keycloakSub); // Current user is the target user

    assertThrows(
        IllegalArgumentException.class, () -> userService.updateUser(userId, request, mockJwt));
  }

  // --- DELETE USER TESTS ---

  @Test
  void testDeleteUser_Success() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
    when(mockJwt.getSubject()).thenReturn("different-sub");

    userService.deleteUser(userId, mockJwt);

    verify(keycloakAdminService, times(1)).deleteUser(keycloakSub);
    verify(userRepository, times(1)).delete(mockUser);
  }

  @Test
  void testDeleteUser_CannotDeleteSelf() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
    when(mockJwt.getSubject()).thenReturn(keycloakSub); // Same sub

    assertThrows(IllegalArgumentException.class, () -> userService.deleteUser(userId, mockJwt));
    verify(keycloakAdminService, never()).deleteUser(anyString());
  }

  @Test
  void testGetAllUsers() {
    when(userRepository.findAll()).thenReturn(List.of(mockUser));
    List<UserResponse> list = userService.getAllUsers();
    assertEquals(1, list.size());
  }
}
