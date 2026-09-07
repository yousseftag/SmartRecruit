-- V7__demo_seed.sql
-- SmartRecruit Realistic Recruitment Agency Demo Seed Data
-- Scenario: Tuesday morning at Norsys Afrique (Casablanca / Agadir / Marrakech)
-- Recruiter Manager: Zineb HADAFI

-- ====================================================================
-- 1. HR TEAM USERS (Aligned with Keycloak realm-export.json)
-- ====================================================================
INSERT INTO app_user (id, keycloak_sub, username, first_name, last_name, email, role)
VALUES 
  ('11111111-1111-1111-1111-111111111110', '1540bed8-f2d3-49d7-b1ce-96fad68983e6', 'admin', 'Sophie', 'El Amrani', 'admin@smartrecruit.com', 'HR_ADMIN'),
  ('11111111-1111-1111-1111-111111111111', '87aa1b2a-d4eb-4511-9dde-eff680db0d17', 'recruteur', 'Zineb', 'HADAFI', 'zineb.hadafi@smartrecruit.com', 'RECRUITER'),
  ('11111111-1111-1111-1111-111111111112', 'd61d5724-ef0b-492e-adbc-e242417a7bb0', 'viewer', 'Leila', 'Mansouri', 'viewer@smartrecruit.com', 'VIEWER')
ON CONFLICT (email) DO UPDATE SET
  keycloak_sub = EXCLUDED.keycloak_sub,
  username = EXCLUDED.username,
  first_name = EXCLUDED.first_name,
  last_name = EXCLUDED.last_name,
  role = EXCLUDED.role;

-- ====================================================================
-- 2. JOB OFFERS (3 Active Campaigns + 1 In-Flight Draft)
-- ====================================================================

-- Offer A: Développeur Full-Stack Java / Angular (Hero Offer with 12 Candidates)
INSERT INTO offer (
  id, created_by, updated_by, title, description_markdown, status, offer_ai_status,
  category_weights, category_criteria, min_score, duration_months, contract_type,
  extracted_requirements, created_at, updated_at
) VALUES (
  '22222222-2222-2222-2222-222222222222',
  '11111111-1111-1111-1111-111111111111', -- Zineb HADAFI created
  '11111111-1111-1111-1111-111111111111', -- Zineb HADAFI reviewed
  'Développeur Full-Stack Senior Java / Angular',
  '### Contexte & Mission
Norsys Afrique renforce son centre d''excellence à **Agadir** et recrute un(e) **Développeur Full-Stack Senior Java / Angular**. Vous intégrerez une équipe agile en charge de la conception et du développement de plateformes métiers critiques pour de grands comptes internationaux.

### Responsabilités principales
- Concevoir des architectures microservices modulaires et résilientes avec **Spring Boot 3** et Java 21.
- Développer des interfaces utilisateur réactives, modernes et performantes sous **Angular 18+** (Signals, Tailwind CSS).
- Garantir la modélisation, l''optimisation des requêtes et l''intégrité des bases de données **PostgreSQL**.
- Participer activement à la conteneurisation **Docker** et aux pipelines CI/CD.
- Accompagner et mentorer les développeurs juniors au sein de la squad.

### Profil recherché
- Formation supérieure : **Bac+5** en informatique (Master ou Diplôme d''Ingénieur d''État).
- Minimum **3 ans d''expérience** avérée sur la stack Java / Spring Boot et Angular en production.
- Excellente maîtrise du **Français** et bon niveau en **Anglais technique**.',
  'ACTIVE',
  'SUCCESS',
  '{"skills": 30, "experience": 25, "coursework": 15, "languages": 15, "localization": 15}',
  '{"skills": ["Java", "Spring Boot", "Angular", "PostgreSQL", "Docker"], "skill_weights": {"Java": 25, "Spring Boot": 25, "Angular": 25, "PostgreSQL": 15, "Docker": 10}, "experience": 36, "coursework": ["Bac+5 (Master, Diplôme d''Ingénieur)", "Diplôme d''Ingénieur d''État"], "languages": ["Français", "Anglais"], "localization": "Agadir"}',
  80,
  null,
  'CDI',
  '{"missing_from_criteria": ["Docker", "Git", "CI/CD"], "insights": "Le profil requiert une solide maîtrise des architectures microservices, des bonnes pratiques de Clean Code et des méthodologies agiles."}',
  NOW() - INTERVAL '7 days',
  NOW() - INTERVAL '1 day'
);

-- Offer B: Stage PFE Data Scientist NLP/LLM (Marrakech)
INSERT INTO offer (
  id, created_by, updated_by, title, description_markdown, status, offer_ai_status,
  category_weights, category_criteria, min_score, duration_months, contract_type,
  extracted_requirements, created_at, updated_at
) VALUES (
  '33333333-3333-3333-3333-333333333333',
  '11111111-1111-1111-1111-111111111110', -- Sophie created
  '11111111-1111-1111-1111-111111111111', -- Zineb updated
  'Stage PFE : Data Scientist & Ingénieur IA (NLP / LLM)',
  '### Contexte du Stage
Au sein du laboratoire d''innovation technologique de Norsys à **Marrakech**, vous participerez au développement de la nouvelle génération d''outils d''analyse sémantique automatique et de matching de profils par Intelligence Artificielle.

### Missions confiées
- Concevoir et entraîner des modèles de traitement automatique du langage naturel (NLP) et de NER.
- Développer des microservices d''inférence asynchrones avec **FastAPI** et **PyTorch**.
- Évaluer et affiner des modèles de fondation open-source via HuggingFace et Transformers.

### Profil requis
- Étudiant en dernière année d''école d''ingénieurs ou Master 2 en Data Science / IA (**Bac+5**).
- Solides compétences en programmation **Python** et Deep Learning.
- Excellente maîtrise du **Français** et de l''**Anglais**.',
  'ACTIVE',
  'SUCCESS',
  '{"skills": 35, "experience": 10, "coursework": 25, "languages": 15, "localization": 15}',
  '{"skills": ["Python", "PyTorch", "FastAPI", "Transformers", "HuggingFace"], "skill_weights": {"Python": 30, "PyTorch": 25, "FastAPI": 20, "Transformers": 15, "HuggingFace": 10}, "experience": 0, "coursework": ["Bac+5 (Master, Diplôme d''Ingénieur)", "Master en Data Science / Intelligence Artificielle"], "languages": ["Français", "Anglais"], "localization": "Marrakech"}',
  70,
  6,
  'Stage',
  '{"missing_from_criteria": ["Scikit-Learn", "Git"], "insights": "Stage pré-embauche orienté R&D NLP, intégration de LLMs et APIs d''inférence."}',
  NOW() - INTERVAL '5 days',
  NOW() - INTERVAL '3 days'
);

-- Offer C: Ingénieur Cloud & DevOps (Full Remote)
INSERT INTO offer (
  id, created_by, updated_by, title, description_markdown, status, offer_ai_status,
  category_weights, category_criteria, min_score, duration_months, contract_type,
  extracted_requirements, created_at, updated_at
) VALUES (
  '44444444-4444-4444-4444-444444444444',
  '11111111-1111-1111-1111-111111111111', -- Zineb created
  '11111111-1111-1111-1111-111111111111',
  'Ingénieur Cloud & DevOps (Full Remote)',
  '### Mission
Norsys recrute un(e) **Ingénieur(e) Cloud & DevOps** en **Télétravail complet (Full Remote 100%)** pour piloter l''industrialisation, la résilience et la sécurité de ses infrastructures cloud.

### Vos responsabilités
- Automatiser le provisionnement d''environnements cloud sur **AWS** via **Terraform**.
- Déployer, superviser et maintenir des clusters **Kubernetes** en haute disponibilité.
- Élaborer et optimiser des pipelines CI/CD industriels avec conteneurs **Docker**.
- Mettre en place l''observabilité (Prometheus, Grafana).

### Compétences & Profil
- Diplôme d''ingénieur ou Master universitaire spécialisé (**Bac+5**).
- Au moins **2 ans d''expérience** réussie en automatisation DevOps / Cloud.',
  'ACTIVE',
  'SUCCESS',
  '{"skills": 35, "experience": 25, "coursework": 15, "languages": 10, "localization": 15}',
  '{"skills": ["Docker", "Kubernetes", "AWS", "Terraform", "CI/CD"], "skill_weights": {"Docker": 20, "Kubernetes": 25, "AWS": 25, "Terraform": 15, "CI/CD": 15}, "experience": 24, "coursework": ["Bac+5 (Master, Diplôme d''Ingénieur)", "Diplôme d''Ingénieur d''État"], "languages": ["Français", "Anglais"], "localization": "Full Remote / Télétravail 100%"}',
  75,
  null,
  'CDI',
  '{"missing_from_criteria": ["Ansible", "Linux", "Prometheus"], "insights": "Exigence forte sur les compétences Infrastructure as Code (Terraform) et Kubernetes."}',
  NOW() - INTERVAL '6 days',
  NOW() - INTERVAL '2 days'
);

