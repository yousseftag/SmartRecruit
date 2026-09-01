# Frontend - SmartRecruit Setup & Architecture Guide

This document outlines the initial setup, architectural decisions, and technology stack for the **SmartRecruit** frontend application. It serves as a guide for the development team, especially those newer to modern Angular.

---

## 1. Technology Stack

*   **Framework:** Angular v21+ (Latest Stable)
*   **Language:** TypeScript v5.9+
*   **Styling:** Tailwind CSS v4
*   **State Management / Reactivity:** Angular Signals
*   **Authentication:** Keycloak (OAuth2 / OIDC)

---

## 2. Key Angular Concepts (For Developers New to Angular)

To work efficiently in this project, here are the modern Angular concepts you need to know:

*   **Standalone Components:** We do not use `app.module.ts` or `NgModule`. Every component (`.ts` file) is completely independent and imports exactly what it needs directly. If a component needs a button, it imports the button directly.
*   **Signals:** This is Angular's new way of handling reactive data (state). Instead of complex RxJS observables, you wrap data in a `signal()`. When the signal's value changes, Angular surgically updates only the exact part of the HTML where that signal is used, leading to incredibly fast UI updates.
*   **Services & Dependency Injection:** Business logic and API calls (like fetching Candidates) do not live in Components. They live in **Services** (e.g., `CandidateService`). Components then "inject" these services via the `inject(CandidateService)` function.
*   **Lazy Loading:** We load the application in chunks. If a user visits the Dashboard, they only download the Dashboard code. The code for the Administration panel is fetched only if they navigate there. This is configured in `app.routes.ts` using `loadComponent`.
*   **Tailwind v4 (No CSS Files):** There is no `tailwind.config.js` because version 4 is a major rewrite designed to be completely CSS-driven. Furthermore, all component-specific `.css` files have been purged to strictly enforce utility-first styling directly in HTML templates. 
    *   **⚠️ CRITICAL STYLING RULE:** Our custom semantic colors and themes are defined globally in `src/styles.css` using the `@theme` directive. You **MUST** use these semantic aliases (e.g., `text-ink`, `bg-blue`, `border-line`, `text-slate`) instead of default Tailwind colors (like `text-gray-700` or `bg-blue-500`). This ensures the app's branding stays synchronized with our Keycloak theme.

---

## 3. Directory Structure Explained

The application uses a Feature-Driven Architecture. Code is grouped by what it does (e.g., "offers") rather than what it is (e.g., "controllers"). You will find this structure inside `src/app/`:

```text
src/app/
 ├── core/                 # Global singletons: HTTP Interceptors, Guards, API Services
 ├── shared/               # Reusable dumb components: DataTable, StatusBadge, KanbanBoard
 ├── layout/               # Application shell: Sidebar, Header, Footer
 ├── pages/                # The main business modules (Pages)
 │    ├── dashboard/       # Main KPIs and charts
 │    ├── offers/          # Offer lists, unified creation/editing form, and details
 │    ├── candidates/      # Candidate profiles, bulk import, manual dropzone
 │    ├── workflow/        # Kanban pipeline
 │    ├── reporting/       # Data analytics, reporting, and exports
 │    ├── administration/  # User & role management (Keycloak admin)
 │    └── settings/        # Workspace rules and email templates
 ├── app.ts                # The root shell container
 └── app.routes.ts         # Global routing definition
```

### `core/` (The Brain)
Contains singleton services and logic that are instantiated once and used globally across the app.
*   **Services:** Files communicating with the backend (e.g., `offer.service.ts`, `auth.service.ts`).
*   **Interceptors:** Code that intercepts all outgoing HTTP requests to attach the Keycloak JWT Bearer token.
*   **Guards:** Logic to prevent unauthorized users from accessing certain routes (e.g., hiding Administration from non-admins).

### `shared/` (The UI Toolkit)
Contains reusable "dumb" components that only care about presentation. They take data in (Inputs) and emit events out (Outputs). They contain no business logic.
*   **Components:** `DataTable`, `StatusBadge`, `FileDropzone`, `ScoreGauge`, `KanbanBoard`.

### `layout/` (The Skeleton)
Contains the shell of the application that remains constant across different pages.
*   **Components:** `Sidebar`, `Header`, `Footer`.

### `pages/` (The Pages)
Contains the actual pages and business modules. Each feature is self-contained.
*   `dashboard/`: Main KPIs and charts.
*   `offers/`: Offer lists, unified `offer-form` for creation/editing, and candidate ranking.
*   `candidates/`: Candidate profiles, manual dropzone, and bulk ZIP import.
*   `workflow/`: The Kanban pipeline board for moving candidates through interview stages.
*   `reporting/`: Data analytics, reporting, and exports.
*   `administration/`: User & role management (Restricted to Keycloak admins).
*   `settings/`: Workspace rules and email template editor.

### Root Files
*   `app.ts`: The root container holding the layout and the `<router-outlet>`.
*   `app.routes.ts`: Defines the lazy-loaded paths linking URLs to Feature components.

---

## 4. Running the Application

First, ensure all required packages are installed by running:

```bash
pnpm install
```

Then, to start the local development server, run:

```bash
pnpm start
# or 
ng serve
```

Once the server is running, open your browser and navigate to `http://localhost:4200/`. The application will automatically reload whenever you modify any of the source files.


