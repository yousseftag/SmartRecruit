# SmartRecruit Keycloak Architecture Guide

This document explains the modern Keycloak authentication architecture implemented in this project. It is designed to solve critical performance and rendering bugs that occur when mixing public and secured routes in an Angular 18+ application.

---

## 1. The Problem We Solved

Previously, the application was configured to initialize Keycloak globally at bootstrap (using an `APP_INITIALIZER`). 
This caused a massive bottleneck for the public-facing features (like the Careers Job Board):
1. **The Iframe Hang:** Keycloak attempted to silently check the user's session using a hidden `<iframe>`. 
2. **Blocked Rendering:** Because initialization was global, Angular forced **every** route—even public ones—to wait for this iframe check to finish before drawing the screen.
3. **Infinite Loading:** If the iframe was blocked by browser privacy settings or CSP headers, the entire app froze, resulting in an infinite "Chargement des offres..." screen.

---

## 2. The New Architecture

We completely revamped how Keycloak integrates with Angular to guarantee that public pages load instantly, while HR administrative pages remain fully secured.

### A. Deferred Lazy Initialization (`auth.guard.ts`)
We removed Keycloak initialization from `app.config.ts`. Keycloak **no longer boots up when the app loads**. 

Instead, Keycloak is initialized **lazily** inside `auth.guard.ts` using the `KeycloakInitService`. 
* **Public Routes:** If a user visits `/careers`, Keycloak is never loaded, saving bandwidth and instantly rendering the page.
* **Secured Routes:** If an HR admin visits `/dashboard`, the `auth.guard` triggers, initializes Keycloak, checks their roles, and redirects them to the Keycloak login screen if necessary.

### B. Smart Custom Interceptor (`keycloak-bearer.interceptor.ts`)
We replaced the buggy default `includeBearerTokenInterceptor` with our own application-owned `keycloakBearerInterceptor`.

This interceptor evaluates every outgoing HTTP request:
1. **Public Traffic:** If the URL matches `/api/v1/public/**`, it calls `next(request)` **immediately**. It completely bypasses Keycloak, ensuring the HTTP call is fired instantly.
2. **Secured Traffic:** If the URL does not contain `/public/`, it pauses the request, forces a Keycloak token refresh, injects the `Authorization: Bearer <token>` header, and sends it to the Spring Boot backend.

### C. Signals for Reactivity
All frontend components consuming data (like `CareersList`) now strictly use Angular Signals (`signal()`) instead of standard class properties. This guarantees that when the HTTP request resolves, Angular's zoneless change detection instantly repaints the UI.

---

## 3. Developer Guidelines

If you are a developer building a new feature on this project, follow these rules:

1. **You only need one `HttpClient`:** You do not need any special HTTP backends or hacks. Just inject `HttpClient` and use it normally in your Services.
2. **URL Naming Matters:** The custom interceptor relies purely on the URL string. 
   - If your endpoint does NOT require auth, the URL **must** contain `/public/` (e.g., `/api/v1/public/jobs`).
   - If your endpoint DOES require auth, do not use `/public/` in the URL.
3. **Use Signals:** When binding HTTP data to the HTML template, always wrap your data in a `signal()` and update it using `.set()`. 

*Note: If you modify the HTTP providers or interceptors in the future, always perform a hard browser reload (`Ctrl+Shift+R`) during local development.*
