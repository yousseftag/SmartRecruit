# AI service contract

## Status

The AI engine (FastAPI) is **not part of this repository**. This contract is the source of truth for the integration; the responsible team will implement it later. When the engine is absent, in-JVM simulators drive the identical message flow (`AI_SIMULATION_ENABLED=true`), so there is no code coupling today.

The contract covers two independent pipelines:

| Pipeline | Request | Result |
|----------|---------|--------|
| Offer pre-processing | `offer.processing.queue` | HTTP `POST /api/v1/internal/offers/sync` |
| CV extraction & scoring | `cv.processing.queue` | `cv.sync.queue` (primary) or HTTP `POST /api/v1/internal/cv/sync` (fallback) |

## Offer pre-processing

### Request (Spring Boot → AI)

Queue `offer.processing.queue` (producer `OfferIngestionProducer`):

```json
{
  "offer_id": "uuid",
  "criterias": {
    "min_score": 80,
    "category_weights": { "skills": 20, "experience": 20, "coursework": 20, "languages": 20, "localization": 20 },
    "category_criteria": {
      "skills": ["java", "js", "spring"],
      "skill_weights": { "java": 5, "spring": 4, "js": 2 },
      "experience": 12,
      "coursework": ["bac+5", "master"],
      "languages": ["English", "French"],
      "localization": "Agadir"
    },
    "description_markdown": "...",
    "duration_months": 6,
    "contract_type": "CDI"
  }
}
```

`skill_weights` is optional (1–5 per skill); when omitted, all skills are weighted equally.

### Response (AI → Spring Boot)

HTTP `POST /api/v1/internal/offers/sync` (`OfferSyncController`) using `OfferSyncCallbackDto`:

```json
{
  "offer_id": "uuid",
  "aiStatus": "SUCCESS",
  "extracted_requirements": {
    "missing_from_criteria": ["Docker", "Agile methodology"],
    "insights": "Extra requirements found in the job description."
  }
}
```

### Offer status lifecycle

| Status | Meaning |
|--------|---------|
| `PENDING` | Set on create/update; awaiting AI. |
| `STALLED` | `OfferExtractionStallDetector` marked it after > 2 min (`OFFER_STALL_THRESHOLD_MINUTES`). |
| `SUCCESS` | Extraction complete; the offer may be published. |
| `FAILED` | Extraction failed; the offer cannot become `ACTIVE`. |

## CV extraction & scoring

### Request (Spring Boot → AI)

Queue `cv.processing.queue` (producer `CvIngestionProducer`):

```json
{
  "application_id": "uuid",
  "offer_id": "uuid",
  "cv_id": "uuid",
  "storage_key": "resumes/{uuid}.pdf"
}
```

The FastAPI engine downloads the CV from MinIO using `storage_key`, then performs extraction, vectorization, and weighted scoring in one asynchronous step.

### Response (AI → Spring Boot)

Primary channel: `cv.sync.queue` consumed by `CvSyncConsumer`. Fallback: HTTP `POST /api/v1/internal/cv/sync` (`NlpController`). Payload class `SyncRequestDto`:

```json
{
  "applicationId": "uuid",
  "offerId": "uuid",
  "cvId": "uuid",
  "extractionStatus": "SUCCESS",
  "extractedData": {
    "candidate_info": {
      "first_name": "John",
      "last_name": "Doe",
      "email": "john.doe@email.com",
      "phone": "+123456789",
      "current_job_title": "Backend Developer"
    },
    "description_markdown": "Backend developer with 2 years of experience...",
    "skills": ["java", "spring boot", "postgres"],
    "experience": 24,
    "coursework": ["master"],
    "languages": ["English"],
    "localization": "Agadir"
  },
  "extracted_matching": {
    "matched_criteria": {
      "skills": ["java", "spring"],
      "experience": true,
      "coursework": ["master"],
      "languages": ["English"],
      "localization": "Agadir"
    },
    "strengths": ["Exceeds required experience (24 > 12)"],
    "weaknesses": ["Missing JavaScript (js) from required skills"]
  },
  "category_scores": {
    "skills": 18.5,
    "experience": 20.0,
    "coursework": 20.0,
    "languages": 10.0,
    "localization": 20.0
  },
  "total_score": 88.5
}
```

### Validation rules

Enforced by `SyncRequestDto`:

- **Terminal status only** — `extractionStatus` must be `SUCCESS` or `FAILED`. `PENDING`/`STALLED` returns `400 Bad Request`.
- **Score sum integrity** — when both `category_scores` and `total_score` are present, `total_score` must equal the sum of the five category scores exactly.

### CV status lifecycle

| Status | Meaning |
|--------|---------|
| `PENDING` | Queued or being parsed. |
| `STALLED` | `ExtractionStallDetector` marked it after > 5 min (`CV_STALL_THRESHOLD_MINUTES`). |
| `SUCCESS` | Extraction and scoring complete. |
| `FAILED` | Parsing error (unreadable/corrupted file, engine error). |

Both sync paths call the same `NlpService.syncCvData()` method.

## Related

- [Messaging](messaging.md)
- [CV Ingestion](../04-features/cv-ingestion.md)
- [Offer AI Pipeline](../04-features/offer-ai.md)