-- Offer D: Product Owner (Casablanca - In-Flight Draft created this morning)
INSERT INTO offer (
  id, created_by, updated_by, title, description_markdown, status, offer_ai_status,
  category_weights, category_criteria, min_score, duration_months, contract_type,
  extracted_requirements, created_at, updated_at
) VALUES (
  '55555555-5555-5555-5555-555555555555',
  '11111111-1111-1111-1111-111111111110', -- Sophie created this morning
  '11111111-1111-1111-1111-111111111110',
  'Product Owner — Transformation Digitale (Casablanca)',
  '### Contexte
Dans le cadre de l''accélération de nos projets de digitalisation bancaire à **Casablanca**, nous recrutons un(e) **Product Owner** expérimenté(e).

### Missions
- Définir et prioriser le product backlog en étroite collaboration avec les parties prenantes métier.
- Rédiger les User Stories et animer les cérémonies agiles Scrum.
- Suivre les KPIs de livraison et garantir la valeur métier délivrée.',
  'DRAFT',
  'PENDING',
  '{"skills": 30, "experience": 30, "coursework": 15, "languages": 15, "localization": 10}',
  '{"skills": ["Scrum", "Agile", "Jira", "Product Backlog", "User Stories"], "experience": 36, "coursework": ["Bac+5"], "languages": ["Français", "Anglais"], "localization": "Casablanca"}',
  75,
  null,
  'CDI',
  null,
  NOW() - INTERVAL '3 hours',
  NOW() - INTERVAL '3 hours'
);


-- ====================================================================
-- 3. CANDIDATES, CV FILES & APPLICATIONS
-- ====================================================================

-- --------------------------------------------------------------------
-- OFFER A: Java / Angular (Hero Offer with full variety)
-- --------------------------------------------------------------------

-- 1. Amine Tazi (Top Match 88.50% >= 80% -> SHORTLISTED, passed_min_score: true)
INSERT INTO candidate (id, first_name, last_name, email, phone, created_at)
VALUES ('10000000-0000-0000-0000-000000000001', 'Amine', 'Tazi', 'amine.tazi@example.com', '+212661122334', NOW() - INTERVAL '1 day');

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data, uploaded_at, processed_at)
VALUES (
  '11000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001',
  'resumes/11000000-0000-0000-0000-000000000001.pdf', 'CV_Amine_Tazi_Senior_Java.pdf',
  'sha256_amine_tazi_1', 'SUCCESS',
  '{"candidate_info": {"first_name": "Amine", "last_name": "Tazi", "email": "amine.tazi@example.com", "phone": "+212661122334", "current_job_title": "Lead Développeur Java / Angular"}, "description_markdown": "Ingénieur Full-Stack expérimenté avec plus de 4 ans d''expertise Java / Spring Boot et Angular.", "skills": ["Java", "Spring Boot", "Angular", "PostgreSQL", "Docker", "Git"], "experience": 48, "coursework": ["Bac+5 (Master, Diplôme d''Ingénieur)"], "languages": ["Français", "Anglais"], "localization": "Agadir"}',
  NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day' + INTERVAL '8 seconds'
);

INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, passed_min_score, category_scores, extracted_matching, applied_at, scored_at)
VALUES (
  '12000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001',
  '22222222-2222-2222-2222-222222222222', '11000000-0000-0000-0000-000000000001',
  'SHORTLISTED', 88.50, true,
  '{"skills": 26.50, "experience": 24.00, "coursework": 14.00, "languages": 11.00, "localization": 13.00}',
  '{"matched_criteria": {"skills": ["Java", "Spring Boot", "Angular", "PostgreSQL", "Docker"], "experience": true, "coursework": ["Bac+5 (Master, Diplôme d''Ingénieur)"], "languages": ["Français", "Anglais"], "localization": "Agadir"}, "strengths": ["Excellente maîtrise technique de Spring Boot et Angular", "4 ans d''expérience en architecture microservices", "Diplôme d''Ingénieur d''État", "Basé à Agadir"], "weaknesses": []}',
  NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day' + INTERVAL '8 seconds'
);

-- 2. Sara Benali (Below threshold 46.00% < 80% -> REJECTED, passed_min_score: false)
INSERT INTO candidate (id, first_name, last_name, email, phone, created_at)
VALUES ('20000000-0000-0000-0000-000000000002', 'Sara', 'Benali', 'sara.benali@example.com', '+212662233445', NOW() - INTERVAL '6 days');

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data, uploaded_at, processed_at)
VALUES (
  '21000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000002',
  'resumes/21000000-0000-0000-0000-000000000002.pdf', 'CV_Sara_Benali_Web.pdf',
  'sha256_sara_benali_2', 'SUCCESS',
  '{"candidate_info": {"first_name": "Sara", "last_name": "Benali", "email": "sara.benali@example.com", "phone": "+212662233445", "current_job_title": "Développeuse Web Junior"}, "description_markdown": "Développeuse junior passionnée par le web frontend léger (HTML, CSS, JS, PHP).", "skills": ["HTML", "CSS", "JavaScript", "PHP"], "experience": 6, "coursework": ["Bac+2 (BTS, DUT)"], "languages": ["Français"], "localization": "Marrakech"}',
  NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days' + INTERVAL '6 seconds'
);

INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, passed_min_score, category_scores, extracted_matching, applied_at, scored_at)
VALUES (
  '22000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000002',
  '22222222-2222-2222-2222-222222222222', '21000000-0000-0000-0000-000000000002',
  'REJECTED', 46.00, false,
  '{"skills": 10.00, "experience": 8.00, "coursework": 8.00, "languages": 10.00, "localization": 10.00}',
  '{"matched_criteria": {"skills": ["JavaScript"], "experience": false, "coursework": [], "languages": ["Français"], "localization": null}, "strengths": ["Bases en développement web front-end"], "weaknesses": ["Compétences clés manquantes : Java, Spring Boot, Angular, PostgreSQL", "Expérience inférieure au seuil (6 mois vs 36 mois requis)", "Bac+2 vs Bac+5 demandé"]}',
  NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days' + INTERVAL '6 seconds'
);

-- 3. Nour El Houda Alami (Strong Profile 83.00% >= 80% -> INTERVIEWING, passed_min_score: true)
INSERT INTO candidate (id, first_name, last_name, email, phone, created_at)
VALUES ('10000000-0000-0000-0000-000000000003', 'Nour El Houda', 'Alami', 'nour.alami@example.com', '+212663987654', NOW() - INTERVAL '1 day');

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data, uploaded_at, processed_at)
VALUES (
  '11000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000003',
  'resumes/11000000-0000-0000-0000-000000000003.pdf', 'CV_Nour_Alami_FullStack.pdf',
  'sha256_nour_alami_3', 'SUCCESS',
  '{"candidate_info": {"first_name": "Nour El Houda", "last_name": "Alami", "email": "nour.alami@example.com", "phone": "+212663987654", "current_job_title": "Ingénieure d''Études Java & Angular"}, "description_markdown": "Ingénieure logicielle avec 3 ans et demi d''expérience sur Spring Boot et Angular.", "skills": ["Java", "Spring Boot", "Angular", "TypeScript", "PostgreSQL", "Docker"], "experience": 42, "coursework": ["Diplôme d''Ingénieur d''État (ENSIAS)"], "languages": ["Français", "Anglais"], "localization": "Agadir"}',
  NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day' + INTERVAL '7 seconds'
);

INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, passed_min_score, category_scores, extracted_matching, applied_at, scored_at)
VALUES (
  '12000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000003',
  '22222222-2222-2222-2222-222222222222', '11000000-0000-0000-0000-000000000003',
  'INTERVIEWING', 83.00, true,
  '{"skills": 25.00, "experience": 22.00, "coursework": 14.00, "languages": 10.00, "localization": 12.00}',
  '{"matched_criteria": {"skills": ["Java", "Spring Boot", "Angular", "PostgreSQL", "Docker"], "experience": true, "coursework": ["Diplôme d''Ingénieur d''État"], "languages": ["Français", "Anglais"], "localization": "Agadir"}, "strengths": ["Ingénieure ENSIAS rigoureuse", "Excellente couverture des technologies cibles", "Localisation Agadir confirmée"], "weaknesses": ["Expérience un peu plus courte sur Docker"]}',
  NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day' + INTERVAL '7 seconds'
);

-- 4. Khalid Amrani (High Score 81.00% >= 80% -> NEW, awaiting recruiter review)
INSERT INTO candidate (id, first_name, last_name, email, phone, created_at)
VALUES ('10000000-0000-0000-0000-000000000004', 'Khalid', 'Amrani', 'khalid.amrani@example.com', '+212661554433', NOW() - INTERVAL '2 hours');

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data, uploaded_at, processed_at)
VALUES (
  '11000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000004',
  'resumes/11000000-0000-0000-0000-000000000004.pdf', 'CV_Khalid_Amrani.pdf',
  'sha256_khalid_amrani_4', 'SUCCESS',
  '{"candidate_info": {"first_name": "Khalid", "last_name": "Amrani", "email": "khalid.amrani@example.com", "phone": "+212661554433", "current_job_title": "Développeur Full-Stack"}, "description_markdown": "Développeur Full-Stack avec 3 ans d''expérience Java, Spring Boot, Angular et MySQL.", "skills": ["Java", "Spring Boot", "Angular", "PostgreSQL", "Git"], "experience": 38, "coursework": ["Master Informatique"], "languages": ["Français", "Anglais"], "localization": "Agadir"}',
  NOW() - INTERVAL '2 hours', NOW() - INTERVAL '2 hours' + INTERVAL '8 seconds'
);

INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, passed_min_score, category_scores, extracted_matching, applied_at, scored_at)
VALUES (
  '12000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000004',
  '22222222-2222-2222-2222-222222222222', '11000000-0000-0000-0000-000000000004',
  'NEW', 81.00, true,
  '{"skills": 24.00, "experience": 22.00, "coursework": 13.00, "languages": 10.00, "localization": 12.00}',
  '{"matched_criteria": {"skills": ["Java", "Spring Boot", "Angular", "PostgreSQL"], "experience": true, "coursework": ["Master Informatique"], "languages": ["Français", "Anglais"], "localization": "Agadir"}, "strengths": ["Profil qualifié au-dessus du seuil de 80%", "Disponible immédiatement sur Agadir"], "weaknesses": ["Compétence Docker non explicitée sur le CV"]}',
  NOW() - INTERVAL '2 hours', NOW() - INTERVAL '2 hours' + INTERVAL '8 seconds'
);

-- 5. Hamza Bennani (Top Score 85.00% >= 80% -> NEW, awaiting recruiter review)
INSERT INTO candidate (id, first_name, last_name, email, phone, created_at)
VALUES ('10000000-0000-0000-0000-000000000021', 'Hamza', 'Bennani', 'hamza.bennani@example.com', '+212661889900', NOW() - INTERVAL '3 hours');

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data, uploaded_at, processed_at)
VALUES (
  '11000000-0000-0000-0000-000000000021', '10000000-0000-0000-0000-000000000021',
  'resumes/11000000-0000-0000-0000-000000000021.pdf', 'CV_Hamza_Bennani.pdf',
  'sha256_hamza_bennani_21', 'SUCCESS',
  '{"candidate_info": {"first_name": "Hamza", "last_name": "Bennani", "email": "hamza.bennani@example.com", "phone": "+212661889900", "current_job_title": "Ingénieur Logiciel Java / Angular"}, "description_markdown": "Développeur expérimenté Spring Boot et Angular avec expertise Docker et tests unitaires.", "skills": ["Java", "Spring Boot", "Angular", "Docker", "PostgreSQL"], "experience": 40, "coursework": ["Diplôme d''Ingénieur"], "languages": ["Français", "Anglais"], "localization": "Agadir"}',
  NOW() - INTERVAL '3 hours', NOW() - INTERVAL '3 hours' + INTERVAL '6 seconds'
);

INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, passed_min_score, category_scores, extracted_matching, applied_at, scored_at)
VALUES (
  '12000000-0000-0000-0000-000000000021', '10000000-0000-0000-0000-000000000021',
  '22222222-2222-2222-2222-222222222222', '11000000-0000-0000-0000-000000000021',
  'NEW', 85.00, true,
  '{"skills": 26.00, "experience": 23.00, "coursework": 14.00, "languages": 10.00, "localization": 12.00}',
  '{"matched_criteria": {"skills": ["Java", "Spring Boot", "Angular", "Docker", "PostgreSQL"], "experience": true, "coursework": ["Diplôme d''Ingénieur"], "languages": ["Français", "Anglais"], "localization": "Agadir"}, "strengths": ["Profil senior très complet", "Bonne maîtrise des conteneurs Docker", "Réside à Agadir"], "weaknesses": []}',
  NOW() - INTERVAL '3 hours', NOW() - INTERVAL '3 hours' + INTERVAL '6 seconds'
);

-- 6. Fatine Alaoui (Score 82.50% >= 80% -> NEW, awaiting recruiter review)
INSERT INTO candidate (id, first_name, last_name, email, phone, created_at)
VALUES ('10000000-0000-0000-0000-000000000022', 'Fatine', 'Alaoui', 'fatine.alaoui@example.com', '+212662776655', NOW() - INTERVAL '4 hours');

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data, uploaded_at, processed_at)
VALUES (
  '11000000-0000-0000-0000-000000000022', '10000000-0000-0000-0000-000000000022',
  'resumes/11000000-0000-0000-0000-000000000022.pdf', 'CV_Fatine_Alaoui.pdf',
  'sha256_fatine_alaoui_22', 'SUCCESS',
  '{"candidate_info": {"first_name": "Fatine", "last_name": "Alaoui", "email": "fatine.alaoui@example.com", "phone": "+212662776655", "current_job_title": "Développeuse Full-Stack Web"}, "description_markdown": "3 ans d''expérience en développement d''applications web d''entreprise avec Java et Angular.", "skills": ["Java", "Spring Boot", "Angular", "PostgreSQL"], "experience": 36, "coursework": ["Master Informatique"], "languages": ["Français", "Anglais"], "localization": "Agadir"}',
  NOW() - INTERVAL '4 hours', NOW() - INTERVAL '4 hours' + INTERVAL '7 seconds'
);

INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, passed_min_score, category_scores, extracted_matching, applied_at, scored_at)
VALUES (
  '12000000-0000-0000-0000-000000000022', '10000000-0000-0000-0000-000000000022',
  '22222222-2222-2222-2222-222222222222', '11000000-0000-0000-0000-000000000022',
  'NEW', 82.50, true,
  '{"skills": 25.00, "experience": 22.00, "coursework": 13.50, "languages": 10.00, "localization": 12.00}',
  '{"matched_criteria": {"skills": ["Java", "Spring Boot", "Angular", "PostgreSQL"], "experience": true, "coursework": ["Master Informatique"], "languages": ["Français", "Anglais"], "localization": "Agadir"}, "strengths": ["Alignement technique solide", "Excellente communication"], "weaknesses": []}',
  NOW() - INTERVAL '4 hours', NOW() - INTERVAL '4 hours' + INTERVAL '7 seconds'
);

