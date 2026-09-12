# Administration & settings

## Purpose

Admin-only surfaces for managing users/roles and workspace-level configuration.

## User management

Available at `/hr/administration` (role `HR_ADMIN`). All operations use the Keycloak Admin API and the local `app_user` table: list/create/update/delete users, plus the authenticated user's own profile. Endpoints and roles are listed in the [API Reference](../06-api/reference.md#users).

Role assignment: `HR_ADMIN`, `RECRUITER`, `VIEWER`. See [Security](../03-architecture/security.md) for the dual-write/compensating-rollback behavior and the JIT provisioning model.

## Settings

Workspace settings are exposed through the settings pages:

- **Email templates** (`/hr/settings/templates`) — edit the HTML templates used by the workflow module. See [Workflow](workflow.md).
- **Workspace defaults** — default values used when creating offer scoring criteria.

## Roles recap

| Role | Access |
|------|--------|
| `HR_ADMIN` | Full access, including user management and settings |
| `RECRUITER` | Offers, imports, candidates, workflow, reporting, templates |
| `VIEWER` | Read-only across offers, candidates, dashboard, and reporting |

## Related

- [Security](../03-architecture/security.md)
- [Workflow](workflow.md)
- [Conventions](../05-integrations/conventions.md)
