# CI/CD

## Pipeline

Defined in `.gitlab-ci.yml`. Three stages run in parallel for backend and frontend:

| Stage | Backend | Frontend |
|-------|---------|----------|
| Lint | `./mvnw spotless:check` | `pnpm run format:check` |
| Test | `./mvnw test -Dspring.profiles.active=test` | `pnpm run test --watch=false` |
| Build | `./mvnw package -DskipTests` → `.jar` | `pnpm run build` → `dist/` |

Backend tests spin up PostgreSQL, MinIO, and RabbitMQ service containers. Build artifacts are retained for one week.

## Triggers

The pipeline runs only on:

- Merge requests (`merge_request_event`), or
- Pushes to `develop`.

**Smart triggers** — jobs run only when relevant paths change: backend jobs on `backend/**/*`, frontend jobs on `frontend/**/*`. A frontend-only MR skips all Java jobs and vice versa.

## Fixing failures locally

**Formatting (most common):**

```bash
cd backend && ./mvnw spotless:apply
cd frontend && npx prettier --write "src/**/*.{ts,html,css}"
```

Enable "Format on Save" in your IDE to avoid these failures entirely (already configured in `.vscode/settings.json`; for IntelliJ use the Prettier package and the Google Java Format plugin).

**`ERR_PNPM_NO_LOCKFILE`** — you ran `npm install`. Remove `frontend/package-lock.json`, run `pnpm install` from `frontend/`, and commit `pnpm-lock.yaml`.

CI never auto-formats code; formatted files must be committed by you.

## Planned improvements

- Path filtering so `docs/**` and `*.md` changes consume no runner minutes.
- Python jobs (Ruff + Pytest) once the AI service repository is integrated.
- Tiered triggers: fast lint/tests on MRs, full packaging on merges.
- Docker image builds, registry push, and a container smoke test.

## Related

- [Contributing](../../CONTRIBUTING.md)
- [Getting Started](../02-getting-started.md)
