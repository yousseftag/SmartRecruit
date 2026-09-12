# SmartRecruit

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![Angular](https://img.shields.io/badge/Angular-21-DD0031?style=flat-square&logo=angular&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=flat-square&logo=postgresql&logoColor=white)
![Keycloak](https://img.shields.io/badge/Keycloak-24-008AAA?style=flat-square&logo=keycloak&logoColor=white)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3-FF6600?style=flat-square&logo=rabbitmq&logoColor=white)
![MinIO](https://img.shields.io/badge/MinIO-S3-C72E49?style=flat-square&logo=minio&logoColor=white)

An intelligent HR recruitment platform that automates CV processing, weighted AI scoring, candidate pipeline management, and campaign reporting.

Built for **Norsys Afrique** — Angular 21 frontend · Spring Boot 4.1 backend · external AI engine (contracted, simulated locally).

## Overview

```mermaid
flowchart LR
    Users(["Candidates & HR"]) --> Angular["Angular 21\n:4200"]
    Angular -->|REST + JWT| Spring["Spring Boot 4.1\n:8080"]
    Angular -.->|OIDC| Keycloak["Keycloak 24\n:8081"]
    Spring <-->|JDBC| Postgres[("PostgreSQL 16\n:5432")]
    Spring <-->|files| MinIO[("MinIO\n:9000")]
    Spring <-->|tasks / results| RabbitMQ["RabbitMQ\n:5672"]
    RabbitMQ <-->|tasks / results| AI["FastAPI AI engine\n:8000 (not in repo)"]
```

The AI engine is external; local runs use in-JVM simulators over the same RabbitMQ flow (`AI_SIMULATION_ENABLED=true`). See the full [component map](docs/03-architecture/README.md).

**Stack:** Angular 21 · Spring Boot 4.1 / Java 21 · PostgreSQL 16 · Keycloak 24 · RabbitMQ · MinIO — full detail in the [backend](docs/03-architecture/backend.md) and [frontend](docs/03-architecture/frontend.md) architecture.

## Quick Start

```bash
# 1. Start infrastructure
docker compose up -d

# 2. Backend  → http://localhost:8080
cd backend && ./mvnw spring-boot:run

# 3. Frontend → http://localhost:4200
cd frontend && pnpm install && pnpm start
```

Default login: **`admin` / `admin`** (`HR_ADMIN`). Full setup, ports, and credentials: [Getting Started](docs/02-getting-started.md).

## Screenshots

<table>
  <tr>
    <td width="50%"><img src="docs/assets/00-careers-portal.png" width="100%" alt="Public careers portal — offer listing"></td>
    <td width="50%"><img src="docs/assets/01-dashboard-kpis.png" width="100%" alt="Dashboard KPIs and 7-day chart"></td>
  </tr>
  <tr>
    <td width="50%"><img src="docs/assets/02-offer-detail-criteria.png" width="100%" alt="Offer detail — weighted criteria and AI status"></td>
    <td width="50%"><img src="docs/assets/03-cv-import-progress.png" width="100%" alt="CV bulk import — per-file progress panel"></td>
  </tr>
  <tr>
    <td width="50%"><img src="docs/assets/04-candidate-profile.png" width="100%" alt="Candidate profile — AI score and criteria validation"></td>
    <td width="50%"><img src="docs/assets/05-reporting-charts.png" width="100%" alt="Reporting — funnel and score distribution"></td>
  </tr>
  <tr>
    <td width="50%"><img src="docs/assets/06-workflow-board.png" width="100%" alt="Kanban workflow board"></td>
    <td width="50%"><a href="docs/assets/07-cv-pipeline-async.mp4"><img src="docs/assets/07-cv-pipeline-async-poster.png" width="100%" alt="Async CV extraction pipeline demo"></a></td>
  </tr>
</table>

*Click the last thumbnail to play the async CV pipeline demo (MP4, 52 s). Full list: [asset index](docs/assets/README.md).*

## Documentation

Start at the [documentation home](docs/README.md). Common entry points:

| Topic | Guide |
|-------|-------|
| Project context, roles, scope | [Overview](docs/01-overview.md) |
| Run the full stack | [Getting Started](docs/02-getting-started.md) |
| System design | [Architecture](docs/03-architecture/README.md) |
| Features | [CV Ingestion](docs/04-features/cv-ingestion.md) · [Reporting](docs/04-features/reporting.md) |
| REST API | [API Reference](docs/06-api/reference.md) |
| Contributing | [Git workflow](CONTRIBUTING.md) |
