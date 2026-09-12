# Design decisions

Rationale behind the platform's key architectural choices. Each entry follows *Decision → Why*.

## Backend

**Package-by-feature, not by layer.**
Domain modules (`offer`, `application`, `workflow`, …) keep their controllers, services, repositories, and entities together, so a change stays within one folder.

**No dedicated candidate or scoring module.**
`Candidate`, `CvFile`, `Application`, and `WorkflowStatusHistory` form one aggregate and live together in `application`. Score data is a 1:1 record on the application because the AI returns extraction and score in a single step, so a separate table only adds a join per ranking load.

**Spring Boot is the sole database gateway.**
The FastAPI engine has no DB access. The backend assembles the grading rubric into the message and persists results. This keeps the schema authoritative and the AI service stateless.

**Database triggers for `updated_at`.**
`@UpdateTimestamp` is bypassed by raw SQL. A `BEFORE UPDATE` trigger guarantees the timestamp is always honest, regardless of the writer.

## Messaging & AI flow

**RabbitMQ consumer over synchronous HTTP callback.**
Durable queues survive restarts with zero result loss, decouple the engine from the backend's address, and absorb callback bursts that would otherwise exhaust connection pools. The HTTP endpoint remains as a fallback.

**Document-level dedup, offer-level scoring.**
A CV is parsed once per unique SHA-256, but the match score is recomputed for every application against each offer's criteria. Reusing extraction while rescoring per offer is both faster and more correct.

**Null-only candidate enrichment.**
AI extraction only fills null or blank candidate fields. This prevents simulation drift or a later extraction from overwriting confirmed data.

**Configurable simulation mode.**
`SimulationNlpService` / `SimulationOfferService` run only when `AI_SIMULATION_ENABLED=true`. Development needs no external worker, and production disables it with zero code changes.

## Data modeling

**Candidate/CV separation (1:N).**
Supports a future authenticated-candidate model where one person manages multiple CVs, without a schema migration.

**Nullable `candidate.email` with `UNIQUE`.**
Enables both re-application identity matching and anonymous bulk "ghost" CVs (multiple `NULL`s are allowed in a PostgreSQL unique constraint).

**Strict vs flexible nullability.**
Hard invariants are enforced in SQL (`NOT NULL`, `CHECK`, `UNIQUE`); soft business rules (e.g. `min_score`, `contract_type`) stay nullable and are enforced at the DTO/frontend layer to avoid hardcoding policy in the schema.

**`ON DELETE SET NULL` for audit references.**
Deleting an HR user preserves offers and audit history; `ON DELETE CASCADE` is reserved for true ownership (candidate → CVs/applications).

## Security

**Stateless JWT + DB-first RBAC.**
No server-side sessions. Roles are read from PostgreSQL on each request (JWT claim as fallback), so a role change takes effect on the next call rather than at token expiry.

**JIT provisioning.**
Users are created locally from JWT claims on first login, so there is no manual seeding and no password ever reaches the application DB.

## Frontend

**Deferred lazy Keycloak initialization.**
Global init issues `check-sso` iframe requests that freeze public pages. Initializing only when `authGuard` hits a protected route keeps `/careers/**` instant.

**Reactive polling with backoff and stall detection.**
An RxJS `expand` stream guarantees one in-flight request, backs off 3 s → 15 s, and stops when the backend Quartz sweeper marks a job `STALLED`, replacing blind polling with an actionable "Relancer".

## Related

- [Architecture](../03-architecture/README.md)
- [CV Ingestion](../04-features/cv-ingestion.md)
- [Database](../03-architecture/database.md)
