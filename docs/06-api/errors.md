# Error model

## Response shape

All handled errors return a JSON `ApiErrorResponse`:

```json
{
  "timestamp": "2026-09-11T10:15:30",
  "status": 404,
  "error": "Not Found",
  "message": "Offer not found: 93000000-0000-0000-0000-000000000001"
}
```

## Status mapping

| Status | Triggered by | Message |
|--------|--------------|---------|
| 400 Bad Request | `MethodArgumentNotValidException`, `BindException` | Field-level validation messages, comma-joined |
| 400 Bad Request | `IllegalArgumentException` | The exception message (business rules, e.g. weight sum) |
| 400 Bad Request | `KeycloakIntegrationException` (username invalid) | French message about invalid characters |
| 401 Unauthorized | `AuthenticationException` | `Authentication failed` |
| 403 Forbidden | `AccessDeniedException` | `You don't have permission to access this resource` |
| 404 Not Found | `ResourceNotFoundException` | The exception message |
| 409 Conflict | `DuplicateResourceException` | The exception message |
| 502 Bad Gateway | `KeycloakIntegrationException` (other) | The exception message |
| 500 Internal Server Error | Any unhandled exception | The exception message |

## Exception types

| Type | Typical use |
|------|-------------|
| `ResourceNotFoundException` | Entity lookup miss (offer, application, user) |
| `DuplicateResourceException` | Unique constraint violation (e.g. duplicate email) |
| `UserNotFoundException` | Auth-specific not-found |
| `KeycloakIntegrationException` | Keycloak Admin API failures; mapped to user-friendly messages |
| `IllegalStateException` / `IllegalArgumentException` | Business rule violations (e.g. publish gate) |

## Guidance

- Always throw a typed exception; never return raw stack traces or ad-hoc maps.
- Validation messages surface directly to the frontend — keep them user-safe.
- The `GlobalExceptionHandler` is the single place that formats error bodies.

## Related

- [API Reference](reference.md)
- [Backend Architecture](../03-architecture/backend.md)
- [Security](../03-architecture/security.md)
