export enum UserRole {
  HR_ADMIN = 'HR_ADMIN',
  RECRUITER = 'RECRUITER',
  VIEWER = 'VIEWER',
}

export interface UserResponse {
  id: string;
  keycloakSub: string;
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  role: UserRole;
  createdAt: string;
  warning?: string;
}

export interface UpdateProfileRequest {
  firstName: string;
  lastName: string;
  email: string;
}

export interface CreateUserRequest {
  username: string;
  email: string;
  role: string;
  firstName?: string;
  lastName?: string;
}

export interface UpdateUserRequest {
  email: string;
  role: string;
  firstName?: string;
  lastName?: string;
}

export interface UserProfile {
  sub: string;
  firstName: string;
  lastName: string;
  fullName: string;
  email: string;
  preferredUsername: string;
}
