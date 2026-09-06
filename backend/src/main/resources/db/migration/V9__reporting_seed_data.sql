
-- V7__reporting_seed_data.sql
-- Rich, realistic demo seed data for SmartRecruit recruitment reporting, dashboards, and candidate evaluations.
-- Adheres to PostgreSQL Flyway standards and ensures 100% coherence across all platform features.

-- ====================================================================
-- SECTION 1: MODERNIZE V6 GENERIC OFFERS (Position Title 1..15 -> Real Roles)
-- ====================================================================

UPDATE offer SET 
  title = 'Tech Lead Java / Spring Boot',
  description_markdown = 'Norsys Afrique recherche un Tech Lead Java / Spring Boot pour encadrer nos équipes de développement, concevoir des architectures microservices distribuées et garantir les standards de clean code.',
  category_weights = '{"skills": 35, "experience": 30, "coursework": 15, "languages": 10, "localization": 10}',
  category_criteria = '{"skills": ["java", "spring boot", "microservices", "docker", "kafka", "postgresql"], "experience": 48, "coursework": ["bac+5", "master"], "languages": ["English", "French"], "localization": "Casablanca"}',
  min_score = 80,
  contract_type = 'CDI',
  status = 'ACTIVE'
WHERE id = md5('offer1')::uuid;

UPDATE offer SET 
  title = 'Développeur Front-End Angular Senior',
  description_markdown = 'Rejoignez notre centre de compétences front-end pour concevoir des applications web réactives de haute performance avec Angular 17+, TailwindCSS, RxJS et State Management.',
  category_weights = '{"skills": 35, "experience": 25, "coursework": 15, "languages": 10, "localization": 15}',
  category_criteria = '{"skills": ["angular", "typescript", "tailwindcss", "rxjs", "jest", "html5"], "experience": 36, "coursework": ["bac+5", "master"], "languages": ["English", "French"], "localization": "Agadir"}',
  min_score = 78,
  contract_type = 'CDI',
  status = 'ACTIVE'
WHERE id = md5('offer2')::uuid;

UPDATE offer SET 
  title = 'Architecte Solutions Cloud AWS',
  description_markdown = 'Conception, déploiement et optimisation d''infrastructures Cloud AWS sécurisées et résilientes (EKS, Lambda, Terraform, CloudWatch, FinOps).',
  category_weights = '{"skills": 35, "experience": 30, "coursework": 15, "languages": 10, "localization": 10}',
  category_criteria = '{"skills": ["aws", "terraform", "kubernetes", "cloud architecture", "docker", "finops"], "experience": 60, "coursework": ["bac+5", "master"], "languages": ["English", "French"], "localization": "Rabat"}',
  min_score = 82,
  contract_type = 'CDI',
  status = 'ACTIVE'
WHERE id = md5('offer3')::uuid;

UPDATE offer SET 
  title = 'Consultant Cybersécurité & SecOps',
  description_markdown = 'Accompagnement de nos clients sur la sécurisation des architectures applicatives, gestion des vulnérabilités, audits ISO 27001 et intégration DevSecOps.',
  category_weights = '{"skills": 35, "experience": 25, "coursework": 20, "languages": 10, "localization": 10}',
  category_criteria = '{"skills": ["cybersecurity", "iso 27001", "siem", "pentest", "devsecops", "owasp"], "experience": 36, "coursework": ["bac+5", "master"], "languages": ["English", "French"], "localization": "Casablanca"}',
  min_score = 75,
  contract_type = 'CDI',
  status = 'ACTIVE'
WHERE id = md5('offer4')::uuid;

UPDATE offer SET 
  title = 'Ingénieur QA & Automatisation des Tests',
  description_markdown = 'Mise en place de frameworks d''automatisation de tests bout-en-bout (Cypress, Playwright, Selenium) et intégration continue des tests de charge.',
  category_weights = '{"skills": 30, "experience": 25, "coursework": 20, "languages": 10, "localization": 15}',
  category_criteria = '{"skills": ["cypress", "playwright", "selenium", "junit", "ci/cd", "postman"], "experience": 24, "coursework": ["bac+5", "master"], "languages": ["English", "French"], "localization": "Agadir"}',
  min_score = 70,
  contract_type = 'CDI',
  status = 'ACTIVE'
WHERE id = md5('offer5')::uuid;

UPDATE offer SET 
  title = 'Product Owner - Solutions Digitales',
  description_markdown = 'Pilotage de la vision produit, rédaction des User Stories, priorisation du backlog et coordination étroite entre les équipes métier et techniques en méthodologie Scrum.',
  category_weights = '{"skills": 30, "experience": 30, "coursework": 20, "languages": 10, "localization": 10}',
  category_criteria = '{"skills": ["scrum", "product backlog", "user stories", "jira", "agile", "ux"], "experience": 36, "coursework": ["bac+5", "master"], "languages": ["English", "French"], "localization": "Casablanca"}',
  min_score = 75,
  contract_type = 'CDI',
  status = 'ACTIVE'
WHERE id = md5('offer6')::uuid;

UPDATE offer SET 
  title = 'Développeur Mobile Flutter & React Native',
  description_markdown = 'Développement d''applications mobiles cross-platform iOS & Android performantes, intégration des APIs REST et publication sur les stores.',
  category_weights = '{"skills": 35, "experience": 25, "coursework": 15, "languages": 10, "localization": 15}',
  category_criteria = '{"skills": ["flutter", "dart", "react native", "mobile", "rest api", "git"], "experience": 24, "coursework": ["bac+5", "master"], "languages": ["English", "French"], "localization": "Rabat"}',
  min_score = 72,
  contract_type = 'CDI',
  status = 'ACTIVE'
WHERE id = md5('offer7')::uuid;

UPDATE offer SET 
  title = 'Data Engineer & Analytics Specialist',
  description_markdown = 'Conception de pipelines de données scalables, data lakes et entrepôts de données cloud avec Python, Apache Spark, Airflow et Snowflake.',
  category_weights = '{"skills": 35, "experience": 25, "coursework": 20, "languages": 10, "localization": 10}',
  category_criteria = '{"skills": ["python", "spark", "sql", "airflow", "snowflake", "etl"], "experience": 36, "coursework": ["bac+5", "master"], "languages": ["English", "French"], "localization": "Casablanca"}',
  min_score = 78,
  contract_type = 'CDI',
  status = 'ACTIVE'
WHERE id = md5('offer8')::uuid;

UPDATE offer SET 
  title = 'Scrum Master & Agile Delivery Manager',
  description_markdown = 'Accompagnement des rituels agiles, coaching des squads pluridisciplinaires, levée des points de blocage et amélioration continue de la vélocité.',
  category_weights = '{"skills": 30, "experience": 30, "coursework": 15, "languages": 10, "localization": 15}',
  category_criteria = '{"skills": ["scrum", "kanban", "agile coaching", "jira", "facilitation", "lean"], "experience": 36, "coursework": ["bac+5", "master"], "languages": ["English", "French"], "localization": "Agadir"}',
  min_score = 75,
  contract_type = 'CDI',
  status = 'ACTIVE'
WHERE id = md5('offer9')::uuid;

UPDATE offer SET 
  title = 'Ingénieur Systèmes & Réseaux Linux',
  description_markdown = 'Administration des serveurs Linux Debian/RedHat, configuration des équipements réseaux, supervision Prometheus/Grafana et haute disponibilité.',
  category_weights = '{"skills": 35, "experience": 25, "coursework": 15, "languages": 10, "localization": 15}',
  category_criteria = '{"skills": ["linux", "bash", "cisco", "virtualization", "dns", "grafana"], "experience": 24, "coursework": ["bac+5", "master"], "languages": ["English", "French"], "localization": "Marrakech"}',
  min_score = 70,
  contract_type = 'CDI',
  status = 'CLOSED'
WHERE id = md5('offer10')::uuid;

UPDATE offer SET 
  title = 'Développeur Back-End Python / FastAPI',
  description_markdown = 'Développement de microservices asynchrones avec Python 3.11, FastAPI, SQLAlchemy, PostgreSQL et intégration dans des conteneurs Docker.',
  category_weights = '{"skills": 35, "experience": 25, "coursework": 15, "languages": 10, "localization": 15}',
  category_criteria = '{"skills": ["python", "fastapi", "postgresql", "docker", "redis", "asyncio"], "experience": 24, "coursework": ["bac+5", "master"], "languages": ["English", "French"], "localization": "Casablanca"}',
  min_score = 75,
  contract_type = 'CDI',
  status = 'ACTIVE'
WHERE id = md5('offer11')::uuid;

UPDATE offer SET 
  title = 'Consultant ERP & Systèmes d''Information',
  description_markdown = 'Analyse des processus métier, déploiement et paramétrage de modules ERP (SAP / Odoo), formation des utilisateurs clés.',
  category_weights = '{"skills": 30, "experience": 30, "coursework": 20, "languages": 10, "localization": 10}',
  category_criteria = '{"skills": ["erp", "sap", "odoo", "sql", "processus metier", "gestion de projet"], "experience": 36, "coursework": ["bac+5", "master"], "languages": ["English", "French"], "localization": "Rabat"}',
  min_score = 70,
  contract_type = 'CDI',
  status = 'CLOSED'
WHERE id = md5('offer12')::uuid;

UPDATE offer SET 
  title = 'UI/UX Designer & Ergonome Web',
  description_markdown = 'Création d''expériences utilisateurs intuitives, wireframes, prototypes interactifs sur Figma et maintenance de design systems complets.',
  category_weights = '{"skills": 35, "experience": 25, "coursework": 15, "languages": 10, "localization": 15}',
  category_criteria = '{"skills": ["figma", "ui/ux", "design system", "wireframing", "prototyping", "usability"], "experience": 24, "coursework": ["bac+5", "master"], "languages": ["English", "French"], "localization": "Agadir"}',
  min_score = 75,
  contract_type = 'CDI',
  status = 'ACTIVE'
