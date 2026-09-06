-- V4__dashboard_dummy_data.sql
-- Bulk insert of dummy data for Dashboard testing using generate_series and deterministic UUIDs

-- 1. Insert 10 Users
INSERT INTO app_user (id, keycloak_sub, username, first_name, last_name, email, role)
SELECT 
    md5('user' || i)::uuid,
    'sub-user-' || i,
    'user' || i,
    'First' || i,
    'Last' || i,
    'user' || i || '@smartrecruit.com',
    CASE WHEN i % 3 = 0 THEN 'HR_ADMIN' WHEN i % 3 = 1 THEN 'RECRUITER' ELSE 'VIEWER' END
FROM generate_series(1, 10) i
ON CONFLICT DO NOTHING;

-- 2. Insert 15 Offers
WITH offer_base AS (
    SELECT 
        i,
        md5('offer' || i)::uuid as id,
        md5('user' || ((i % 10) + 1)::text)::uuid as created_by,
        md5('user' || ((i % 10) + 1)::text)::uuid as updated_by,
        'Position Title ' || i as title,
        'Description for position ' || i as description_markdown,
        CASE i % 10
            WHEN 0 THEN 'DRAFT' 
            WHEN 1 THEN 'CLOSED' 
            ELSE 'ACTIVE' 
        END as status,
        CURRENT_TIMESTAMP - (floor(random() * 3) || ' days')::interval - (floor(random() * 24) || ' hours')::interval as c_time,
        (i % 2 = 0) as is_updated
    FROM generate_series(1, 15) i
)
INSERT INTO offer (id, created_by, updated_by, title, description_markdown, status, category_weights, min_score, created_at, updated_at)
SELECT 
    id, created_by, updated_by, title, description_markdown, status,
    '{"skills": 20, "experience": 20, "coursework": 20, "languages": 20, "localization": 20}',
    70,
    c_time,
    CASE WHEN is_updated THEN c_time + (floor(random() * 12) + 1 || ' hours')::interval ELSE c_time END
FROM offer_base
ON CONFLICT DO NOTHING;

-- 3. Insert 30 Candidates
INSERT INTO candidate (id, first_name, last_name, email)
SELECT 
    md5('candidate' || i)::uuid,
    'CandFirst' || i,
    'CandLast' || i,
    'candidate' || i || '@example.com'
FROM generate_series(1, 30) i
ON CONFLICT DO NOTHING;

-- 4. Insert 30 CV Files
INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status)
SELECT 
    md5('cv' || i)::uuid,
    md5('candidate' || i)::uuid,
    'cvs/cand' || i || '.pdf',
    'cand' || i || '.pdf',
    md5('hash' || i),
    CASE 
        WHEN i = 4 THEN 'FAILED' 
        WHEN i = 15 THEN 'FAILED' 
        WHEN i = 24 THEN 'PENDING' 
        ELSE 'SUCCESS' 
    END
FROM generate_series(1, 30) i
ON CONFLICT DO NOTHING;

-- 5. Insert 30 Applications
WITH app_base AS (
    SELECT 
        i,
        md5('app' || i)::uuid as id,
        md5('candidate' || i)::uuid as candidate_id,
        md5('offer' || ((i % 15) + 1)::text)::uuid as offer_id,
        md5('cv' || i)::uuid as cv_file_id,
        CASE i % 10
            WHEN 0 THEN 'REJECTED'
            WHEN 1 THEN 'HIRED'
            WHEN 2 THEN 'INTERVIEWING'
            WHEN 3 THEN 'SHORTLISTED'
            ELSE 'NEW' 
        END as status,
        floor(random() * 50 + 50) as total_score,
        CURRENT_TIMESTAMP - (floor(random() * 6) || ' days')::interval - (floor(random() * 24) || ' hours')::interval as app_time
    FROM generate_series(1, 30) i
)
INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, passed_min_score, applied_at)
SELECT 
    id, candidate_id, offer_id, cv_file_id, status, total_score, 
    (i % 10 < 8), -- Exactly 80% AI success rate
    app_time
FROM app_base
ON CONFLICT DO NOTHING;

-- 6. Insert Workflow Status History
-- For applications that are NOT NEW, insert a SHORTLISTED transition
INSERT INTO workflow_status_history (id, application_id, from_status, to_status, changed_by, changed_at)
SELECT 
    md5('wf1' || id::text)::uuid,
    id,
    'NEW',
    'SHORTLISTED',
    md5('user' || ((floor(random() * 10) + 1)::int)::text)::uuid,
    applied_at + (floor(random() * 12) + 1 || ' hours')::interval
FROM application
WHERE status != 'NEW'
ON CONFLICT DO NOTHING;

-- For applications that are HIRED, INTERVIEWING, or REJECTED, add their final transition
INSERT INTO workflow_status_history (id, application_id, from_status, to_status, changed_by, changed_at)
SELECT 
    md5('wf2' || id::text)::uuid,
    id,
    'SHORTLISTED',
    status,
    md5('user' || ((floor(random() * 10) + 1)::int)::text)::uuid,
    applied_at + (floor(random() * 48) + 24 || ' hours')::interval
FROM application
WHERE status IN ('HIRED', 'INTERVIEWING', 'REJECTED')
ON CONFLICT DO NOTHING;
