import { ExtractedCandidateInfo } from './candidate.model';

export type ExtractionStatus = 'PENDING' | 'SUCCESS' | 'FAILED';

export interface ExtractedData {
  candidate_info?: ExtractedCandidateInfo | null;
  description_markdown?: string | null;
  skills?: string[] | null;
  experience?: number | null; // Total months
  coursework?: string[] | null;
  languages?: string[] | null;
  localization?: string | null;
}

export interface MatchedCriteria {
  skills?: string[] | null;
  experience?: boolean | null;
  coursework?: string[] | null;
  languages?: string[] | null;
  localization?: string | null;
}

export interface ExtractedMatching {
  matched_criteria?: MatchedCriteria | null;
  strengths?: string[] | null;
  weaknesses?: string[] | null;
}

export interface CategoryScores {
  skills?: number | null;
  experience?: number | null;
  coursework?: number | null;
  languages?: number | null;
  localization?: number | null;
  [key: string]: number | null | undefined;
}
