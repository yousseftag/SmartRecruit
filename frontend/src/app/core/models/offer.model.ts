export interface OfferSummaryResponse {
  id: string;
  title: string;
}

export interface CategoryCriteria {
  skills: string[];
  experience: number;
  coursework: string[];
  languages: string[];
  localization: string;
}

export interface OfferPublicResponse {
  id: string;
  title: string;
  descriptionMarkdown: string | null;
  categoryCriteria: CategoryCriteria | null;
  durationMonths: number | null;
  contractType: string | null;
  createdAt: string;
}
