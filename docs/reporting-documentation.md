# Reporting & Analytics Feature Documentation

## 1. Functional Specifications & Business Logic

The SmartRecruit Reporting & Analytics module provides HR leadership, recruiters, and hiring managers with operational intelligence and executive insights into job campaign effectiveness, candidate conversion velocity, AI matching accuracy, and talent quality.

The module supports two core filtering dimensions across all metrics, charts, and exports:
1. **Offer Scoping (`offerId`)**: Enables granular performance analysis for a single job position or aggregate consolidated reporting across all organizational positions. When omitted or set to null, metrics are computed across all active and archived job offers.
2. **Analytical Period (`period`)**: Constrains data ingestion and conversion history to specific timeframes:
   - `LAST_30_DAYS` (`"30d"`): Trailing 30-day window from current UTC timestamp (`now.minusDays(30)`).
   - `LAST_90_DAYS` (`"90d"`): Trailing 90-day quarterly window (`now.minusDays(90)`).
   - `THIS_YEAR` (`"1y"`): From January 1st of the current calendar year up to present (`now.withDayOfYear(1)`).
   - `ALL` (`"all"`): Complete campaign lifecycle without temporal truncation (start date is `null`).

---

### 1.1. Executive Campaign Key Performance Indicators (KPIs)

The analytical summary row delivers four high-level operational metric cards calculated in real time:

1. **Volume de Campagne (Campaign Volume)**:
   - Primary metric: Total candidate applications received (`totalApplications`).
   - Secondary metric: Number of applications processed through the automated AI scoring pipeline (`screenedApplications`), alongside the screening completion percentage:
     $$\text{Screening Rate} = \frac{\text{screenedApplications}}{\text{totalApplications}} \times 100$$
2. **Qualité Talent (Talent Quality)**:
   - Primary metric: Arithmetic average of candidate evaluation scores (`averageScore`, 0–100%).
   - Secondary metric: Peak evaluation score achieved in the selected scope (`maxScore`).
3. **Sélectivité de l'Offre (Offer Selectivity)**:
   - Primary metric: The proportion of screened candidates meeting or exceeding the offer's minimum qualification score threshold (`passed_min_score = true`):
     $$\text{Qualification Rate} = \frac{\text{qualifiedCount}}{\text{totalApplications}} \times 100$$
   - Secondary metric: Absolute count of candidates passing the minimum score (`qualifiedCount`).
4. **Bilan des Recrutements (Recruitment Outcome)**:
   - Primary metric: Number of candidates hired (`hiredCount`), alongside the conversion-to-hire yield:
     $$\text{Conversion Rate} = \frac{\text{hiredCount}}{\text{totalApplications}} \times 100$$
   - Secondary metric: Number of unsuccessful/rejected applications (`rejectedCount`).
5. **Mathematical Safety**: All rate formulas include division-by-zero guards (`COALESCE` in SQL and zero-checks in Java) to safely return `0.0%` when a job offer has received zero applications.

---

### 1.2. Recruitment Conversion Funnel

The recruitment funnel provides visibility into candidate progression and drop-off across five sequential milestones:

```
[ Reçues (100%) ]
       │
       ▼
[ Admissibles IA ]
       │
       ▼
[ Présélectionnés ]
       │
       ▼
[ Entretiens ]
       │
       ▼
[ Recrutés ]
```

Each stage represents a subset or progressive drop-off from previous steps, governed by both current application status and historical workflow audit transitions:

1. **Reçues (Total Received)**:
   - All submitted applications within the selected scope. Base count represents $100\%$.
2. **Admissibles IA (AI Qualified)**:
   - Applications where the automated scoring algorithm validated that candidate qualifications satisfy job criteria:
     $$\text{Condition: } a.\text{passed\_min\_score} = \text{true}$$
3. **Présélectionnés (Shortlisted)**:
   - Candidates accepted into the recruitment pipeline. Includes applications currently in `SHORTLISTED`, `INTERVIEWING`, or `HIRED` status, or candidates having an audit record of passing through `SHORTLISTED`:
     $$\text{Condition: } a.\text{status} \in (\text{'SHORTLISTED'}, \text{'INTERVIEWING'}, \text{'HIRED'}) \lor \exists w \in \text{workflow\_history}: w.\text{to\_status} = \text{'SHORTLISTED'}$$
