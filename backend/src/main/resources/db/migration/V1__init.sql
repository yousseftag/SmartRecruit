-- V1__init.sql
-- Requires PostgreSQL 13+ for gen_random_uuid() (pgcrypto is built-in via pgcrypto ext on some images).

CREATE TABLE app_user (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    keycloak_sub VARCHAR(255) UNIQUE NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    role VARCHAR(50) NOT NULL CHECK (role IN ('HR_ADMIN', 'RECRUITER', 'VIEWER')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE offer (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    -- Architecture: ON DELETE SET NULL prevents losing business data if an HR employee leaves/is deleted.
    created_by UUID REFERENCES app_user(id) ON DELETE SET NULL,
    title VARCHAR(255) NOT NULL,
    description_markdown TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT'
        CHECK (status IN ('ACTIVE', 'DRAFT', 'CLOSED')),
    -- Expected JSON Structure:
    -- { "skills": 20, "experience": 20, "coursework": 20, "languages": 20, "localization": 20 }
    category_weights JSONB NOT NULL, -- app-level validation: values must sum to 100
    -- Expected JSON Structure:
    -- {
    --   "skills": ["java", "js", "spring"],
    --   "experience": 12,
    --   "coursework": ["bac+5", "master"],
    --   "languages": ["English", "French"],
    --   "localization": "Agadir"
    -- }
    category_criteria JSONB,
    min_score INTEGER,
    duration_months INTEGER,
    contract_type VARCHAR(50),
    -- Expected JSON Structure (from FastAPI):
    -- {
    --   "missing_from_criteria": ["Docker", "Agile methodology"],
    --   "insights": "Found extra requirements in the job description..."
    -- }
    extracted_requirements JSONB,    -- null until FastAPI extraction completes
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_offer_created_by ON offer(created_by);
CREATE INDEX idx_offer_status ON offer(status);

-- Keep updated_at honest on every UPDATE (Postgres won't do this automatically).
CREATE OR REPLACE FUNCTION set_updated_at() RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_offer_updated_at
    BEFORE UPDATE ON offer
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- Architecture: Separated from cv_file (1:N) to natively support future V2 Candidate Authentication (Keycloak)
-- where one candidate can log in, manage multiple CVs, and select which one to apply with.
CREATE TABLE candidate (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    -- Nullable: Populated asynchronously via AI extraction. May remain null if extraction fails.
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    phone VARCHAR(50),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_candidate_email ON candidate(email);

CREATE TABLE cv_file (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    candidate_id UUID NOT NULL REFERENCES candidate(id) ON DELETE CASCADE,
    storage_key VARCHAR(255) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    checksum_sha256 VARCHAR(64) NOT NULL UNIQUE, -- enforces the dedup guarantee at the DB level
    extraction_status VARCHAR(50) NOT NULL DEFAULT 'PENDING'
        CHECK (extraction_status IN ('PENDING', 'SUCCESS', 'FAILED')),
    -- Extracted raw text/data from the CV (JSON Structure)
    -- {
    --   "candidate_info": { "first_name": "John", "last_name": "Doe", "email": "john@email.com", "phone": "+123", "current_job_title": "Backend Developer" },
    --   "description_markdown": "Backend developer with 2 years of experience...",
    --   "skills": ["java", "spring boot"],
    --   "experience": 24,
    --   "coursework": ["master"],
    --   "languages": ["English"],
    --   "localization": "Agadir"
    -- }
    extracted_data JSONB,
    uploaded_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMPTZ
);
CREATE INDEX idx_cv_file_candidate ON cv_file(candidate_id);
-- checksum_sha256 is already indexed implicitly via its UNIQUE constraint.

-- score_breakdown merged in here: strictly 1:1 with application (one AI-computed
-- score per application, per the fused-flow sequence diagram), so a separate
-- table just adds a join every ranking-page load for no benefit.
CREATE TABLE application (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    candidate_id UUID NOT NULL REFERENCES candidate(id) ON DELETE CASCADE,
    offer_id UUID NOT NULL REFERENCES offer(id) ON DELETE CASCADE,
    -- Architecture: CASCADE avoids triangle-dependency crashes when a Candidate is deleted.
    cv_file_id UUID NOT NULL REFERENCES cv_file(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL DEFAULT 'NEW'
        -- Workflow Status Dictionary (Original Domain Language):
        -- 'NEW'            : Newly applied, pending AI extraction and scoring
        -- 'SHORTLISTED'    : Passed initial AI/Recruiter check
        -- 'INTERVIEWING'   : Currently in interview process
        -- 'FOLLOW_UP'      : Follow-up required
        -- 'HIRED'          : Job offer extended/accepted
        -- 'REJECTED'       : Candidate was rejected
        -- 'ARCHIVED'       : Application archived for future reference
        CHECK (status IN ('NEW', 'SHORTLISTED', 'INTERVIEWING', 'FOLLOW_UP', 'HIRED', 'REJECTED', 'ARCHIVED')),

    -- Nullable AI Fields: Populated asynchronously via NLP callback.
    total_score NUMERIC(5, 2),
    -- Expected JSON Structure:
    -- { "skills": 66.67, "experience": 100.0, "coursework": 100.0, "languages": 50.0, "localization": 100.0 }
    category_scores JSONB,
    -- Expected JSON Structure:
    -- {
    --   "matched_criteria": { "skills": ["java"], "experience": true, "coursework": ["master"], "languages": ["English"], "localization": "Agadir" },
    --   "strengths": ["Strong Java background", "Master degree matches requirements"],
    --   "weaknesses": ["Missing JS skills", "Lacks 2 years of required experience"]
    -- }
    extracted_matching JSONB,
    scored_at TIMESTAMPTZ,

    applied_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- one candidate can't apply twice to the same offer
    CONSTRAINT uq_application_candidate_offer UNIQUE (candidate_id, offer_id)
);
CREATE INDEX idx_application_offer ON application(offer_id);
CREATE INDEX idx_application_candidate ON application(candidate_id);
CREATE INDEX idx_application_status ON application(status);
-- Ranking queries filter by offer and sort by score together — composite index covers both.
CREATE INDEX idx_application_offer_score ON application(offer_id, total_score DESC);

CREATE TABLE workflow_status_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    application_id UUID NOT NULL REFERENCES application(id) ON DELETE CASCADE,
    from_status VARCHAR(50),
    to_status VARCHAR(50) NOT NULL,
    -- Architecture: Preserves audit logs even if the HR employee is deleted.
    changed_by UUID REFERENCES app_user(id) ON DELETE SET NULL,
    changed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_workflow_history_application ON workflow_status_history(application_id);


