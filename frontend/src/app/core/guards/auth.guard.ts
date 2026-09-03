import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import Keycloak from 'keycloak-js';
import { AuthService } from '../auth/auth.service';
import { KeycloakInitService } from '../auth/keycloak-init.service';
import { UserRole } from '../models/user.model';

export const authGuard: CanActivateFn = async (route, state) => {
  const keycloak = inject(Keycloak);
  const router = inject(Router);
  const keycloakInit = inject(KeycloakInitService);
  const authService = inject(AuthService);

  // Do not initialize Keycloak during application bootstrap. Doing so with
  // check-sso can leave public pages permanently blank while an iframe waits.
  await keycloakInit.init();

  if (!keycloak.authenticated) {
    await keycloak.login({
      redirectUri: window.location.origin + state.url,
    });
    return false;
  }

  authService.syncAuthState();

  const requiredRoles = route.data?.['roles'] as (UserRole | string)[];

  if (!requiredRoles || requiredRoles.length === 0) {
    return true;
  }

  if (authService.hasAnyRole(requiredRoles)) {
    return true;
  }

  router.navigate(['/hr/dashboard']);
  return false;
};