4. **Entretiens (Interviewing)**:
   - Candidates evaluated through formal interview stages. Includes current `INTERVIEWING` or `HIRED` records, or any application with an audit entry to `INTERVIEWING`:
     $$\text{Condition: } a.\text{status} \in (\text{'INTERVIEWING'}, \text{'HIRED'}) \lor \exists w \in \text{workflow\_history}: w.\text{to\_status} = \text{'INTERVIEWING'}$$
5. **Recrutés (Hired)**:
   - Final successful outcomes where employment offers were accepted:
     $$\text{Condition: } a.\text{status} = \text{'HIRED'}$$

Every stage computes its count and its relative conversion efficiency as a percentage of total applications received:
$$\text{Stage Percentage} = \frac{\text{Stage Count}}{\text{Total Received}} \times 100$$

---

### 1.3. AI Score Distribution

Candidate evaluation scores (ranging from 0.0 to 100.0) are bucketed into four distinct competence tiers:

* **Excellents ($\ge 85\%$)**: Top-tier profiles exhibiting near-perfect alignment with technical skills, experience tenure, and academic coursework requirements.
* **Qualifiés ($70\% - 84.9\%$)**: Strong candidate profiles satisfying all core job criteria with minor gaps.
* **Moyens ($50\% - 69.9\%$)**: Profiles with partial qualification overlap or transferable experience below desired seniority thresholds.
* **Insuffisants ($< 50\%$)**: Profiles with significant mismatch against mandatory job criteria.

In the web interface, this distribution is rendered using an interactive Chart.js doughnut chart, highlighting candidate distribution across competency tiers.

---

### 1.4. Top Candidates Leaderboard & Ranking

The reporting interface presents a deterministic ranking of top candidates for the active offer or consolidated scope:

* **Sorting Hierarchy**: Candidate records are sorted strictly by `a.total_score DESC NULLS LAST`, followed by `a.applied_at ASC` (earliest submission takes precedence for tied scores).
* **Projected Attributes & Columns**:
  - **Rank**: Consecutive ordinal position (`#1`, `#2`, `#3` highlighted with visual medal badges).
  - **Candidate Identity**: Full name, email address, and phone number.
  - **Job Title**: Associated position title.
  - **Global Match Score**: Numeric percentage (e.g., `96.5%`).
  - **Admissibility**: Badge indicating whether the score satisfies the offer's minimum threshold (`"Admissible"` vs `"Sous le seuil"`).
  - **Current Status**: Localized workflow stage (`Nouveau`, `Présélectionné`, `En Entretien`, `Relance`, `Recruté`, `Rejeté`, `Archivé`).
  - **Submission Date**: Parsed timestamp (`applied_at`) formatted as `DD/MM/YYYY` in the UI table (and `yyyy-MM-dd HH:mm` in Excel).
  - **Action Link**: Direct profile link button navigating to `/hr/candidates/:applicationId`.
  - **Category Subscores (Payload & Excel)**: Subscore breakdowns (Skills, Experience, Education/Coursework, Languages) are provided in the DTO payload (`categoryScores`) and formatted into dedicated Excel export columns.
* **Deep-Link Navigation**: Clicking any candidate row or candidate name navigates directly to `/hr/candidates/:applicationId`, allowing recruiters to inspect the full resume, extracted JSON criteria, and interview notes.

---

## 2. Architecture & Technical Optimizations

