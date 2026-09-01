# Feature 3: Candidate Application & Bulk CV Ingestion with Asynchronous AI Matching

---

## 1. Feature Overview

The **Candidate Application & Ingestion Pipeline** is the core entry point for candidate profiles into SmartRecruit. It supports two main ingestion channels:
1. **Public Candidate Application (`/apply`)**: Direct candidate portal submission with verified form inputs (First name, Last name, Email, Phone, Offer ID, CV file).
2. **HR Bulk Import (`/import`)**: Recruiter batch upload of standalone CV files (PDF/DOCX) or multi-file ZIP archives against a target job offer.

Key System Capabilities:
- **Instant SHA-256 Deduplication**: Prevents duplicate document storage in MinIO and reuses extracted candidate profile data across multiple applications.
- **Offer-Specific Scoring**: While CV text extraction is cached at the document level (`CvFile`), candidate-offer matching and scoring are computed per `Application`.
- **Asynchronous Queue-Driven AI Pipeline (RabbitMQ)**: Non-blocking asynchronous message queues (`cv.processing.queue` and `cv.sync.queue`) provide reliable backpressure, decoupled workers, and zero data loss.
- **Resilient State Machine**: 6 distinct frontend task states (`PENDING`, `UPLOADING`, `PARSING`, `SUCCESS`, `DUPLICATE`, `FAILED`) with selective retries and stall detection.
- **Data Synchronization**: In-place null-only candidate enrichment and lazy authenticated PDF preview streaming.

---

## 2. Ingestion Workflows & End-to-End Flow

### A. Public Direct Application (`POST /api/v1/applications/apply`)
1. Candidate fills out the public application form on the careers portal.
2. Form data (`ApplyRequest`) includes `firstName`, `lastName`, `email`, `phone`, `offerId`, and the CV file (`MultipartFile`).
3. Backend looks up candidate by email:
   - If candidate exists $\rightarrow$ updates name/phone and reuses existing `Candidate` record.
   - If candidate is new $\rightarrow$ creates a new `Candidate` entity.
4. File is processed: SHA-256 checksum calculated, uploaded to MinIO storage, linked to `CvFile` and `Application` in `PENDING` extraction state.
5. Dispatches CV extraction task to RabbitMQ `cv.processing.queue`.

---

### B. HR Bulk Import & Asynchronous AI Architecture (`POST /api/v1/applications/import`)

```mermaid
sequenceDiagram
    autonumber
    actor Recruiter as HR / Recruiter (UI)
    participant FE as Angular Frontend
    participant API as Spring Boot Backend (Ingestion)
    participant MinIO as MinIO Storage
    participant DB as PostgreSQL Database
    participant RMQ_REQ as RabbitMQ (cv.processing.queue)
    participant Worker as AI Worker / Simulation
    participant RMQ_RES as RabbitMQ (cv.sync.queue)
    participant Consumer as CvSyncConsumer (@RabbitListener)

    Note over Recruiter, FE: Phase 1: Ingestion & Storage (~200ms)
    Recruiter->>FE: Select Offer + Drop PDF/DOCX/ZIP
    FE->>API: POST /api/v1/applications/import (Multipart)
    
    loop For each file / ZIP entry
        API->>API: Calculate SHA-256 Checksum
        API->>DB: Check if Hash Exists (cv_file)
        alt New CV Document
            API->>MinIO: Upload File Stream (resumes/{uuid}.ext)
            API->>DB: Create CvFile (PENDING) & Candidate Placeholder
        else Existing CV Document
            API->>DB: Fetch existing Candidate & CvFile from Checksum
        end
        
        API->>DB: Check uq_application_candidate_offer (Candidate + Offer)
        alt Candidate Already Applied to this Offer
            API-->>API: Tag as DUPLICATE (Skip DB write)
        else Valid New Application
            API->>DB: Save Application (Status: NEW, TotalScore: null)
            API->>RMQ_REQ: CvIngestionProducer publishes task to cv.processing.queue
        end
    end
    
    API-->>FE: HTTP 200 ImportResponse (fileStatuses with PENDING)

    Note over FE, Worker: Phase 2: Asynchronous AI Extraction & Queue Sync
    loop Poll until Terminal State (Exponential Backoff: 2s -> 3s -> 4.5s...)
        FE->>API: GET /api/v1/applications/{id}/extraction-status
        API->>DB: Check Application (totalScore) & CvFile (extractionStatus)
        API-->>FE: { id, extractionStatus: PENDING | SUCCESS | FAILED | STALLED }
    end

    RMQ_REQ->>Worker: Consumes task from cv.processing.queue
    Note over Worker: Runs NLP entity extraction & Offer match scoring (6-9s)
    Worker->>RMQ_RES: Publishes SyncRequestDto to cv.sync.queue

    RMQ_RES->>Consumer: @RabbitListener pulls message from cv.sync.queue
    Consumer->>DB: NlpService updates CvFile, Application (Score), Candidate (Enrichment)
    
    FE-->>FE: Polling receives SUCCESS / FAILED
    FE-->>Recruiter: UI badge turns Green/Red; Candidate List & Profile reflect live scores
```