WHERE id = md5('offer13')::uuid;

UPDATE offer SET 
  title = 'Lead Data Scientist & IA Générative',
  description_markdown = 'R&D sur les modèles LLM, architectures RAG, fine-tuning de modèles de fondation et déploiement de solutions d''IA conversationnelle d''entreprise.',
  category_weights = '{"skills": 40, "experience": 30, "coursework": 15, "languages": 10, "localization": 5}',
  category_criteria = '{"skills": ["python", "pytorch", "llm", "transformers", "rag", "langchain"], "experience": 48, "coursework": ["bac+5", "phd", "master"], "languages": ["English", "French"], "localization": "Casablanca"}',
  min_score = 82,
  contract_type = 'CDI',
  status = 'ACTIVE'
WHERE id = md5('offer14')::uuid;

UPDATE offer SET 
  title = 'Directeur de Projets Informatiques',
  description_markdown = 'Direction stratégique de programmes IT complexes, gestion des budgets d''envergure, encadrement de chefs de projet et relation client grand compte.',
  category_weights = '{"skills": 25, "experience": 40, "coursework": 15, "languages": 10, "localization": 10}',
  category_criteria = '{"skills": ["direction de projet", "pmp", "gouvernance", "budget", "agile", "gestion des risques"], "experience": 72, "coursework": ["bac+5", "master"], "languages": ["English", "French"], "localization": "Casablanca"}',
  min_score = 80,
  contract_type = 'CDI',
  status = 'DRAFT'
WHERE id = md5('offer15')::uuid;


-- ====================================================================
-- SECTION 2: UPGRADE V6 CANDIDATES (Realistic Moroccan Names & Phone Numbers)
-- ====================================================================

UPDATE candidate SET first_name = 'Omar', last_name = 'Idrissi', email = 'omar.idrissi@example.com', phone = '+212661010203' WHERE id = md5('candidate1')::uuid;
UPDATE candidate SET first_name = 'Zineb', last_name = 'Chraibi', email = 'zineb.chraibi@example.com', phone = '+212662020304' WHERE id = md5('candidate2')::uuid;
UPDATE candidate SET first_name = 'Hamza', last_name = 'El Fassi', email = 'hamza.elfassi@example.com', phone = '+212663030405' WHERE id = md5('candidate3')::uuid;
UPDATE candidate SET first_name = 'Salma', last_name = 'Kabbaj', email = 'salma.kabbaj@example.com', phone = '+212664040506' WHERE id = md5('candidate4')::uuid;
UPDATE candidate SET first_name = 'Tarik', last_name = 'Alami', email = 'tarik.alami@example.com', phone = '+212665050607' WHERE id = md5('candidate5')::uuid;
UPDATE candidate SET first_name = 'Nadia', last_name = 'Chaoui', email = 'nadia.chaoui@example.com', phone = '+212666060708' WHERE id = md5('candidate6')::uuid;
UPDATE candidate SET first_name = 'Reda', last_name = 'Mansouri', email = 'reda.mansouri@example.com', phone = '+212667070809' WHERE id = md5('candidate7')::uuid;
UPDATE candidate SET first_name = 'Ghita', last_name = 'Benkirane', email = 'ghita.benkirane@example.com', phone = '+212668080910' WHERE id = md5('candidate8')::uuid;
UPDATE candidate SET first_name = 'Yassine', last_name = 'Bouzid', email = 'yassine.bouzid@example.com', phone = '+212669091011' WHERE id = md5('candidate9')::uuid;
UPDATE candidate SET first_name = 'Leila', last_name = 'Senhaji', email = 'leila.senhaji@example.com', phone = '+212660101112' WHERE id = md5('candidate10')::uuid;
UPDATE candidate SET first_name = 'Mehdi', last_name = 'Bennani', email = 'mehdi.bennani@example.com', phone = '+212671111213' WHERE id = md5('candidate11')::uuid;
UPDATE candidate SET first_name = 'Kenza', last_name = 'Tazi', email = 'kenza.tazi@example.com', phone = '+212672121314' WHERE id = md5('candidate12')::uuid;
UPDATE candidate SET first_name = 'Anass', last_name = 'Berrada', email = 'anass.berrada@example.com', phone = '+212673131415' WHERE id = md5('candidate13')::uuid;
UPDATE candidate SET first_name = 'Hajar', last_name = 'Lahlou', email = 'hajar.lahlou@example.com', phone = '+212674141516' WHERE id = md5('candidate14')::uuid;
UPDATE candidate SET first_name = 'Soufiane', last_name = 'Filali', email = 'soufiane.filali@example.com', phone = '+212675151617' WHERE id = md5('candidate15')::uuid;
UPDATE candidate SET first_name = 'Meryem', last_name = 'Alaoui', email = 'meryem.alaoui@example.com', phone = '+212676161718' WHERE id = md5('candidate16')::uuid;
UPDATE candidate SET first_name = 'Oussama', last_name = 'Cherkaoui', email = 'oussama.cherkaoui@example.com', phone = '+212677171819' WHERE id = md5('candidate17')::uuid;
UPDATE candidate SET first_name = 'Asmaa', last_name = 'Naciri', email = 'asmaa.naciri@example.com', phone = '+212678181920' WHERE id = md5('candidate18')::uuid;
UPDATE candidate SET first_name = 'Walid', last_name = 'Amrani', email = 'walid.amrani@example.com', phone = '+212679192021' WHERE id = md5('candidate19')::uuid;
UPDATE candidate SET first_name = 'Imane', last_name = 'Bennis', email = 'imane.bennis@example.com', phone = '+212670202122' WHERE id = md5('candidate20')::uuid;
UPDATE candidate SET first_name = 'Nabil', last_name = 'Jaidi', email = 'nabil.jaidi@example.com', phone = '+212681212223' WHERE id = md5('candidate21')::uuid;
UPDATE candidate SET first_name = 'Kaoutar', last_name = 'Sabri', email = 'kaoutar.sabri@example.com', phone = '+212682222324' WHERE id = md5('candidate22')::uuid;
UPDATE candidate SET first_name = 'Adil', last_name = 'Tahiri', email = 'adil.tahiri@example.com', phone = '+212683232425' WHERE id = md5('candidate23')::uuid;
UPDATE candidate SET first_name = 'Fatima Ezzahra', last_name = 'Belhaj', email = 'fe.belhaj@example.com', phone = '+212684242526' WHERE id = md5('candidate24')::uuid;
UPDATE candidate SET first_name = 'Younes', last_name = 'Kadiri', email = 'younes.kadiri@example.com', phone = '+212685252627' WHERE id = md5('candidate25')::uuid;
UPDATE candidate SET first_name = 'Samira', last_name = 'Bouanani', email = 'samira.bouanani@example.com', phone = '+212686262728' WHERE id = md5('candidate26')::uuid;
UPDATE candidate SET first_name = 'Khalid', last_name = 'Mekouar', email = 'khalid.mekouar@example.com', phone = '+212687272829' WHERE id = md5('candidate27')::uuid;
UPDATE candidate SET first_name = 'Siham', last_name = 'Zniber', email = 'siham.zniber@example.com', phone = '+212688282930' WHERE id = md5('candidate28')::uuid;
UPDATE candidate SET first_name = 'Ismail', last_name = 'Lemseffer', email = 'ismail.lemseffer@example.com', phone = '+212689293031' WHERE id = md5('candidate29')::uuid;
UPDATE candidate SET first_name = 'Chaimaa', last_name = 'Ghellab', email = 'chaimaa.ghellab@example.com', phone = '+212680303132' WHERE id = md5('candidate30')::uuid;

-- Enrich CV extractions and category scores for V6 applications so subscores are visible
UPDATE cv_file SET 
  extracted_data = '{
    "candidate_info": {"current_job_title": "Ingénieur d''Études & Développement"},
    "description_markdown": "Ingénieur passionné par les technologies du cloud et le développement logiciel moderne avec une solide formation d''ingénieur d''état.",
    "skills": ["Java", "Spring Boot", "Angular", "PostgreSQL", "Docker", "Git", "REST API"],
    "experience": 30,
    "coursework": ["Bac+5", "Diplôme d''Ingénieur d''État"],
    "languages": ["French", "English", "Arabic"],
    "localization": "Casablanca"
  }',
  original_filename = 'CV_Professionnel_Ingenieur.pdf'
WHERE extraction_status = 'SUCCESS' AND extracted_data IS NULL;

UPDATE application SET 
  category_scores = jsonb_build_object(
    'skills', round((total_score * 0.35)::numeric, 1),
    'experience', round((total_score * 0.25)::numeric, 1),
    'coursework', round((total_score * 0.15)::numeric, 1),
    'languages', round((total_score * 0.10)::numeric, 1),
    'localization', round((total_score * 0.15)::numeric, 1)
  ),
  extracted_matching = jsonb_build_object(
    'matched_criteria', jsonb_build_object(
      'skills', jsonb_build_array('java', 'spring boot', 'angular'),
      'experience', 30,
      'coursework', jsonb_build_array('bac+5', 'master'),
      'languages', jsonb_build_array('English', 'French'),
      'localization', 'Casablanca'
    ),
    'strengths', jsonb_build_array(
      'Bonnes compétences techniques sur le socle applicatif ciblé',
      'Formation supérieure Bac+5 conforme aux attentes',
      'Expérience confirmée en méthode Agile Scrum'
    ),
    'weaknesses', CASE WHEN total_score < 70 THEN jsonb_build_array('Expérience légèrement inférieure au seuil souhaité', 'Maîtrise partielle des outils d''orchestration cloud') ELSE jsonb_build_array() END
  )