-- 7. Tariq Berrada (Senior 88.00% >= 80% -> NEW, awaiting recruiter review)
INSERT INTO candidate (id, first_name, last_name, email, phone, created_at)
VALUES ('10000000-0000-0000-0000-000000000023', 'Tariq', 'Berrada', 'tariq.berrada@example.com', '+212663665544', NOW() - INTERVAL '5 hours');

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data, uploaded_at, processed_at)
VALUES (
  '11000000-0000-0000-0000-000000000023', '10000000-0000-0000-0000-000000000023',
  'resumes/11000000-0000-0000-0000-000000000023.pdf', 'CV_Tariq_Berrada.pdf',
  'sha256_tariq_berrada_23', 'SUCCESS',
  '{"candidate_info": {"first_name": "Tariq", "last_name": "Berrada", "email": "tariq.berrada@example.com", "phone": "+212663665544", "current_job_title": "Ingénieur d''Études et Développement"}, "description_markdown": "Ingénieur avec 4 ans d''expérience Java, Spring Boot, microservices et Angular.", "skills": ["Java", "Spring Boot", "Angular", "Docker", "PostgreSQL"], "experience": 48, "coursework": ["Ingénieur d''État (EMI)"], "languages": ["Français", "Anglais"], "localization": "Agadir"}',
  NOW() - INTERVAL '5 hours', NOW() - INTERVAL '5 hours' + INTERVAL '7 seconds'
);

INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, passed_min_score, category_scores, extracted_matching, applied_at, scored_at)
VALUES (
  '12000000-0000-0000-0000-000000000023', '10000000-0000-0000-0000-000000000023',
  '22222222-2222-2222-2222-222222222222', '11000000-0000-0000-0000-000000000023',
  'NEW', 88.00, true,
  '{"skills": 27.00, "experience": 24.00, "coursework": 14.00, "languages": 10.00, "localization": 13.00}',
  '{"matched_criteria": {"skills": ["Java", "Spring Boot", "Angular", "Docker", "PostgreSQL"], "experience": true, "coursework": ["Ingénieur d''État (EMI)"], "languages": ["Français", "Anglais"], "localization": "Agadir"}, "strengths": ["Excellente formation EMI", "Expertise confirmée microservices et cloud"], "weaknesses": []}',
  NOW() - INTERVAL '5 hours', NOW() - INTERVAL '5 hours' + INTERVAL '7 seconds'
);

-- 8. Othmane Saidi (Below threshold 68.00% < 80% -> NEW, extracted but failed threshold)
INSERT INTO candidate (id, first_name, last_name, email, phone, created_at)
VALUES ('10000000-0000-0000-0000-000000000024', 'Othmane', 'Saidi', 'othmane.saidi@example.com', '+212664554433', NOW() - INTERVAL '6 hours');

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data, uploaded_at, processed_at)
VALUES (
  '11000000-0000-0000-0000-000000000024', '10000000-0000-0000-0000-000000000024',
  'resumes/11000000-0000-0000-0000-000000000024.pdf', 'CV_Othmane_Saidi.pdf',
  'sha256_othmane_saidi_24', 'SUCCESS',
  '{"candidate_info": {"first_name": "Othmane", "last_name": "Saidi", "email": "othmane.saidi@example.com", "phone": "+212664554433", "current_job_title": "Développeur Java Junior"}, "description_markdown": "Développeur Java avec 1 an et demi d''expérience sur Spring Boot et React.", "skills": ["Java", "Spring Boot", "React", "MySQL"], "experience": 18, "coursework": ["Licence Professionnelle Informatique"], "languages": ["Français"], "localization": "Agadir"}',
  NOW() - INTERVAL '6 hours', NOW() - INTERVAL '6 hours' + INTERVAL '6 seconds'
);

INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, passed_min_score, category_scores, extracted_matching, applied_at, scored_at)
VALUES (
  '12000000-0000-0000-0000-000000000024', '10000000-0000-0000-0000-000000000024',
  '22222222-2222-2222-2222-222222222222', '11000000-0000-0000-0000-000000000024',
  'NEW', 68.00, false,
  '{"skills": 18.00, "experience": 15.00, "coursework": 11.00, "languages": 10.00, "localization": 14.00}',
  '{"matched_criteria": {"skills": ["Java", "Spring Boot"], "experience": false, "coursework": [], "languages": ["Français"], "localization": "Agadir"}, "strengths": ["Bases correctes en Java / Spring Boot", "Réside à Agadir"], "weaknesses": ["Angular manquant (maîtrise React)", "Expérience inférieure au seuil de 3 ans", "Bac+3 vs Bac+5 demandé"]}',
  NOW() - INTERVAL '6 hours', NOW() - INTERVAL '6 hours' + INTERVAL '6 seconds'
);

-- 9. Ayoub Chraibi (In-Flight Extraction -> PENDING, uploaded 30 seconds ago, live spinner)
INSERT INTO candidate (id, first_name, last_name, email, phone, created_at)
VALUES ('50000000-0000-0000-0000-000000000005', 'Ayoub', 'Chraibi', 'ayoub.chraibi@example.com', '+212665566778', NOW() - INTERVAL '30 seconds');

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data, uploaded_at, processed_at)
VALUES (
  '51000000-0000-0000-0000-000000000005', '50000000-0000-0000-0000-000000000005',
  'resumes/51000000-0000-0000-0000-000000000005.pdf', 'CV_Ayoub_Chraibi_FullStack.pdf',
  'sha256_ayoub_pending_5', 'PENDING', null,
  NOW() - INTERVAL '30 seconds', null
);

INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, passed_min_score, category_scores, extracted_matching, applied_at, scored_at)
VALUES (
  '52000000-0000-0000-0000-000000000005', '50000000-0000-0000-0000-000000000005',
  '22222222-2222-2222-2222-222222222222', '51000000-0000-0000-0000-000000000005',
  'NEW', null, null, null, null,
  NOW() - INTERVAL '30 seconds', null
);

-- 10. Salma Bennani (AI Extraction FAILED, Has Contact Info -> Red Badge & Retry Button)
INSERT INTO candidate (id, first_name, last_name, email, phone, created_at)
VALUES ('60000000-0000-0000-0000-000000000006', 'Salma', 'Bennani', 'salma.bennani@example.com', '+212666677889', NOW() - INTERVAL '1 day');

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data, uploaded_at, processed_at)
VALUES (
  '61000000-0000-0000-0000-000000000006', '60000000-0000-0000-0000-000000000006',
  'resumes/61000000-0000-0000-0000-000000000006.pdf', 'CV_Salma_Bennani_Scan.pdf',
  'sha256_salma_failed_6', 'FAILED', null,
  NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day' + INTERVAL '4 seconds'
);

INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, passed_min_score, category_scores, extracted_matching, applied_at, scored_at)
VALUES (
  '62000000-0000-0000-0000-000000000006', '60000000-0000-0000-0000-000000000006',
  '22222222-2222-2222-2222-222222222222', '61000000-0000-0000-0000-000000000006',
  'NEW', null, null, null, null,
  NOW() - INTERVAL '1 day', null
);

-- 11. Ghost Candidate (Corrupted Bulk PDF -> FAILED, null name/email -> "Candidat Inconnu" Ghost UI)
INSERT INTO candidate (id, first_name, last_name, email, phone, created_at)
VALUES ('70000000-0000-0000-0000-000000000007', null, null, null, null, NOW() - INTERVAL '2 days');

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data, uploaded_at, processed_at)
VALUES (
  '71000000-0000-0000-0000-000000000007', '70000000-0000-0000-0000-000000000007',
  'resumes/71000000-0000-0000-0000-000000000007.pdf', 'Scan_Document_Illisible.pdf',
  'sha256_ghost_corrupted_7', 'FAILED', null,
  NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days' + INTERVAL '3 seconds'
);

INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, passed_min_score, category_scores, extracted_matching, applied_at, scored_at)
VALUES (
  '72000000-0000-0000-0000-000000000007', '70000000-0000-0000-0000-000000000007',
  '22222222-2222-2222-2222-222222222222', '71000000-0000-0000-0000-000000000007',
  'NEW', null, null, null, null,
  NOW() - INTERVAL '2 days', null
);

