# Security & identity

## Purpose

SmartRecruit uses a hybrid identity model: **Keycloak** owns credentials, sessions, and token issuance; **PostgreSQL** stores minimal user metadata needed for domain foreign keys (who created an offer, who changed a status).

## Core principles

| # | Principle | Rationale |
|---|-----------|-----------|
| 1 | Zero password footprint | Passwords, MFA, and sessions live only in Keycloak; no credentials in the application DB. |
| 2 | Stateless JWT verification | The backend is an OAuth2 Resource Server validating signatures against Keycloak's JWKS. |
| 3 | Local relational identity | `app_user.keycloak_sub` (UUID) links Keycloak to domain FKs. |
| 4 | JIT provisioning | First login auto-creates the `app_user` row from JWT claims. |
| 5 | DB-first RBAC | Roles are resolved from PostgreSQL each request, so changes apply immediately. |
| 6 | Non-blocking public traffic | `/careers/**` never initializes Keycloak. |

## Keycloak configuration

Realm `smartrecruit`, its clients, roles, and the backend service account are documented in [Keycloak Integration](../05-integrations/keycloak.md). The backend validates JWTs against the realm's JWKS and operates strictly inside the `smartrecruit` realm — it never uses the `master` realm.

## Startup — admin seeder

`AdminSeeder` runs on `ApplicationReadyEvent` and self-heals the default account:

```mermaid
flowchart TD
    A([ApplicationReadyEvent]) --> B{admin in Keycloak?}
    B -- No --> C[Create admin/admin with HR_ADMIN]
    B -- Yes --> D[Read sub + profile]
    C --> D
    D --> E{admin in PostgreSQL?}
    E -- "same sub" --> F([Done])
    E -- "different sub" --> G[Update keycloak_sub]
    E -- No --> H[INSERT app_user]
    G --> F
    H --> F

    classDef event fill:#008AAA,fill-opacity:0.18,stroke:#22B8D6,stroke-width:2px;
    classDef decision fill:#FF6600,fill-opacity:0.18,stroke:#FF8A3D,stroke-width:2px;
    classDef action fill:#6DB33F,fill-opacity:0.18,stroke:#8ED16A,stroke-width:2px;
    classDef done fill:#4169E1,fill-opacity:0.18,stroke:#6D8EFF,stroke-width:2px;
    class A event;
    class B,E decision;
    class C,D,G,H action;
    class F done;
```

The `admin` account is also pre-seeded in `realm-export.json`; the seeder enforces role mappings on every startup.

## Frontend authentication

- **Deferred lazy init** — Keycloak is *not* initialized in `APP_INITIALIZER` (which causes `check-sso` iframe freezes on public pages). `KeycloakInitService` initializes only when `authGuard` first intercepts a protected route.
- **Reactive state** — `AuthService` exposes `isAuthenticated`, `roles`, `currentUser` signals plus derived `isAdmin`, `isRecruiter`, `isViewer` computeds. State syncs on token refresh.
- **Bearer interceptor** — `keycloakBearerInterceptor` skips `/api/v1/public/**` and attaches `Authorization: Bearer <JWT>` elsewhere, refreshing if the token expires within 30 s.

## Backend authentication

`SecurityConfig` configures a stateless OAuth2 Resource Server:

- Session policy `STATELESS`; CSRF disabled; CORS from `app.cors.allowed-origins`.
- `permitAll()`: `/v3/api-docs/**`, `/swagger-ui/**`, `/api/v1/public/**`, `/api/v1/internal/**`.
- Everything else requires a valid Bearer JWT.

`JwtAuthConverter` resolves authorities **DB-first**: if the user exists in `app_user`, the role is read from PostgreSQL (`ROLE_<role>`); otherwise it falls back to the JWT `realm_access.roles` claim. This makes role changes effective on the next request.

`SecurityUtils` exposes `getCurrentUserSub()` and `getCurrentUser()` (with JIT provisioning fallback) to any component.

## JIT provisioning

On the first protected call for a new user:

1. No `app_user` matching `keycloak_sub`? Search by username, then email.
2. If found — link by updating `keycloak_sub` and syncing profile fields.
3. If not found — insert a new `app_user` from JWT claims; role from `realm_access.roles`.

`GET /api/v1/users/me` is strictly read-only; synchronization happens via JIT provisioning or explicit updates.

## User lifecycle

**Create (`HR_ADMIN`):** validate uniqueness, generate a random password (`app.security.password.length`, default 12), create the Keycloak user with a temporary password, assign the realm role and account client roles, insert `app_user`, send a welcome email. Failures trigger compensating deletion in Keycloak.

**Update (`PUT /api/v1/users/me`):** dual-write — Keycloak first, then PostgreSQL. If PostgreSQL fails, a compensating rollback restores the previous Keycloak state.

**Delete (`HR_ADMIN`):** self-deletion returns `400`; Keycloak deletion is idempotent (a 404 is ignored so legacy records still clean up in PostgreSQL).

## RBAC matrix

<details>
<summary>Full endpoint-to-role matrix</summary>

| Endpoint pattern | Method | Roles |
|------------------|--------|-------|
| `/api/v1/public/**` | GET, POST | Anonymous |
| `/api/v1/internal/**` | POST | Open (internal network) |
| `/api/v1/users/me` | GET, PUT | Any authenticated |
| `/api/v1/users/**` | All | `HR_ADMIN` |
| `/api/v1/offers` (read) | GET | `HR_ADMIN`, `RECRUITER`, `VIEWER` |
| `/api/v1/offers/**` (write) | POST, PUT, PATCH | `HR_ADMIN`, `RECRUITER` |
| `/api/v1/applications` (read) | GET | `HR_ADMIN`, `RECRUITER`, `VIEWER` |
| `/api/v1/applications/**` (write) | PUT, POST, DELETE | `HR_ADMIN`, `RECRUITER` |
| `/api/v1/workflow/**` | All | `HR_ADMIN`, `RECRUITER` |
| `/api/v1/dashboard/**` | GET | `HR_ADMIN`, `RECRUITER`, `VIEWER` |
| `/api/v1/reporting/**` | GET | `HR_ADMIN`, `RECRUITER`, `VIEWER` |

</details>

## First login sequence

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant Guard as authGuard
    participant KC as Keycloak
    participant Auth as AuthService
    participant API as Spring Boot
    participant DB as PostgreSQL

    User->>Guard: Navigate to /hr/dashboard
    rect rgba(34, 184, 214, 0.14)
    Note over User,Auth: OIDC login (PKCE)
    Guard->>KC: Lazy init + redirect (PKCE)
    User->>KC: Credentials
    KC-->>Auth: JWT + refresh token
    end
    rect rgba(141, 209, 106, 0.14)
    Note over Auth,DB: JIT provisioning
    Auth->>API: GET /api/v1/users/me
    API->>DB: SELECT by keycloak_sub
    DB-->>API: Not found
    API->>DB: INSERT app_user (JIT)
    API-->>Auth: UserResponse
    end
```

## Related

- [Backend](backend.md)
- [Keycloak Integration](../05-integrations/keycloak.md)
- [API Errors](../06-api/errors.md)