---

## 3. API & Messaging Reference

### A. HTTP REST Endpoints

| Endpoint | Method | Role | Description |
|---|---|---|---|
| `/api/v1/applications/apply` | `POST` | `PUBLIC` | Direct public applicant form submission (Candidate + CvFile + Application). |
| `/api/v1/applications/import` | `POST` | `HR_ADMIN`, `RECRUITER` | Ingests a batch of PDF/DOCX CVs or ZIP archives against `offerId`. Returns minimal `ImportResponse`. |
| `/api/v1/applications/{id}/extraction-status` | `GET` | `HR_ADMIN`, `RECRUITER`, `VIEWER` | Lightweight JPQL projection query for UI polling (evaluates application score + CV extraction status). |
| `/api/v1/applications/{id}/cv` | `GET` | `HR_ADMIN`, `RECRUITER`, `VIEWER` | Authenticated lazy PDF streaming directly from MinIO with inline disposition. |
| `/api/v1/applications/{id}/re-extract` | `POST` | `HR_ADMIN`, `RECRUITER` | Resets application scores to null, sets extraction status to `PENDING`, and re-dispatches to RabbitMQ. |
| `/api/v1/internal/cv/sync` | `POST` | `INTERNAL` | Asynchronous AI webhook endpoint (kept for backward compatibility and fallback testing). |

### B. RabbitMQ AMQP Channels

| Queue | Exchange | Routing Key | Direction | Producer | Consumer |
|---|---|---|---|---|---|
| `cv.processing.queue` | `ai.exchange` | `cv.routing.key` | Request | `CvIngestionProducer` (Spring Boot) | Python AI Worker / `SimulationNlpService` |
| `cv.sync.queue` | `ai.exchange` | `cv.sync.routing.key` | Response | Python AI Worker / `SimulationNlpService` | `CvSyncConsumer` (Spring Boot) |
| `offer.processing.queue` | `ai.exchange` | `offer.routing.key` | Request | Spring Boot | Python AI Worker |

---

## 4. Data Transfer Objects (DTOs)

### A. `ApplyRequest` (Public Portal)
```java
public record ApplyRequest(
    @NotBlank String firstName,
    @NotBlank String lastName,
    @NotBlank @Email String email,
    String phone,
    @NotNull UUID offerId,
    @NotNull MultipartFile file
) {}
```

### B. `ImportResponse` & `FileImportStatus` (Bulk Ingestion)
```java
public record ImportResponse(List<FileImportStatus> fileStatuses) {
  public record FileImportStatus(
      String filename,
      UUID applicationId,       // Present if a new application was created
      Integer extractedCount,   // Number of CVs extracted if archive
      String errorCode,         // "DUPLICATE", "INVALID_FORMAT", "CORRUPTED", "EMPTY_ARCHIVE"
      String message,           // Human-readable summary message
      List<String> subErrors    // Detailed sub-errors for skipped/failed files inside ZIP
  ) {}
}
```

### C. `SyncRequestDto` (RabbitMQ & Webhook Callback Payload)
```java
public class SyncRequestDto {
  @NotNull private UUID applicationId;
  @NotNull private UUID offerId;
  @NotNull private UUID cvId;
  @NotNull private ExtractionStatus extractionStatus; // SUCCESS | FAILED
  private ExtractedDataDto extractedData;
  private ExtractedMatchingDto extractedMatching;
  private CategoryScoresDto categoryScores;
  private BigDecimal totalScore;
}
```

---

## 5. State Machine & Task Status Matrix

```
[ PENDING ] ──► [ UPLOADING (50%) ] ──► [ PARSING (75%) ] ──► [ SUCCESS (100%) ]
     │                                         │
     ├─────────────► [ DUPLICATE ]             ├─────────────► [ STALLED (0%) ] ──► [ Relancer ]
     │                                         │
     └─────────────► [ FAILED ]                └─────────────► [ FAILED (0%) ]
```

