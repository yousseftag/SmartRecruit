-- V2__seed_data.sql

-- 1. Create a Recruiter User 
INSERT INTO app_user (id, keycloak_sub, first_name, last_name, email, role)
VALUES 
('11111111-1111-1111-1111-111111111111', 'recruiter-sub-123', 'John', 'Recruiter', 'recruiter@smartrecruit.com', 'RECRUITER')
ON CONFLICT (email) DO NOTHING;

-- 2. Create 3 Distinct Job Offers
INSERT INTO offer (id, created_by, title, description_markdown, status, category_weights, category_criteria, min_score, duration_months, contract_type)
VALUES 
(
  '22222222-2222-2222-2222-222222222222', 
  '11111111-1111-1111-1111-111111111111', 
  'Senior Full-Stack Developer', 
  'Norsys Afrique recherche un Développeur Full-Stack Senior passionné pour rejoindre notre équipe à Agadir. Vous développerez avec Spring Boot, Angular et PostgreSQL.', 
  'ACTIVE', 
  '{"skills": 30, "experience": 30, "coursework": 15, "languages": 10, "localization": 15}',
  '{"skills": ["java", "spring boot", "angular", "postgresql"], "experience": 36, "coursework": ["bac+5", "master"], "languages": ["English", "French"], "localization": "Agadir"}', 
  80,
  null, 
  'CDI'
),
(
  '33333333-3333-3333-3333-333333333333', 
  '11111111-1111-1111-1111-111111111111', 
  'Stage: Data Scientist (NLP / LLM)', 
  'Rejoignez notre équipe R&D pour concevoir le moteur d''extraction et de matching intelligent des CVs avec FastAPI, PyTorch et Transformers.', 
  'ACTIVE', 
  '{"skills": 40, "experience": 10, "coursework": 25, "languages": 15, "localization": 10}',
  '{"skills": ["python", "pytorch", "fastapi", "huggingface", "spacy"], "experience": 0, "coursework": ["bac+5", "engineering"], "languages": ["English", "French"], "localization": "Casablanca"}', 
  70,
  6, 
  'Stage'
),
(
  '44444444-4444-4444-4444-444444444444', 
  '11111111-1111-1111-1111-111111111111', 
  'DevOps & Cloud Engineer', 
  'Recherche d''un ingénieur DevOps pour orchestrer nos pipelines CI/CD, clusters Kubernetes et infrastructure cloud AWS.', 
  'ACTIVE', 
  '{"skills": 35, "experience": 25, "coursework": 15, "languages": 10, "localization": 15}',
  '{"skills": ["docker", "kubernetes", "aws", "terraform", "ci/cd"], "experience": 24, "coursework": ["bac+5", "master"], "languages": ["English", "French"], "localization": "Rabat"}', 
  75,
  null, 
  'CDI'
);

-- 3. Candidate 1 (Alice): NEW, FAILED (Public Apply Flow: Has a name, AI failed)
INSERT INTO candidate (id, first_name, last_name, email, phone)
VALUES ('10000000-0000-0000-0000-000000000001', 'Alice', 'Smith', 'alice@example.com', '+212600000001');

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data)
VALUES ('11000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 'dummy/key/alice.pdf', 'alice.pdf', 'hash1', 'FAILED', null);

INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, category_scores, extracted_matching)
VALUES ('12000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', '22222222-2222-2222-2222-222222222222', '11000000-0000-0000-0000-000000000001', 'NEW', null, null, null);

-- 4. Candidate 2 (Bob): NEW, PENDING (Public Apply Flow: Has a name, waiting for AI extraction)
INSERT INTO candidate (id, first_name, last_name, email, phone)
VALUES ('20000000-0000-0000-0000-000000000002', 'Bob', 'Jones', 'bob@example.com', '+212600000002');

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data)
VALUES ('21000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000002', 'dummy/key/bob.pdf', 'bob.pdf', 'hash2', 'PENDING', null);

INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, category_scores, extracted_matching)
VALUES ('22000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000002', '22222222-2222-2222-2222-222222222222', '21000000-0000-0000-0000-000000000002', 'NEW', null, null, null);

-- 5. Candidate 3 (Charlie): NEW, SUCCESS, Low Score
INSERT INTO candidate (id, first_name, last_name, email, phone)
VALUES ('30000000-0000-0000-0000-000000000003', 'Charlie', 'Brown', 'charlie@example.com', '+212600000003');

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data)
VALUES ('31000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000003', 'dummy/key/charlie.pdf', 'charlie.pdf', 'hash3', 'SUCCESS', '{"candidate_info": {"first_name": "Charlie", "last_name": "Brown"}}');

INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, category_scores, extracted_matching)
VALUES ('32000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000003', '22222222-2222-2222-2222-222222222222', '31000000-0000-0000-0000-000000000003', 'NEW', 45.00, '{"skills": 40}', '{"weaknesses": ["Low experience"]}');

-- 6. Candidate 4 (David): NEW, SUCCESS, Good Score
INSERT INTO candidate (id, first_name, last_name, email, phone)
VALUES ('40000000-0000-0000-0000-000000000004', 'David', 'Lee', 'david@example.com', '+212600000004');

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data)
VALUES ('41000000-0000-0000-0000-000000000004', '40000000-0000-0000-0000-000000000004', 'dummy/key/david.pdf', 'david.pdf', 'hash4', 'SUCCESS', '{"candidate_info": {"first_name": "David", "last_name": "Lee"}}');

INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, category_scores, extracted_matching)
VALUES ('42000000-0000-0000-0000-000000000004', '40000000-0000-0000-0000-000000000004', '22222222-2222-2222-2222-222222222222', '41000000-0000-0000-0000-000000000004', 'NEW', 85.00, '{"skills": 90}', '{"strengths": ["Great skills"]}');

-- 7. Candidate 5 (Ghost - Corrupted): NEW, FAILED (No name, No email. AI crashed)
INSERT INTO candidate (id, first_name, last_name, email, phone)
VALUES ('50000000-0000-0000-0000-000000000005', null, null, null, null);

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data)
VALUES ('51000000-0000-0000-0000-000000000005', '50000000-0000-0000-0000-000000000005', 'dummy/key/ghost.pdf', 'ghost.pdf', 'hash5', 'FAILED', null);

INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, category_scores, extracted_matching)
VALUES ('52000000-0000-0000-0000-000000000005', '50000000-0000-0000-0000-000000000005', '22222222-2222-2222-2222-222222222222', '51000000-0000-0000-0000-000000000005', 'NEW', null, null, null);
