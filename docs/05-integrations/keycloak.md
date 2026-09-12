# Keycloak integration

## Purpose

Keycloak is the identity provider. The backend consumes its JWTs and, for user administration, calls its Admin REST API.

## Realm & clients

| Item | Value |
|------|-------|
| Realm | `smartrecruit` |
| Frontend client | `smartrecruit-frontend` — public SPA, Authorization Code + PKCE |
| Backend client | `smartrecruit-backend` — confidential service account (Client Credentials) |
| Realm roles | `HR_ADMIN`, `RECRUITER`, `VIEWER` |
| Account roles | `view-profile`, `manage-account` assigned to users |
| Theme | `smartrecruit` (custom login theme) |

Realm import lives at `infrastructure/keycloak/import/realm-export.json`; the custom theme at `infrastructure/keycloak/themes/smartrecruit`. Keycloak runs with `--import-realm` on startup.

## Backend admin API

`KeycloakAdminService` wraps the official `keycloak-admin-client` and authenticates as `smartrecruit-backend` against the `smartrecruit` realm using least-privilege `realm-management` roles:

`manage-users`, `view-users`, `query-users`, `view-realm`, `query-clients`, `view-clients`.

It never accesses the `master` realm. Profile updates use a **fetch-before-update** pattern to preserve existing user representation attributes.

## Configuration

The `KEYCLOAK_*` environment variables (see [Configuration](../07-operations/configuration.md#keycloak)) map to the `keycloak.*` YAML block:

```yaml
keycloak:
  realm: smartrecruit
  url: ${KEYCLOAK_URL:http://localhost:8081}/realms/${keycloak.realm}
  admin:
    server-url: ${KEYCLOAK_SERVER_URL:http://localhost:8081}
    client-id: ${KEYCLOAK_BACKEND_CLIENT_ID:smartrecruit-backend}
    client-secret: ${KEYCLOAK_BACKEND_SECRET:smartrecruit-backend-secret}
```

## Authentication flows

- **Login** — Angular redirects to Keycloak (PKCE); the returned JWT is attached to API calls by the bearer interceptor.
- **Verification** — Spring Boot validates the JWT signature against the realm's JWKS endpoint (issuer URI derived from `keycloak.url`).
- **Provisioning** — first authenticated request triggers JIT provisioning into `app_user`.

Details and sequence diagrams are in [Security](../03-architecture/security.md).

## Related

- [Security](../03-architecture/security.md)
- [Conventions](conventions.md)
- [Configuration](../07-operations/configuration.md)