-- 12. Nadia Taha (Score 77.00% < 80% -> ARCHIVED)
INSERT INTO candidate (id, first_name, last_name, email, phone, created_at)
VALUES ('10000000-0000-0000-0000-000000000009', 'Nadia', 'Taha', 'nadia.taha@example.com', '+212668877665', NOW() - INTERVAL '6 days');

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data, uploaded_at, processed_at)
VALUES (
  '11000000-0000-0000-0000-000000000009', '10000000-0000-0000-0000-000000000009',
  'resumes/11000000-0000-0000-0000-000000000009.pdf', 'CV_Nadia_Taha.pdf',
  'sha256_nadia_taha_9', 'SUCCESS',
  '{"candidate_info": {"first_name": "Nadia", "last_name": "Taha", "email": "nadia.taha@example.com", "phone": "+212668877665", "current_job_title": "Développeuse Front-End Angular"}, "description_markdown": "Développeuse Angular expérimentée avec 3 ans de pratique frontend.", "skills": ["Angular", "TypeScript", "HTML", "CSS", "Docker"], "experience": 36, "coursework": ["Bac+5"], "languages": ["Français", "Anglais"], "localization": "Casablanca"}',
  NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days' + INTERVAL '8 seconds'
);

INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, passed_min_score, category_scores, extracted_matching, applied_at, scored_at)
VALUES (
  '12000000-0000-0000-0000-000000000009', '10000000-0000-0000-0000-000000000009',
  '22222222-2222-2222-2222-222222222222', '11000000-0000-0000-0000-000000000009',
  'ARCHIVED', 77.00, false,
  '{"skills": 22.00, "experience": 20.00, "coursework": 14.00, "languages": 11.00, "localization": 10.00}',
  '{"matched_criteria": {"skills": ["Angular", "Docker"], "experience": true, "coursework": ["Bac+5"], "languages": ["Français", "Anglais"], "localization": null}, "strengths": ["Solide maîtrise d''Angular"], "weaknesses": ["Expérience Java / Spring Boot limitée", "Basée à Casablanca"]}',
  NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days' + INTERVAL '8 seconds'
);


-- --------------------------------------------------------------------
-- OFFER B: Stage PFE Data Science NLP/LLM (3 Candidates)
-- --------------------------------------------------------------------

-- 13. Youssef Mansouri (PFE Candidate 74.00% >= 70% -> INTERVIEWING, passed_min_score: true)
INSERT INTO candidate (id, first_name, last_name, email, phone, created_at)
VALUES ('30000000-0000-0000-0000-000000000003', 'Youssef', 'Mansouri', 'youssef.mansouri@example.com', '+212663344556', NOW() - INTERVAL '1 day');

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data, uploaded_at, processed_at)
VALUES (
  '31000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000003',
  'resumes/31000000-0000-0000-0000-000000000003.pdf', 'CV_Youssef_Mansouri_NLP.pdf',
  'sha256_youssef_nlp_3', 'SUCCESS',
  '{"candidate_info": {"first_name": "Youssef", "last_name": "Mansouri", "email": "youssef.mansouri@example.com", "phone": "+212663344556", "current_job_title": "Élève Ingénieur IA / NLP"}, "description_markdown": "Élève ingénieur en dernière année, passionné par le traitement automatique du langage naturel (NLP) et les LLMs.", "skills": ["Python", "PyTorch", "Transformers", "FastAPI", "HuggingFace"], "experience": 0, "coursework": ["Bac+5 (Master, Diplôme d''Ingénieur)"], "languages": ["Français", "Anglais"], "localization": "Marrakech"}',
  NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day' + INTERVAL '9 seconds'
);

INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, passed_min_score, category_scores, extracted_matching, applied_at, scored_at)
VALUES (
  '32000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000003',
  '33333333-3333-3333-3333-333333333333', '31000000-0000-0000-0000-000000000003',
  'INTERVIEWING', 74.00, true,
  '{"skills": 28.00, "experience": 8.00, "coursework": 18.00, "languages": 10.00, "localization": 10.00}',
  '{"matched_criteria": {"skills": ["Python", "PyTorch", "Transformers", "FastAPI", "HuggingFace"], "experience": true, "coursework": ["Bac+5 (Master, Diplôme d''Ingénieur)"], "languages": ["Français", "Anglais"], "localization": "Marrakech"}, "strengths": ["Excellente maîtrise Python / PyTorch / Transformers", "Projets académiques NLP concrets", "Réside à Marrakech"], "weaknesses": ["Stage de fin d''études (pas d''expérience pro préalable)"]}',
  NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day' + INTERVAL '9 seconds'
);

-- 14. Imane Zaid (Candidate 61.00% < 70% -> REJECTED, passed_min_score: false)
INSERT INTO candidate (id, first_name, last_name, email, phone, created_at)
VALUES ('30000000-0000-0000-0000-000000000011', 'Imane', 'Zaid', 'imane.zaid@example.com', '+212664112233', NOW() - INTERVAL '5 days');

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data, uploaded_at, processed_at)
VALUES (
  '31000000-0000-0000-0000-000000000011', '30000000-0000-0000-0000-000000000011',
  'resumes/31000000-0000-0000-0000-000000000011.pdf', 'CV_Imane_Zaid.pdf',
  'sha256_imane_zaid_11', 'SUCCESS',
  '{"candidate_info": {"first_name": "Imane", "last_name": "Zaid", "email": "imane.zaid@example.com", "phone": "+212664112233", "current_job_title": "Étudiante Master Data"}, "description_markdown": "Étudiante en Master 1 informatique, orientée analyse de données statistiques et BI.", "skills": ["Python", "SQL", "PowerBI"], "experience": 0, "coursework": ["Bac+4"], "languages": ["Français"], "localization": "Rabat"}',
  NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days' + INTERVAL '7 seconds'
);

INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, passed_min_score, category_scores, extracted_matching, applied_at, scored_at)
VALUES (
  '32000000-0000-0000-0000-000000000011', '30000000-0000-0000-0000-000000000011',
  '33333333-3333-3333-3333-333333333333', '31000000-0000-0000-0000-000000000011',
  'REJECTED', 61.00, false,
  '{"skills": 18.00, "experience": 8.00, "coursework": 15.00, "languages": 10.00, "localization": 10.00}',
  '{"matched_criteria": {"skills": ["Python"], "experience": true, "coursework": [], "languages": ["Français"], "localization": null}, "strengths": ["Bonnes bases en Python"], "weaknesses": ["Pas d''expérience en NLP avancé / Deep Learning (PyTorch, Transformers)", "Niveau d''études Bac+4 vs Bac+5 demandé"]}',
  NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days' + INTERVAL '7 seconds'
);

-- 15. Soukaina Tazi (PFE Candidate 76.00% >= 70% -> NEW, passed_min_score: true)
INSERT INTO candidate (id, first_name, last_name, email, phone, created_at)
VALUES ('30000000-0000-0000-0000-000000000025', 'Soukaina', 'Tazi', 'soukaina.tazi@example.com', '+212665998877', NOW() - INTERVAL '1 hour');

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data, uploaded_at, processed_at)
VALUES (
  '31000000-0000-0000-0000-000000000025', '30000000-0000-0000-0000-000000000025',
  'resumes/31000000-0000-0000-0000-000000000025.pdf', 'CV_Soukaina_Tazi_PFE.pdf',
  'sha256_soukaina_tazi_25', 'SUCCESS',
  '{"candidate_info": {"first_name": "Soukaina", "last_name": "Tazi", "email": "soukaina.tazi@example.com", "phone": "+212665998877", "current_job_title": "Élève Ingénieure IA & Data Science"}, "description_markdown": "Élève ingénieure en dernière année à l''INSEA, passionnée par le Traitement Automatique du Langage Naturel.", "skills": ["Python", "PyTorch", "FastAPI", "Transformers"], "experience": 0, "coursework": ["Bac+5 (Ingénieur d''État)"], "languages": ["Français", "Anglais"], "localization": "Marrakech"}',
  NOW() - INTERVAL '1 hour', NOW() - INTERVAL '1 hour' + INTERVAL '8 seconds'
);

INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, passed_min_score, category_scores, extracted_matching, applied_at, scored_at)
VALUES (
  '32000000-0000-0000-0000-000000000025', '30000000-0000-0000-0000-000000000025',
  '33333333-3333-3333-3333-333333333333', '31000000-0000-0000-0000-000000000025',
  'NEW', 76.00, true,
  '{"skills": 29.00, "experience": 8.00, "coursework": 19.00, "languages": 10.00, "localization": 10.00}',
  '{"matched_criteria": {"skills": ["Python", "PyTorch", "FastAPI", "Transformers"], "experience": true, "coursework": ["Bac+5 (Ingénieur d''État)"], "languages": ["Français", "Anglais"], "localization": "Marrakech"}, "strengths": ["Formation d''excellence INSEA", "Projets pertinents en NLP / PyTorch", "Réside à Marrakech"], "weaknesses": []}',
  NOW() - INTERVAL '1 hour', NOW() - INTERVAL '1 hour' + INTERVAL '8 seconds'
);


-- --------------------------------------------------------------------
-- OFFER C: Cloud & DevOps (Showcasing HIRED, FOLLOW_UP, STALLED, and NEW)
-- --------------------------------------------------------------------

-- 16. Karim Idrissi (Senior DevOps 91.00% >= 75% -> HIRED, passed_min_score: true)
INSERT INTO candidate (id, first_name, last_name, email, phone, created_at)
VALUES ('40000000-0000-0000-0000-000000000004', 'Karim', 'Idrissi', 'karim.idrissi@example.com', '+212664455667', NOW() - INTERVAL '3 days');

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data, uploaded_at, processed_at)
VALUES (
  '41000000-0000-0000-0000-000000000004', '40000000-0000-0000-0000-000000000004',
  'resumes/41000000-0000-0000-0000-000000000004.pdf', 'CV_Karim_Idrissi_DevOps.pdf',
  'sha256_karim_devops_4', 'SUCCESS',
  '{"candidate_info": {"first_name": "Karim", "last_name": "Idrissi", "email": "karim.idrissi@example.com", "phone": "+212664455667", "current_job_title": "Ingénieur DevOps & Cloud AWS"}, "description_markdown": "Ingénieur Cloud & DevOps chevronné avec 3 ans d''expérience en conception d''infrastructures sous AWS et Kubernetes.", "skills": ["Docker", "Kubernetes", "AWS", "Terraform", "CI/CD", "Linux"], "experience": 36, "coursework": ["Bac+5 (Master, Diplôme d''Ingénieur)"], "languages": ["Français", "Anglais"], "localization": "Full Remote / Télétravail 100%"}',
  NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days' + INTERVAL '6 seconds'
);

INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, passed_min_score, category_scores, extracted_matching, applied_at, scored_at)
VALUES (
  '42000000-0000-0000-0000-000000000004', '40000000-0000-0000-0000-000000000004',
  '44444444-4444-4444-4444-444444444444', '41000000-0000-0000-0000-000000000004',
  'HIRED', 91.00, true,
  '{"skills": 32.00, "experience": 24.00, "coursework": 14.00, "languages": 9.00, "localization": 12.00}',
  '{"matched_criteria": {"skills": ["Docker", "Kubernetes", "AWS", "Terraform", "CI/CD"], "experience": true, "coursework": ["Bac+5 (Master, Diplôme d''Ingénieur)"], "languages": ["Français", "Anglais"], "localization": "Full Remote / Télétravail 100%"}, "strengths": ["Profil senior couvrant 100% des compétences requises", "Certification AWS Solutions Architect", "Expérience confirmée sur Kubernetes en production"], "weaknesses": []}',
  NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days' + INTERVAL '6 seconds'
);

-- 17. Hana El Fassi (Score 79.00% >= 75% -> FOLLOW_UP, needs follow-up email)
INSERT INTO candidate (id, first_name, last_name, email, phone, created_at)
VALUES ('40000000-0000-0000-0000-000000000014', 'Hana', 'El Fassi', 'hana.elfassi@example.com', '+212665123456', NOW() - INTERVAL '3 days');

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data, uploaded_at, processed_at)
VALUES (
  '41000000-0000-0000-0000-000000000014', '40000000-0000-0000-0000-000000000014',
  'resumes/41000000-0000-0000-0000-000000000014.pdf', 'CV_Hana_ElFassi_Cloud.pdf',
  'sha256_hana_cloud_14', 'SUCCESS',
  '{"candidate_info": {"first_name": "Hana", "last_name": "El Fassi", "email": "hana.elfassi@example.com", "phone": "+212665123456", "current_job_title": "Administratrice Systèmes & Cloud"}, "description_markdown": "Spécialiste Cloud avec 2 ans d''expérience sur Docker, Terraform et AWS.", "skills": ["Docker", "AWS", "Terraform", "CI/CD", "Linux"], "experience": 24, "coursework": ["Bac+5 (Ingénieur d''État)"], "languages": ["Français", "Anglais"], "localization": "Full Remote / Télétravail 100%"}',
  NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days' + INTERVAL '7 seconds'
);

INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, passed_min_score, category_scores, extracted_matching, applied_at, scored_at)
VALUES (
  '42000000-0000-0000-0000-000000000014', '40000000-0000-0000-0000-000000000014',
  '44444444-4444-4444-4444-444444444444', '41000000-0000-0000-0000-000000000014',
  'FOLLOW_UP', 79.00, true,
  '{"skills": 27.00, "experience": 20.00, "coursework": 14.00, "languages": 8.00, "localization": 10.00}',
  '{"matched_criteria": {"skills": ["Docker", "AWS", "Terraform", "CI/CD"], "experience": true, "coursework": ["Bac+5 (Ingénieur d''État)"], "languages": ["Français", "Anglais"], "localization": "Full Remote / Télétravail 100%"}, "strengths": ["Bonne autonomie sur AWS et Terraform", "Formation solide"], "weaknesses": ["Expérience Kubernetes encore débutante"]}',
  NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days' + INTERVAL '7 seconds'
);

-- 18. Bilal Ouafi (Score 58.00% < 75% -> REJECTED, passed_min_score: false)
INSERT INTO candidate (id, first_name, last_name, email, phone, created_at)
VALUES ('40000000-0000-0000-0000-000000000015', 'Bilal', 'Ouafi', 'bilal.ouafi@example.com', '+212666778899', NOW() - INTERVAL '4 days');

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data, uploaded_at, processed_at)
VALUES (
  '41000000-0000-0000-0000-000000000015', '40000000-0000-0000-0000-000000000015',
  'resumes/41000000-0000-0000-0000-000000000015.pdf', 'CV_Bilal_Ouafi.pdf',
  'sha256_bilal_ouafi_15', 'SUCCESS',
  '{"candidate_info": {"first_name": "Bilal", "last_name": "Ouafi", "email": "bilal.ouafi@example.com", "phone": "+212666778899", "current_job_title": "Technicien Réseaux"}, "description_markdown": "Technicien réseaux et support avec notions de virtualisation.", "skills": ["Linux", "Docker"], "experience": 12, "coursework": ["Bac+2 (DUT Réseaux)"], "languages": ["Français"], "localization": "Casablanca"}',
  NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days' + INTERVAL '5 seconds'
);

INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, passed_min_score, category_scores, extracted_matching, applied_at, scored_at)
VALUES (
  '42000000-0000-0000-0000-000000000015', '40000000-0000-0000-0000-000000000015',
  '44444444-4444-4444-4444-444444444444', '41000000-0000-0000-0000-000000000015',
  'REJECTED', 58.00, false,
  '{"skills": 16.00, "experience": 14.00, "coursework": 10.00, "languages": 8.00, "localization": 10.00}',
  '{"matched_criteria": {"skills": ["Docker"], "experience": false, "coursework": [], "languages": ["Français"], "localization": null}, "strengths": ["Bases en administration Linux"], "weaknesses": ["Pas d''expérience Kubernetes ni Terraform", "Bac+2 vs Bac+5 demandé"]}',
  NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days' + INTERVAL '5 seconds'
);

