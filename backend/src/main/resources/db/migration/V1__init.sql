-- V1__init.sql
-- Requires PostgreSQL 13+ for gen_random_uuid() (pgcrypto is built-in via pgcrypto ext on some images).

CREATE TABLE app_user (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    keycloak_sub VARCHAR(255) UNIQUE NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    role VARCHAR(50) NOT NULL CHECK (role IN ('ADMIN_RH', 'RECRUTEUR', 'VIEWER')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE offer (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_by UUID REFERENCES app_user(id) ON DELETE SET NULL,
    title VARCHAR(255) NOT NULL,
    description_markdown TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT' 
        CHECK (status IN ('ACTIVE', 'DRAFT', 'CLOSED')),
    category_weights JSONB NOT NULL, -- app-level validation: values must sum to 100
    -- Storing extracted vectors as JSONB is required for the "SHA-256 Short-Circuit" to instantly score new CVs
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

CREATE TABLE candidate (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    email VARCHAR(255),
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
    extraction_status VARCHAR(50) NOT NULL DEFAULT 'EN_ATTENTE'
        CHECK (extraction_status IN ('EN_ATTENTE', 'OK', 'ERREUR')),
    -- Storing extracted vectors as JSONB is required to instantly score this CV against future offers
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
    cv_file_id UUID NOT NULL REFERENCES cv_file(id) ON DELETE RESTRICT,
    status VARCHAR(50) NOT NULL DEFAULT 'NOUVEAU'
        -- Workflow Status Dictionary (Original Domain Language):
        -- 'NOUVEAU'        : Newly applied, pending AI extraction and scoring
        -- 'PRESELECTIONNE' : Passed initial AI/Recruiter check
        -- 'CONVOQUE'       : Currently in interview process
        -- 'RELANCE'        : Follow-up required
        -- 'CONFIRME'       : Job offer extended/accepted
        -- 'REFUSE'         : Candidate was rejected
        -- 'ARCHIVE'        : Application archived for future reference
        CHECK (status IN ('NOUVEAU', 'PRESELECTIONNE', 'CONVOQUE', 'RELANCE', 'CONFIRME', 'REFUSE', 'ARCHIVE')),

    -- score fields (formerly score_breakdown), null until FastAPI's callback fires
    total_score NUMERIC(5, 2),
    category_scores JSONB,
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
    changed_by UUID REFERENCES app_user(id) ON DELETE SET NULL,
    changed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_workflow_history_application ON workflow_status_history(application_id);
