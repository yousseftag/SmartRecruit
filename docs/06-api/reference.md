# API Reference

All endpoints are rooted under `/api/v1`. Authentication is a Keycloak Bearer JWT unless marked *Public* or *Internal*.

Roles: `H` = `HR_ADMIN`, `R` = `RECRUITER`, `V` = `VIEWER`.

## Offers

| Method | Path | Roles | Description |
|--------|------|:-----:|-------------|
| GET | `/public/offers` | Public | List published offers |
| GET | `/public/offers/{id}` | Public | Published offer detail |
| GET | `/offers` | H R V | List offers (HR view) |
| GET | `/offers/titles` | H R V | Offer titles for filters/dropdowns |
| GET | `/offers/{id}` | H R V | Offer detail |
| POST | `/offers` | H R | Create offer (dispatches AI pre-processing) |
| PUT | `/offers/{id}` | H R | Update offer (re-dispatches AI pre-processing) |
| PATCH | `/offers/{id}/publish` | H R | Publish — requires `offerAiStatus == SUCCESS` |
| PATCH | `/offers/{id}/close` | H R | Close offer |
| PATCH | `/offers/{id}/reopen` | H R | Reopen offer |
| POST | `/offers/{id}/reprocess` | H R | Re-run AI requirement extraction |

## Applications & CV ingestion

| Method | Path | Roles | Description |
|--------|------|:-----:|-------------|
| POST | `/public/applications/apply` | Public | Submit a public application (multipart) |
| GET | `/applications` | H R V | List applications |
| GET | `/applications/{id}` | H R V | Application detail |
| POST | `/applications/import` | H R | Bulk import PDF/DOCX/ZIP (multipart) |
| GET | `/applications/{id}/extraction-status` | H R V | Lightweight polling projection |
| PUT | `/applications/{id}/status` | H R | Change workflow status (audited) |
| POST | `/applications/{id}/re-extract` | H R | Reset scores and re-dispatch to AI |
| DELETE | `/applications/{id}` | H R | Delete application |
| GET | `/applications/{id}/cv` | H R V | Stream the CV from MinIO |

## Workflow & email

| Method | Path | Roles | Description |
|--------|------|:-----:|-------------|
| GET | `/workflow/templates` | H R | List email templates |
| GET | `/workflow/templates/{id}` | H R | Get template |
| POST | `/workflow/templates` | H R | Create template |
| PUT | `/workflow/templates/{id}` | H R | Update template |
| DELETE | `/workflow/templates/{id}` | H R | Delete template |
| POST | `/workflow/applications/{id}/send-email` | H R | Render + send a template |

## Dashboard

| Method | Path | Roles | Description |
|--------|------|:-----:|-------------|
| GET | `/dashboard/stats` | H R V | KPI summary |
| GET | `/dashboard/priority-offers` | H R V | Offers with most new applications |
| GET | `/dashboard/recent-activities` | H R V | Recent activity feed |
| GET | `/dashboard/applications-by-day` | H R V | 7-day application volume |

## Reporting

| Method | Path | Roles | Description |
|--------|------|:-----:|-------------|
| GET | `/reporting/stats` | H R V | KPIs + funnel + distribution + top candidates |
| GET | `/reporting/candidates` | H R V | Ranked candidate rows |
| GET | `/reporting/export/excel` | H R V | Streaming `.xlsx` |
| GET | `/reporting/export/pdf` | H R V | Streaming `.pdf` |

Common reporting query params: `offerId` (UUID), `period` (`30d`/`90d`/`1y`/`ALL`), `limit` (exports/candidates), `timezone` or `X-Timezone`.

## Users

| Method | Path | Roles | Description |
|--------|------|:-----:|-------------|
| GET | `/users/me` | Any | Current user profile |
| PUT | `/users/me` | Any | Update own profile (dual-write) |
| GET | `/users` | H | List users |
| POST | `/users` | H | Create user |
| PUT | `/users/{id}` | H | Update user |
| DELETE | `/users/{id}` | H | Delete user |

## Internal (AI callbacks)

| Method | Path | Roles | Description |
|--------|------|:-----:|-------------|
| POST | `/internal/cv/sync` | Internal | CV extraction/scoring result (fallback) |
| POST | `/internal/offers/sync` | Internal | Offer requirement-extraction result |

Internal endpoints are `permitAll()` and intended for broker/network-isolated traffic.

## Documentation endpoint

| Path | Description |
|------|-------------|
| `/swagger-ui.html`, `/v3/api-docs/**` | OpenAPI / Swagger UI (public) |

## Related

- [Error Model](errors.md)
- [Security](../03-architecture/security.md)
- [AI Contract](../05-integrations/ai-contract.md)
