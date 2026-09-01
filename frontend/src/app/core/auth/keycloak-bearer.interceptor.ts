import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import Keycloak from 'keycloak-js';
import { from, switchMap } from 'rxjs';

const securedApiPattern = /\/api\/v1\/(?!public\/)/i;

/**
 * Adds a Keycloak token exclusively to protected API calls.
 *
 * The early return is intentionally before injecting or invoking Keycloak, so
 * public endpoints cannot wait for authentication initialization or refresh.
 */
export const keycloakBearerInterceptor: HttpInterceptorFn = (request, next) => {
  if (!securedApiPattern.test(request.url)) {
    return next(request);
  }

  const keycloak = inject(Keycloak);

  if (!keycloak.authenticated) {
    return next(request);
  }

  return from(keycloak.updateToken(30)).pipe(
    switchMap(() => {
      const token = keycloak.token;
      return next(
        token ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : request,
      );
    }),
  );
};