```
┌───────────────────────────────────────────────────────────────────────┐
│                           Angular 17+ Frontend                        │
│   ReportingComponent  ◄──►  ReportingService (RxJS / Binary Blobs)   │
└───────────────────────────────────┬───────────────────────────────────┘
                                    │ HTTP REST
┌───────────────────────────────────▼───────────────────────────────────┐
│                      Spring Boot REST Controller                      │
│                          ReportingController                          │
└───────────────┬───────────────────┬───────────────────┬───────────────┘
                │                   │                   │
┌───────────────▼──────────┐ ┌──────▼─────────────┐ ┌───▼───────────────┐
│     ReportingService     │ │ ExcelExportService │ │  PdfExportService │
│ (Aggregation & Metrics)  │ │   (Apache POI)     │ │     (OpenPDF)     │
└───────────────┬──────────┘ └────────────────────┘ └───────────────────┘
                │ Native SQL Projections
┌───────────────▼───────────────────────────────────────────────────────┐
│               PostgreSQL Database (ReportingRepository)               │
│      application  •  candidate  •  offer  •  workflow_history         │
└───────────────────────────────────────────────────────────────────────┘
```

### 2.1. Backend: Single-Pass Native Projections

To guarantee high responsiveness under heavy database loads (tens of thousands of applications), the reporting module avoids hydrating JPA entities or performing in-memory aggregations:

1. **PostgreSQL `FILTER (WHERE ...)` Clauses**:
   - Instead of running multiple round-trips or loading collections into JVM memory, metrics are computed inside PostgreSQL using `COUNT(*) FILTER (WHERE ...)` and `AVG(...) FILTER (WHERE ...)`.
   - Key performance statistics (KPIs), conversion funnels, and score distributions are each computed in a single SQL query pass.
2. **Consolidated REST Payload (`GET /api/v1/reporting/stats`)**:
   - Rather than making four separate HTTP requests (`/kpis`, `/funnel`, `/distribution`, `/top-candidates`), the backend aggregates all four analytical blocks into a single unified `ReportingDashboardResponseDto`. This eliminates UI flickering, network latency, and connection pool saturation.
3. **Exact Runtime JDBC Type Handling**:
   - In native SQL projections, PostgreSQL `TIMESTAMPTZ` (`a.applied_at`) is returned by the JDBC driver as a `java.time.Instant`.
   - `ReportingService` converts this exact runtime type directly to UTC `OffsetDateTime` (`instant.atOffset(ZoneOffset.UTC)`), guaranteeing that dates are correctly populated across web payloads and Excel cells.

### 2.2. Frontend: Reactive State Management & Memory-Safe Downloads

The frontend is built on Angular 17+ standalone architecture:

- **Reactive State Pipeline**: Triggered automatically whenever the user changes the selected job offer or period filter.
- **Binary Streaming**: Export requests handle binary `Blob` responses with memory-safe object URLs (`URL.createObjectURL(blob)`), programmatic download invocation, and immediate URL revocation (`URL.revokeObjectURL`) to prevent browser memory leaks.
- **Resilient Fallbacks**: Integrated skeleton loaders during asynchronous data fetching, graceful error state handling, and contextual empty illustrations when no applications match the selected criteria.

---

## 3. Enterprise Export Engines

The reporting module provides automated, on-demand export capabilities in two standard business formats.

### 3.1. Excel Workbook Export (`ExcelExportService`)

Built with **Apache POI** (`poi-ooxml`), the Excel export streams candidate data into structured `.xlsx` workbooks:

* **In-Memory Streaming**: Generates the complete binary workbook in memory via `ByteArrayOutputStream` without writing temporary files to server disks.
* **Worksheet Layout**:
  - Row 0: Frozen header row with column titles.
  - Rows 1..N: Candidate records with alternating zebra row styling for visual contrast.
* **Columns Included**:
  1. Rang (Numeric Rank)
  2. Nom Complet (Full Name)
  3. Email
  4. Téléphone (Phone Number)
  5. Intitulé de l'Offre (Position Title)
  6. Date Candidature (Formatted `yyyy-MM-dd HH:mm`)
  7. Statut (Localized workflow status label)
  8. Score Global IA (%) (Numeric decimal score)
  9. Admissible (Binary "Oui" / "Non" indicator)
  10. Score Compétences (Skills category subscore)
  11. Score Expérience (Experience category subscore)
  12. Score Formation (Education/Coursework category subscore)
  13. Score Langues (Languages category subscore)