| State | Badge | Color | Retryable? | Behavior |
|---|---|---|---|---|
| **`PENDING`** | `En attente` | Gray / Slate | Yes | Queued in memory; waiting for recruiter to click "Démarrer l'importation". |
| **`UPLOADING`** | `Téléchargement` | Blue | No | Multipart HTTP transfer to backend (50% progress). |
| **`PARSING`** | `Extraction IA` | Amber Pulse | No | Backend created application; polling `/extraction-status` with backoff (75% progress). |
| **`STALLED`** | `Bloqué` | Orange | **Yes** | Backend marked job stalled (>5 min pending) or restore probe exceeded age. Inline "Relancer" resets & redispatches. |
| **`SUCCESS`** | `Terminé` | Emerald / Green | No | AI extraction and offer scoring completed. Direct link to candidate profile. |
| **`DUPLICATE`** | `Doublon` | Amber Solid | **No** | Candidate already applied to this specific job offer. Excluded from retry queue. |
| **`FAILED`** | `Échec` | Red | **Yes** | Corrupted ZIP, unreadable PDF, or AI failure. Can be retried via "Relancer". |

---

## 6. Key Architectural Decisions & Rationale

### 1. RabbitMQ Asynchronous Consumer vs Synchronous HTTP Webhook
- **Decision**: Implemented `CvSyncConsumer` listening to `cv.sync.queue`.
- **Rationale**:
  - **Downtime Resilience**: If Spring Boot restarts during a deployment, RabbitMQ holds all completed extraction messages in durable on-disk queues. Zero data loss.
  - **Decoupling**: The Python worker does not need to know Spring Boot's host/port, only the message broker.
  - **Backpressure**: Prevents hundreds of simultaneous HTTP callbacks from overwhelming Spring Boot and PostgreSQL connection pools.

### 2. Document-Level SHA-256 Deduplication vs Offer-Level Scoring
- **Decision**: `CvFile` is cached by SHA-256 hash, but `Application` status evaluates whether that specific application has been scored.
- **Rationale**: If Candidate X applies to 3 different offers with the same PDF, the raw document extraction (skills, experience, contact info) is reused immediately, but the match score against each distinct offer's requirements is independently computed and tracked.

### 3. MinIO Configuration Lifecycle
- **Decision**: Moved MinIO bucket creation to `MinioConfig` `@PostConstruct` / `@Bean` lifecycle.
- **Rationale**: Avoids redundant `bucketExists()` network calls to MinIO on every file upload. The bucket is verified once at application startup.

### 4. Null-Only Candidate Enrichment
- **Decision**: `NlpService` only updates `Candidate` entity fields (`firstName`, `lastName`, `email`, `phone`) if they are currently `null` or blank.
- **Rationale**: Prevents AI simulation drift from overwriting confirmed user data, avoids broken foreign keys, and guarantees 100% synchronization between Candidate List and Profile views.

### 5. Configurable Dev/Prod AI Simulation Mode
- **Decision**: Added `@ConditionalOnProperty(name = "app.ai-simulation.enabled")` to `SimulationNlpService` backed by `AI_SIMULATION_ENABLED`.
- **Rationale**: In development, Spring Boot can simulate realistic sequential AI extraction (6–9s) without requiring the Python service. In production, switching `AI_SIMULATION_ENABLED=false` completely disables the simulation with **zero code changes**.

### 6. Reactive Non-Overlapping Polling with Exponential Backoff & Backend Stall Detection
- **Decision**: Replaced `setInterval`-based polling with an RxJS `expand` + `timer` + `switchMap` stream (`watchExtractionStatus$`) paired with a backend Quartz scheduler (`CvStallDetectionJob`).
- **Rationale**:
  - **Non-Overlapping Stream**: Guarantees exactly one in-flight HTTP request at a time.
  - **Exponential Backoff**: Starts at 2s, multiplies by 1.5×, capped at 15s. Reduces server pressure while staying responsive early.
  - **Soft Stalled Warning**: At attempt 10 (~30s), displays an informational warning while continuing to poll.
  - **Backend Stall Sweeper**: Backend Quartz scheduler sweeps every 1 minute and flags any `CvFile` pending for >5 min as `STALLED`, stopping pointless polling and showing an inline "Relancer" action.

---

## 7. Configuration Reference

```yaml
# application.yml
app:
  cors:
    allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:4200,http://localhost:8080}
  cv-ingestion:
    stall-threshold-minutes: ${CV_STALL_THRESHOLD_MINUTES:5}
  ai-simulation:
    enabled: ${AI_SIMULATION_ENABLED:true}

spring:
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: 5672
    username: ${RABBITMQ_USER:guest}
    password: ${RABBITMQ_PASS:guest}
```



