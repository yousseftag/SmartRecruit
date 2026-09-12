# Workflow & communications

## Purpose

The workflow module moves applications through the recruitment pipeline, records an audit trail, and sends templated emails to candidates.

## Screenshot

![Kanban workflow board — all 6 pipeline columns with candidate cards](../assets/06-workflow-board.png)

## Status pipeline

```
NEW → SHORTLISTED → INTERVIEWING → FOLLOW_UP → HIRED | REJECTED | ARCHIVED
```

Transitions are applied via `PUT /api/v1/applications/{id}/status`. Every change appends a row to `workflow_status_history` (`from_status`, `to_status`, `changed_by`, `changed_at`) — this log feeds the dashboard activity feed and the reporting funnel.

Applications are presented as a Kanban board (`/hr/workflow`), where cards are moved between stage columns.

## Audit history

`workflow_status_history` is append-only. It preserves who moved an application and when, even if the HR user is later deleted (the reference becomes `NULL`). The reporting funnel relies on historical transitions so that candidates who advanced to terminal states still count in intermediate stages.

## Email templates

Templates live in the `email_template` table and are editable at `/hr/settings/templates`. Each has a `template_key`, subject, and HTML body with placeholders such as `{{nom_candidat}}`, `{{titre_offre}}`, and `{{nom_recruteur}}`.

Seed templates (`V5__email_templates.sql`):

| Key | Purpose |
|-----|---------|
| `INTERVIEW_INVITATION` | Interview convocation |
| `FOLLOW_UP` | Candidate follow-up |
| `REJECTION` | Rejection notice |

`POST /api/v1/workflow/applications/{id}/send-email` renders a template and sends it via SMTP (Mailpit in development).

## Reference

Workflow and email endpoints, with roles, are listed in the [API Reference](../06-api/reference.md#workflow--email).

## Related

- [CV Ingestion](cv-ingestion.md)
- [Dashboard](dashboard.md)
- [Reporting](reporting.md)
