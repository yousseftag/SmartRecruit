import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  ApplicationResponse,
  ApplicationExtractionStatusResponse,
  ApplicationSummaryResponse,
  ImportResponse,
  UpdateApplicationStatusRequest,
} from '../models/application.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class ApplicationService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/api/v1/applications`;
  private publicApiUrl = `${environment.apiUrl}/api/v1/public/applications`;

  // --- Secured (HR internal) ---

  /** HR bulk imports from the dashboard (ZIP archives and multiple PDF files) */
  importCandidates(offerId: string, files: File[]): Observable<ImportResponse> {
    const formData = new FormData();
    files.forEach((file) => formData.append('files', file));
    return this.http.post<ImportResponse>(`${this.apiUrl}/import?offerId=${offerId}`, formData);
  }

  /** Polls the current NLP extraction status of an application */
  pollExtractionStatus(id: string): Observable<ApplicationExtractionStatusResponse> {
    return this.http.get<ApplicationExtractionStatusResponse>(
      `${this.apiUrl}/${id}/extraction-status`,
    );
  }

  /** Retrieves the full, detailed profile of a single application */
  getApplicationDetails(id: string): Observable<ApplicationResponse> {
    return this.http.get<ApplicationResponse>(`${this.apiUrl}/${id}`);
  }

  /** Updates the workflow stage (e.g. NEW -> SHORTLISTED -> REJECTED) */
  updateApplicationStatus(id: string, status: string): Observable<void> {
    const payload: UpdateApplicationStatusRequest = { status };
    return this.http.put<void>(`${this.apiUrl}/${id}/status`, payload);
  }

  /** Re-triggers AI extraction and scoring for an existing candidate */
  reExtractCv(id: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${id}/re-extract`, {});
  }

  /** Deletes an application */
  deleteApplication(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  /** Fetches all applications (summary), optionally filtered by job offer ID */
  getAllApplications(offerId?: string): Observable<ApplicationSummaryResponse[]> {
    const url = offerId ? `${this.apiUrl}?offerId=${encodeURIComponent(offerId)}` : this.apiUrl;
    return this.http.get<ApplicationSummaryResponse[]>(url);
  }

  /** Downloads or streams the original candidate CV file as a Blob */
  downloadCv(id: string): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${id}/cv`, {
      responseType: 'blob',
    });
  }

  // --- Public (No Auth needed) ---

  /** Handles public candidate self-application form */
  applyToOffer(formData: FormData): Observable<void> {
    return this.http.post<void>(`${this.publicApiUrl}/apply`, formData);
  }
}