* **Numeric Data Formatting**: Match scores and subscores are stored as numeric cells (`Double`), allowing HR analysts to perform native Excel formulas (`SUM`, `AVERAGE`, sorting, and conditional formatting) directly upon opening.
* **Auto-Sized Columns**: All 13 columns are programmatically fitted to content dimensions with sensible minimum safety margins.

### 3.2. Executive Synthesis PDF Report (`PdfExportService`)

Built with **OpenPDF**, the PDF export engine produces high-resolution, executive-ready A4 portrait documents:

* **Executive Header**: Organization branding, document title, target campaign identification, generation date, and active time-window metadata.
* **KPI Metric Grid**: Four high-visibility metric cards summarizing Total Candidates, AI Qualification Rate, Screened Rate, and Average Evaluation Score.
* **Recruitment Funnel Synthesis**: A visual table displaying candidate volumes across each of the 5 conversion milestones alongside stage-by-stage conversion efficiency percentages.
* **Score Distribution Summary**: Structured breakdown of candidate counts across the four competency tiers (Excellents, Qualifiés, Moyens, Insuffisants).
* **Top 10 Ranked Candidates Table**: Structured summary of top candidates including Rank (`#1`, `#2`, ...), Candidate Name, Email, Position Title, Score percentage, and Workflow Status.
* **Standardized Dynamic Footer**: A dedicated PDF page event listener (`PdfFooterHelper`) prints a confidentiality notice (`"Confidentiel - Usage interne SmartRecruit Platform"`) and dynamic page numbering (`"Page X"`) across all document pages.

### 3.3. Filename Normalization & Slugs

Export filenames are generated dynamically using ASCII normalization (`Normalizer.normalize`) and hyphen slugification:
- Clean ASCII transformation converts `"Consolidé (Toutes les offres)"` to `consolide-toutes-les-offres`.
- Excel filename pattern: `reporting-candidats-[slug].xlsx`
- PDF filename pattern: `rapport-synthese-[slug].pdf`
- Headers: `Content-Disposition: attachment; filename="reporting-candidats-[slug].xlsx"`

---

## 4. REST API Reference

All endpoints are rooted under `/api/v1/reporting` and secured by Spring Security RBAC.

### Authorization Matrix

| Endpoint | Method | Permitted Roles | Description |
| :--- | :---: | :---: | :--- |
| `/api/v1/reporting/stats` | `GET` | `HR_ADMIN`, `RECRUITER`, `VIEWER` | Unified analytical dashboard payload |
| `/api/v1/reporting/candidates` | `GET` | `HR_ADMIN`, `RECRUITER`, `VIEWER` | Filtered candidate ranking dataset |
| `/api/v1/reporting/export/excel` | `GET` | `HR_ADMIN`, `RECRUITER`, `VIEWER` | Binary stream of styled Excel `.xlsx` |
| `/api/v1/reporting/export/pdf` | `GET` | `HR_ADMIN`, `RECRUITER`, `VIEWER` | Binary stream of executive PDF `.pdf` |

---

### Endpoint: `GET /api/v1/reporting/stats`

Retrieves the complete analytical payload for the dashboard.

#### Query Parameters
- `offerId` *(UUID, optional)*: Filter by job position. When omitted or null, returns consolidated metrics across all company offers.
- `period` *(String, optional, default: `"ALL"`)*: Analytical timeframe (`"30d"`, `"90d"`, `"1y"`, `"ALL"`).

