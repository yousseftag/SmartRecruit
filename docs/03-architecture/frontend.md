# Frontend architecture

## Purpose

Angular 21 single-page application serving two experiences: the public careers portal (`/careers/**`) and the authenticated HR dashboard (`/hr/**`).

## Technology stack

| Concern | Choice |
|---------|--------|
| Framework | Angular 21 (standalone components, no NgModules) |
| Language | TypeScript 5.9 |
| Reactivity | Angular Signals (`signal`, `computed`) |
| Styling | Tailwind CSS v4 (CSS-driven, no `tailwind.config.js`) |
| Charts | Chart.js via `ng2-charts` |
| Icons | `@lucide/angular` |
| Auth | `keycloak-angular` / OIDC |
| Tests | Vitest |
| Formatting | Prettier |

## Key concepts

- **Standalone components** — each component imports exactly what it needs; there is no `app.module.ts`.
- **Signals** — reactive state is held in `signal()` and derived with `computed()` rather than RxJS subjects for component state.
- **Services** — API and business logic live in injectable services, not components.
- **Lazy loading** — routes use `loadComponent`, so feature bundles load on navigation.
- **Tailwind v4 semantic colors** — custom brand colors and themes are declared globally in `src/styles.css` with `@theme`. Use semantic aliases (`text-ink`, `bg-blue`, `border-line`, `text-slate`) instead of default Tailwind palette classes. Component `.css` files are intentionally absent to enforce utility-first styling.

## Directory structure

```text
src/app/
├── core/                 # Singletons: auth, guards, HTTP interceptors, API services, models
│   ├── auth/             # AuthService, keycloak init, bearer interceptor
│   ├── guards/           # authGuard
│   ├── models/           # Typed API models (offer, candidate, application, reporting, …)
│   └── services/         # application, offer, dashboard, reporting, workflow, user, theme
├── shared/               # Reusable presentational components and pipes
├── layout/               # App shell: main-layout, header, sidebar, footer
├── pages/                # Feature pages
│   ├── public/           # careers-list, career-detail (+ apply form)
│   ├── dashboard/
│   ├── offers/           # offers-list, offer-form, offer-detail
│   ├── candidates/       # candidates-list, candidate-import, candidate-profile
│   ├── workflow/         # workflow-board (Kanban)
│   ├── reporting/
│   ├── administration/   # user-management
│   ├── settings/         # template-settings
│   ├── edit-profile/
│   ├── sandbox/
│   └── not-found/
├── app.ts                # Root shell
├── app.html
└── app.routes.ts         # Global routing
```

## Routing

| Path | Access | Feature |
|------|--------|---------|
| `/careers`, `/careers/:id` | Public | Job listings and application |
| `/hr/dashboard` | Authenticated | KPIs and activity |
| `/hr/offers/**` | Read: all roles; write: `HR_ADMIN`, `RECRUITER` | Offer management |
| `/hr/candidates/**` | Read: all roles; import: `HR_ADMIN`, `RECRUITER` | Candidate management |
| `/hr/workflow` | Authenticated | Kanban pipeline |
| `/hr/reporting` | Authenticated | Analytics and exports |
| `/hr/administration` | `HR_ADMIN` | User management |
| `/hr/settings/templates` | `HR_ADMIN`, `RECRUITER` | Email templates |

Route-level role restrictions are declared in `data.roles` and enforced by `authGuard`. Authentication is described in [Security](security.md).

## Configuration

Runtime configuration lives in `src/environments/environment.ts` (Keycloak URL/realm/client, API URL, polling parameters). The HTTP bearer interceptor is registered in `app.config.ts`.

## Related

- [Security](security.md)
- [Dashboard](../04-features/dashboard.md)
- [API Reference](../06-api/reference.md)