WHERE total_score IS NOT NULL AND category_scores IS NULL;


-- ====================================================================
-- SECTION 3: NEW HIGH-FIDELITY CANDIDATES FOR MAIN DEMO OFFERS
-- ====================================================================

-- --------------------------------------------------------------------
-- A. Senior Full-Stack Developer (22222222-2222-2222-2222-222222222222, Agadir, min 80)
-- --------------------------------------------------------------------

-- A1. Tariq Benchekroun (Rank 1 Candidate, Score 96.50% >= 80% -> ADMISSIBLE, HIRED)
INSERT INTO candidate (id, first_name, last_name, email, phone)
VALUES ('91000000-0000-0000-0000-000000000001', 'Tariq', 'Benchekroun', 'tariq.benchekroun@example.com', '+212661998877')
ON CONFLICT (email) DO NOTHING;

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data, uploaded_at, processed_at)
VALUES (
  '92000000-0000-0000-0000-000000000001',
  '91000000-0000-0000-0000-000000000001',
  'resumes/CV_Tariq_Benchekroun.pdf',
  'CV_Tariq_Benchekroun_LeadFullStack.pdf',
  'hash_tariq_benchekroun_1',
  'SUCCESS',
  '{
    "candidate_info": {
      "first_name": "Tariq",
      "last_name": "Benchekroun",
      "email": "tariq.benchekroun@example.com",
      "phone": "+212661998877",
      "current_job_title": "Lead Développeur Full-Stack Java / Angular"
    },
    "description_markdown": "Architecte et Tech Lead Full-Stack avec 5 ans d''expérience dans l''écosystème Spring Boot, Angular 17 et Cloud PostgreSQL. Passionné par le Clean Code, le DDD et les architectures résilientes.",
    "skills": ["Java", "Spring Boot", "Angular", "PostgreSQL", "Docker", "Kubernetes", "TypeScript", "Clean Architecture"],
    "experience": 60,
    "coursework": ["Bac+5", "Ingénieur d''État en Génie Logiciel", "Master MIAGE"],
    "languages": ["French", "English", "Arabic"],
    "localization": "Agadir"
  }',
  NOW() - INTERVAL '8 days',
  NOW() - INTERVAL '8 days' + INTERVAL '8 seconds'
) ON CONFLICT (checksum_sha256) DO NOTHING;

INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, passed_min_score, category_scores, extracted_matching, applied_at, scored_at)
VALUES (
  '93000000-0000-0000-0000-000000000001',
  '91000000-0000-0000-0000-000000000001',
  '22222222-2222-2222-2222-222222222222',
  '92000000-0000-0000-0000-000000000001',
  'HIRED',
  96.50,
  true,
  '{"skills": 29.5, "experience": 30.0, "coursework": 15.0, "languages": 10.0, "localization": 15.0}',
  '{
    "matched_criteria": {
      "skills": ["java", "spring boot", "angular", "postgresql"],
      "experience": 60,
      "coursework": ["bac+5", "master"],
      "languages": ["English", "French"],
      "localization": "Agadir"
    },
    "strengths": [
      "Maîtrise remarquable de la stack ciblée Spring Boot et Angular",
      "5 ans d''expérience solide dépassant largement les 36 mois requis",
      "Diplôme d''Ingénieur d''État hautement pertinent",
      "Localisation idéale à Agadir (prise de poste immédiate)",
      "Excellente maîtrise de l''anglais technique et du français"
    ],
    "weaknesses": []
  }',
  NOW() - INTERVAL '8 days',
  NOW() - INTERVAL '8 days' + INTERVAL '8 seconds'
) ON CONFLICT (candidate_id, offer_id) DO NOTHING;

-- A2. Loubna El Amrani (Score 92.00% >= 80% -> ADMISSIBLE, INTERVIEWING)
INSERT INTO candidate (id, first_name, last_name, email, phone)
VALUES ('91000000-0000-0000-0000-000000000002', 'Loubna', 'El Amrani', 'loubna.elamrani@example.com', '+212662998877')
ON CONFLICT (email) DO NOTHING;

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data, uploaded_at, processed_at)
VALUES (
  '92000000-0000-0000-0000-000000000002',
  '91000000-0000-0000-0000-000000000002',
  'resumes/CV_Loubna_ElAmrani.pdf',
  'CV_Loubna_ElAmrani_FullStack.pdf',
  'hash_loubna_elamrani_2',
  'SUCCESS',
  '{
    "candidate_info": {
      "first_name": "Loubna",
      "last_name": "El Amrani",
      "email": "loubna.elamrani@example.com",
      "phone": "+212662998877",
      "current_job_title": "Développeuse Full-Stack Java / Angular"
    },
    "description_markdown": "Développeuse avec 4 ans d''expérience en ingénierie logicielle chez des ESN de premier plan. Spécialisée dans les architectures orientées services et le développement d''interfaces réactives sous Angular.",
    "skills": ["Java", "Spring Boot", "Angular", "PostgreSQL", "Git", "Maven", "TailwindCSS"],
    "experience": 48,
    "coursework": ["Bac+5", "Master en Ingénierie du Web et Systèmes d''Information"],
    "languages": ["French", "English", "Arabic"],
    "localization": "Agadir"
  }',
  NOW() - INTERVAL '3 days',
  NOW() - INTERVAL '3 days' + INTERVAL '6 seconds'
) ON CONFLICT (checksum_sha256) DO NOTHING;

INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, passed_min_score, category_scores, extracted_matching, applied_at, scored_at)
VALUES (
  '93000000-0000-0000-0000-000000000002',
  '91000000-0000-0000-0000-000000000002',
  '22222222-2222-2222-2222-222222222222',
  '92000000-0000-0000-0000-000000000002',
  'INTERVIEWING',
  92.00,
  true,
  '{"skills": 28.0, "experience": 29.0, "coursework": 15.0, "languages": 10.0, "localization": 15.0}',
  '{
    "matched_criteria": {
      "skills": ["java", "spring boot", "angular", "postgresql"],
      "experience": 48,
      "coursework": ["bac+5", "master"],
      "languages": ["English", "French"],
      "localization": "Agadir"
    },
    "strengths": [
      "Profil équilibré front-end et back-end",
      "Très bonne expérience sur PostgreSQL et optimisation de requêtes SQL",
      "Mobilité immédiate sur Agadir"
    ],
    "weaknesses": []
  }',
  NOW() - INTERVAL '3 days',
  NOW() - INTERVAL '3 days' + INTERVAL '6 seconds'
) ON CONFLICT (candidate_id, offer_id) DO NOTHING;

-- A3. Ayoub Chraibi (Score 86.50% >= 80% -> ADMISSIBLE, SHORTLISTED)
INSERT INTO candidate (id, first_name, last_name, email, phone)
VALUES ('91000000-0000-0000-0000-000000000003', 'Ayoub', 'Chraibi', 'ayoub.chraibi@example.com', '+212663998877')
ON CONFLICT (email) DO NOTHING;

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data, uploaded_at, processed_at)
VALUES (
  '92000000-0000-0000-0000-000000000003',
  '91000000-0000-0000-0000-000000000003',
  'resumes/CV_Ayoub_Chraibi.pdf',
  'CV_Ayoub_Chraibi_Dev.pdf',
  'hash_ayoub_chraibi_3',
  'SUCCESS',
  '{
    "candidate_info": {
      "first_name": "Ayoub",
      "last_name": "Chraibi",
      "email": "ayoub.chraibi@example.com",
      "phone": "+212663998877",
      "current_job_title": "Développeur Logiciel Java Spring"
    },
    "description_markdown": "Développeur back-end expérimenté en Java, Spring Cloud et intégration front-end Angular.",
    "skills": ["Java", "Spring Boot", "Angular", "MySQL", "Docker", "REST"],
    "experience": 40,
    "coursework": ["Bac+5", "Master Informatique"],
    "languages": ["French", "Arabic", "English"],
    "localization": "Casablanca"
  }',
  NOW() - INTERVAL '14 days',
  NOW() - INTERVAL '14 days' + INTERVAL '7 seconds'
) ON CONFLICT (checksum_sha256) DO NOTHING;

INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, passed_min_score, category_scores, extracted_matching, applied_at, scored_at)
VALUES (
  '93000000-0000-0000-0000-000000000003',
  '91000000-0000-0000-0000-000000000003',
  '22222222-2222-2222-2222-222222222222',
  '92000000-0000-0000-0000-000000000003',
  'SHORTLISTED',
  86.50,
  true,
  '{"skills": 28.5, "experience": 27.0, "coursework": 15.0, "languages": 9.0, "localization": 7.0}',
  '{
    "matched_criteria": {
      "skills": ["java", "spring boot", "angular"],
      "experience": 40,
      "coursework": ["bac+5", "master"],
      "languages": ["English", "French"],
      "localization": "Casablanca"
    },
    "strengths": [
      "Solide expertise sur l''écosystème Spring Boot",
      "Excellentes bases algorithmiques"
    ],
    "weaknesses": [
      "Localisé à Casablanca : prévoir un accord pour le déménagement à Agadir"
    ]
  }',
  NOW() - INTERVAL '14 days',
  NOW() - INTERVAL '14 days' + INTERVAL '7 seconds'
) ON CONFLICT (candidate_id, offer_id) DO NOTHING;

