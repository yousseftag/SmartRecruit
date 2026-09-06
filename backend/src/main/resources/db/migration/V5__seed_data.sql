-- V5__seed_data.sql

-- 1. Create a Recruiter User (Preserving required test user and mock author)
INSERT INTO app_user (id, keycloak_sub, username, first_name, last_name, email, role)
VALUES 
('11111111-1111-1111-1111-111111111111', 'recruiter-sub-123', 'recruiter', 'John', 'Recruteur', 'recruiter@smartrecruit.com', 'RECRUITER')
ON CONFLICT (email) DO NOTHING;

-- 2. Create 3 Distinct Job Offers (French, Markdown, Curated Norsys Locations & Criteria)
INSERT INTO offer (id, created_by, updated_by, title, description_markdown, status, category_weights, category_criteria, min_score, duration_months, contract_type, extracted_requirements)
VALUES 
(
  '22222222-2222-2222-2222-222222222222', 
  '11111111-1111-1111-1111-111111111111', 
  '11111111-1111-1111-1111-111111111111',
  'Développeur Full-Stack Senior Java / Angular', 
  '### Contexte & Mission
Norsys Afrique renforce son centre d''excellence à **Agadir** et recrute un(e) **Développeur Full-Stack Senior Java / Angular**. Vous intégrerez une équipe passionnée et agile en charge de la conception et du développement de plateformes métiers critiques pour de grands comptes internationaux.

### Responsabilités principales
- Concevoir des architectures microservices modulaires et résilientes avec **Spring Boot 3** et Java 21.
- Développer des interfaces utilisateur réactives, modernes et performantes sous **Angular 17+** (Signals, Tailwind CSS).
- Garantir la modélisation, l''optimisation des requêtes et l''intégrité des bases de données relationnelles **PostgreSQL**.
- Participer activement à la conteneurisation **Docker**, à l''industrialisation des pipelines CI/CD et aux revues de code rigoureuses.
- Accompagner et mentorer les développeurs juniors au sein de la squad dans un esprit de Software Craftsmanship.

### Profil recherché
- Formation supérieure : **Bac+5** en informatique (Master ou Diplôme d''Ingénieur d''État).
- Minimum **3 ans d''expérience** avérée sur la stack Java / Spring Boot et Angular en production.
- Excellente maîtrise du **Français** et très bon niveau en **Anglais technique**.
- Sens prononcé de la qualité logicielle (tests automatisés, Clean Architecture, principes SOLID).', 
  'ACTIVE', 
  '{"skills": 30, "experience": 25, "coursework": 15, "languages": 15, "localization": 15}',
  '{"skills": ["Java", "Spring Boot", "Angular", "PostgreSQL", "Docker"], "skill_weights": {"Java": 25, "Spring Boot": 25, "Angular": 25, "PostgreSQL": 15, "Docker": 10}, "experience": 36, "coursework": ["Bac+5 (Master, Diplôme d''Ingénieur)", "Diplôme d''Ingénieur d''État"], "languages": ["Français", "Anglais"], "localization": "Agadir"}', 
  80,
  null, 
  'CDI',
  '{"missing_from_criteria": ["Docker", "Git", "CI/CD"], "insights": "Le profil nécessite une solide maîtrise des architectures découplées, des bonnes pratiques de Clean Code et des méthodologies agiles."}'
),
(
  '33333333-3333-3333-3333-333333333333', 
  '11111111-1111-1111-1111-111111111111', 
  '11111111-1111-1111-1111-111111111111',
  'Stage PFE : Data Scientist & Ingénieur IA (NLP / LLM)', 
  '### Contexte du Stage
Au sein du laboratoire d''innovation technologique de Norsys à **Marrakech**, vous participerez activement au développement de la nouvelle génération d''outils d''analyse sémantique automatique et de matching de profils par Intelligence Artificielle.

### Missions confiées
- Concevoir et entraîner des modèles de traitement automatique du langage naturel (NLP) et d''extraction d''entités (NER).
- Développer des microservices d''inférence asynchrones et haute performance avec **FastAPI** et **PyTorch**.
- Évaluer, affiner et intégrer des modèles de fondation ouverts via l''écosystème **HuggingFace** et Transformers.
- Mettre en place des métriques d''évaluation quantitatives et qualitatives des algorithmes de matching.

### Profil requis
- Étudiant en dernière année d''école d''ingénieurs ou Master 2 spécialisé en Data Science / Intelligence Artificielle (**Bac+5**).
- Solides compétences en programmation **Python** et frameworks de Deep Learning.
- Excellente maîtrise du **Français** et bon niveau en **Anglais**.
- Stage conventionné de 6 mois avec forte opportunité de recrutement en CDI à l''issue.', 
  'ACTIVE', 
  '{"skills": 35, "experience": 10, "coursework": 25, "languages": 15, "localization": 15}',
  '{"skills": ["Python", "PyTorch", "FastAPI", "Transformers", "HuggingFace"], "skill_weights": {"Python": 30, "PyTorch": 25, "FastAPI": 20, "Transformers": 15, "HuggingFace": 10}, "experience": 0, "coursework": ["Bac+5 (Master, Diplôme d''Ingénieur)", "Master en Data Science / Intelligence Artificielle"], "languages": ["Français", "Anglais"], "localization": "Marrakech"}', 
  70,
  6, 
  'Stage',
  '{"missing_from_criteria": ["Scikit-Learn", "Git"], "insights": "Stage pré-embauche orienté R&D NLP, intégration de LLMs et déploiement d''APIs d''inférence légères."}'
),
(
  '44444444-4444-4444-4444-444444444444', 
  '11111111-1111-1111-1111-111111111111', 
  '11111111-1111-1111-1111-111111111111',
  'Ingénieur Cloud & DevOps', 
  '### Mission
Norsys recrute un(e) **Ingénieur(e) Cloud & DevOps** en **Télétravail complet (Full Remote 100%)** pour piloter l''industrialisation, la résilience et la sécurité de ses infrastructures cloud.

### Vos responsabilités
- Automatiser le provisionnement d''environnements cloud sur **AWS** via **Terraform** (Infrastructure as Code).
- Déployer, superviser et maintenir des clusters **Kubernetes** en haute disponibilité.
- Élaborer et optimiser des pipelines CI/CD industriels avec GitLab CI et conteneurs **Docker**.
- Mettre en place des stratégies complètes d''observabilité, de monitoring et de gestion des alertes (Prometheus, Grafana).

### Compétences & Profil
- Diplôme d''ingénieur ou Master universitaire spécialisé (**Bac+5**).
- Au moins **2 ans d''expérience** réussie sur un rôle similaire d''automatisation DevOps / Cloud.
- Maîtrise confirmée de Docker, Kubernetes, Terraform et des services fondamentaux AWS.
- Autonomie, rigueur méthodologique et excellente communication écrite et orale en Français.', 
  'ACTIVE', 
  '{"skills": 35, "experience": 25, "coursework": 15, "languages": 10, "localization": 15}',
  '{"skills": ["Docker", "Kubernetes", "AWS", "Terraform", "CI/CD"], "skill_weights": {"Docker": 20, "Kubernetes": 25, "AWS": 25, "Terraform": 15, "CI/CD": 15}, "experience": 24, "coursework": ["Bac+5 (Master, Diplôme d''Ingénieur)", "Diplôme d''Ingénieur d''État"], "languages": ["Français", "Anglais"], "localization": "Full Remote / Télétravail 100%"}', 
  75,
  null, 
  'CDI',
  '{"missing_from_criteria": ["Ansible", "Linux", "Prometheus"], "insights": "Exigence forte sur les compétences Infrastructure as Code (Terraform) et l''orchestration de conteneurs Kubernetes."}'
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
    "description_markdown": "Ingénieur Full-Stack expérimenté avec plus de 4 ans d''expertise dans la conception d''applications d''entreprise robustes sous Java / Spring Boot et Angular. Adepte des pratiques Clean Code, de l''architecture microservices et des méthodologies agiles.",
    "skills": ["Java", "Spring Boot", "Angular", "PostgreSQL", "Docker", "Git", "Clean Architecture"],
    "experience": 48,
    "coursework": ["Bac+5 (Master, Diplôme d''Ingénieur)", "Diplôme d''Ingénieur d''État"],
    "languages": ["Français", "Anglais"],
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
  '{"skills": 26.50, "experience": 24.00, "coursework": 14.00, "languages": 11.00, "localization": 13.00}', 
  '{
    "matched_criteria": {
      "skills": ["Java", "Spring Boot", "Angular", "PostgreSQL", "Docker"],
      "experience": true,
      "coursework": ["Bac+5 (Master, Diplôme d''Ingénieur)", "Diplôme d''Ingénieur d''État"],
      "languages": ["Français", "Anglais"],
      "localization": "Agadir"
    },
    "strengths": [
      "Excellente maîtrise technique de la stack cible Spring Boot et Angular",
      "4 ans d''expérience validée en environnement Agile et microservices",
      "Diplôme d''Ingénieur d''État parfaitement aligné avec les exigences",
      "Réside actuellement à Agadir (disponibilité immédiate pour le site)"
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
    "description_markdown": "Développeuse junior passionnée par l''intégration web moderne, le responsive design et les applications front-end légères (HTML, CSS, JavaScript, PHP).",
    "skills": ["HTML", "CSS", "JavaScript", "PHP", "MySQL"],
    "experience": 6,
    "coursework": ["Bac+2 (BTS, DUT, DEUG)"],
    "languages": ["Français"],
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
  '{"skills": 10.00, "experience": 8.00, "coursework": 8.00, "languages": 10.00, "localization": 10.00}', 
  '{
    "matched_criteria": {
      "skills": ["JavaScript"],
      "experience": false,
      "coursework": [],
      "languages": ["Français"],
      "localization": null
    },
    "strengths": [
      "Bonnes bases fondamentales en développement web et intégration frontend"
    ],
    "weaknesses": [
      "Compétences clés de l''offre absentes du profil : Java, Spring Boot, Angular, PostgreSQL",
      "Expérience professionnelle inférieure au seuil attendu (6 mois vs 36 mois requis)",
      "Niveau d''études inférieur au prérequis (Bac+2 vs Bac+5 demandé)",
      "Localisée à Marrakech alors que le poste est basé à Agadir"
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
    "description_markdown": "Élève ingénieur en dernière année d''école d''ingénieurs, passionné par le traitement automatique du langage naturel (NLP), les LLMs et les architectures Transformers. Auteur de projets académiques d''extraction automatique d''informations.",
    "skills": ["Python", "PyTorch", "Transformers", "FastAPI", "HuggingFace", "Scikit-Learn"],
    "experience": 0,
    "coursework": ["Bac+5 (Master, Diplôme d''Ingénieur)", "Master en Data Science / Intelligence Artificielle"],
    "languages": ["Français", "Anglais"],
    "localization": "Marrakech"
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
  '{"skills": 28.00, "experience": 8.00, "coursework": 18.00, "languages": 10.00, "localization": 10.00}', 
  '{
    "matched_criteria": {
      "skills": ["Python", "PyTorch", "Transformers", "FastAPI", "HuggingFace"],
      "experience": true,
      "coursework": ["Bac+5 (Master, Diplôme d''Ingénieur)"],
      "languages": ["Français", "Anglais"],
      "localization": "Marrakech"
    },
    "strengths": [
      "Maîtrise remarquable de l''écosystème Python, PyTorch et Transformers",
      "Projets académiques concrets sur l''extraction d''entités nommées (NER) et LLMs",
      "Parfait alignement académique Bac+5 en Data Science / IA",
      "Basé à Marrakech, proximité directe avec le centre R&D"
    ],
    "weaknesses": [
      "Première expérience professionnelle (stage de fin d''études)"
    ]
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
    "description_markdown": "Ingénieur Cloud & DevOps chevronné avec 3 ans d''expérience en conception d''infrastructures cloud-native sous AWS. Expert en automatisation Terraform, gestion de clusters Kubernetes et pipelines CI/CD industriels.",
    "skills": ["Docker", "Kubernetes", "AWS", "Terraform", "CI/CD", "Linux", "Prometheus", "Ansible"],
    "experience": 36,
    "coursework": ["Bac+5 (Master, Diplôme d''Ingénieur)", "Diplôme d''Ingénieur d''État"],
    "languages": ["Français", "Anglais"],
    "localization": "Full Remote / Télétravail 100%"
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
  '{"skills": 32.00, "experience": 24.00, "coursework": 14.00, "languages": 9.00, "localization": 12.00}', 
  '{
    "matched_criteria": {
      "skills": ["Docker", "Kubernetes", "AWS", "Terraform", "CI/CD"],
      "experience": true,
      "coursework": ["Bac+5 (Master, Diplôme d''Ingénieur)"],
      "languages": ["Français", "Anglais"],
      "localization": "Full Remote / Télétravail 100%"
    },
    "strengths": [
      "Profil senior couvrant 100% des compétences cibles DevOps et Cloud",
      "Certification AWS Solutions Architect validée",
      "Expérience confirmée sur Kubernetes en production",
      "Habitué au travail collaboratif en télétravail complet"
    ],
    "weaknesses": []
  }',
  NOW() - INTERVAL '4 days',
  NOW() - INTERVAL '4 days' + INTERVAL '6 seconds'
);

-- Candidate 5: Ayoub Chraibi (Public Ingestion -> PENDING, in-flight extraction)
INSERT INTO candidate (id, first_name, last_name, email, phone)
VALUES ('50000000-0000-0000-0000-000000000005', 'Ayoub', 'Chraibi', 'ayoub.chraibi@example.com', '+212665566778');

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data, uploaded_at, processed_at)
VALUES (
  '51000000-0000-0000-0000-000000000005', 
  '50000000-0000-0000-0000-000000000005', 
  'resumes/51000000-0000-0000-0000-000000000005.pdf', 
  'CV_Ayoub_Chraibi_FullStack.pdf', 
  'hash_ayoub_pending_5', 
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

-- Candidate 6: Salma Bennani (AI Extraction FAILED, Has Contact Info -> Red Badge & Retry action)
INSERT INTO candidate (id, first_name, last_name, email, phone)
VALUES ('60000000-0000-0000-0000-000000000006', 'Salma', 'Bennani', 'salma.bennani@example.com', '+212666677889');

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data, uploaded_at, processed_at)
VALUES (
  '61000000-0000-0000-0000-000000000006', 
  '60000000-0000-0000-0000-000000000006', 
  'resumes/61000000-0000-0000-0000-000000000006.pdf', 
  'CV_Salma_Bennani_Scan.pdf', 
  'hash_salma_failed_6', 
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
  'Scan_Document_Illisible.pdf', 
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
