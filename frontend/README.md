# SmartRecruit Frontend

Angular 21+ Single Page Application (SPA) utilizing Signals, Standalone Components, and Tailwind CSS v4 for the HR recruitment workspace and public career portal.

---

## Quick Start

### 1. Install Dependencies
```bash
pnpm install
```

### 2. Run Development Server
```bash
pnpm start
# or: ng serve
```

Navigate to [http://localhost:4200](http://localhost:4200). The application automatically reloads on any source file changes.

> **Requires**: Backend running on `localhost:8080` and Keycloak running on `localhost:8081`.  
> See the [Getting Started guide](../docs/02-getting-started.md) to start all infrastructure containers.

---

## Detailed Documentation

Full documentation lives in the central [`docs/`](../docs/README.md) set:

- **[Getting Started](../docs/02-getting-started.md)** — Prerequisites and running the full stack
- **[Frontend Architecture](../docs/03-architecture/frontend.md)** — Standalone components, Signals, Tailwind v4, folder structure
- **[Contributing](../CONTRIBUTING.md)** — Prettier, git flow, Conventional Commits
- **[Conventions](../docs/05-integrations/conventions.md)** — English keywords ↔ French UI labels
- **[Dashboard](../docs/04-features/dashboard.md)** — Metrics, charts, activity history
- **[CV Ingestion](../docs/04-features/cv-ingestion.md)** — Bulk upload UI, progress polling, dropzone
