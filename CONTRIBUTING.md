# Contributing

This guide covers the Git workflow for the SmartRecruit monorepo (frontend, backend, and the external AI service).

## Initial setup (once)

After cloning, configure Git to use the team commit template:

```bash
git config --local commit.template .gitmessage
```

This opens a formatted template whenever you run `git commit`.

## Daily workflow

### 1. Get the latest code

```bash
git checkout develop
git pull origin develop
```

### 2. Create a branch

Never work directly on `develop`. Use a `<folder>/<short-description>` name:

```bash
git checkout -b frontend/login-page
```

### 3. Make and stage your changes

```bash
git add .              # all changes
git add path/to/file   # or specific files
```

### 4. Commit

Run `git commit` without `-m` so the template opens. Messages follow the [Conventional Commits spec](https://www.conventionalcommits.org/en/v1.0.0/): `type(scope): message`, e.g. `feat(frontend): add login page`.

### 5. Push

```bash
git push -u origin <your-branch-name>
```

### 6. Open a pull request

Open a PR against `develop`, describe the change, and request a review. Once approved, it is merged into `develop`.

## Before you push

Run the relevant checks locally to keep CI green. See [CI/CD](docs/07-operations/ci-cd.md) for the full list and troubleshooting:

```bash
cd backend  && ./mvnw spotless:apply && ./mvnw test
cd frontend && npx prettier --write "src/**/*.{ts,html,css}" && pnpm run test --watch=false
```

When changing documentation, follow the [documentation style guide](docs/README.md#documentation-style-guide).
