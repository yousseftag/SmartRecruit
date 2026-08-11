export interface UserResponse {
  id: string;
  keycloakSub: string;
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  role: string;
  createdAt: string;
}

export interface UpdateProfileRequest {
  firstName: string;
  lastName: string;
  email: string;
}