-- 19. Kenza Mounir (Score 85.00% >= 75% -> SHORTLISTED, passed_min_score: true)
INSERT INTO candidate (id, first_name, last_name, email, phone, created_at)
VALUES ('40000000-0000-0000-0000-000000000016', 'Kenza', 'Mounir', 'kenza.mounir@example.com', '+212667112233', NOW() - INTERVAL '4 days');

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data, uploaded_at, processed_at)
VALUES (
  '41000000-0000-0000-0000-000000000016', '40000000-0000-0000-0000-000000000016',
  'resumes/41000000-0000-0000-0000-000000000016.pdf', 'CV_Kenza_Mounir_DevOps.pdf',
  'sha256_kenza_mounir_16', 'SUCCESS',
  '{"candidate_info": {"first_name": "Kenza", "last_name": "Mounir", "email": "kenza.mounir@example.com", "phone": "+212667112233", "current_job_title": "Consultante Cloud DevOps"}, "description_markdown": "Ingénieure DevOps avec 3 ans d''expérience en automatisation d''infrastructures CI/CD et conteneurisation.", "skills": ["Docker", "Kubernetes", "AWS", "Terraform", "CI/CD"], "experience": 36, "coursework": ["Diplôme d''Ingénieur d''État"], "languages": ["Français", "Anglais"], "localization": "Full Remote / Télétravail 100%"}',
  NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days' + INTERVAL '8 seconds'
);

INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, passed_min_score, category_scores, extracted_matching, applied_at, scored_at)
VALUES (
  '42000000-0000-0000-0000-000000000016', '40000000-0000-0000-0000-000000000016',
  '44444444-4444-4444-4444-444444444444', '41000000-0000-0000-0000-000000000016',
  'SHORTLISTED', 85.00, true,
  '{"skills": 30.00, "experience": 22.00, "coursework": 14.00, "languages": 9.00, "localization": 10.00}',
  '{"matched_criteria": {"skills": ["Docker", "Kubernetes", "AWS", "Terraform", "CI/CD"], "experience": true, "coursework": ["Diplôme d''Ingénieur d''État"], "languages": ["Français", "Anglais"], "localization": "Full Remote / Télétravail 100%"}, "strengths": ["Excellente maîtrise Kubernetes et GitLab CI", "Expérience reconnue en environnement distant"], "weaknesses": []}',
  NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days' + INTERVAL '8 seconds'
);

-- 20. Mehdi Alaoui (Stalled Extraction > 5 min -> STALLED Orange Warning & Inline Retry)
INSERT INTO candidate (id, first_name, last_name, email, phone, created_at)
VALUES ('80000000-0000-0000-0000-000000000008', 'Mehdi', 'Alaoui', 'mehdi.alaoui@example.com', '+212667788990', NOW() - INTERVAL '15 minutes');

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data, uploaded_at, processed_at)
VALUES (
  '81000000-0000-0000-0000-000000000008', '80000000-0000-0000-0000-000000000008',
  'resumes/81000000-0000-0000-0000-000000000008.pdf', 'CV_Mehdi_Alaoui_Senior.pdf',
  'sha256_mehdi_stalled_8', 'STALLED', null,
  NOW() - INTERVAL '15 minutes', null
);

INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, passed_min_score, category_scores, extracted_matching, applied_at, scored_at)
VALUES (
  '82000000-0000-0000-0000-000000000008', '80000000-0000-0000-0000-000000000008',
  '44444444-4444-4444-4444-444444444444', '81000000-0000-0000-0000-000000000008',
  'NEW', null, null, null, null,
  NOW() - INTERVAL '15 minutes', null
);

-- 21. Ghita Mansouri (Cloud Candidate 82.00% >= 75% -> NEW, passed_min_score: true)
INSERT INTO candidate (id, first_name, last_name, email, phone, created_at)
VALUES ('40000000-0000-0000-0000-000000000026', 'Ghita', 'Mansouri', 'ghita.mansouri@example.com', '+212661223344', NOW() - INTERVAL '4 hours');

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data, uploaded_at, processed_at)
VALUES (
  '41000000-0000-0000-0000-000000000026', '40000000-0000-0000-0000-000000000026',
  'resumes/41000000-0000-0000-0000-000000000026.pdf', 'CV_Ghita_Mansouri_DevOps.pdf',
  'sha256_ghita_mansouri_26', 'SUCCESS',
  '{"candidate_info": {"first_name": "Ghita", "last_name": "Mansouri", "email": "ghita.mansouri@example.com", "phone": "+212661223344", "current_job_title": "Ingénieure Cloud"}, "description_markdown": "Ingénieure Cloud certifiée AWS avec compétences Kubernetes et Docker.", "skills": ["Docker", "Kubernetes", "AWS", "Terraform"], "experience": 28, "coursework": ["Bac+5"], "languages": ["Français", "Anglais"], "localization": "Full Remote / Télétravail 100%"}',
  NOW() - INTERVAL '4 hours', NOW() - INTERVAL '4 hours' + INTERVAL '6 seconds'
);

INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, passed_min_score, category_scores, extracted_matching, applied_at, scored_at)
VALUES (
  '42000000-0000-0000-0000-000000000026', '40000000-0000-0000-0000-000000000026',
  '44444444-4444-4444-4444-444444444444', '41000000-0000-0000-0000-000000000026',
  'NEW', 82.00, true,
  '{"skills": 28.00, "experience": 22.00, "coursework": 13.00, "languages": 9.00, "localization": 10.00}',
  '{"matched_criteria": {"skills": ["Docker", "Kubernetes", "AWS", "Terraform"], "experience": true, "coursework": ["Bac+5"], "languages": ["Français", "Anglais"], "localization": "Full Remote / Télétravail 100%"}, "strengths": ["Bonne maîtrise AWS & Terraform"], "weaknesses": []}',
  NOW() - INTERVAL '4 hours', NOW() - INTERVAL '4 hours' + INTERVAL '6 seconds'
);

-- 22. Yassir Idrissi (Full-Stack 84.00% >= 80% -> NEW, passed_min_score: true)
INSERT INTO candidate (id, first_name, last_name, email, phone, created_at)
VALUES ('10000000-0000-0000-0000-000000000027', 'Yassir', 'Idrissi', 'yassir.idrissi@example.com', '+212662334455', NOW() - INTERVAL '2 hours');

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data, uploaded_at, processed_at)
VALUES (
  '11000000-0000-0000-0000-000000000027', '10000000-0000-0000-0000-000000000027',
  'resumes/11000000-0000-0000-0000-000000000027.pdf', 'CV_Yassir_Idrissi.pdf',
  'sha256_yassir_idrissi_27', 'SUCCESS',
  '{"candidate_info": {"first_name": "Yassir", "last_name": "Idrissi", "email": "yassir.idrissi@example.com", "phone": "+212662334455", "current_job_title": "Développeur Senior Java"}, "description_markdown": "Développeur Senior Java / Angular avec solide expérience bancaire.", "skills": ["Java", "Spring Boot", "Angular", "Docker", "PostgreSQL"], "experience": 44, "coursework": ["Bac+5"], "languages": ["Français", "Anglais"], "localization": "Agadir"}',
  NOW() - INTERVAL '2 hours', NOW() - INTERVAL '2 hours' + INTERVAL '8 seconds'
);

INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, passed_min_score, category_scores, extracted_matching, applied_at, scored_at)
VALUES (
  '12000000-0000-0000-0000-000000000027', '10000000-0000-0000-0000-000000000027',
  '22222222-2222-2222-2222-222222222222', '11000000-0000-0000-0000-000000000027',
  'NEW', 84.00, true,
  '{"skills": 26.00, "experience": 23.00, "coursework": 13.00, "languages": 10.00, "localization": 12.00}',
  '{"matched_criteria": {"skills": ["Java", "Spring Boot", "Angular", "Docker", "PostgreSQL"], "experience": true, "coursework": ["Bac+5"], "languages": ["Français", "Anglais"], "localization": "Agadir"}, "strengths": ["Profil très solide et disponible"], "weaknesses": []}',
  NOW() - INTERVAL '2 hours', NOW() - INTERVAL '2 hours' + INTERVAL '8 seconds'
);


