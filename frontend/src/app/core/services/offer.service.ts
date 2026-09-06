import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  OfferTitleResponse,
  OfferPublicResponse,
  OfferPublicSummaryResponse,
  OfferInternalResponse,
  CreateOfferRequest,
} from '../models/offer.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class OfferService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/api/v1/offers`;
  private publicApiUrl = `${environment.apiUrl}/api/v1/public/offers`;

  // --- Secured (HR internal) ---

  /** Fetches all job offers ordered by creation date descending */
  getAllOffers(): Observable<OfferInternalResponse[]> {
    return this.http.get<OfferInternalResponse[]>(this.apiUrl);
  }

  /** Fetches single offer details with internal metadata & extracted requirements */
  getOfferById(id: string): Observable<OfferInternalResponse> {
    return this.http.get<OfferInternalResponse>(`${this.apiUrl}/${id}`);
  }

  /** Creates a new job offer in DRAFT status with PENDING AI status */
  createOffer(request: CreateOfferRequest): Observable<OfferInternalResponse> {
    return this.http.post<OfferInternalResponse>(this.apiUrl, request);
  }

  /** Updates an existing DRAFT job offer */
  updateOffer(id: string, request: CreateOfferRequest): Observable<OfferInternalResponse> {
    return this.http.put<OfferInternalResponse>(`${this.apiUrl}/${id}`, request);
  }

  /** Publishes a DRAFT offer to ACTIVE status (requires AI SUCCESS) */
  publishOffer(id: string): Observable<OfferInternalResponse> {
    return this.http.patch<OfferInternalResponse>(`${this.apiUrl}/${id}/publish`, {});
  }

  /** Closes an ACTIVE offer */
  closeOffer(id: string): Observable<OfferInternalResponse> {
    return this.http.patch<OfferInternalResponse>(`${this.apiUrl}/${id}/close`, {});
  }

  /** Reopens a CLOSED offer back to ACTIVE */
  reopenOffer(id: string): Observable<OfferInternalResponse> {
    return this.http.patch<OfferInternalResponse>(`${this.apiUrl}/${id}/reopen`, {});
  }

  /** Re-triggers AI pre-processing and vectorization on a DRAFT offer */
  reprocessOffer(id: string): Observable<OfferInternalResponse> {
    return this.http.post<OfferInternalResponse>(`${this.apiUrl}/${id}/reprocess`, {});
  }

  /** Fetches lightweight active offer titles for dropdowns */
  getOfferTitles(): Observable<OfferTitleResponse[]> {
    return this.http.get<OfferTitleResponse[]>(`${this.apiUrl}/titles`);
  }

  // --- Public (Careers Portal) ---

  /** Fetches public active job offers list */
  getPublicOffers(): Observable<OfferPublicSummaryResponse[]> {
    return this.http.get<OfferPublicSummaryResponse[]>(this.publicApiUrl);
  }

  /** Fetches single public offer details */
  getPublicOfferById(id: string): Observable<OfferPublicResponse> {
    return this.http.get<OfferPublicResponse>(`${this.publicApiUrl}/${id}`);
  }
}
