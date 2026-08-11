import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { OfferSummaryResponse, OfferPublicResponse } from '../models/offer.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class OfferService {
  private apiUrl = `${environment.apiUrl}/api/v1/offers`;
  private publicApiUrl = `${environment.apiUrl}/api/v1/public/offers`;
  
  constructor(private http: HttpClient) {}

  // --- Secured (HR internal) ---
  getOfferSummaries(): Observable<OfferSummaryResponse[]> {
    return this.http.get<OfferSummaryResponse[]>(`${this.apiUrl}/summary`);
  }

  // --- Public (the interceptor immediately passes this URL through) ---
  getPublicOffers(): Observable<OfferPublicResponse[]> {
    return this.http.get<OfferPublicResponse[]>(this.publicApiUrl);
  }

  getPublicOfferById(id: string): Observable<OfferPublicResponse> {
    return this.http.get<OfferPublicResponse>(`${this.publicApiUrl}/${id}`);
  }
}