-- A4. Nisrine Mouline (Score 85.00% >= 80% -> ADMISSIBLE, SHORTLISTED)
INSERT INTO candidate (id, first_name, last_name, email, phone)
VALUES ('91000000-0000-0000-0000-000000000021', 'Nisrine', 'Mouline', 'nisrine.mouline.dev@example.com', '+212664112233')
ON CONFLICT (email) DO NOTHING;

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data, uploaded_at, processed_at)
VALUES (
  '92000000-0000-0000-0000-000000000021',
  '91000000-0000-0000-0000-000000000021',
  'resumes/CV_Nisrine_Mouline.pdf',
  'CV_Nisrine_Mouline_FullStack.pdf',
  'hash_nisrine_mouline_21',
  'SUCCESS',
  '{
    "candidate_info": {
      "first_name": "Nisrine",
      "last_name": "Mouline",
      "email": "nisrine.mouline.dev@example.com",
      "phone": "+212664112233",
      "current_job_title": "Développeuse Full-Stack Java / Angular"
    },
    "description_markdown": "Ingénieure logicielle avec 3 ans d''expérience en conception d''applications d''entreprise Spring Boot et d''interfaces riches Angular.",
    "skills": ["Java", "Spring Boot", "Angular", "PostgreSQL", "Git", "REST"],
    "experience": 38,
    "coursework": ["Bac+5", "Master Génie Informatique"],
    "languages": ["French", "English", "Arabic"],
    "localization": "Rabat"
  }',
  NOW() - INTERVAL '9 days',
  NOW() - INTERVAL '9 days' + INTERVAL '6 seconds'
) ON CONFLICT (checksum_sha256) DO NOTHING;

INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, passed_min_score, category_scores, extracted_matching, applied_at, scored_at)
VALUES (
  '93000000-0000-0000-0000-000000000021',
  '91000000-0000-0000-0000-000000000021',
  '22222222-2222-2222-2222-222222222222',
  '92000000-0000-0000-0000-000000000021',
  'SHORTLISTED',
  85.00,
  true,
  '{"skills": 27.0, "experience": 26.0, "coursework": 15.0, "languages": 9.5, "localization": 7.5}',
  '{
    "matched_criteria": {
      "skills": ["java", "spring boot", "angular", "postgresql"],
      "experience": 38,
      "coursework": ["bac+5", "master"],
      "languages": ["English", "French"],
      "localization": "Rabat"
    },
    "strengths": [
      "Bonne maîtrise technique globale sur la pile Spring Boot et Angular",
      "Expérience reconnue sur des projets agiles en équipe distribuée"
    ],
    "weaknesses": [
      "Localisée à Rabat (prévoir organisation du déménagement à Agadir)"
    ]
  }',
  NOW() - INTERVAL '9 days',
  NOW() - INTERVAL '9 days' + INTERVAL '6 seconds'
) ON CONFLICT (candidate_id, offer_id) DO NOTHING;

-- A5. Karim Benjelloun (Score 84.00% >= 80% -> ADMISSIBLE, SHORTLISTED)
INSERT INTO candidate (id, first_name, last_name, email, phone)
VALUES ('91000000-0000-0000-0000-000000000022', 'Karim', 'Benjelloun', 'karim.benjelloun.fs@example.com', '+212665112233')
ON CONFLICT (email) DO NOTHING;

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data, uploaded_at, processed_at)
VALUES (
  '92000000-0000-0000-0000-000000000022',
  '91000000-0000-0000-0000-000000000022',
  'resumes/CV_Karim_Benjelloun.pdf',
  'CV_Karim_Benjelloun_Dev.pdf',
  'hash_karim_benjelloun_22',
  'SUCCESS',
  '{
    "candidate_info": {
      "first_name": "Karim",
      "last_name": "Benjelloun",
      "email": "karim.benjelloun.fs@example.com",
      "phone": "+212665112233",
      "current_job_title": "Ingénieur d''Études Full Stack Java & Angular"
    },
    "description_markdown": "Développeur passionné par l''ingénierie web moderne, architecture en couches et APIs REST performantes.",
    "skills": ["Java", "Spring Boot", "Angular", "MySQL", "Git"],
    "experience": 36,
    "coursework": ["Bac+5", "Master Informatique & Systèmes"],
    "languages": ["French", "English", "Arabic"],
    "localization": "Casablanca"
  }',
  NOW() - INTERVAL '7 days',
  NOW() - INTERVAL '7 days' + INTERVAL '5 seconds'
) ON CONFLICT (checksum_sha256) DO NOTHING;

INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, passed_min_score, category_scores, extracted_matching, applied_at, scored_at)
VALUES (
  '93000000-0000-0000-0000-000000000022',
  '91000000-0000-0000-0000-000000000022',
  '22222222-2222-2222-2222-222222222222',
  '92000000-0000-0000-0000-000000000022',
  'SHORTLISTED',
  84.00,
  true,
  '{"skills": 26.5, "experience": 25.5, "coursework": 15.0, "languages": 9.0, "localization": 8.0}',
  '{
    "matched_criteria": {
      "skills": ["java", "spring boot", "angular"],
      "experience": 36,
      "coursework": ["bac+5", "master"],
      "languages": ["English", "French"],
      "localization": "Casablanca"
    },
    "strengths": [
      "Expérience de 3 ans exactement conforme aux prérequis",
      "Solides compétences d''intégration front-end Angular"
    ],
    "weaknesses": [
      "Réside à Casablanca"
    ]
  }',
  NOW() - INTERVAL '7 days',
  NOW() - INTERVAL '7 days' + INTERVAL '5 seconds'
) ON CONFLICT (candidate_id, offer_id) DO NOTHING;

-- A6. Youssef Alami (Score 83.00% >= 80% -> ADMISSIBLE, NEW - Pending Recruiter Review)
INSERT INTO candidate (id, first_name, last_name, email, phone)
VALUES ('91000000-0000-0000-0000-000000000023', 'Youssef', 'Alami', 'youssef.alami.fullstack@example.com', '+212666112233')
ON CONFLICT (email) DO NOTHING;

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data, uploaded_at, processed_at)
VALUES (
  '92000000-0000-0000-0000-000000000023',
  '91000000-0000-0000-0000-000000000023',
  'resumes/CV_Youssef_Alami.pdf',
  'CV_Youssef_Alami.pdf',
  'hash_youssef_alami_23',
  'SUCCESS',
  '{
    "candidate_info": {
      "first_name": "Youssef",
      "last_name": "Alami",
      "email": "youssef.alami.fullstack@example.com",
      "phone": "+212666112233",
      "current_job_title": "Développeur Web & Mobile Full-Stack"
    },
    "description_markdown": "Développeur Full-Stack autonome ayant contribué à la mise en oeuvre d''architectures modulaires et réactives.",
    "skills": ["Java", "Spring Boot", "Angular", "TypeScript", "PostgreSQL"],
    "experience": 36,
    "coursework": ["Bac+5", "Ingénieur d''État en Informatique"],
    "languages": ["French", "English", "Arabic"],
    "localization": "Tanger"
  }',
  NOW() - INTERVAL '2 days',
  NOW() - INTERVAL '2 days' + INTERVAL '6 seconds'
) ON CONFLICT (checksum_sha256) DO NOTHING;

INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, passed_min_score, category_scores, extracted_matching, applied_at, scored_at)
VALUES (
  '93000000-0000-0000-0000-000000000023',
  '91000000-0000-0000-0000-000000000023',
  '22222222-2222-2222-2222-222222222222',
  '92000000-0000-0000-0000-000000000023',
  'NEW',
  83.00,
  true,
  '{"skills": 26.0, "experience": 25.0, "coursework": 14.5, "languages": 9.5, "localization": 8.0}',
  '{
    "matched_criteria": {
      "skills": ["java", "spring boot", "angular"],
      "experience": 36,
      "coursework": ["bac+5", "master"],
      "languages": ["English", "French"],
      "localization": "Tanger"
    },
    "strengths": [
      "Polyvalence technique appréciée",
      "Niveau d''anglais opérationnel"
    ],
    "weaknesses": [
      "Localisé à Tanger"
    ]
  }',
  NOW() - INTERVAL '2 days',
  NOW() - INTERVAL '2 days' + INTERVAL '6 seconds'
) ON CONFLICT (candidate_id, offer_id) DO NOTHING;

-- A7. Salma Bennis (Score 82.50% >= 80% -> ADMISSIBLE, NEW - Pending Review)
INSERT INTO candidate (id, first_name, last_name, email, phone)
VALUES ('91000000-0000-0000-0000-000000000024', 'Salma', 'Bennis', 'salma.bennis.dev@example.com', '+212667112233')
ON CONFLICT (email) DO NOTHING;

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data, uploaded_at, processed_at)
VALUES (
  '92000000-0000-0000-0000-000000000024',
  '91000000-0000-0000-0000-000000000024',
  'resumes/CV_Salma_Bennis.pdf',
  'CV_Salma_Bennis_FS.pdf',
  'hash_salma_bennis_24',
  'SUCCESS',
  '{
    "candidate_info": {
      "first_name": "Salma",
      "last_name": "Bennis",
      "email": "salma.bennis.dev@example.com",
      "phone": "+212667112233",
      "current_job_title": "Développeuse Java Spring / Front-End"
    },
    "description_markdown": "Développeuse avec une orientation forte vers le back-end et les bases de données relationnelles avancées.",
    "skills": ["Java", "Spring Boot", "PostgreSQL", "Docker", "Angular"],
    "experience": 36,
    "coursework": ["Bac+5", "Master MIAGE"],
    "languages": ["French", "English", "Arabic"],
    "localization": "Rabat"
  }',
  NOW() - INTERVAL '36 hours',
  NOW() - INTERVAL '36 hours' + INTERVAL '5 seconds'
) ON CONFLICT (checksum_sha256) DO NOTHING;

INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, passed_min_score, category_scores, extracted_matching, applied_at, scored_at)
VALUES (
  '93000000-0000-0000-0000-000000000024',
  '91000000-0000-0000-0000-000000000024',
  '22222222-2222-2222-2222-222222222222',
  '92000000-0000-0000-0000-000000000024',
  'NEW',
  82.50,
  true,
  '{"skills": 25.5, "experience": 25.0, "coursework": 15.0, "languages": 9.0, "localization": 8.0}',
  '{
    "matched_criteria": {
      "skills": ["java", "spring boot", "postgresql"],
      "experience": 36,
      "coursework": ["bac+5", "master"],
      "languages": ["English", "French"],
      "localization": "Rabat"
    },
    "strengths": [
      "Excellente maîtrise back-end Java / Spring Data JPA",
      "Maîtrise avancée des requêtes PostgreSQL"
    ],
    "weaknesses": [
      "Pratique de tests unitaires front-end Angular à parfaire"
    ]
  }',
  NOW() - INTERVAL '36 hours',
  NOW() - INTERVAL '36 hours' + INTERVAL '5 seconds'
) ON CONFLICT (candidate_id, offer_id) DO NOTHING;

-- A8. Mehdi Chafik (Score 81.50% >= 80% -> ADMISSIBLE, NEW - Pending Review)
INSERT INTO candidate (id, first_name, last_name, email, phone)
VALUES ('91000000-0000-0000-0000-000000000025', 'Mehdi', 'Chafik', 'mehdi.chafik.code@example.com', '+212668112233')
ON CONFLICT (email) DO NOTHING;

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data, uploaded_at, processed_at)
VALUES (
  '92000000-0000-0000-0000-000000000025',
  '91000000-0000-0000-0000-000000000025',
  'resumes/CV_Mehdi_Chafik.pdf',
  'CV_Mehdi_Chafik_Dev.pdf',
  'hash_mehdi_chafik_25',
  'SUCCESS',
  '{
    "candidate_info": {
      "first_name": "Mehdi",
      "last_name": "Chafik",
      "email": "mehdi.chafik.code@example.com",
      "phone": "+212668112233",
      "current_job_title": "Développeur Full-Stack Java / TypeScript"
    },
    "description_markdown": "Développeur agile aimant concevoir des architectures frontend modulaires et des microservices Spring Boot.",
    "skills": ["Java", "Angular", "TypeScript", "Docker", "PostgreSQL"],
    "experience": 35,
    "coursework": ["Bac+5", "Ingénieur Réseaux & Systèmes"],
    "languages": ["French", "English", "Arabic"],
    "localization": "Casablanca"
  }',
  NOW() - INTERVAL '20 hours',
  NOW() - INTERVAL '20 hours' + INTERVAL '5 seconds'
) ON CONFLICT (checksum_sha256) DO NOTHING;

INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, passed_min_score, category_scores, extracted_matching, applied_at, scored_at)
VALUES (
  '93000000-0000-0000-0000-000000000025',
  '91000000-0000-0000-0000-000000000025',
  '22222222-2222-2222-2222-222222222222',
  '92000000-0000-0000-0000-000000000025',
  'NEW',
  81.50,
  true,
  '{"skills": 25.0, "experience": 24.5, "coursework": 14.5, "languages": 9.5, "localization": 8.0}',
  '{
    "matched_criteria": {
      "skills": ["java", "angular", "docker"],
      "experience": 35,
      "coursework": ["bac+5", "master"],
      "languages": ["English", "French"],
      "localization": "Casablanca"
    },
    "strengths": [
      "Excellente rigueur de code sur TypeScript et Angular",
      "Expérience pratique avec la conteneurisation Docker"
    ],
    "weaknesses": [
      "Expérience de 35 mois très proche du seuil minimal"
    ]
  }',
  NOW() - INTERVAL '20 hours',
  NOW() - INTERVAL '20 hours' + INTERVAL '5 seconds'
) ON CONFLICT (candidate_id, offer_id) DO NOTHING;

-- A9. Kenza El Fassi (Score 80.50% >= 80% -> ADMISSIBLE, NEW - Applied within last 24h)
INSERT INTO candidate (id, first_name, last_name, email, phone)
VALUES ('91000000-0000-0000-0000-000000000026', 'Kenza', 'El Fassi', 'kenza.elfassi.fs@example.com', '+212669112233')
ON CONFLICT (email) DO NOTHING;

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data, uploaded_at, processed_at)
VALUES (
  '92000000-0000-0000-0000-000000000026',
  '91000000-0000-0000-0000-000000000026',
  'resumes/CV_Kenza_ElFassi.pdf',
  'CV_Kenza_ElFassi_Software.pdf',
  'hash_kenza_elfassi_26',
  'SUCCESS',
  '{
    "candidate_info": {
      "first_name": "Kenza",
      "last_name": "El Fassi",
      "email": "kenza.elfassi.fs@example.com",
      "phone": "+212669112233",
      "current_job_title": "Ingénieure Logicielle Full-Stack"
    },
    "description_markdown": "Ingénieure diplômée spécialisée dans le cycle de vie logiciel et les interfaces web réactives.",
    "skills": ["Java", "Spring Boot", "Angular", "Git", "PostgreSQL"],
    "experience": 34,
    "coursework": ["Bac+5", "Ingénieur d''État"],
    "languages": ["French", "English", "Arabic"],
    "localization": "Fès"
  }',
  NOW() - INTERVAL '14 hours',
  NOW() - INTERVAL '14 hours' + INTERVAL '4 seconds'
) ON CONFLICT (checksum_sha256) DO NOTHING;

INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, passed_min_score, category_scores, extracted_matching, applied_at, scored_at)
VALUES (
  '93000000-0000-0000-0000-000000000026',
  '91000000-0000-0000-0000-000000000026',
  '22222222-2222-2222-2222-222222222222',
  '92000000-0000-0000-0000-000000000026',
  'NEW',
  80.50,
  true,
  '{"skills": 25.0, "experience": 24.0, "coursework": 14.5, "languages": 9.0, "localization": 8.0}',
  '{
    "matched_criteria": {
      "skills": ["java", "spring boot", "angular"],
      "experience": 34,
      "coursework": ["bac+5", "master"],
      "languages": ["English", "French"],
      "localization": "Fès"
    },
    "strengths": [
      "Score admissible validant le seuil minimal de 80%",
      "Excellente formation académique d''ingénieur"
    ],
    "weaknesses": [
      "Localisée à Fès",
      "Expérience de 34 mois légèrement inférieure aux 36 mois ciblés"
    ]
  }',
  NOW() - INTERVAL '14 hours',
  NOW() - INTERVAL '14 hours' + INTERVAL '4 seconds'
) ON CONFLICT (candidate_id, offer_id) DO NOTHING;

-- A10. Hicham Belkadi (Score 76.50% < 80% -> SOUS SEUIL, NEW - Applied within last 24h)
INSERT INTO candidate (id, first_name, last_name, email, phone)
VALUES ('91000000-0000-0000-0000-000000000004', 'Hicham', 'Belkadi', 'hicham.belkadi@example.com', '+212664998877')
ON CONFLICT (email) DO NOTHING;

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data, uploaded_at, processed_at)
VALUES (
  '92000000-0000-0000-0000-000000000004',
  '91000000-0000-0000-0000-000000000004',
  'resumes/CV_Hicham_Belkadi.pdf',
  'CV_Hicham_Belkadi.pdf',
  'hash_hicham_belkadi_4',
  'SUCCESS',
  '{
    "candidate_info": {
      "first_name": "Hicham",
      "last_name": "Belkadi",
      "email": "hicham.belkadi@example.com",
      "phone": "+212664998877",
      "current_job_title": "Développeur Java Junior"
    },
    "description_markdown": "Développeur passionné avec 2 ans d''expérience en développement Java et Angular.",
    "skills": ["Java", "Spring Boot", "Angular", "PostgreSQL"],
    "experience": 24,
    "coursework": ["Bac+5", "Master Informatique"],
    "languages": ["French", "English", "Arabic"],
    "localization": "Agadir"
  }',
  NOW() - INTERVAL '12 hours',
  NOW() - INTERVAL '12 hours' + INTERVAL '5 seconds'
) ON CONFLICT (checksum_sha256) DO NOTHING;

INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, passed_min_score, category_scores, extracted_matching, applied_at, scored_at)
VALUES (
  '93000000-0000-0000-0000-000000000004',
  '91000000-0000-0000-0000-000000000004',
  '22222222-2222-2222-2222-222222222222',
  '92000000-0000-0000-0000-000000000004',
  'NEW',
  76.50,
  false,
  '{"skills": 25.0, "experience": 17.0, "coursework": 14.0, "languages": 8.0, "localization": 12.5}',
  '{
    "matched_criteria": {
      "skills": ["java", "spring boot", "angular", "postgresql"],
      "experience": 24,
      "coursework": ["bac+5", "master"],
      "languages": ["English", "French"],
      "localization": "Agadir"
    },
    "strengths": [
      "Bonne maîtrise technique des technologies fondamentales",
      "Réside à Agadir"
    ],
    "weaknesses": [
      "Expérience de 24 mois inférieure aux 36 mois demandés pour un poste Senior"
    ]
  }',
  NOW() - INTERVAL '12 hours',
  NOW() - INTERVAL '12 hours' + INTERVAL '5 seconds'
) ON CONFLICT (candidate_id, offer_id) DO NOTHING;