#### Response Schema (`200 OK`)
```json
{
  "kpis": {
    "totalApplications": 17,
    "screenedApplications": 14,
    "screenedRate": 82.4,
    "averageScore": 79.1,
    "maxScore": 96.5,
    "qualifiedCount": 10,
    "qualificationRate": 58.8,
    "hiredCount": 1,
    "conversionRate": 5.9,
    "rejectedCount": 2
  },
  "funnel": [
    { "stage": "Reçues", "count": 17, "percentage": 100.0 },
    { "stage": "Admissibles IA", "count": 10, "percentage": 58.8 },
    { "stage": "Présélectionnés", "count": 6, "percentage": 35.3 },
    { "stage": "Entretiens", "count": 3, "percentage": 17.6 },
    { "stage": "Recrutés", "count": 1, "percentage": 5.9 }
  ],
  "scoreDistribution": {
    "excellentCount": 3,
    "qualifiedCount": 7,
    "moderateCount": 2,
    "insufficientCount": 2
  },
  "topCandidates": [
    {
      "rank": 1,
      "applicationId": "93000000-0000-0000-0000-000000000001",
      "candidateId": "91000000-0000-0000-0000-000000000001",
      "fullName": "Tariq Benchekroun",
      "email": "tariq.benchekroun@example.com",
      "phone": "+212661998877",
      "offerTitle": "Senior Full-Stack Developer",
      "totalScore": 96.5,
      "isAdmissible": true,
      "status": "HIRED",
      "appliedAt": "2026-08-30T10:15:00Z",
      "categoryScores": {
        "skills": 29.5,
        "experience": 30.0,
        "coursework": 15.0,
        "languages": 10.0,
        "localization": 15.0
      }
    }
  ]
}
```

---

### Endpoint: `GET /api/v1/reporting/candidates`

Retrieves candidate rankings with optional limit filtering.

#### Query Parameters
- `offerId` *(UUID, optional)*: Filter by job position.
- `period` *(String, optional, default: `"ALL"`)*: Analytical timeframe (`"30d"`, `"90d"`, `"1y"`, `"ALL"`).
- `limit` *(int, optional, default: `0`)*: Maximum rows to return (`0` returns all matching rows).

#### Response (`200 OK`)
Returns an array of `CandidateReportRowDto` matching the schema shown above in `topCandidates`.

---

### Endpoint: `GET /api/v1/reporting/export/excel`

Streams a structured Excel workbook.

#### Query Parameters & Headers
- `offerId` *(UUID, optional)*: Filter by job position.
- `period` *(String, optional, default: `"ALL"`)*: Analytical timeframe.
- `limit` *(int, optional, default: `0`)*: Maximum candidate rows (0 for unpaged).
- `timezone` *(String, optional)*: Client timezone identifier (e.g. `"Africa/Casablanca"`).
- `X-Timezone` *(Header, optional)*: Alternative client timezone header.

#### Response Headers
- `Content-Type`: `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`
- `Content-Disposition`: `attachment; filename="reporting-candidats-[slug].xlsx"`

---

### Endpoint: `GET /api/v1/reporting/export/pdf`

Streams an executive summary PDF report.

#### Query Parameters & Headers
- `offerId` *(UUID, optional)*: Filter by job position.
- `period` *(String, optional, default: `"ALL"`)*: Analytical timeframe.
- `timezone` *(String, optional)*: Client timezone identifier (e.g. `"Africa/Casablanca"`).
- `X-Timezone` *(Header, optional)*: Alternative client timezone header.

#### Response Headers
- `Content-Type`: `application/pdf`
- `Content-Disposition`: `attachment; filename="rapport-synthese-[slug].pdf"`

---

## 5. Database Schema Dependencies & Auditing

For the reporting module to compute metrics accurately, the underlying database schema requires specific fields to be maintained by upstream platform modules:

> [!IMPORTANT]
> If any of the following fields are omitted or left unpopulated during candidate ingestion or workflow transitions, reporting metrics will be skewed.

### 5.1. Entity: `Application` (`application` table)
* **`passed_min_score` (Boolean)**:
  - Required by the AI Qualification Rate and Recruitment Funnel stage 2 ("Admissibles IA").
  - Must be explicitly set to `true` or `false` once automated CV scoring concludes. Never leave `null` for screened profiles.
* **`total_score` (Double / Numeric)**:
  - Required for Score Distribution categorization, Average Candidate Score, Maximum Score, and Leaderboard ranking.
* **`applied_at` (TIMESTAMPTZ)**:
  - Mandatory timestamp tracking when the candidate submitted their application. Used for all temporal period filtering (`30d`, `90d`, `1y`) and candidate table dates.
* **`category_scores` (JSONB)**:
  - Key-value breakdown of subscores (skills, experience, coursework, languages). Exported into individual Excel columns and visualized in candidate files.

