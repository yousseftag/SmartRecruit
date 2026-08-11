-- V2__seed_data.sql

-- 1. Create a Recruiter User 
-- We use a fixed UUID so we can reference it in offers
INSERT INTO app_user (id, keycloak_sub, first_name, last_name, email, role)
VALUES 
('11111111-1111-1111-1111-111111111111', 'recruiter-sub-123', 'John', 'Recruiter', 'recruiter@smartrecruit.com', 'RECRUITER')
ON CONFLICT (email) DO NOTHING;

-- 2. Create Offers
INSERT INTO offer (id, created_by, title, description_markdown, status, category_weights, category_criteria, duration_months, contract_type)
VALUES 
(
  '22222222-2222-2222-2222-222222222222', 
  '11111111-1111-1111-1111-111111111111', 
  'Senior Full-Stack Developer', 
  'Norsys Afrique recherche un Développeur Full-Stack Senior passionné pour rejoindre notre équipe à Agadir. Vous participerez activement au développement de SmartRecruit, une plateforme innovante propulsée par l''IA pour révolutionner le recrutement. Vous travaillerez avec Spring Boot, Angular, et PostgreSQL.', 
  'ACTIVE', 
  '{"skills": 30, "experience": 30, "coursework": 15, "languages": 10, "localization": 15}',
  '{"skills": ["java", "js", "spring", "angular"], "experience": 36, "coursework": ["bac+5", "master"], "languages": ["English", "French"], "localization": "Agadir"}', 
  null, 
  'CDI'
),
(
  '33333333-3333-3333-3333-333333333333', 
  '11111111-1111-1111-1111-111111111111', 
  'Stage: Data Scientist (NLP)', 
  'Rejoignez notre équipe de R&D pour construire le moteur NLP de parsing de CV. Ce stage de fin d''études est une excellente opportunité pour appliquer vos connaissances en Machine Learning et NLP sur un vrai produit.', 
  'ACTIVE', 
  '{"skills": 40, "experience": 10, "coursework": 25, "languages": 15, "localization": 10}',
  '{"skills": ["python", "pytorch", "fastapi", "spacy", "huggingface"], "experience": 0, "coursework": ["bac+5", "engineering"], "languages": ["English", "French"], "localization": "Casablanca"}', 
  6, 
  'Stage'
);

-- 3. Create a Dummy Candidate and Application (already "Extracted") to preview Shot 4 UI
INSERT INTO candidate (id, first_name, last_name, email, phone)
VALUES ('44444444-4444-4444-4444-444444444444', 'Alice', 'Smith', 'alice.smith@example.com', '+212600000000');

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data)
VALUES (
  '55555555-5555-5555-5555-555555555555', 
  '44444444-4444-4444-4444-444444444444', 
  'dummy/key/alice_cv.pdf', 
  'alice_cv.pdf', 
  'dummy_hash_1', 
  'SUCCESS',
  '{
    "candidate_info": { "first_name": "Alice", "last_name": "Smith", "email": "alice.smith@example.com", "phone": "+212600000000" },
    "description_markdown": "Full-stack developer with strong Java skills and frontend experience.",
    "skills": ["java", "spring boot", "angular"],
    "experience": 24,
    "coursework": ["master"],
    "languages": ["English", "French"],
    "localization": "Agadir"
  }'
);

INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, category_scores, extracted_matching)
VALUES (
  '66666666-6666-6666-6666-666666666666',
  '44444444-4444-4444-4444-444444444444',
  '22222222-2222-2222-2222-222222222222',
  '55555555-5555-5555-5555-555555555555',
  'NEW',
  85.50,
  '{"skills": 90, "experience": 80, "coursework": 100, "languages": 100, "localization": 100}',
  '{
    "matched_criteria": { "skills": ["java", "spring", "angular"], "experience": true, "coursework": ["master"], "languages": ["English", "French"], "localization": "Agadir" },
    "strengths": ["Strong Java background", "Master degree matches requirements", "Perfect localization"],
    "weaknesses": ["Missing plain JS skills", "Lacks 1 year of required experience"]
  }'
);