-- A11. Zineb Tazi (Score 71.00% < 80% -> SOUS SEUIL, FOLLOW_UP - Applied in Last 90d window)
INSERT INTO candidate (id, first_name, last_name, email, phone)
VALUES ('91000000-0000-0000-0000-000000000005', 'Zineb', 'Tazi', 'zineb.tazi.dev@example.com', '+212665998877')
ON CONFLICT (email) DO NOTHING;

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data, uploaded_at, processed_at)
VALUES (
  '92000000-0000-0000-0000-000000000005',
  '91000000-0000-0000-0000-000000000005',
  'resumes/CV_Zineb_Tazi.pdf',
  'CV_Zineb_Tazi_Web.pdf',
  'hash_zineb_tazi_5',
  'SUCCESS',
  '{
    "candidate_info": {
      "first_name": "Zineb",
      "last_name": "Tazi",
      "email": "zineb.tazi.dev@example.com",
      "phone": "+212665998877",
      "current_job_title": "Développeuse Web Full-Stack"
    },
    "description_markdown": "Développeuse spécialisée en intégration Angular et microservices Spring Boot.",
    "skills": ["Java", "Spring Boot", "Angular", "MongoDB"],
    "experience": 28,
    "coursework": ["Bac+5", "Master Ingénierie Logicielle"],
    "languages": ["French", "English"],
    "localization": "Marrakech"
  }',
  NOW() - INTERVAL '40 days',
  NOW() - INTERVAL '40 days' + INTERVAL '6 seconds'
) ON CONFLICT (checksum_sha256) DO NOTHING;

INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, passed_min_score, category_scores, extracted_matching, applied_at, scored_at)
VALUES (
  '93000000-0000-0000-0000-000000000005',
  '91000000-0000-0000-0000-000000000005',
  '22222222-2222-2222-2222-222222222222',
  '92000000-0000-0000-0000-000000000005',
  'FOLLOW_UP',
  71.00,
  false,
  '{"skills": 23.0, "experience": 18.0, "coursework": 14.0, "languages": 8.0, "localization": 8.0}',
  '{
    "matched_criteria": {
      "skills": ["java", "spring boot", "angular"],
      "experience": 28,
      "coursework": ["bac+5", "master"],
      "languages": ["English", "French"],
      "localization": "Marrakech"
    },
    "strengths": [
      "Bonne maîtrise d''Angular et TypeScript",
      "Formation académique de qualité"
    ],
    "weaknesses": [
      "Expérience PostgreSQL absente (expérience sur MongoDB)",
      "Localisation à Marrakech"
    ]
  }',
  NOW() - INTERVAL '40 days',
  NOW() - INTERVAL '40 days' + INTERVAL '6 seconds'
) ON CONFLICT (candidate_id, offer_id) DO NOTHING;

-- A12. Rachid Zeroual (Score 38.00% < 80% -> SOUS SEUIL, REJECTED - Applied in 2026 window)
INSERT INTO candidate (id, first_name, last_name, email, phone)
VALUES ('91000000-0000-0000-0000-000000000006', 'Rachid', 'Zeroual', 'rachid.zeroual@example.com', '+212666998877')
ON CONFLICT (email) DO NOTHING;

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data, uploaded_at, processed_at)
VALUES (
  '92000000-0000-0000-0000-000000000006',
  '91000000-0000-0000-0000-000000000006',
  'resumes/CV_Rachid_Zeroual.pdf',
  'CV_Rachid_Zeroual_Web.pdf',
  'hash_rachid_zeroual_6',
  'SUCCESS',
  '{
    "candidate_info": {
      "first_name": "Rachid",
      "last_name": "Zeroual",
      "email": "rachid.zeroual@example.com",
      "phone": "+212666998877",
      "current_job_title": "Intégrateur Web Débutant"
    },
    "description_markdown": "Développeur junior orienté WordPress et création de sites vitrines.",
    "skills": ["HTML", "CSS", "WordPress", "PHP"],
    "experience": 6,
    "coursework": ["Bac+2", "DUT"],
    "languages": ["French", "Arabic"],
    "localization": "Tanger"
  }',
  NOW() - INTERVAL '120 days',
  NOW() - INTERVAL '120 days' + INTERVAL '4 seconds'
) ON CONFLICT (checksum_sha256) DO NOTHING;

INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, passed_min_score, category_scores, extracted_matching, applied_at, scored_at)
VALUES (
  '93000000-0000-0000-0000-000000000006',
  '91000000-0000-0000-0000-000000000006',
  '22222222-2222-2222-2222-222222222222',
  '92000000-0000-0000-0000-000000000006',
  'REJECTED',
  38.00,
  false,
  '{"skills": 8.5, "experience": 6.0, "coursework": 8.0, "languages": 7.5, "localization": 8.0}',
  '{
    "matched_criteria": {
      "skills": [],
      "experience": 6,
      "coursework": ["bac+2"],
      "languages": ["French"],
      "localization": "Tanger"
    },
    "strengths": [
      "Bases solides en intégration HTML/CSS"
    ],
    "weaknesses": [
      "Compétences clés manquantes : Java, Spring Boot, Angular, PostgreSQL",
      "Niveau d''études Bac+2 insuffisant pour le poste Senior (Bac+5 requis)",
      "Expérience insuffisante (6 mois vs 36 mois requis)"
    ]
  }',
  NOW() - INTERVAL '120 days',
  NOW() - INTERVAL '120 days' + INTERVAL '4 seconds'
) ON CONFLICT (candidate_id, offer_id) DO NOTHING;


-- --------------------------------------------------------------------
-- B. Stage: Data Scientist (NLP / LLM) (33333333-3333-3333-3333-333333333333, Casablanca, min 70)
-- --------------------------------------------------------------------

-- B1. Salma Idrissi (Score 95.00% >= 70% -> ADMISSIBLE, HIRED)
INSERT INTO candidate (id, first_name, last_name, email, phone)
VALUES ('91000000-0000-0000-0000-000000000007', 'Salma', 'Idrissi', 'salma.idrissi.ai@example.com', '+212667998877')
ON CONFLICT (email) DO NOTHING;

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data, uploaded_at, processed_at)
VALUES (
  '92000000-0000-0000-0000-000000000007',
  '91000000-0000-0000-0000-000000000007',
  'resumes/CV_Salma_Idrissi.pdf',
  'CV_Salma_Idrissi_NLP.pdf',
  'hash_salma_idrissi_7',
  'SUCCESS',
  '{
    "candidate_info": {
      "first_name": "Salma",
      "last_name": "Idrissi",
      "email": "salma.idrissi.ai@example.com",
      "phone": "+212667998877",
      "current_job_title": "Élève Ingénieure en IA & Science des Données"
    },
    "description_markdown": "Étudiante en dernière année d''école d''ingénieurs d''état (ENSIAS), spécialisée en NLP, fine-tuning de modèles LLM et bibliothèques HuggingFace / PyTorch.",
    "skills": ["Python", "PyTorch", "FastAPI", "HuggingFace", "Spacy", "Transformers", "LangChain"],
    "experience": 0,
    "coursework": ["Bac+5", "Diplôme d''Ingénieur d''État en IA"],
    "languages": ["English", "French", "Arabic"],
    "localization": "Casablanca"
  }',
  NOW() - INTERVAL '10 days',
  NOW() - INTERVAL '10 days' + INTERVAL '9 seconds'
) ON CONFLICT (checksum_sha256) DO NOTHING;

INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, passed_min_score, category_scores, extracted_matching, applied_at, scored_at)
VALUES (
  '93000000-0000-0000-0000-000000000007',
  '91000000-0000-0000-0000-000000000007',
  '33333333-3333-3333-3333-333333333333',
  '92000000-0000-0000-0000-000000000007',
  'HIRED',
  95.00,
  true,
  '{"skills": 39.0, "experience": 10.0, "coursework": 25.0, "languages": 13.0, "localization": 8.0}',
  '{
    "matched_criteria": {
      "skills": ["python", "pytorch", "fastapi", "huggingface", "spacy"],
      "experience": 0,
      "coursework": ["bac+5", "engineering"],
      "languages": ["English", "French"],
      "localization": "Casablanca"
    },
    "strengths": [
      "Excellente maîtrise technique de PyTorch, Transformers et HuggingFace",
      "Projet de fin d''études sur l''extraction d''information à partir de documents non structurés",
      "Localisée à Casablanca, ville du stage",
      "Niveau d''anglais technique bilingue"
    ],
    "weaknesses": []
  }',
  NOW() - INTERVAL '10 days',
  NOW() - INTERVAL '10 days' + INTERVAL '9 seconds'
) ON CONFLICT (candidate_id, offer_id) DO NOTHING;

-- B2. Othmane Bennani (Score 88.00% >= 70% -> ADMISSIBLE, INTERVIEWING)
INSERT INTO candidate (id, first_name, last_name, email, phone)
VALUES ('91000000-0000-0000-0000-000000000008', 'Othmane', 'Bennani', 'othmane.bennani.nlp@example.com', '+212668998877')
ON CONFLICT (email) DO NOTHING;

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data, uploaded_at, processed_at)
VALUES (
  '92000000-0000-0000-0000-000000000008',
  '91000000-0000-0000-0000-000000000008',
  'resumes/CV_Othmane_Bennani.pdf',
  'CV_Othmane_Bennani.pdf',
  'hash_othmane_bennani_8',
  'SUCCESS',
  '{
    "candidate_info": {
      "first_name": "Othmane",
      "last_name": "Bennani",
      "email": "othmane.bennani.nlp@example.com",
      "phone": "+212668998877",
      "current_job_title": "Étudiant Master IA & Big Data"
    },
    "description_markdown": "Étudiant en Master Data Science avec de solides compétences en apprentissage automatique et traitement du langage naturel.",
    "skills": ["Python", "PyTorch", "FastAPI", "Scikit-Learn", "NLP"],
    "experience": 0,
    "coursework": ["Bac+5", "Master Data Science"],
    "languages": ["French", "English", "Arabic"],
    "localization": "Casablanca"
  }',
  NOW() - INTERVAL '5 days',
  NOW() - INTERVAL '5 days' + INTERVAL '6 seconds'
) ON CONFLICT (checksum_sha256) DO NOTHING;

INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, passed_min_score, category_scores, extracted_matching, applied_at, scored_at)
VALUES (
  '93000000-0000-0000-0000-000000000008',
  '91000000-0000-0000-0000-000000000008',
  '33333333-3333-3333-3333-333333333333',
  '92000000-0000-0000-0000-000000000008',
  'INTERVIEWING',
  88.00,
  true,
  '{"skills": 35.0, "experience": 10.0, "coursework": 23.0, "languages": 12.0, "localization": 8.0}',
  '{
    "matched_criteria": {
      "skills": ["python", "pytorch", "fastapi"],
      "experience": 0,
      "coursework": ["bac+5", "master"],
      "languages": ["English", "French"],
      "localization": "Casablanca"
    },
    "strengths": [
      "Bonne maîtrise des architectures de deep learning",
      "Expérience pratique avec FastAPI pour le déploiement d''APIs d''inférence"
    ],
    "weaknesses": []
  }',
  NOW() - INTERVAL '5 days',
  NOW() - INTERVAL '5 days' + INTERVAL '6 seconds'
) ON CONFLICT (candidate_id, offer_id) DO NOTHING;

-- B3. Amira Filali (Score 44.00% < 70% -> SOUS SEUIL, REJECTED)
INSERT INTO candidate (id, first_name, last_name, email, phone)
VALUES ('91000000-0000-0000-0000-000000000009', 'Amira', 'Filali', 'amira.filali@example.com', '+212669998877')
ON CONFLICT (email) DO NOTHING;

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data, uploaded_at, processed_at)
VALUES (
  '92000000-0000-0000-0000-000000000009',
  '91000000-0000-0000-0000-000000000009',
  'resumes/CV_Amira_Filali.pdf',
  'CV_Amira_Filali.pdf',
  'hash_amira_filali_9',
  'SUCCESS',
  '{
    "candidate_info": {
      "first_name": "Amira",
      "last_name": "Filali",
      "email": "amira.filali@example.com",
      "phone": "+212669998877",
      "current_job_title": "Étudiante Licence Informatique"
    },
    "description_markdown": "Étudiante en informatique générale intéressée par l''intelligence artificielle.",
    "skills": ["C++", "Java", "SQL"],
    "experience": 0,
    "coursework": ["Bac+3", "Licence Fondamentale"],
    "languages": ["French", "Arabic"],
    "localization": "Fès"
  }',
  NOW() - INTERVAL '70 days',
  NOW() - INTERVAL '70 days' + INTERVAL '5 seconds'
) ON CONFLICT (checksum_sha256) DO NOTHING;

INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, passed_min_score, category_scores, extracted_matching, applied_at, scored_at)
VALUES (
  '93000000-0000-0000-0000-000000000009',
  '91000000-0000-0000-0000-000000000009',
  '33333333-3333-3333-3333-333333333333',
  '92000000-0000-0000-0000-000000000009',
  'REJECTED',
  44.00,
  false,
  '{"skills": 12.0, "experience": 6.0, "coursework": 12.0, "languages": 8.0, "localization": 6.0}',
  '{
    "matched_criteria": {
      "skills": [],
      "experience": 0,
      "coursework": ["bac+3"],
      "languages": ["French"],
      "localization": "Fès"
    },
    "strengths": [
      "Motivation pour l''apprentissage des algorithmes d''IA"
    ],
    "weaknesses": [
      "Compétences requises absentes : Python, PyTorch, FastAPI, HuggingFace",
      "Niveau d''études Bac+3 inférieur au niveau Bac+5 requis",
      "Localisée à Fès (stage présentiel à Casablanca)"
    ]
  }',
  NOW() - INTERVAL '70 days',
  NOW() - INTERVAL '70 days' + INTERVAL '5 seconds'
) ON CONFLICT (candidate_id, offer_id) DO NOTHING;


-- --------------------------------------------------------------------
-- C. DevOps & Cloud Engineer (44444444-4444-4444-4444-444444444444, Rabat, min 75)
-- --------------------------------------------------------------------

-- C1. Nabil Cherkaoui (Score 95.00% >= 75% -> ADMISSIBLE, HIRED)
INSERT INTO candidate (id, first_name, last_name, email, phone)
VALUES ('91000000-0000-0000-0000-000000000010', 'Nabil', 'Cherkaoui', 'nabil.cherkaoui.cloud@example.com', '+212670998877')
ON CONFLICT (email) DO NOTHING;

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data, uploaded_at, processed_at)
VALUES (
  '92000000-0000-0000-0000-000000000010',
  '91000000-0000-0000-0000-000000000010',
  'resumes/CV_Nabil_Cherkaoui.pdf',
  'CV_Nabil_Cherkaoui_DevOps.pdf',
  'hash_nabil_cherkaoui_10',
  'SUCCESS',
  '{
    "candidate_info": {
      "first_name": "Nabil",
      "last_name": "Cherkaoui",
      "email": "nabil.cherkaoui.cloud@example.com",
      "phone": "+212670998877",
      "current_job_title": "Ingénieur DevOps & Cloud Certifié AWS"
    },
    "description_markdown": "Ingénieur infrastructure avec 4 ans d''expérience en conception de pipelines CI/CD, orchestration Kubernetes et automatisation Terraform sur AWS.",
    "skills": ["Docker", "Kubernetes", "AWS", "Terraform", "CI/CD", "Helm", "GitLab CI", "Prometheus"],
    "experience": 48,
    "coursework": ["Bac+5", "Ingénieur d''État en Systèmes & Réseaux"],
    "languages": ["French", "English", "Arabic"],
    "localization": "Rabat"
  }',
  NOW() - INTERVAL '15 days',
  NOW() - INTERVAL '15 days' + INTERVAL '7 seconds'
) ON CONFLICT (checksum_sha256) DO NOTHING;

INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, passed_min_score, category_scores, extracted_matching, applied_at, scored_at)
VALUES (
  '93000000-0000-0000-0000-000000000010',
  '91000000-0000-0000-0000-000000000010',
  '44444444-4444-4444-4444-444444444444',
  '92000000-0000-0000-0000-000000000010',
  'HIRED',
  95.00,
  true,
  '{"skills": 34.0, "experience": 25.0, "coursework": 15.0, "languages": 10.0, "localization": 11.0}',
  '{
    "matched_criteria": {
      "skills": ["docker", "kubernetes", "aws", "terraform", "ci/cd"],
      "experience": 48,
      "coursework": ["bac+5", "master"],
      "languages": ["English", "French"],
      "localization": "Rabat"
    },
    "strengths": [
      "Profil DevOps complet correspondant à 100% des critères",
      "Certifications AWS Solutions Architect et CKA validées",
      "Réside à Rabat"
    ],
    "weaknesses": []
  }',
  NOW() - INTERVAL '15 days',
  NOW() - INTERVAL '15 days' + INTERVAL '7 seconds'
) ON CONFLICT (candidate_id, offer_id) DO NOTHING;

-- C2. Houda Benjelloun (Score 89.50% >= 75% -> ADMISSIBLE, INTERVIEWING)
INSERT INTO candidate (id, first_name, last_name, email, phone)
VALUES ('91000000-0000-0000-0000-000000000011', 'Houda', 'Benjelloun', 'houda.benjelloun@example.com', '+212671998877')
ON CONFLICT (email) DO NOTHING;

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data, uploaded_at, processed_at)
VALUES (
  '92000000-0000-0000-0000-000000000011',
  '91000000-0000-0000-0000-000000000011',
  'resumes/CV_Houda_Benjelloun.pdf',
  'CV_Houda_Benjelloun.pdf',
  'hash_houda_benjelloun_11',
  'SUCCESS',
  '{
    "candidate_info": {
      "first_name": "Houda",
      "last_name": "Benjelloun",
      "email": "houda.benjelloun@example.com",
      "phone": "+212671998877",
      "current_job_title": "Ingénieure DevOps & Cloud"
    },
    "description_markdown": "Ingénieure Cloud spécialisée en conteneurisation Docker/Kubernetes et automatisation des déploiements.",
    "skills": ["Docker", "Kubernetes", "AWS", "CI/CD", "Linux", "Git"],
    "experience": 32,
    "coursework": ["Bac+5", "Master Télécoms & Réseaux"],
    "languages": ["French", "English", "Arabic"],
    "localization": "Rabat"
  }',
  NOW() - INTERVAL '6 days',
  NOW() - INTERVAL '6 days' + INTERVAL '6 seconds'
) ON CONFLICT (checksum_sha256) DO NOTHING;

INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, passed_min_score, category_scores, extracted_matching, applied_at, scored_at)
VALUES (
  '93000000-0000-0000-0000-000000000011',
  '91000000-0000-0000-0000-000000000011',
  '44444444-4444-4444-4444-444444444444',
  '92000000-0000-0000-0000-000000000011',
  'INTERVIEWING',
  89.50,
  true,
  '{"skills": 32.0, "experience": 23.0, "coursework": 14.5, "languages": 9.5, "localization": 10.5}',
  '{
    "matched_criteria": {
      "skills": ["docker", "kubernetes", "aws", "ci/cd"],
      "experience": 32,
      "coursework": ["bac+5", "master"],
      "languages": ["English", "French"],
      "localization": "Rabat"
    },
    "strengths": [
      "Excellente maîtrise des environnements conteneurisés",
      "Expérience démontrée sur AWS"
    ],
    "weaknesses": [
      "Compétence Terraform à approfondir"
    ]
  }',
  NOW() - INTERVAL '6 days',
  NOW() - INTERVAL '6 days' + INTERVAL '6 seconds'
) ON CONFLICT (candidate_id, offer_id) DO NOTHING;

