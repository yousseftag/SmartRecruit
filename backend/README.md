# SmartRecruit Backend

Spring Boot 4.1 / Java 21 REST API — the sole gateway to PostgreSQL, orchestrating Keycloak authentication, MinIO file storage, and asynchronous RabbitMQ AI tasks.

---

## Quick Start

### 1. Start Infrastructure
```bash
# From project root:
docker compose up -d
```

### 2. Run the Application
```bash
./mvnw spring-boot:run
```

- **REST API**: `http://localhost:8080`
- **Swagger / OpenAPI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### 3. Run Tests
```bash
./mvnw clean test
```

### 4. Code Formatting
```bash
./mvnw spotless:apply
```

---

## Detailed Documentation

Full documentation lives in the central [`docs/`](../docs/README.md) set:

- **[Backend Architecture](../docs/03-architecture/backend.md)** — Package-by-feature layout, design rules
- **[Database Architecture](../docs/03-architecture/database.md)** — Schema, FK cascades, indexes, Flyway
- **[Security](../docs/03-architecture/security.md)** — Keycloak OIDC, JIT provisioning, DB-first RBAC
- **[CV Ingestion & Scoring](../docs/04-features/cv-ingestion.md)** — MinIO deduplication, AMQP queues, state machine
- **[Offer AI Pipeline](../docs/04-features/offer-ai.md)** — Requirement extraction, state machine, publish gate
- **[AI Service Contract](../docs/05-integrations/ai-contract.md)** — Payload schemas and `SyncRequestDto` validation
- **[API Reference](../docs/06-api/reference.md)** — Endpoints, roles, error model
