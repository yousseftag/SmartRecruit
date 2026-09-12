# Backend architecture

## Purpose

Spring Boot 4.1 / Java 21 serves the REST API, owns all business logic, and is the only service that talks to PostgreSQL. It orchestrates Keycloak, MinIO, and RabbitMQ.

## Technology stack

| Concern | Choice |
|---------|--------|
| Runtime | Java 21 LTS · Spring Boot 4.1.0 |
| Persistence | Spring Data JPA + Flyway (`ddl-auto: validate`) |
| Security | Spring Security OAuth2 Resource Server (Keycloak JWKS) |
| Identity admin | Keycloak Admin Client 24.0.0 |
| Messaging | Spring AMQP (`spring-boot-starter-amqp`) with `Jackson2JsonMessageConverter` |
| Scheduling | Quartz |
| Storage | MinIO Java SDK 8.5.7 |
| API docs | Springdoc OpenAPI 3.0.0 |
| JSONB mapping | Hibernate 6 `@JdbcTypeCode(SqlTypes.JSON)` (built-in) |
| JSON serialization | Jackson |
| Excel export | Apache POI 5.3.0 (`poi-ooxml`) |
| PDF export | OpenPDF 2.0.3 |
| Archive handling | Apache Commons Compress 1.26.2 |
| Boilerplate | Lombok |
| Formatting | Spotless 2.43.0 + google-java-format 1.22.0 (GOOGLE style) |

## Package structure

Package-by-feature (domain-driven). Each module is self-contained with its own `controllers`, `services`, `repositories`, `entities`, `dtos`, and `mappers`.

```text
com.smartrecruit.backend
├── config/            # Security, CORS, RabbitMQ, MinIO, OpenAPI
├── security/          # JwtAuthConverter, SecurityUtils
├── exceptions/        # GlobalExceptionHandler, custom exceptions
├── modules/
│   ├── auth/          # AppUser, UserRole, UserService, AdminSeeder
│   ├── offer/         # Offer entity, publish gate, offer AI pipeline
│   ├── application/   # Candidate, CvFile, Application, scoring, ingestion
│   ├── workflow/      # Status transitions, audit history, email templates
│   ├── dashboard/     # KPI aggregation
│   └── reporting/     # Analytics, Excel/PDF export
└── integration/
    ├── keycloak/      # Keycloak Admin REST client
    ├── storage/       # MinIO FileStorageService
    ├── messaging/     # RabbitMQ producers & consumers
    └── email/         # SMTP / templated email
```

The `application` module owns `Candidate`, `CvFile`, `Application`, and `WorkflowStatusHistory` because they form one aggregate. There is intentionally **no separate candidate or scoring module** — score data lives on the application as a 1:1 record.

## Rules

1. **Public vs protected endpoints** — candidate-facing routes are `permitAll()` and rooted under `/api/v1/public/**`; recruiter routes use `@PreAuthorize`. See [Security](security.md).
2. **No scoring logic** — never add a scoring service. Score fields are written only by the AI sync path.
3. **Specific exceptions** — throw typed exceptions (`ResourceNotFoundException`, `DuplicateResourceException`, `KeycloakIntegrationException`); the `GlobalExceptionHandler` maps them to `ApiErrorResponse`.
4. **Schema via Flyway only** — never rely on Hibernate DDL. See [Migrations](../07-operations/migrations.md).
5. **Timezone** — JDBC time zone is pinned to UTC; native query timestamps are normalized to `OffsetDateTime`.

## Testing

| Layer | Approach |
|-------|----------|
| Web | `@SpringBootTest` + `MockMvc`, service layer mocked with `@MockitoBean` |
| Integration | `@ActiveProfiles("test")` for safe overrides |
| Run | `./mvnw clean test` |

## Related

- [Database](database.md)
- [Security](security.md)
- [API Reference](../06-api/reference.md)