-- ====================================================================
-- 4. WORKFLOW STATUS HISTORY (Powers the Dashboard Recent Activity Feed)
-- Chronological event stream reflecting Zineb HADAFI and Sophie El Amrani''s actions
-- ====================================================================

-- Karim Idrissi: NEW -> SHORTLISTED (by Zineb HADAFI, 3 days ago)
INSERT INTO workflow_status_history (id, application_id, from_status, to_status, changed_by, changed_at)
VALUES (
  '90000000-0000-0000-0000-000000000001',
  '42000000-0000-0000-0000-000000000004',
  'NEW', 'SHORTLISTED',
  '11111111-1111-1111-1111-111111111111', -- Zineb HADAFI
  NOW() - INTERVAL '2 days' - INTERVAL '18 hours'
);

-- Karim Idrissi: SHORTLISTED -> INTERVIEWING (by Sophie, 2 days ago)
INSERT INTO workflow_status_history (id, application_id, from_status, to_status, changed_by, changed_at)
VALUES (
  '90000000-0000-0000-0000-000000000002',
  '42000000-0000-0000-0000-000000000004',
  'SHORTLISTED', 'INTERVIEWING',
  '11111111-1111-1111-1111-111111111110', -- Sophie
  NOW() - INTERVAL '2 days' - INTERVAL '4 hours'
);

-- Karim Idrissi: INTERVIEWING -> HIRED (by Zineb HADAFI, yesterday afternoon)
INSERT INTO workflow_status_history (id, application_id, from_status, to_status, changed_by, changed_at)
VALUES (
  '90000000-0000-0000-0000-000000000003',
  '42000000-0000-0000-0000-000000000004',
  'INTERVIEWING', 'HIRED',
  '11111111-1111-1111-1111-111111111111', -- Zineb HADAFI
  NOW() - INTERVAL '1 day' - INTERVAL '3 hours'
);

-- Amine Tazi: NEW -> SHORTLISTED (by Zineb HADAFI, yesterday evening)
INSERT INTO workflow_status_history (id, application_id, from_status, to_status, changed_by, changed_at)
VALUES (
  '90000000-0000-0000-0000-000000000004',
  '12000000-0000-0000-0000-000000000001',
  'NEW', 'SHORTLISTED',
  '11111111-1111-1111-1111-111111111111', -- Zineb HADAFI
  NOW() - INTERVAL '1 day' + INTERVAL '2 hours'
);

-- Nour El Houda Alami: NEW -> SHORTLISTED (by Zineb HADAFI, yesterday)
INSERT INTO workflow_status_history (id, application_id, from_status, to_status, changed_by, changed_at)
VALUES (
  '90000000-0000-0000-0000-000000000005',
  '12000000-0000-0000-0000-000000000003',
  'NEW', 'SHORTLISTED',
  '11111111-1111-1111-1111-111111111111', -- Zineb HADAFI
  NOW() - INTERVAL '1 day' + INTERVAL '3 hours'
);

-- Nour El Houda Alami: SHORTLISTED -> INTERVIEWING (by Zineb HADAFI, yesterday at 17h45)
INSERT INTO workflow_status_history (id, application_id, from_status, to_status, changed_by, changed_at)
VALUES (
  '90000000-0000-0000-0000-000000000006',
  '12000000-0000-0000-0000-000000000003',
  'SHORTLISTED', 'INTERVIEWING',
  '11111111-1111-1111-1111-111111111111', -- Zineb HADAFI
  NOW() - INTERVAL '18 hours'
);

-- Sara Benali: NEW -> REJECTED (by Zineb HADAFI, 5 days ago)
INSERT INTO workflow_status_history (id, application_id, from_status, to_status, changed_by, changed_at)
VALUES (
  '90000000-0000-0000-0000-000000000007',
  '22000000-0000-0000-0000-000000000002',
  'NEW', 'REJECTED',
  '11111111-1111-1111-1111-111111111111', -- Zineb HADAFI
  NOW() - INTERVAL '5 days'
);

-- Nadia Taha: NEW -> ARCHIVED (by Zineb HADAFI, 5 days ago)
INSERT INTO workflow_status_history (id, application_id, from_status, to_status, changed_by, changed_at)
VALUES (
  '90000000-0000-0000-0000-000000000008',
  '12000000-0000-0000-0000-000000000009',
  'NEW', 'ARCHIVED',
  '11111111-1111-1111-1111-111111111111', -- Zineb HADAFI
  NOW() - INTERVAL '5 days' - INTERVAL '2 hours'
);

-- Youssef Mansouri: NEW -> SHORTLISTED (by Sophie, yesterday morning)
INSERT INTO workflow_status_history (id, application_id, from_status, to_status, changed_by, changed_at)
VALUES (
  '90000000-0000-0000-0000-000000000009',
  '32000000-0000-0000-0000-000000000003',
  'NEW', 'SHORTLISTED',
  '11111111-1111-1111-1111-111111111110', -- Sophie
  NOW() - INTERVAL '1 day' + INTERVAL '1 hour'
);

-- Youssef Mansouri: SHORTLISTED -> INTERVIEWING (by Sophie, yesterday afternoon)
INSERT INTO workflow_status_history (id, application_id, from_status, to_status, changed_by, changed_at)
VALUES (
  '90000000-0000-0000-0000-000000000010',
  '32000000-0000-0000-0000-000000000003',
  'SHORTLISTED', 'INTERVIEWING',
  '11111111-1111-1111-1111-111111111110', -- Sophie
  NOW() - INTERVAL '16 hours'
);

-- Imane Zaid: NEW -> REJECTED (by Sophie, 4 days ago)
INSERT INTO workflow_status_history (id, application_id, from_status, to_status, changed_by, changed_at)
VALUES (
  '90000000-0000-0000-0000-000000000011',
  '32000000-0000-0000-0000-000000000011',
  'NEW', 'REJECTED',
  '11111111-1111-1111-1111-111111111110', -- Sophie
  NOW() - INTERVAL '4 days'
);

-- Hana El Fassi: NEW -> SHORTLISTED (by Zineb HADAFI, 2 days ago)
INSERT INTO workflow_status_history (id, application_id, from_status, to_status, changed_by, changed_at)
VALUES (
  '90000000-0000-0000-0000-000000000012',
  '42000000-0000-0000-0000-000000000014',
  'NEW', 'SHORTLISTED',
  '11111111-1111-1111-1111-111111111111', -- Zineb HADAFI
  NOW() - INTERVAL '2 days'
);

-- Hana El Fassi: SHORTLISTED -> FOLLOW_UP (by Zineb HADAFI, this morning at 09h05)
INSERT INTO workflow_status_history (id, application_id, from_status, to_status, changed_by, changed_at)
VALUES (
  '90000000-0000-0000-0000-000000000013',
  '42000000-0000-0000-0000-000000000014',
  'SHORTLISTED', 'FOLLOW_UP',
  '11111111-1111-1111-1111-111111111111', -- Zineb HADAFI
  NOW() - INTERVAL '1 hour'
);

-- Bilal Ouafi: NEW -> REJECTED (by Zineb HADAFI, 3 days ago)
INSERT INTO workflow_status_history (id, application_id, from_status, to_status, changed_by, changed_at)
VALUES (
  '90000000-0000-0000-0000-000000000014',
  '42000000-0000-0000-0000-000000000015',
  'NEW', 'REJECTED',
  '11111111-1111-1111-1111-111111111111', -- Zineb HADAFI
  NOW() - INTERVAL '3 days'
);

-- Kenza Mounir: NEW -> SHORTLISTED (by Zineb HADAFI, 2 days ago)
INSERT INTO workflow_status_history (id, application_id, from_status, to_status, changed_by, changed_at)
VALUES (
  '90000000-0000-0000-0000-000000000015',
  '42000000-0000-0000-0000-000000000016',
  'NEW', 'SHORTLISTED',
  '11111111-1111-1111-1111-111111111111', -- Zineb HADAFI
  NOW() - INTERVAL '2 days' - INTERVAL '2 hours'
);
