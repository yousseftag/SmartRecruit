# Getting started

## Prerequisites

- Docker & Docker Compose
- JDK 21
- Node.js 22+ and pnpm (`corepack enable pnpm`)

## Step 1 — Infrastructure

All backing services are defined in `docker-compose.yml` at the project root.

```bash
docker compose up -d
```

| Service | URL | Credentials |
|---------|-----|-------------|
| PostgreSQL 16 | [localhost:5432](http://localhost:5432) | See `.env` (`postgres` / `postgres`) |
| RabbitMQ | [localhost:5672](http://localhost:5672) (AMQP) — management UI [localhost:15672](http://localhost:15672) | `guest` / `guest` |
| MinIO | [localhost:9000](http://localhost:9000) — console [localhost:9001](http://localhost:9001) | `minioadmin` / `minioadmin` |
| Keycloak 24 | [localhost:8081](http://localhost:8081) | `admin` / `admin` |
| Mailpit | [localhost:8025](http://localhost:8025) — SMTP [localhost:1025](http://localhost:1025) | none |
| Adminer | [localhost:8082](http://localhost:8082) | DB credentials above |

Copy `.env.example` to `.env` before first run. The full variable list is in [Configuration](07-operations/configuration.md).

## Step 2 — Backend

```bash
cd backend
./mvnw spring-boot:run
```

- API: [http://localhost:8080](http://localhost:8080)
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Step 3 — Frontend

```bash
cd frontend
pnpm install
pnpm start
```

- App: [http://localhost:4200](http://localhost:4200)
- The dev server hot-reloads on source changes.

## Default admin

On first startup, `AdminSeeder` creates and synchronizes a default account in Keycloak and PostgreSQL:

- **Username:** `admin`
- **Password:** `admin`
- **Role:** `HR_ADMIN`

> [!IMPORTANT]
> Change this password before any staging or production deployment.

## AI processing in development

The AI service is not in this repository. By default the backend runs with `AI_SIMULATION_ENABLED=true`, which starts in-JVM simulators that exercise the real RabbitMQ flow so a fresh clone runs end-to-end without the external FastAPI engine. Behavior and timings are documented in [Messaging → Simulation Mode](05-integrations/messaging.md#simulation-mode); the interface itself is in the [AI Contract](05-integrations/ai-contract.md). Set `AI_SIMULATION_ENABLED=false` to disable both simulators.

## Common tasks

| Task | Command |
|------|---------|
| Backend tests | `cd backend && ./mvnw clean test` |
| Backend formatting | `cd backend && ./mvnw spotless:apply` |
| Frontend tests | `cd frontend && pnpm run test --watch=false` |
| Frontend formatting | `cd frontend && npx prettier --write "src/**/*.{ts,html,css}"` |

## Troubleshooting

| Symptom | Cause / fix |
|---------|-------------|
| `401` on every API call | Keycloak not ready or JWT issuer mismatch. Verify `KEYCLOAK_URL` and that the realm imported. |
| CV stuck in `PENDING` | No FastAPI engine/simulator consuming the queue. Check `AI_SIMULATION_ENABLED` and the RabbitMQ UI. |

For Flyway checksum mismatches see [Migrations](07-operations/migrations.md); for pnpm lockfile/CI failures see [CI/CD](07-operations/ci-cd.md).

## Related

- [Contributing](../CONTRIBUTING.md)
- [Configuration](07-operations/configuration.md)
- [CI/CD](07-operations/ci-cd.md)
