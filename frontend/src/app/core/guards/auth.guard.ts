import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import Keycloak from 'keycloak-js';
import { KeycloakInitService } from '../auth/keycloak-init.service';

export const authGuard: CanActivateFn = async (route, state) => {
  const keycloak = inject(Keycloak);
  const router = inject(Router);
  const keycloakInit = inject(KeycloakInitService);

  // Do not initialize Keycloak during application bootstrap. Doing so with
  // check-sso can leave public pages permanently blank while an iframe waits.
  await keycloakInit.init();

  if (!keycloak.authenticated) {
    await keycloak.login({
      redirectUri: window.location.origin + state.url,
    });
    return false;
  }

  const requiredRoles = route.data?.['roles'] as string[];

  if (!requiredRoles || requiredRoles.length === 0) {
    return true;
  }

  const userRoles = keycloak.realmAccess?.roles || [];

  const hasRequiredRole = requiredRoles.some((role) => userRoles.includes(role));

  if (hasRequiredRole) {
    return true;
  }

  router.navigate(['/hr/dashboard']);
  return false;
};
