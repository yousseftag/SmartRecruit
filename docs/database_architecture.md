# 🗄️ SmartRecruit Database Architecture (V1)

This document serves as the official architectural guide for the SmartRecruit PostgreSQL database schema (`V1__init.sql`). It details the complex design decisions, constraints, and operational guidelines required for the AI-driven NLP pipeline.

## 1. Core Architectural Decisions

### 1.1 Candidate & CV Separation (Future-Proofing V2)
- **Design:** `candidate` and `cv_file` are strictly separated into a **1-to-Many** relationship.
- **Why:** While candidates are currently unauthenticated ("stateless"), separating them now natively supports V2 Keycloak authentication. In the future, a logged-in candidate can manage multiple CVs and simply choose which `cv_file_id` to pass when creating an `Application`.

### 1.2 The "Implicit Account" (Stateless Candidates)
- **Constraint:** `email VARCHAR(255) UNIQUE` in the `candidate` table.
- **Why:** If an unauthenticated candidate applies multiple times with the same email, PostgreSQL blocks duplicate `candidate` rows. Spring Boot catches this, finds the existing candidate, and simply attaches the new `cv_file` to their implicit profile. 
- **Note:** PostgreSQL ignores `NULL` in `UNIQUE` constraints. This allows HR to bulk-upload hundreds of anonymous "Ghost" CVs (where email is `NULL`) without crashing the database.

### 1.3 The "Override Application" Rule
- **Constraint:** `UNIQUE (candidate_id, offer_id)` in the `application` table.
- **Why:** A candidate cannot have two active applications for the exact same job. If they re-apply, this constraint forces Spring Boot to find their existing `application` row and update it (e.g., overriding the `cv_file_id` with a newer resume).

### 1.4 Honest Timestamps (Database Triggers)
- **Design:** A `plpgsql` Function and Trigger (`trg_offer_updated_at`) forces `updated_at = CURRENT_TIMESTAMP` before every `UPDATE`.
- **Why:** Relying on Spring Boot's `@UpdateTimestamp` only works if the update goes through Hibernate. By placing the trigger at the database level, it becomes physically impossible for raw SQL scripts, DataGrip, or FastAPI to alter a row without the `updated_at` timestamp reflecting the absolute truth.

---

## 2. Foreign Key Cascade & Safety Rules

Managing relational cleanup is critical. We use a mix of strict rules to ensure the dominoes fall in the correct direction:

### 2.1 `ON DELETE CASCADE` (Automated Cleanup)
- **`cv_file(candidate_id)`:** Deleting a Candidate automatically wipes their CVs.
- **`application(candidate_id)`:** Deleting a Candidate automatically wipes their Applications.
- **`application(offer_id)`:** Deleting a Job Offer automatically wipes all Applications for that job.
- **`application(cv_file_id)`:** Deleting a CV automatically wipes the Application attached to it. *(Note: This prevents a "triangle dependency" crash where deleting a candidate would otherwise fail due to conflicting child locks).*

### 2.2 `ON DELETE SET NULL` (Audit Preservation)
- **`offer(created_by)` & `workflow_status_history(changed_by)`:** If an HR employee (`app_user`) leaves the company and their account is deleted, we **do not** want a `CASCADE` to wipe out the company's job offers or application audit logs! Instead, the ID simply turns to `NULL` (e.g., "Created by: [Deleted User]").

---

## 3. Dealing with AI/NLP Failures (Optional Fields)

AI extraction is not perfect. The schema is highly fault-tolerant by explicitly allowing `NULL` values where AI data might fail or take time to process:

- **`cv_file`:** `extracted_data`, `processed_at` are `NULL` until FastAPI finishes processing.
- **`application`:** `total_score`, `category_scores`, `extracted_matching`, `scored_at` are all `NULL` until FastAPI returns the webhook.
- **`offer`:** `description_markdown`, `category_criteria`, `extracted_requirements` are optional.
- **`candidate`:** `first_name`, `last_name`, `email`, `phone` are optional so HR can bulk-upload CVs anonymously, allowing the AI to extract and populate this data later.

---

## 4. Query Optimization (Indexing)

Indexes (B-Trees) act like the back of a textbook, allowing PostgreSQL to jump directly to specific rows without scanning the entire table.

### 4.1 Standard Indexes
- **`idx_offer_created_by`:** Allows HR reps to instantly load "My Offers" (`WHERE created_by = ?`) without scanning everyone else's offers.
- **`idx_offer_status`:** Instantly filters active vs. closed offers.

### 4.2 The Composite Index (Ranking Dashboard)
- **`CREATE INDEX idx_application_offer_score ON application(offer_id, total_score DESC);`**
- **Why:** The Recruiter Dashboard needs the top candidates for a specific job: `WHERE offer_id = X ORDER BY total_score DESC LIMIT 50`. Without this index, PostgreSQL would load thousands of applications into RAM and sort them manually. With this composite index, the database pre-sorts them on the hard drive. It instantly locates the offer and scoops up the first 50 rows with **zero memory sorting required**.

---

## 5. Development & Flyway Migrations

### Dealing with Flyway Checksum Errors
Flyway rigidly tracks the cryptographic checksum of every applied migration script. 
- **The Error:** If you modify `V1__init.sql` locally *after* you have already run Spring Boot, Flyway will throw a `FlywayValidateException` (checksum mismatch) and refuse to start the app, protecting the database from silent corruption.
- **The Fix (Early Development Only):** While in V1 development, the easiest fix is to completely drop the local schema and let Flyway rebuild it from scratch:
  ```bash
  docker exec -i smartrecruit-postgres psql -U postgres -d smartrecruit -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"
  ```
- **The Fix (Production/Late Development):** Once V1 is finalized and deployed, **never** modify `V1__init.sql`. To add new tables or columns, you must create a brand new file (e.g., `V2__add_new_feature.sql`) and Flyway will sequentially apply it on top of V1.
