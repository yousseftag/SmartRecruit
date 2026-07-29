# 🚀 SmartRecruit CI/CD Pipeline Guide

Welcome! This document briefly explains how our GitLab CI pipeline works to ensure code quality and prevent broken code from reaching the `develop` and `main` branches.

## 🛠️ What Does the Pipeline Do?
Whenever the pipeline runs, it executes three stages in parallel for both the Backend (Java/Spring Boot) and Frontend (Angular/pnpm):

1. **Lint (`backend_lint`, `frontend_lint`)**
   - **Backend:** Checks Java formatting using Spotless (`./mvnw spotless:check`).
   - **Frontend:** Checks TS/HTML/CSS formatting using Prettier (`pnpm run format:check`).
   - *Note: If linting fails, the pipeline crashes immediately. Code must be formatted!*

2. **Test (`backend_test`, `frontend_test`)**
   - **Backend:** Runs JUnit tests (`./mvnw test`).
   - **Frontend:** Runs Angular unit tests (`pnpm run test --watch=false`).

3. **Build (`backend_build`, `frontend_build`)**
   - **Backend:** Packages a production `.jar` file.
   - **Frontend:** Compiles the AOT production bundle into the `dist/` folder.
   - *Note: Build artifacts are automatically saved in GitLab for 1 week before being deleted.*

## 🚦 When Does the Pipeline Run?
To save CI minutes and avoid unnecessary builds, the pipeline **DOES NOT** run on every single commit you make to your personal feature branch. It only triggers automatically when:
- You open or update a **Merge Request** (MR).
- Code is merged into the **`develop`** branch.

## 🧠 Monorepo Smart Triggers
Because SmartRecruit contains both frontend and backend code, the pipeline is smart enough to only test what you actually changed:
- If your MR only modifies `frontend/` files, GitLab completely skips all heavy Java jobs.
- If your MR only modifies `backend/` files, GitLab completely skips all Node/Angular jobs.

## 🚑 How to Fix Pipeline Failures Locally
If the CI pipeline fails because of formatting (Lint stage), you don't need to fix the spacing manually! Run these commands on your local machine before pushing:

- **Fix Java Formatting:** 
  ```bash
  cd backend 
  ./mvnw spotless:apply
  ```
- **Fix Angular Formatting:** 
  ```bash
  cd frontend 
  npx prettier --write "src/**/*.{ts,html,css}"
  ```

## 💡 Pro-Tip: Automate Formatting in Your IDE
To avoid ever having to run those commands manually, configure your editor to "Format on Save" so your code is always CI-ready:

**For VS Code Users:**
- This is already configured globally in the `.vscode/settings.json` file! Just click "Install" when VS Code recommends the Prettier and Java extensions. Formatting happens instantly on `Ctrl+S`.

**For IntelliJ IDEA / WebStorm Users:**
- **Frontend (Prettier):** Go to `Settings > Languages & Frameworks > JavaScript > Prettier`. Select the Prettier package inside `node_modules` and check the box for **"Run on save"**.
- **Backend (Java):** Go to `Settings > Plugins` and install the **"Google Java Format"** plugin, then enable it for the project. Alternatively, you can configure a File Watcher to execute `./mvnw spotless:apply` automatically whenever you save a `.java` file.

## 🚨 Common Pipeline Errors & Troubleshooting

Here are the most common reasons the CI pipeline might fail and exactly how to fix them:

### 1. `ERR_PNPM_NO_LOCKFILE` (Frontend)
**The Problem:** The pipeline strictly uses `pnpm install --frozen-lockfile`. If you used standard `npm install` on your local machine, you likely pushed a `package-lock.json` file instead of the required `pnpm-lock.yaml`.
**The Fix:**
1. Run `rm frontend/package-lock.json`
2. Run `cd frontend && corepack enable pnpm && pnpm install`
3. Commit the newly generated `pnpm-lock.yaml` file.

### 2. "Ghost Commits" / Formatting Loops
**The Problem:** You push code, the CI pipeline fails the linting stage, so you push again, and it fails again.
**The Fix:** CI pipelines are strictly "read-only" gatekeepers. The pipeline will NEVER format your code for you! You must format it locally using the IDE "Format on Save" tips above, or run the spotless/prettier scripts manually, and commit the newly formatted files.
