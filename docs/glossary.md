# Glossary

Domain and technical terms used across the documentation.

## Domain

| Term | Definition |
|------|------------|
| **Candidate** | A person who may apply to offers. Stored independently from CV files (1:N) to support future candidate accounts. May be a "ghost" record (null name/email) until AI extraction completes. |
| **Offer** | A job posting with a markdown description, status (`DRAFT`/`ACTIVE`/`CLOSED`), scoring weights, and criteria. |
| **Application** | The link between a candidate, an offer, and the CV used. Holds the workflow status and the AI score. Unique per `(candidate, offer)`. |
| **CV File** | An uploaded document (PDF/DOCX) stored in MinIO and deduplicated by SHA-256 checksum. Holds the AI-extracted data. |
| **Workflow History** | Append-only audit log of application status transitions. |
| **Email Template** | Configurable, placeholder-driven message used for convocations, follow-ups, and rejections. |

## AI & Messaging

| Term | Definition |
|------|------------|
| **AI engine** | External FastAPI service responsible for NLP extraction and weighted scoring. Contracted but not yet integrated; see [AI Contract](05-integrations/ai-contract.md). |
| **Fused flow** | Design in which extraction, vectorization, and scoring happen in a single asynchronous AI step that returns both extracted data and category scores. |
| **Simulation mode** | In-JVM mock implementation (`SimulationNlpService`, `SimulationOfferService`) enabled by `AI_SIMULATION_ENABLED=true`; see [Messaging](05-integrations/messaging.md#simulation-mode). |
| **Deduplication** | Storing identical CV binaries once, keyed by SHA-256. Extraction is reused; scoring is recomputed per application. See [Storage](05-integrations/storage.md#deduplication). |
| **Enrichment** | Null-only population of candidate fields from AI extraction; confirmed data is never overwritten. |

## Authentication

| Term | Definition |
|------|------------|
| **Keycloak** | Identity provider owning credentials, sessions, and JWT issuance. |
| **JIT provisioning** | Creating a local `app_user` from JWT claims on first authenticated request. |
| **DB-first RBAC** | Role resolution from PostgreSQL on every request, falling back to JWT claims. Changes take effect on the next request. |
| **Dual-write** | Writing a user update to both Keycloak and PostgreSQL, with compensating rollback if the second write fails. |

## Statuses

Status and role enum values, with their French UI labels, are listed in [Conventions](05-integrations/conventions.md#internal-keywords-english). Categories:

- **Application status** — `NEW`, `SHORTLISTED`, `INTERVIEWING`, `FOLLOW_UP`, `HIRED`, `REJECTED`, `ARCHIVED`
- **Offer status** — `DRAFT`, `ACTIVE`, `CLOSED`
- **CV extraction status** and **offer AI status** — `PENDING`, `STALLED`, `SUCCESS`, `FAILED`

## Related

- [Overview](01-overview.md)
- [Conventions](05-integrations/conventions.md)
