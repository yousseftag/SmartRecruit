export type ContractType = 'CDI' | 'CDD' | 'Stage' | 'Freelance';

export interface ContractTypeOption {
  value: ContractType;
  label: string;
}

export const CONTRACT_TYPES: ContractTypeOption[] = [
  { value: 'CDI', label: 'CDI (Durée Indéterminée)' },
  { value: 'CDD', label: 'CDD (Durée Déterminée)' },
  { value: 'Stage', label: 'Stage' },
  { value: 'Freelance', label: 'Freelance / Indépendant' },
];

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
