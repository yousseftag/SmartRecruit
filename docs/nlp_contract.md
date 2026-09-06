# NLP Service Data Contract

This document outlines the JSON payloads exchanged between the **Spring Boot Backend** and the **FastAPI AI Service**. 

The AI Service is responsible for extracting concepts from job offers and candidate CVs (via LLM) and vectorizing them for rapid semantic scoring.

---

## 1. Offer Pre-processing
When a recruiter creates or updates a job offer, Spring Boot sends the criteria to the AI Service. The AI Service pre-processes the criteria and stores it in its Vector DB.

### Request (From Spring Boot to AI)
* **Protocol:** HTTP or RabbitMQ 
* **Queue (if async):** `offer.processing.queue`

```json
{
  "offer_id": "uuid",
  "criterias": {
    "min_score": 80,
    "category_weights": {
      "skills": 20, 
      "experience": 20, 
      "coursework": 20, 
      "languages": 20, 
      "localization": 20
    },
    "category_criteria": {
      "skills": ["java", "js", "spring"],
      "skill_weights": {
        "java": 5,
        "spring": 4,
        "js": 2
      },
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

> **Note on `skill_weights` (Optional):**  
> `skill_weights` is an optional map specifying individual importance ratings (1 to 5) for skills listed in `skills`. If present, the AI scoring engine weights matched skills accordingly (e.g., finding Java contributes more to the skills score than JS). If omitted, all skills are treated with equal weighting. This ensures 100% backward compatibility.

### Response (From AI to Spring Boot)
```json
{
  "offer_id": "uuid",
  "extracted_requirements": {
    "missing_from_criteria": ["Docker", "Agile methodology"],
    "insights": "Found extra requirements in the job description that were not explicitly listed in the category_criteria."
  }
}
```

---

## 2. CV Upload & Processing
When a candidate applies, Spring Boot calculates the PDF's hash. If the CV is new, it uploads it to MinIO and sends an async request to the AI Service to process and score it against the job offer.

### Request (From Spring Boot to AI)
* **Protocol:** RabbitMQ
* **Queue:** `cv.processing.queue`
* **Note:** `application_id`, `offer_id`, and `cv_id` are sent purely as Correlation IDs so they can be returned in the callback.

```json
{
  "application_id": "uuid",
  "offer_id": "uuid",
  "cv_id": "uuid",
  "storage_key": "minio/path/to/cv.pdf"
}
```

### Response (Callback from AI to Spring Boot)
* **Protocol:** HTTP POST
* **Endpoint:** `POST /api/v1/internal/cv/sync`

```json
{
  "applicationId": "b1b82c3c-8a02-4d2a-a92c-55c3c1e21b77",
  "offerId": "f9a82b3d-1e4a-4b9e-a89c-5d3c2e1f4b55",
  "cvId": "e3a82b3d-1e4a-4b9e-a89c-5d3c2e1f4b99",
  "extractionStatus": "SUCCESS",
  
  "extractedData": {
    "candidate_info": {
      "first_name": "John",
      "last_name": "Doe",
      "email": "john.doe@email.com",
      "phone": "+123456789",
      "current_job_title": "Backend Developer"
    },
    "description_markdown": "Backend developer with 2 years of experience building APIs...",
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
