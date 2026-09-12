# Database migrations

## Approach

Schema is managed exclusively by Flyway. Hibernate runs with `ddl-auto: validate`, and `clean-disabled: true` prevents accidental wipes. Scripts live in `backend/src/main/resources/db/migration/` and are applied in order at startup.

## History

| Version | File | Change |
|---------|------|--------|
| V1 | `V1__init.sql` | Core schema: `app_user`, `offer`, `candidate`, `cv_file`, `application`, `workflow_status_history`; indexes and the `updated_at` trigger. |
| V2 | `V2__add_username_to_app_user.sql` | Adds `app_user.username` (unique, not null). |
| V3 | `V3__add_dashboard_columns.sql` | Adds `application.passed_min_score`, `offer.updated_by`, and `idx_application_ai_passed`. |
| V4 | `V4__add_extraction_status_stalled.sql` | Extends the CV extraction status constraint with `STALLED`. |
| V5 | `V5__email_templates.sql` | Creates `email_template` and seeds three French templates. |
| V6 | `V6__add_offer_ai_status.sql` | Adds `offer.offer_ai_status` and its partial index. |
| V7 | `V7__demo_seed.sql` | Demo dataset for dashboard/reporting. See [Seed Data](../08-appendices/seed-data.md). |

## Rules

1. **Never edit an applied migration** — Flyway verifies each script's checksum and refuses to start on a mismatch.
2. **Add a new versioned file** for every change (`V8__...sql`), even in early development, once the script is shared.
3. Keep migrations deterministic and idempotent where possible (`IF NOT EXISTS`, `DROP CONSTRAINT IF EXISTS`).
4. The database is the last line of defense: prefer `NOT NULL`/`CHECK`/`UNIQUE` constraints for hard invariants; delegate soft business rules to the DTO layer.

## Checksum mismatch

If you modified a migration after it was applied, Flyway throws `FlywayValidateException`.

**Early development (V1 not yet shared):** drop and rebuild the local schema:

```bash
docker exec -i smartrecruit-postgres psql -U postgres -d smartrecruit \
  -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"
```

**After V1 is finalized:** never rewrite `V1__init.sql`; create a new `V2__...`-style script instead.

## Related

- [Database Architecture](../03-architecture/database.md)
- [Getting Started](../02-getting-started.md)
- [Seed Data](../08-appendices/seed-data.md)
