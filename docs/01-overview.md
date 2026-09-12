# Overview

## Purpose

SmartRecruit is an intelligent HR recruitment platform built for **Norsys Afrique**. It automates the journey from CV reception to campaign reporting, replacing manual sorting, subjective comparison, and spreadsheet reporting with a weighted, offer-specific AI scoring engine. Its capabilities are detailed under [Functional Scope](#functional-scope).

## Objectives

| # | Objective |
|---|-----------|
| 1 | Automated CV parsing (structured profiles from PDF/DOCX) |
| 2 | AI-powered scoring against per-offer weighted criteria |
| 3 | Audited workflow management with full status history |
| 4 | Automated, templated candidate communications |
| 5 | Campaign reporting and Excel/PDF exports |
| 6 | Centralized Keycloak authentication with role-based access |
| 7 | Reproducible Docker Compose environment |

## User roles

| Role | Keyword | Capabilities |
|------|---------|--------------|
| HR Administrator | `HR_ADMIN` | Everything: user management, offers, bulk imports, reporting, settings |
| Recruiter | `RECRUITER` | Offers, CV imports, candidate review, workflow, reporting |
| Viewer | `VIEWER` | Read-only access to offers, candidates, rankings, reporting |
| Candidate | *(public)* | Career portal: browse offers and submit an application via `/careers/**` |

`HR_ADMIN` inherits all `RECRUITER` capabilities. See [Security](03-architecture/security.md) for the RBAC matrix.

## Functional scope

- **Offers** — create/edit/publish/close with markdown descriptions and weighted scoring criteria across five categories: skills, experience, coursework, languages, localization. Weights must sum to 100%.
- **CV intake** — public application and HR bulk import; SHA-256 deduplication; per-file processing status.
- **AI scoring** — asynchronous NLP extraction and weighted scoring producing `total_score` (0–100) and per-category subscores. Scores are set only by the AI callback and are immutable until a manual re-extract.
- **Workflow** — audited transitions: `NEW → SHORTLISTED → INTERVIEWING → FOLLOW_UP → HIRED | REJECTED | ARCHIVED`, with a Kanban board view.
- **Communications** — configurable email templates and scheduled follow-up reminders.
- **Reporting** — KPIs, funnel, score distribution, top-candidate leaderboard, binary exports.
- **Administration** — user CRUD with Keycloak dual-write, role assignment, workspace settings.

## Non-functional requirements

| Requirement | Implementation |
|-------------|----------------|
| Security | Keycloak OIDC, stateless JWT, no passwords in the application DB |
| Performance | Asynchronous AI via RabbitMQ; CV imports never block the HTTP response |
| Reliability | Durable queues survive restarts; Quartz stall detection prevents silent failures |
| Maintainability | Package-by-feature backend, standalone Angular frontend, Spotless/Prettier |
| Portability | Reproducible via Docker Compose; configuration through `.env` |

## System snapshot

SmartRecruit is a single Spring Boot backend backed by PostgreSQL, MinIO, RabbitMQ, and Keycloak, with an Angular frontend serving both the HR workspace and the public careers portal. The FastAPI AI engine is contracted but not yet part of this repository; locally, in-JVM simulators drive the same RabbitMQ flow (`AI_SIMULATION_ENABLED=true`).

The full component diagram, service directory, and architecture invariants live in [Architecture](03-architecture/README.md). Identity synchronization is detailed in [Security](03-architecture/security.md), and the AI interface in the [AI Contract](05-integrations/ai-contract.md).

## Related

- [Getting Started](02-getting-started.md)
- [Architecture](03-architecture/README.md)
- [Glossary](glossary.md)
