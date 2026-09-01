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

export interface OfferTitleResponse {
  id: string;
  title: string;
}

export interface CategoryCriteria {
  skills?: string[] | null;
  experience?: number | null;
  coursework?: string[] | null;
  languages?: string[] | null;
  localization?: string | null;
  [key: string]: any;
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