### 5.2. Entity: `WorkflowStatusHistory` (`workflow_status_history` table)
* **Pipeline Audit Trail**:
  - The Recruitment Funnel relies on historical state transitions to count candidates who successfully passed through intermediate stages (e.g. `SHORTLISTED` or `INTERVIEWING`) even if their current terminal status is `HIRED` or `REJECTED`.
  - Upstream workflow services **must** record every status transition into `workflow_status_history` with the appropriate `from_status`, `to_status`, `changed_by`, and `changed_at` timestamp.

---

## 6. Testing & Quality Assurance

The reporting feature is validated by comprehensive automated test suites covering nominal, edge-case, and security paths.

### 6.1. Unit & Service Tests (`ReportingServiceTest`)
* **Zero-Division Safety**: Validates that querying empty campaigns returns `0.0%` rates, zero counters, and empty lists without `ArithmeticException` or `NaN` values.
* **Accurate Metric Calculations**: Confirms that percentage conversions, averages, maximums, and funnel drop-offs accurately reflect test datasets.
* **Runtime Type Mapping**: Verifies that `java.time.Instant` values returned from PostgreSQL native queries are properly mapped to UTC `OffsetDateTime` without losing submission dates.

### 6.2. Export Service Tests
* **`ExcelExportServiceTest`**: Validates POI workbook generation, sheet creation, column header names, numeric score cell types, date cell formatting, and binary stream size.
* **`PdfExportServiceTest`**: Validates OpenPDF document creation, metric card rendering, conversion funnel table integration, and non-empty byte stream generation.

### 6.3. Security & Controller Integration Tests (`ReportingControllerTest`)
* **Role-Based Access Control**:
  - Confirms `200 OK` responses for authorized roles: `ROLE_HR_ADMIN`, `ROLE_RECRUITER`, and `ROLE_VIEWER`.
  - Asserts `403 Forbidden` for unauthorized roles (e.g., standard employees or candidates).
  - Asserts `401 Unauthorized` for unauthenticated requests.
* **Export Streaming Headers**: Confirms accurate MIME types (`application/pdf`, `application/vnd.openxmlformats-...`) and RFC-5987 attachment header formatting.

---

## 7. Demo Seed Dataset & Migration (`V9__reporting_seed_data.sql` / `v7.sql`)

To facilitate realistic end-to-end demonstrations, user acceptance testing (UAT), and comprehensive validation of all analytical components (KPI cards, conversion funnel, score distribution, candidate rankings, and binary exports), the platform includes a rich seed data migration script:
* **Script Location**: `backend/src/main/resources/db/migration/V9__reporting_seed_data.sql`
* **Historical Identifier**: Initially designed as `V7__reporting_seed_data.sql` (and commonly referred to as `v7.sql`), renumbered to Flyway version `V9` to avoid version collision with pre-existing migration scripts.

The migration is partitioned into four functional sections:

### 7.1. Section 1: Offer Modernization (Real-World Tech Roles)
The script upgrades 15 generic placeholders (`Position Title 1..15`) inherited from initial schemas into realistic technology roles matching Norsys Africa's business activities:
* **Concrete Positions**:
  - *Tech Lead Java / Spring Boot* (Casablanca, CDI, threshold: 80%)
  - *Développeur Front-End Angular Senior* (Agadir, CDI, threshold: 78%)
  - *Architecte Solutions Cloud AWS* (Rabat, CDI, threshold: 82%)
  - *Consultant Cybersécurité & SecOps* (Casablanca, CDI, threshold: 75%)
  - *Ingénieur QA & Automatisation des Tests* (Agadir, CDI, threshold: 70%)
  - *Product Owner - Solutions Digitales*, *Mobile Flutter / React Native*, *Data Engineer & Analytics*, *Scrum Master*, etc.
* **Criterion Weightings & Schemas**: Each offer configures normalized weights across 5 evaluation pillars (`skills`, `experience`, `coursework`, `languages`, `localization`), mandatory requirements, and minimum qualification score thresholds (`min_score`).

