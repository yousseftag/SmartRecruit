# SmartRecruit Backend

This is the Spring Boot backend for the SmartRecruit application. It serves as the **sole gateway** to the database, orchestrating authentication, storage, and asynchronous AI tasks.

## 🚀 Getting Started

### 1. Start Local Infrastructure
Before running the Spring Boot application, you must start the required databases and brokers. These are defined at the **root** of the project repository.

```bash
# Navigate to the project root
cd ../

# Start the infrastructure in the background
docker compose up -d
```
This spins up:
- **PostgreSQL (pgvector)**: `localhost:5432`
- **RabbitMQ**: `localhost:5672` (Management UI at `localhost:15672`)
- **MinIO (S3 Storage)**: `localhost:9000` (Console at `localhost:9001`)
- **Keycloak (Auth)**: `localhost:8081`
- **Mailpit (Local Email Catcher)**: `localhost:1025` (Web UI at `localhost:8025`)

### 2. Run the Application
You can run the application directly from your IDE, or via Maven:
```bash
./mvnw spring-boot:run
```
Once running, the interactive API documentation (Swagger) is available at:
👉 `http://localhost:8080/swagger-ui.html`

---

## 📦 Core Technologies & Dependencies
- **Java 21 LTS**: Leveraging Virtual Threads for high-concurrency I/O.
- **Spring Boot 3.3+**: The core framework.
- **Flyway**: Strict SQL-based database migrations. *Note: Hibernate `ddl-auto` is set to `validate`. Do not change this; all schema changes must go through Flyway scripts.*
- **Spring Security (OAuth2)**: Secures endpoints via Keycloak JWT validation.
- **Spring Boot Quartz**: Handles robust, persistent scheduling for automated candidate follow-ups ("relances automatiques").
- **Springdoc OpenAPI**: Automatically generates the Swagger documentation.
- **Hibernate Types**: Used to map complex `JSONB` database columns (e.g., `category_weights`) to Java Objects.

---

## 📂 Architecture & Package Structure

We follow a **Package-by-Feature (Domain-Driven)** architecture. Each module is self-contained with its own internal layers (`controllers`, `services`, `repositories`, `entities`, `dtos`, `mappers`).

```text
com.smartrecruit.backend
├── config/            # Security, MinIO, RabbitMQ, Cors configurations
├── security/          # Keycloak JWT converters, custom role extractors
├── exceptions/        # GlobalExceptionHandler (@RestControllerAdvice)
├── modules/           # Grouped by feature (Domain-Driven)
│   ├── auth/          # Users, Roles syncing
│   ├── offer/         # Job Offers
│   ├── candidate/     # Candidate profiles, CV files
│   ├── application/   # Job applications, statuses, scores
│   └── reporting/     # Dashboard stats, PDF/Excel generators
└── integration/       # External services (Storage, Messaging, Email)
```

### ⚠️ Important Architectural Rules
1. **Public vs Private Endpoints**: Candidate-facing endpoints (e.g., fetching public offers, submitting a CV) must be explicitly set to `permitAll()` in the `SecurityFilterChain`. Recruiter endpoints must be protected using `@PreAuthorize`.
2. **No Scoring Module**: Spring Boot **does not** compute scores. Do not create a `scoring` module. The `ScoreBreakdown` entity is simply a data record attached to an Application, so it lives in `modules/application/`.

---

## 🧠 The AI "Fused Flow" (CV Processing)

The AI engine (FastAPI) has **no direct database access**. Spring Boot is responsible for gathering the context, triggering the AI, and persisting the results.

### 1. File Upload (MinIO & SHA-256)
When a CV is uploaded (whether single or in a bulk ZIP archive):
1. Spring Boot calculates the SHA-256 hash of the file.
2. If the hash does not exist in the DB: Upload the file to MinIO.
3. If the hash *does* exist: Skip the MinIO upload (but proceed to scoring).

### 2. RabbitMQ Trigger
Spring Boot publishes a unified payload to RabbitMQ to trigger the AI job. The payload must include:
- `cv_file_id` & `storage_key` (so FastAPI can download the PDF).
- `application_id`, `offer_id`, and the offer's `category_weights` & `extracted_requirements` JSON. *(Since FastAPI cannot read the DB, we must provide the grading rubric in the message).*

### 3. Sync Callback Endpoint
FastAPI will perform extraction, vectorization, and scoring in one asynchronous step. Once finished, it sends an HTTP POST request to the Spring Boot callback endpoint (`/api/v1/internal/cv/sync`).
This endpoint must accept a massive JSON payload containing *both* the `extracted_data` (to update the CV) and the `category_scores` (to create the `ScoreBreakdown` record) in a single transaction.
