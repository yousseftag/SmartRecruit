export interface CandidateResponse {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
}

export interface ExtractedData {
  candidate_info: {
    first_name: string;
    last_name: string;
    email: string;
    phone: string;
  };
  description_markdown: string;
  skills: string[];
  experience: number;
  coursework: string[];
  languages: string[];
  localization: string;
}

export interface ExtractedMatching {
  matched_criteria: Record<string, any>;
  strengths: string[];
  weaknesses: string[];
}

export interface CategoryScores {
  skills: number;
  experience: number;
  coursework: number;
  languages: number;
  localization: number;
}

export interface ApplicationResponse {
  id: string;
  candidate: CandidateResponse;
  offerId: string;
  status: 'NEW' | 'SHORTLISTED' | 'INTERVIEWING' | 'FOLLOW_UP' | 'HIRED' | 'REJECTED' | 'ARCHIVED';
  totalScore?: number;
  categoryScores?: CategoryScores;
  extractedMatching?: ExtractedMatching;
  cvExtractedData?: ExtractedData;
  appliedAt: string;
  scoredAt?: string;
}

export interface ApplicationStatusResponse {
  id: string;
  extractionStatus: 'PENDING' | 'SUCCESS' | 'FAILED';
}
