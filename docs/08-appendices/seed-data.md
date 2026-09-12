# Demo seed data

## Purpose

`V7__demo_seed.sql` populates a coherent, realistic dataset so the dashboard, reporting, and exports can be demonstrated and visually validated without manual data entry.

## What it seeds

**Offers** — generic placeholders are upgraded to real technology roles (e.g. *Tech Lead Java / Spring Boot*, *Développeur Front-End Angular Senior*, *Architecte Solutions Cloud AWS*, *Consultant Cybersécurité & SecOps*, *Ingénieur QA & Automatisation*), each with weights across the five categories, mandatory requirements, and a `min_score` threshold.

**Candidates** — authentic Moroccan professional profiles with `+2126…` contact numbers and rich `category_scores` JSONB including component scores, `strengths`, and `weaknesses`.

**CV profiles** — full `extracted_data` JSON (personal info, coursework, experience, skills, certifications) for the core demonstration offers.

**Score tiers** — profiles deliberately span all four reporting tiers (Excellents ≥ 85, Qualifiés 70–84.9, Moyens 50–69.9, Insuffisants < 50) so the score-distribution chart is fully populated.

**Temporal spread** — `applied_at` values are distributed across the last 24 hours, 7, 30, 45, and 120 days to exercise every period filter (`30d`, `90d`, `1y`, `ALL`).

**Workflow history** — sequential audit transitions (e.g. `NEW → SHORTLISTED → INTERVIEWING → HIRED`) so the reporting funnel counts are monotonic and logically sound.

## Notes

- Demo data is for local/dev/UAT only and should not be loaded in production.

## Related

- [Migrations](../07-operations/migrations.md)
- [Reporting](../04-features/reporting.md)
- [Dashboard](../04-features/dashboard.md)
