-- V5__seed_data.sql

-- 1. Create a Recruiter User 
INSERT INTO app_user (id, keycloak_sub, username, first_name, last_name, email, role)
VALUES 
('11111111-1111-1111-1111-111111111111', 'recruiter-sub-123', 'recruiter', 'John', 'Recruiter', 'recruiter@smartrecruit.com', 'RECRUITER')
ON CONFLICT (email) DO NOTHING;

-- 2. Create 3 Distinct Job Offers
INSERT INTO offer (id, created_by, updated_by, title, description_markdown, status, category_weights, category_criteria, min_score, duration_months, contract_type)
VALUES 
(
  '22222222-2222-2222-2222-222222222222', 
  '11111111-1111-1111-1111-111111111111', 
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

-- ====================================================================
-- 3. CANDIDATE SEED DATA (Covering all 8 Ingestion & Scoring Scenarios)
-- ====================================================================

-- Candidate 1: Amine Tazi (High Match Score 88.50% >= 80% Min -> ADMISSIBLE, SHORTLISTED)
INSERT INTO candidate (id, first_name, last_name, email, phone)
VALUES ('10000000-0000-0000-0000-000000000001', 'Amine', 'Tazi', 'amine.tazi@example.com', '+212661122334');

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data, uploaded_at, processed_at)
VALUES (
  '11000000-0000-0000-0000-000000000001', 
  '10000000-0000-0000-0000-000000000001', 
  'resumes/11000000-0000-0000-0000-000000000001.pdf', 
  'CV_Amine_Tazi_Senior_Java.pdf', 
  'hash_amine_tazi_1', 
  'SUCCESS', 
  '{
    "candidate_info": {
      "first_name": "Amine",
      "last_name": "Tazi",
      "email": "amine.tazi@example.com",
      "phone": "+212661122334",
      "current_job_title": "Lead Développeur Java / Angular"
    },
    "description_markdown": "Ingénieur Full-Stack avec 4 ans d''expérience spécialisé sur l''écosystème Java/Spring Boot et Angular. Passionné par l''architecture microservices et la qualité logicielle.",
    "skills": ["Java", "Spring Boot", "Angular", "PostgreSQL", "Docker", "Git", "Clean Architecture"],
    "experience": 48,
    "coursework": ["Bac+5", "Master Informatique", "Ingénieur d''État"],
    "languages": ["French", "English", "Arabic"],
    "localization": "Agadir"
  }',
  NOW() - INTERVAL '2 days',
  NOW() - INTERVAL '2 days' + INTERVAL '8 seconds'
);

INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, passed_min_score, category_scores, extracted_matching, applied_at, scored_at)
VALUES (
  '12000000-0000-0000-0000-000000000001', 
  '10000000-0000-0000-0000-000000000001', 
  '22222222-2222-2222-2222-222222222222', 
  '11000000-0000-0000-0000-000000000001', 
  'SHORTLISTED', 
  88.50, 
  true, 
  '{"skills": 28.5, "experience": 30.0, "coursework": 15.0, "languages": 10.0, "localization": 15.0}', 
  '{
    "matched_criteria": {
      "skills": ["java", "spring boot", "angular", "postgresql"],
      "experience": 48,
      "coursework": ["bac+5", "master"],
      "languages": ["English", "French"],
      "localization": "Agadir"
    },
    "strengths": [
      "Excellente maîtrise technique sur la stack cible Spring Boot / Angular",
      "4 ans d''expérience validée en environnement Agile",
      "Diplôme Bac+5 Ingénieur d''État parfaitement aligné",
      "Réside actuellement à Agadir (disponibilité immédiate)"
    ],
    "weaknesses": []
  }',
  NOW() - INTERVAL '2 days',
  NOW() - INTERVAL '2 days' + INTERVAL '8 seconds'
);

