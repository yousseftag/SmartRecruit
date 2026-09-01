export interface CandidateResponse {
  id: string;
  firstName?: string | null;
  lastName?: string | null;
  email?: string | null;
  phone?: string | null;
  currentJobTitle?: string | null;
}

export interface ExtractedCandidateInfo {
  first_name?: string | null;
  last_name?: string | null;
  email?: string | null;
  phone?: string | null;
  current_job_title?: string | null;
}
