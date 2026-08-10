export interface ExtractedData {
  candidate_info: {
    first_name: string;
    last_name: string;
    email: string;
    phone: string;
  };
  description_markdown: string;
  skills: string[];
  experience: number; // in months
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

export interface Application {
  id: string;
  candidate_id: string;
  offer_id: string;
  status: 'NEW' | 'SHORTLISTED' | 'INTERVIEWING' | 'FOLLOW_UP' | 'HIRED' | 'REJECTED' | 'ARCHIVED';
  total_score?: number;
  category_scores?: CategoryScores;
  extracted_matching?: ExtractedMatching;
  applied_at: string;
  cv_file?: CvFile;
}

export interface CvFile {
  id: string;
  extraction_status: 'PENDING' | 'SUCCESS' | 'FAILED';
  extracted_data?: ExtractedData;
  original_filename: string;
}
