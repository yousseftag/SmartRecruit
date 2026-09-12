# SmartRecruit Documentation

Technical documentation for the SmartRecruit recruitment platform. The set is organized **general → specific**: start with the overview, then drill into architecture, features, contracts, and operations.

## Reading paths

| If you are… | Read in order |
|-------------|---------------|
| **New to the project** | [01 Overview](01-overview.md) → [02 Getting Started](02-getting-started.md) → [03 Architecture](03-architecture/README.md) |
| **A backend developer** | [03 Architecture: Backend](03-architecture/backend.md) → [03 Architecture: Database](03-architecture/database.md) → [04 Features](04-features/cv-ingestion.md) → [06 API](06-api/reference.md) |
| **A frontend developer** | [03 Architecture: Frontend](03-architecture/frontend.md) → [04 Features](04-features/dashboard.md) → [06 API](06-api/reference.md) |
| **Integrating the AI service** | [05 Integrations: AI contract](05-integrations/ai-contract.md) → [05 Integrations: Messaging](05-integrations/messaging.md) |
| **Operating / deploying** | [07 Operations: Configuration](07-operations/configuration.md) → [07 Operations: CI](07-operations/ci-cd.md) → [07 Operations: Migrations](07-operations/migrations.md) |

## Index

### 01 · Overview
| Document | Contents |
|----------|----------|
| [Overview](01-overview.md) | Business context, objectives, roles, functional scope, NFRs |
| [Glossary](glossary.md) | Shared domain and technical vocabulary |

### 02 · Getting Started
| Document | Contents |
|----------|----------|
| [Getting Started](02-getting-started.md) | Prerequisites, infrastructure, running backend & frontend, default admin |
| [Contributing](../CONTRIBUTING.md) | Git workflow, commits, CI failure fixes |

### 03 · Architecture
| Document | Contents |
|----------|----------|
| [Architecture Index](03-architecture/README.md) | Component map, service directory, invariants |
| [Backend](03-architecture/backend.md) | Spring Boot, package-by-feature, module map, rules |
| [Frontend](03-architecture/frontend.md) | Angular 21, signals, routing, directory structure |
| [Database](03-architecture/database.md) | Schema rationale, FK cascades, indexing, Flyway |
| [Security](03-architecture/security.md) | Keycloak OIDC, JIT provisioning, DB-first RBAC |

### 04 · Features
| Document | Contents |
|----------|----------|
| [CV Ingestion & Scoring](04-features/cv-ingestion.md) | Upload/import, SHA-256 dedup, async pipeline, state machine |
| [Offer AI Pre-Processing](04-features/offer-ai.md) | Requirement extraction, `offerAiStatus`, publish gate |
| [Dashboard](04-features/dashboard.md) | KPIs, trends, activity feed, priority offers |
| [Workflow & Communications](04-features/workflow.md) | Status transitions, audit history, email templates |
| [Reporting & Exports](04-features/reporting.md) | Funnel, score tiers, Excel (POI) & PDF (OpenPDF) |
| [Administration & Settings](04-features/administration.md) | User CRUD, roles, workspace settings |

### 05 · Integrations & Contracts
| Document | Contents |
|----------|----------|
| [AI Service Contract](05-integrations/ai-contract.md) | Payload schemas, validation rules, status lifecycle |
| [Messaging](05-integrations/messaging.md) | RabbitMQ exchanges, queues, routing keys, producers/consumers |
| [Keycloak](05-integrations/keycloak.md) | Realm, clients, roles, service account |
| [Storage](05-integrations/storage.md) | MinIO bucket, object keys, deduplication |
| [Conventions](05-integrations/conventions.md) | English keywords ↔ French UI labels |

### 06 · API Reference
| Document | Contents |
|----------|----------|
| [API Reference](06-api/reference.md) | Every REST endpoint, roles, parameters |
| [Error Model](06-api/errors.md) | `ApiErrorResponse`, status mapping, exception types |

### 07 · Operations
| Document | Contents |
|----------|----------|
| [Configuration](07-operations/configuration.md) | Full environment variable reference |
| [Database Migrations](07-operations/migrations.md) | Flyway V1–V7 history and rules |
| [CI/CD](07-operations/ci-cd.md) | Pipeline stages, smart triggers, troubleshooting |

### 08 · Appendices
| Document | Contents |
|----------|----------|
| [Design Decisions](08-appendices/decisions.md) | Rationale behind key architectural choices |
| [Demo Seed Data](08-appendices/seed-data.md) | `V7__demo_seed.sql` contents and intended use |

---

## Documentation style guide

Apply these rules when editing or adding a page:

- **One `# H1` per file**, sentence-case headings, no emoji in headings.
- **Fixed structure** for feature/architecture pages: *Purpose → How It Works → Reference → Related*. Finish every page with a `## Related` list.
- **Single source of truth**: each fact (endpoints, config keys, enum values, status lifecycles) lives in exactly one canonical document. Other pages summarize in one line and link to it.
- **Canonical owners**:
  | Topic | Owner |
  |-------|-------|
  | Component diagram, invariants, service directory | [03 Architecture](03-architecture/README.md) |
  | Endpoints and role matrix | [06 API Reference](06-api/reference.md) |
  | Environment variables, ports, defaults | [07 Configuration](07-operations/configuration.md) |
  | Enum values and French labels | [05 Conventions](05-integrations/conventions.md) |
  | AI payloads and status lifecycles | [05 AI Contract](05-integrations/ai-contract.md) |
  | RabbitMQ topology and simulation mode | [05 Messaging](05-integrations/messaging.md) |
  | MinIO storage and deduplication | [05 Storage](05-integrations/storage.md) |
  | Identity, JIT provisioning, RBAC | [03 Security](03-architecture/security.md) |
  | Design rationale | [08 Decisions](08-appendices/decisions.md) |
- **Minimal core**: pages stay focused and skimmable; heavy rationale and data dumps live in [08 Appendices](08-appendices/decisions.md).
- **Verify against code**: file paths, endpoints, enum values, and config keys must match the repository.
- **Screenshots and demo media**: store them in [`docs/assets/`](assets/README.md) and follow its naming and size conventions.

## Related

- [Project README](../README.md)
- [Contributing](../CONTRIBUTING.md)
- [Glossary](glossary.md)
- [Screenshot assets](assets/README.md)
