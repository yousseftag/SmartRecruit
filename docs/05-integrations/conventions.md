# Conventions

## Internal keywords (English)

All technical keywords — database values, API payloads, enums, roles — are written in **full English**. The pills below are the exact persisted/serialized values.

### Roles

| Value | French label |
|-------|--------------|
| ![HR_ADMIN](https://img.shields.io/badge/HR__ADMIN-7C3AED?style=flat-square) | Admin RH |
| ![RECRUITER](https://img.shields.io/badge/RECRUITER-2563EB?style=flat-square) | Recruteur |
| ![VIEWER](https://img.shields.io/badge/VIEWER-475569?style=flat-square) | Consultation |

### Application statuses

| Value | French label |
|-------|--------------|
| ![NEW](https://img.shields.io/badge/NEW-6B7280?style=flat-square) | Nouveau |
| ![SHORTLISTED](https://img.shields.io/badge/SHORTLISTED-2563EB?style=flat-square) | Présélectionné |
| ![INTERVIEWING](https://img.shields.io/badge/INTERVIEWING-4F46E5?style=flat-square) | Convoqué |
| ![FOLLOW_UP](https://img.shields.io/badge/FOLLOW__UP-D97706?style=flat-square) | Relancé |
| ![HIRED](https://img.shields.io/badge/HIRED-16A34A?style=flat-square) | Confirmé |
| ![REJECTED](https://img.shields.io/badge/REJECTED-DC2626?style=flat-square) | Refusé |
| ![ARCHIVED](https://img.shields.io/badge/ARCHIVED-475569?style=flat-square) | Archivé |

### Offer statuses

| Value | French label |
|-------|--------------|
| ![DRAFT](https://img.shields.io/badge/DRAFT-6B7280?style=flat-square) | Brouillon |
| ![ACTIVE](https://img.shields.io/badge/ACTIVE-16A34A?style=flat-square) | Active |
| ![CLOSED](https://img.shields.io/badge/CLOSED-DC2626?style=flat-square) | Clôturée |

### CV extraction statuses

| Value | French label |
|-------|--------------|
| ![PENDING](https://img.shields.io/badge/PENDING-D97706?style=flat-square) | En attente |
| ![STALLED](https://img.shields.io/badge/STALLED-EA580C?style=flat-square) | Bloqué |
| ![SUCCESS](https://img.shields.io/badge/SUCCESS-16A34A?style=flat-square) | Terminé |
| ![FAILED](https://img.shields.io/badge/FAILED-DC2626?style=flat-square) | Erreur |

### Offer AI statuses

| Value | French label |
|-------|--------------|
| ![PENDING](https://img.shields.io/badge/PENDING-D97706?style=flat-square) | En attente |
| ![STALLED](https://img.shields.io/badge/STALLED-EA580C?style=flat-square) | Bloqué |
| ![SUCCESS](https://img.shields.io/badge/SUCCESS-16A34A?style=flat-square) | Terminé |
| ![FAILED](https://img.shields.io/badge/FAILED-DC2626?style=flat-square) | Erreur |

## Rules

- Never send French keywords over the API or persist them.
- Never hardcode display strings in components; resolve them through the shared constant mapping.
- When adding an enum value, update this document, the backend enum, and the frontend mapping together.

## Related

- [Glossary](../glossary.md)
- [Frontend](../03-architecture/frontend.md)
