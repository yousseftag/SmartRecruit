import { Injectable, inject } from '@angular/core';
import Keycloak from 'keycloak-js';

/**
 * Initializes Keycloak only when a protected route is entered.
 *
 * Public pages must not wait for a `check-sso` iframe: browsers and Keycloak
 * deployments commonly block that cross-origin flow. Protected routes use a
 * normal top-level login redirect instead.
 */
@Injectable({ providedIn: 'root' })
export class KeycloakInitService {
  private readonly keycloak = inject(Keycloak);
  private initialization?: Promise<boolean>;

  init(): Promise<boolean> {
    if (!this.initialization) {
      this.initialization = this.keycloak.init({
        onLoad: 'login-required',
        checkLoginIframe: false,
        pkceMethod: 'S256',
      });
    }

    return this.initialization;
  }
}
