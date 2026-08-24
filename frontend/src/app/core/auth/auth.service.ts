import { Injectable, computed, inject, signal } from '@angular/core';
import Keycloak from 'keycloak-js';
import { Observable, from, of, Subject } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { UserProfile, UserRole } from '../models/user.model';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private keycloak = inject(Keycloak);

  readonly isAuthenticated = signal<boolean>(false);
  readonly roles = signal<string[]>([]);
  readonly currentUser = signal<UserProfile | null>(null);

  readonly isAdmin = computed(() => this.hasRole(UserRole.HR_ADMIN));
  readonly isRecruiter = computed(() => this.hasRole(UserRole.RECRUITER));
  readonly isViewer = computed(() => this.hasRole(UserRole.VIEWER));

  readonly profileUpdated = new Subject<void>();

  constructor() {
    this.syncAuthState();
  }

  syncAuthState(): void {
    const isAuth = !!this.keycloak.authenticated;
    this.isAuthenticated.set(isAuth);

    if (isAuth) {
      const realmRoles = this.keycloak.realmAccess?.roles || [];
      this.roles.set(realmRoles);
      this.currentUser.set(this.getUserProfile());
    } else {
      this.roles.set([]);
      this.currentUser.set(null);
    }
  }

  hasRole(role: UserRole | string): boolean {
    const target = typeof role === 'string' ? role : (role as string);
    if (this.roles().length > 0) {
      return this.roles().includes(target);
    }
    if (!this.keycloak.authenticated) return false;
    const currentRoles = this.keycloak.realmAccess?.roles || [];
    return currentRoles.includes(target);
  }

  hasAnyRole(roles: (UserRole | string)[]): boolean {
    return roles.some((role) => this.hasRole(role));
  }

  hasAllRoles(roles: (UserRole | string)[]): boolean {
    return roles.every((role) => this.hasRole(role));
  }

  getUserProfile(): UserProfile | null {
    if (this.keycloak.authenticated && this.keycloak.tokenParsed) {
      const token = this.keycloak.tokenParsed as any;
      const firstName = token.given_name || '';
      const lastName = token.family_name || '';
      return {
        sub: token.sub || '',
        firstName,
        lastName,
        fullName: `${firstName} ${lastName}`.trim(),
        email: token.email || '',
        preferredUsername: token.preferred_username || '',
        roles: this.keycloak.realmAccess?.roles || [],
      };
    }
    return null;
  }

  changePassword(): void {
    this.keycloak.login({
      action: 'UPDATE_PASSWORD',
      redirectUri: window.location.href,
    });
  }

  logout(redirectUri: string = `${window.location.origin}/hr`): Observable<void> {
    return from(this.keycloak.logout({ redirectUri }));
  }

  forceTokenRefresh(): Observable<boolean> {
    return from(this.keycloak.updateToken(-1)).pipe(
      catchError((error) => {
        console.error('Failed to refresh token', error);
        return of(false);
      }),
    );
  }
}