-- Candidate 2: Sara Benali (Low Match Score 46.00% < 80% Min -> SOUS SEUIL, REJECTED)
INSERT INTO candidate (id, first_name, last_name, email, phone)
VALUES ('20000000-0000-0000-0000-000000000002', 'Sara', 'Benali', 'sara.benali@example.com', '+212662233445');

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data, uploaded_at, processed_at)
VALUES (
  '21000000-0000-0000-0000-000000000002', 
  '20000000-0000-0000-0000-000000000002', 
  'resumes/21000000-0000-0000-0000-000000000002.pdf', 
  'CV_Sara_Benali_Web.pdf', 
  'hash_sara_benali_2', 
  'SUCCESS', 
  '{
    "candidate_info": {
      "first_name": "Sara",
      "last_name": "Benali",
      "email": "sara.benali@example.com",
      "phone": "+212662233445",
      "current_job_title": "Développeuse Web Junior"
    },
    "description_markdown": "Développeuse débutante passionnée par le développement front-end basique (HTML, CSS, PHP).",
    "skills": ["HTML", "CSS", "JavaScript", "PHP", "MySQL"],
    "experience": 6,
    "coursework": ["Bac+2", "BTS Informatique"],
    "languages": ["French", "Arabic"],
    "localization": "Marrakech"
  }',
  NOW() - INTERVAL '3 days',
  NOW() - INTERVAL '3 days' + INTERVAL '7 seconds'
);

INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, passed_min_score, category_scores, extracted_matching, applied_at, scored_at)
VALUES (
  '22000000-0000-0000-0000-000000000002', 
  '20000000-0000-0000-0000-000000000002', 
  '22222222-2222-2222-2222-222222222222', 
  '21000000-0000-0000-0000-000000000002', 
  'REJECTED', 
  46.00, 
  false, 
  '{"skills": 12.0, "experience": 8.0, "coursework": 8.0, "languages": 8.0, "localization": 10.0}', 
  '{
    "matched_criteria": {
      "skills": ["javascript"],
      "experience": 6,
      "coursework": ["bac+2"],
      "languages": ["French"],
      "localization": "Marrakech"
    },
    "strengths": [
      "Bonnes bases en intégration web et PHP"
    ],
    "weaknesses": [
      "Compétences requises manquantes : Java, Spring Boot, Angular, PostgreSQL",
      "Expérience insuffisante (6 mois vs 36 mois demandés)",
      "Localisation différente (Marrakech vs Agadir demandée)"
    ]
  }',
  NOW() - INTERVAL '3 days',
  NOW() - INTERVAL '3 days' + INTERVAL '7 seconds'
);

-- Candidate 3: Youssef Mansouri (Data Science Intern 74.00% >= 70% Min -> ADMISSIBLE, INTERVIEWING)
INSERT INTO candidate (id, first_name, last_name, email, phone)
VALUES ('30000000-0000-0000-0000-000000000003', 'Youssef', 'Mansouri', 'youssef.mansouri@example.com', '+212663344556');

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data, uploaded_at, processed_at)
VALUES (
  '31000000-0000-0000-0000-000000000003', 
  '30000000-0000-0000-0000-000000000003', 
  'resumes/31000000-0000-0000-0000-000000000003.pdf', 
  'CV_Youssef_Mansouri_NLP.pdf', 
  'hash_youssef_nlp_3', 
  'SUCCESS', 
  '{
    "candidate_info": {
      "first_name": "Youssef",
      "last_name": "Mansouri",
      "email": "youssef.mansouri@example.com",
      "phone": "+212663344556",
      "current_job_title": "Élève Ingénieur IA / NLP"
    },
    "description_markdown": "Étudiant en dernière année d''école d''ingénieurs, passionné par le traitement automatique du langage naturel (NLP) et les architectures Transformers.",
    "skills": ["Python", "PyTorch", "Transformers", "FastAPI", "HuggingFace", "Scikit-Learn"],
    "experience": 0,
    "coursework": ["Bac+5", "Élève Ingénieur", "Master Recherche IA"],
    "languages": ["English", "French", "Arabic"],
    "localization": "Casablanca"
  }',
  NOW() - INTERVAL '1 day',
  NOW() - INTERVAL '1 day' + INTERVAL '9 seconds'
);

INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, passed_min_score, category_scores, extracted_matching, applied_at, scored_at)
VALUES (
  '32000000-0000-0000-0000-000000000003', 
  '30000000-0000-0000-0000-000000000003', 
  '33333333-3333-3333-3333-333333333333', 
  '31000000-0000-0000-0000-000000000003', 
  'INTERVIEWING', 
  74.00, 
  true, 
  '{"skills": 32.0, "experience": 8.0, "coursework": 20.0, "languages": 10.0, "localization": 4.0}', 
  '{
    "matched_criteria": {
      "skills": ["python", "pytorch", "fastapi", "huggingface"],
      "experience": 0,
      "coursework": ["bac+5", "engineering"],
      "languages": ["English", "French"],
      "localization": "Casablanca"
    },
    "strengths": [
      "Maîtrise avancée de Python, PyTorch et Transformers",
      "Projets universitaires pertinents en extraction d''entités nommées (NER)",
      "Excellente maîtrise de l''anglais technique"
    ],
    "weaknesses": []
  }',
  NOW() - INTERVAL '1 day',
  NOW() - INTERVAL '1 day' + INTERVAL '9 seconds'
);

-- Candidate 4: Karim Idrissi (DevOps & Cloud Engineer 91.00% >= 75% Min -> ADMISSIBLE, HIRED)
INSERT INTO candidate (id, first_name, last_name, email, phone)
VALUES ('40000000-0000-0000-0000-000000000004', 'Karim', 'Idrissi', 'karim.idrissi@example.com', '+212664455667');

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data, uploaded_at, processed_at)
VALUES (
  '41000000-0000-0000-0000-000000000004', 
  '40000000-0000-0000-0000-000000000004', 
  'resumes/41000000-0000-0000-0000-000000000004.pdf', 
  'CV_Karim_Idrissi_DevOps.pdf', 
  'hash_karim_devops_4', 
  'SUCCESS', 
  '{
    "candidate_info": {
      "first_name": "Karim",
      "last_name": "Idrissi",
      "email": "karim.idrissi@example.com",
      "phone": "+212664455667",
      "current_job_title": "Ingénieur DevOps & Cloud AWS"
    },
    "description_markdown": "Expert en automatisation d''infrastructure (IaC), orchestration Kubernetes et pipelines GitLab CI/CD.",
    "skills": ["Docker", "Kubernetes", "AWS", "Terraform", "CI/CD", "Ansible", "Linux", "Prometheus"],
    "experience": 36,
    "coursework": ["Bac+5", "Ingénieur d''État Réseaux & Systèmes"],
    "languages": ["French", "English", "Arabic"],
    "localization": "Rabat"
  }',
  NOW() - INTERVAL '4 days',
  NOW() - INTERVAL '4 days' + INTERVAL '6 seconds'
);

INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, passed_min_score, category_scores, extracted_matching, applied_at, scored_at)
VALUES (
  '42000000-0000-0000-0000-000000000004', 
  '40000000-0000-0000-0000-000000000004', 
  '44444444-4444-4444-4444-444444444444', 
  '41000000-0000-0000-0000-000000000004', 
  'HIRED', 
  91.00, 
  true, 
  '{"skills": 33.0, "experience": 24.0, "coursework": 15.0, "languages": 9.0, "localization": 10.0}', 
  '{
    "matched_criteria": {
      "skills": ["docker", "kubernetes", "aws", "terraform", "ci/cd"],
      "experience": 36,
      "coursework": ["bac+5", "master"],
      "languages": ["English", "French"],
      "localization": "Rabat"
    },
    "strengths": [
      "Profil senior complet correspondant exactement aux exigences de l''offre",
      "Certification AWS Solutions Architect Associate validée",
      "Réside à Rabat, ville du poste"
    ],
    "weaknesses": []
  }',
  NOW() - INTERVAL '4 days',
  NOW() - INTERVAL '4 days' + INTERVAL '6 seconds'
);

-- Candidate 5: Bob Jones (Public Ingestion -> PENDING, in-flight extraction)
INSERT INTO candidate (id, first_name, last_name, email, phone)
VALUES ('50000000-0000-0000-0000-000000000005', 'Bob', 'Jones', 'bob.jones@example.com', '+212665566778');

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data, uploaded_at, processed_at)
VALUES (
  '51000000-0000-0000-0000-000000000005', 
  '50000000-0000-0000-0000-000000000005', 
  'resumes/51000000-0000-0000-0000-000000000005.pdf', 
  'CV_Bob_Jones.pdf', 
  'hash_bob_pending_5', 
  'PENDING', 
  null,
  NOW() - INTERVAL '2 minutes',
  null
);

INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, passed_min_score, category_scores, extracted_matching, applied_at, scored_at)
VALUES (
  '52000000-0000-0000-0000-000000000005', 
  '50000000-0000-0000-0000-000000000005', 
  '22222222-2222-2222-2222-222222222222', 
  '51000000-0000-0000-0000-000000000005', 
  'NEW', 
  null, 
  null, 
  null, 
  null,
  NOW() - INTERVAL '2 minutes',
  null
);

-- Candidate 6: Alice Smith (AI Extraction FAILED, Has Contact Info -> Red Badge & Retry action)
INSERT INTO candidate (id, first_name, last_name, email, phone)
VALUES ('60000000-0000-0000-0000-000000000006', 'Alice', 'Smith', 'alice.smith@example.com', '+212666677889');

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data, uploaded_at, processed_at)
VALUES (
  '61000000-0000-0000-0000-000000000006', 
  '60000000-0000-0000-0000-000000000006', 
  'resumes/61000000-0000-0000-0000-000000000006.pdf', 
  'CV_Alice_Smith_Scanned.pdf', 
  'hash_alice_failed_6', 
  'FAILED', 
  null,
  NOW() - INTERVAL '30 minutes',
  NOW() - INTERVAL '30 minutes' + INTERVAL '4 seconds'
);

INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, passed_min_score, category_scores, extracted_matching, applied_at, scored_at)
VALUES (
  '62000000-0000-0000-0000-000000000006', 
  '60000000-0000-0000-0000-000000000006', 
  '22222222-2222-2222-2222-222222222222', 
  '61000000-0000-0000-0000-000000000006', 
  'NEW', 
  null, 
  null, 
  null, 
  null,
  NOW() - INTERVAL '30 minutes',
  null
);

-- Candidate 7: Ghost Candidate (Corrupted Bulk PDF -> FAILED, No name/email -> "Candidat Inconnu" Ghost UI)
INSERT INTO candidate (id, first_name, last_name, email, phone)
VALUES ('70000000-0000-0000-0000-000000000007', null, null, null, null);

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data, uploaded_at, processed_at)
VALUES (
  '71000000-0000-0000-0000-000000000007', 
  '70000000-0000-0000-0000-000000000007', 
  'resumes/71000000-0000-0000-0000-000000000007.pdf', 
  'Corrupted_Document_Scan.pdf', 
  'hash_ghost_corrupted_7', 
  'FAILED', 
  null,
  NOW() - INTERVAL '1 hour',
  NOW() - INTERVAL '1 hour' + INTERVAL '3 seconds'
);

INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, passed_min_score, category_scores, extracted_matching, applied_at, scored_at)
VALUES (
  '72000000-0000-0000-0000-000000000007', 
  '70000000-0000-0000-0000-000000000007', 
  '22222222-2222-2222-2222-222222222222', 
  '71000000-0000-0000-0000-000000000007', 
  'NEW', 
  null, 
  null, 
  null, 
  null,
  NOW() - INTERVAL '1 hour',
  null
);

-- Candidate 8: Mehdi Alaoui (Stalled Extraction > 5 min -> STALLED Orange Warning & Retry Button)
INSERT INTO candidate (id, first_name, last_name, email, phone)
VALUES ('80000000-0000-0000-0000-000000000008', 'Mehdi', 'Alaoui', 'mehdi.alaoui@example.com', '+212667788990');

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data, uploaded_at, processed_at)
VALUES (
  '81000000-0000-0000-0000-000000000008', 
  '80000000-0000-0000-0000-000000000008', 
  'resumes/81000000-0000-0000-0000-000000000008.pdf', 
  'CV_Mehdi_Alaoui_DevOps.pdf', 
  'hash_mehdi_stalled_8', 
  'STALLED', 
  null,
  NOW() - INTERVAL '15 minutes',
  null
);

INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, passed_min_score, category_scores, extracted_matching, applied_at, scored_at)
VALUES (
  '82000000-0000-0000-0000-000000000008', 
  '80000000-0000-0000-0000-000000000008', 
  '44444444-4444-4444-4444-444444444444', 
  '81000000-0000-0000-0000-000000000008', 
  'NEW', 
  null, 
  null, 
  null, 
  null,
  NOW() - INTERVAL '15 minutes',
  null
);