-- C3. Walid Kadiri (Score 78.50% >= 75% -> ADMISSIBLE, NEW - Applied within last 24h)
INSERT INTO candidate (id, first_name, last_name, email, phone)
VALUES ('91000000-0000-0000-0000-000000000012', 'Walid', 'Kadiri', 'walid.kadiri.devops@example.com', '+212672998877')
ON CONFLICT (email) DO NOTHING;

INSERT INTO cv_file (id, candidate_id, storage_key, original_filename, checksum_sha256, extraction_status, extracted_data, uploaded_at, processed_at)
VALUES (
  '92000000-0000-0000-0000-000000000012',
  '91000000-0000-0000-0000-000000000012',
  'resumes/CV_Walid_Kadiri.pdf',
  'CV_Walid_Kadiri.pdf',
  'hash_walid_kadiri_12',
  'SUCCESS',
  '{
    "candidate_info": {
      "first_name": "Walid",
      "last_name": "Kadiri",
      "email": "walid.kadiri.devops@example.com",
      "phone": "+212672998877",
      "current_job_title": "Administrateur Systèmes & DevOps Junior"
    },
    "description_markdown": "Jeune diplômé d''école d''ingénieurs passionné par l''intégration continue et le cloud computing.",
    "skills": ["Docker", "AWS", "CI/CD", "Linux", "GitLab"],
    "experience": 20,
    "coursework": ["Bac+5", "Ingénieur Réseaux"],
    "languages": ["French", "English"],
    "localization": "Rabat"
  }',
  NOW() - INTERVAL '18 hours',
  NOW() - INTERVAL '18 hours' + INTERVAL '5 seconds'
) ON CONFLICT (checksum_sha256) DO NOTHING;

INSERT INTO application (id, candidate_id, offer_id, cv_file_id, status, total_score, passed_min_score, category_scores, extracted_matching, applied_at, scored_at)
VALUES (
  '93000000-0000-0000-0000-000000000012',
  '91000000-0000-0000-0000-000000000012',
  '44444444-4444-4444-4444-444444444444',
  '92000000-0000-0000-0000-000000000012',
  'NEW',
  78.50,
  true,
  '{"skills": 28.0, "experience": 18.0, "coursework": 14.0, "languages": 8.5, "localization": 10.0}',
  '{
    "matched_criteria": {
      "skills": ["docker", "aws", "ci/cd"],
      "experience": 20,
      "coursework": ["bac+5", "master"],
      "languages": ["English", "French"],
      "localization": "Rabat"
    },
    "strengths": [
      "Bonne maîtrise de Docker et des pipelines CI/CD",
      "Dynamisme et fort potentiel d''apprentissage"
    ],
    "weaknesses": [
      "Expérience pratique Kubernetes encore limitée"
    ]
  }',
  NOW() - INTERVAL '18 hours',
  NOW() - INTERVAL '18 hours' + INTERVAL '5 seconds'
) ON CONFLICT (candidate_id, offer_id) DO NOTHING;


-- ====================================================================
-- SECTION 4: AUDIT TRAIL (Workflow Status History for Multi-Stage Candidates)
-- ====================================================================

-- Tariq Benchekroun: NEW -> SHORTLISTED -> INTERVIEWING -> HIRED
INSERT INTO workflow_status_history (id, application_id, from_status, to_status, changed_by, changed_at)
VALUES 
(
  '94000000-0000-0000-0000-000000000001',
  '93000000-0000-0000-0000-000000000001',
  'NEW',
  'SHORTLISTED',
  '11111111-1111-1111-1111-111111111111',
  NOW() - INTERVAL '7 days'
),
(
  '94000000-0000-0000-0000-000000000002',
  '93000000-0000-0000-0000-000000000001',
  'SHORTLISTED',
  'INTERVIEWING',
  '11111111-1111-1111-1111-111111111111',
  NOW() - INTERVAL '5 days'
),
(
  '94000000-0000-0000-0000-000000000003',
  '93000000-0000-0000-0000-000000000001',
  'INTERVIEWING',
  'HIRED',
  '11111111-1111-1111-1111-111111111111',
  NOW() - INTERVAL '2 days'
)
ON CONFLICT DO NOTHING;

-- Loubna El Amrani: NEW -> SHORTLISTED -> INTERVIEWING
INSERT INTO workflow_status_history (id, application_id, from_status, to_status, changed_by, changed_at)
VALUES 
(
  '94000000-0000-0000-0000-000000000004',
  '93000000-0000-0000-0000-000000000002',
  'NEW',
  'SHORTLISTED',
  '11111111-1111-1111-1111-111111111111',
  NOW() - INTERVAL '2 days'
),
(
  '94000000-0000-0000-0000-000000000005',
  '93000000-0000-0000-0000-000000000002',
  'SHORTLISTED',
  'INTERVIEWING',
  '11111111-1111-1111-1111-111111111111',
  NOW() - INTERVAL '1 day'
)
ON CONFLICT DO NOTHING;

-- Salma Idrissi: NEW -> SHORTLISTED -> INTERVIEWING -> HIRED
INSERT INTO workflow_status_history (id, application_id, from_status, to_status, changed_by, changed_at)
VALUES 
(
  '94000000-0000-0000-0000-000000000006',
  '93000000-0000-0000-0000-000000000007',
  'NEW',
  'SHORTLISTED',
  '11111111-1111-1111-1111-111111111111',
  NOW() - INTERVAL '8 days'
),
(
  '94000000-0000-0000-0000-000000000007',
  '93000000-0000-0000-0000-000000000007',
  'SHORTLISTED',
  'INTERVIEWING',
  '11111111-1111-1111-1111-111111111111',
  NOW() - INTERVAL '5 days'
),
(
  '94000000-0000-0000-0000-000000000008',
  '93000000-0000-0000-0000-000000000007',
  'INTERVIEWING',
  'HIRED',
  '11111111-1111-1111-1111-111111111111',
  NOW() - INTERVAL '1 day'
)
ON CONFLICT DO NOTHING;

-- Nabil Cherkaoui: NEW -> SHORTLISTED -> INTERVIEWING -> HIRED
INSERT INTO workflow_status_history (id, application_id, from_status, to_status, changed_by, changed_at)
VALUES 
(
  '94000000-0000-0000-0000-000000000009',
  '93000000-0000-0000-0000-000000000010',
  'NEW',
  'SHORTLISTED',
  '11111111-1111-1111-1111-111111111111',
  NOW() - INTERVAL '12 days'
),
(
  '94000000-0000-0000-0000-000000000010',
  '93000000-0000-0000-0000-000000000010',
  'SHORTLISTED',
  'INTERVIEWING',
  '11111111-1111-1111-1111-111111111111',
  NOW() - INTERVAL '7 days'
),
(
  '94000000-0000-0000-0000-000000000011',
  '93000000-0000-0000-0000-000000000010',
  'INTERVIEWING',
  'HIRED',
  '11111111-1111-1111-1111-111111111111',
  NOW() - INTERVAL '3 days'
)
ON CONFLICT DO NOTHING;

-- --------------------------------------------------------------------
-- Workflow Calibrations for Senior Full-Stack Developer Funnel Integrity
-- --------------------------------------------------------------------

-- 1. Fix Sara Benali workflow history (Sara was REJECTED directly, never SHORTLISTED)
DELETE FROM workflow_status_history 
WHERE application_id = '22000000-0000-0000-0000-000000000002' 
  AND to_status = 'SHORTLISTED';

UPDATE workflow_status_history 
SET from_status = 'NEW' 
WHERE application_id = '22000000-0000-0000-0000-000000000002' 
  AND to_status = 'REJECTED';

-- 2. Upgrade Amine Tazi (V5) to INTERVIEWING (completing the 3 interviewing candidates)
UPDATE application 
SET status = 'INTERVIEWING' 
WHERE id = '12000000-0000-0000-0000-000000000001';

INSERT INTO workflow_status_history (id, application_id, from_status, to_status, changed_by, changed_at)
VALUES (
  '94000000-0000-0000-0000-000000000012',
  '12000000-0000-0000-0000-000000000001',
  'SHORTLISTED',
  'INTERVIEWING',
  '11111111-1111-1111-1111-111111111111',
  NOW() - INTERVAL '1 day'
) ON CONFLICT DO NOTHING;

-- 3. Ayoub Chraibi: NEW -> SHORTLISTED
INSERT INTO workflow_status_history (id, application_id, from_status, to_status, changed_by, changed_at)
VALUES (
  '94000000-0000-0000-0000-000000000013',
  '93000000-0000-0000-0000-000000000003',
  'NEW',
  'SHORTLISTED',
  '11111111-1111-1111-1111-111111111111',
  NOW() - INTERVAL '10 days'
) ON CONFLICT DO NOTHING;

-- 4. Nisrine Mouline: NEW -> SHORTLISTED
INSERT INTO workflow_status_history (id, application_id, from_status, to_status, changed_by, changed_at)
VALUES (
  '94000000-0000-0000-0000-000000000014',
  '93000000-0000-0000-0000-000000000021',
  'NEW',
  'SHORTLISTED',
  '11111111-1111-1111-1111-111111111111',
  NOW() - INTERVAL '6 days'
) ON CONFLICT DO NOTHING;

-- 5. Karim Benjelloun: NEW -> SHORTLISTED
INSERT INTO workflow_status_history (id, application_id, from_status, to_status, changed_by, changed_at)
VALUES (
  '94000000-0000-0000-0000-000000000015',
  '93000000-0000-0000-0000-000000000022',
  'NEW',
  'SHORTLISTED',
  '11111111-1111-1111-1111-111111111111',
  NOW() - INTERVAL '4 days'
) ON CONFLICT DO NOTHING;
