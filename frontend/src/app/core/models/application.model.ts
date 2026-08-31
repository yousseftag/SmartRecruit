import { CandidateResponse } from './candidate.model';
import {
  CategoryScores,
  ExtractedData,
  ExtractedMatching,
  ExtractionStatus,
} from './ai-extraction.model';

// Re-export for seamless import compatibility
export * from './candidate.model';
export * from './ai-extraction.model';

export type WorkflowStatus =
  'NEW' | 'SHORTLISTED' | 'INTERVIEWING' | 'FOLLOW_UP' | 'HIRED' | 'REJECTED' | 'ARCHIVED';

export interface ApplicationSummaryResponse {
  id: string;
  candidate?: CandidateResponse | null;
  offerId: string;
  offerTitle: string;
  offerMinScore: number | null;
  status: WorkflowStatus | string;
  extractionStatus?: ExtractionStatus | null;
  totalScore: number | null;
  appliedAt: string;
}

export interface ApplicationResponse {
  id: string;
  candidate?: CandidateResponse | null;
  offerId: string;
  offerTitle?: string | null;
  offerMinScore: number | null;
  offerRequiredSkills?: string[] | null;
  status: WorkflowStatus | string;
  extractionStatus?: ExtractionStatus | null;
  cvFileId?: string | null;
  cvOriginalFilename?: string | null;
  totalScore: number | null;
  categoryScores?: CategoryScores | null;
  extractedMatching?: ExtractedMatching | null;
  cvExtractedData?: ExtractedData | null;
  appliedAt: string;
  scoredAt?: string | null;
}

export interface ApplicationExtractionStatusResponse {
  id: string;
  extractionStatus: ExtractionStatus;
}

export type ApplicationStatusResponse = ApplicationExtractionStatusResponse;

export interface FileImportStatus {
  filename: string;
  applicationId?: string | null;
  extractedCount: number;
  errorCode?: string | null;
  message?: string | null;
  subErrors?: string[];
}

export interface ImportResponse {
  fileStatuses: FileImportStatus[];
}

export interface UpdateApplicationStatusRequest {
  status: WorkflowStatus | string;
}

export type TaskStatus =
  | 'PENDING'
  | 'UPLOADING'
  | 'PARSING'
  | 'STALLED'
  | 'SUCCESS'
  | 'FAILED'
  | 'DUPLICATE';

export interface UploadTask {
  file?: File;
  filename: string;
  isZip: boolean;
  size: number;
  applicationId?: string;
  status: TaskStatus;
  progress: number;
  errorCode?: string;
  errorMessage?: string;
  summaryMessage?: string;
  subErrors?: string[];
  savedAt?: number;
}