### 7.2. Section 2: Candidate Identity Upgrade & Contact Enrichment
Generic candidate records are upgraded to authentic Moroccan professional profiles:
* **Identity & Localized Contacts**: Replaces placeholder names with authentic identities (e.g., *Omar Idrissi*, *Zineb Chraibi*, *Hamza El Fassi*, *Salma Kabbaj*, *Tarik Alami*, *Nadia Chaoui*, *Reda Mansouri*) and Moroccan cellular numbering (`+2126...`).
* **Structured Evaluation Breakdown**: Injects rich JSONB payloads into `application.category_scores` containing component scores, qualitative `strengths` (e.g., *Excellente maîtrise de Spring Boot*, *Solide expérience en Clean Architecture*), and targeted `weaknesses` (e.g., *Expérience légèrement inférieure au seuil souhaité*).

### 7.3. Section 3: High-Fidelity Candidate Profiles & Temporal Period Distribution
Section 3 injects complete candidate profiles into the core demonstration offers (notably the *Senior Full-Stack Developer* campaign in Agadir):
* **Complete Resume Extractions (`cv_file`)**: Populates `extracted_data` with detailed JSON structures including candidate personal information, academic coursework, professional employment tenures, core technical skills, and certifications.
* **Score Tier Calibration**: Injects profiles spanning all 4 analytical competence tiers to guarantee full visual representation in the Score Distribution doughnut chart:
  - **Excellents ($\ge 85\%$)**: e.g., Tariq Benchekroun (96.5%), Loubna El Amrani (91.0%).
  - **Qualifiés ($70\% - 84.9\%$)**: e.g., Ayoub Chraibi (83.5%), Nisrine Mouline (78.0%).
  - **Moyens ($50\% - 69.9\%$)**: e.g., Youssef Alami (64.0%).
  - **Insuffisants ($< 50\%$)**: e.g., Othmane Bennani (42.0%).
* **Temporal Distribution Strategy**: Candidate submission dates (`applied_at`) are intentionally distributed across temporal intervals to validate each analytical timeframe filter in the UI:
  - *Trailing 24 Hours* (`NOW() - INTERVAL '14 hours'`): Demonstrates real-time ingestion.
  - *Trailing 7 Days* (`NOW() - INTERVAL '3 days'`): Validates weekly volume shifts.
  - *Trailing 30 Days (`30d`)* (`NOW() - INTERVAL '15 days'`): Populates the 30-day filter window.
  - *Trailing 90 Days (`90d`)* (`NOW() - INTERVAL '45 days'`): Populates quarterly reporting.
  - *This Year / Older (`1y` / `ALL`)* (`NOW() - INTERVAL '120 days'`): Provides full-year historical data.

### 7.4. Section 4: Workflow Audit History & Funnel Integrity
The recruitment funnel relies on SQL audit logs to track candidate progression. Section 4 populates sequential state transitions in `workflow_status_history`:
* **Multi-Stage Audit Transitions**:
  - Example (Tariq Benchekroun): `NEW` $\rightarrow$ `SHORTLISTED` (at day -7) $\rightarrow$ `INTERVIEWING` (at day -5) $\rightarrow$ `HIRED` (at day -2).
  - Example (Loubna El Amrani): `NEW` $\rightarrow$ `SHORTLISTED` (at day -8) $\rightarrow$ `INTERVIEWING` (at day -4).
* **Enforcing Mathematical Monotonicity**:
  - In a recruitment funnel, milestone counts must strictly follow sequential drop-off rules. Without audit history, candidates who move to terminal stages (`HIRED` or `INTERVIEWING`) would disappear from intermediate counts (`SHORTLISTED`), creating illogical metrics (e.g., more interviewees than shortlisted candidates).
  - By inserting historical transition records (`to_status = 'SHORTLISTED'`, `to_status = 'INTERVIEWING'`), the funnel query evaluates both current status and historical passage:
    $$\text{Received} \ge \text{Admissibles IA} \ge \text{Présélectionnés} \ge \text{Entretiens} \ge \text{Recrutés}$$
  - This ensures that for any selected job campaign, every conversion stage displays logically sound, non-inverting conversion yields.

