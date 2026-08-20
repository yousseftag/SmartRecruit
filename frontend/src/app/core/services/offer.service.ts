import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  OfferTitleResponse,
  OfferPublicResponse,
  OfferPublicSummaryResponse,
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
