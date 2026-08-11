import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApplicationResponse, ApplicationStatusResponse } from '../models/application.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class ApplicationService {
  private apiUrl = `${environment.apiUrl}/api/v1/applications`;
  
  constructor(private http: HttpClient) {}

  // --- Secured (HR internal) ---
  importCandidates(offerId: string, files: File[]): Observable<string[]> {
    const formData = new FormData();
    files.forEach(file => formData.append('files', file));
    return this.http.post<string[]>(`${this.apiUrl}/import?offerId=${offerId}`, formData);
  }

  pollExtractionStatus(id: string): Observable<ApplicationStatusResponse> {
    return this.http.get<ApplicationStatusResponse>(`${this.apiUrl}/${id}/status`);
  }

  getApplicationDetails(id: string): Observable<ApplicationResponse> {
    return this.http.get<ApplicationResponse>(`${this.apiUrl}/${id}`);
  }

  // --- Public (the interceptor immediately passes this URL through) ---
  applyToOffer(formData: FormData): Observable<void> {
    return this.http.post<void>(`${environment.apiUrl}/api/v1/public/applications/apply`, formData);
  }
}
