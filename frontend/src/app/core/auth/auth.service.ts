import { Injectable, inject, signal } from '@angular/core';
import Keycloak from 'keycloak-js';

export interface UserProfile {
  firstName: string;
  lastName: string;
  fullName: string;
  email: string;
  preferredUsername: string;
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private keycloak = inject(Keycloak);

  isAuthenticated = signal<boolean>(false);
  isAdmin = signal<boolean>(false);

  constructor() {
    this.isAuthenticated.set(!!this.keycloak.authenticated);
    if (this.keycloak.authenticated) {
      this.isAdmin.set(this.hasRole('ADMIN_RH'));
    }
  }

  hasRole(role: string): boolean {
    if (!this.keycloak.authenticated) return false;
    const roles = this.keycloak.realmAccess?.roles || [];
    return roles.includes(role);
  }

  getUserProfile(): UserProfile | null {
    if (this.keycloak.authenticated && this.keycloak.tokenParsed) {
      const token = this.keycloak.tokenParsed as any;
      const firstName = token.given_name || '';
      const lastName = token.family_name || '';
      return {
        firstName,
        lastName,
        fullName: `${firstName} ${lastName}`.trim(),
        email: token.email || '',
        preferredUsername: token.preferred_username || '',
      };
    }
    return null;
  }

  manageAccount(): void {
    this.keycloak.login({ action: 'UPDATE_PROFILE' });
  }

  changePassword(): void {
    this.keycloak.login({
      action: 'UPDATE_PASSWORD',
      redirectUri: window.location.href,
    });
  }

  logout(redirectUri: string = window.location.origin): Promise<void> {
    return this.keycloak.logout({ redirectUri });
  }
}
