// ============================================================================
// NORSYS RECRUITMENT CONFIGURATION & PRESETS (EASY EDIT SECTION)
// ============================================================================

export type ContractType = 'CDI' | 'CDD' | 'Stage' | 'Autre';

export interface ContractTypeOption {
  value: ContractType;
  label: string;
}

/** Types de contrats disponibles pour Norsys */
export const CONTRACT_TYPES: ContractTypeOption[] = [
  { value: 'CDI', label: 'CDI (Durée Indéterminée)' },
  { value: 'CDD', label: 'CDD (Durée Déterminée)' },
  { value: 'Stage', label: 'Stage' },
  { value: 'Autre', label: 'Autre' },
];

/** Villes et modalités de travail cibles pour Norsys Maroc */
export const COMMON_LOCATIONS: readonly string[] = [
  'Agadir',
  'Marrakech',
  'Full Remote / Télétravail 100%',
  'Hybride - Agadir',
  'Hybride - Marrakech',
];

/** Langues courantes requises */
export const COMMON_LANGUAGES: readonly string[] = ['Français', 'Anglais', 'Français & Anglais'];

/** Diplômes et niveaux académiques courants */
export const COMMON_DEGREES: readonly string[] = [
  'Bac+2 (BTS, DUT, DEUG)',
  'Bac+3 (Licence, Bachelor)',
  "Bac+5 (Master, Diplôme d'Ingénieur)",
  "Diplôme d'Ingénieur d'État",
  'Master en Informatique / Ingénierie Logicielle',
  'Master en Systèmes & Réseaux / Cybersécurité',
  'Master en Data Science / Intelligence Artificielle',
  'Master en Management / Commerce',
  'Doctorat / PhD',
];

/** Compétences techniques courantes (suggestions rapides) */
export const COMMON_TECH_SKILLS: readonly string[] = [
  'Java',
  'Spring Boot',
  'Angular',
  'TypeScript',
  'React',
  'Node.js',
  'Python',
  'Docker',
  'Kubernetes',
  'PostgreSQL',
  'MongoDB',
  'RabbitMQ',
  'Kafka',
  'Git',
  'CI/CD',
  'AWS',
  'Azure',
  'GCP',
  'Linux',
  'REST APIs',
];

// ============================================================================
// DTOs & BACKEND DATA CONTRACTS
// ============================================================================

export type OfferStatus = 'DRAFT' | 'ACTIVE' | 'CLOSED';
export type OfferAiStatus = 'PENDING' | 'STALLED' | 'SUCCESS' | 'FAILED';

export interface CategoryWeights {
  skills: number;
  experience: number;
  coursework: number;
  languages: number;
  localization: number;
  [key: string]: number | undefined;
}

export interface CategoryCriteria {
  skills?: string[] | null;
  skill_weights?: Record<string, number> | null;
  experience?: number | null;
  coursework?: string[] | null;
  languages?: string[] | null;
  localization?: string | null;
  [key: string]: any;
}

export interface ExtractedRequirements {
  missing_from_criteria?: string[] | null;
  insights?: string | null;
  [key: string]: any;
}

export interface OfferTitleResponse {
  id: string;
  title: string;
}

export interface OfferPublicResponse {
  id: string;
  title: string;
  descriptionMarkdown?: string | null;
  categoryCriteria?: CategoryCriteria | null;
  durationMonths?: number | null;
  contractType?: ContractType | string | null;
  createdAt: string;
}

export interface OfferPublicSummaryResponse {
  id: string;
  title: string;
  contractType?: ContractType | string | null;
  durationMonths?: number | null;
  localization?: string | null;
  experience?: number | null;
  createdAt: string;
}

export interface OfferInternalResponse {
  id: string;
  createdBy?: string | null;
  updatedBy?: string | null;
  title: string;
  descriptionMarkdown?: string | null;
  status: OfferStatus;
  offerAiStatus: OfferAiStatus;
  categoryWeights: CategoryWeights;
  categoryCriteria?: CategoryCriteria | null;
  minScore?: number | null;
  durationMonths?: number | null;
  contractType?: ContractType | string | null;
  extractedRequirements?: ExtractedRequirements | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreateOfferRequest {
  title: string;
  descriptionMarkdown?: string | null;
  categoryWeights: CategoryWeights;
  categoryCriteria?: CategoryCriteria | null;
  minScore?: number | null;
  durationMonths?: number | null;
  contractType?: ContractType | string | null;
}
