import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ReportingDashboardResponse, CandidateReportRow } from '../models/reporting.model';

@Injectable({
  providedIn: 'root',
})
export class ReportingService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/api/v1/reporting`;

  /**
   * Retrieves campaign analytics dashboard metrics including KPIs, recruitment funnel,
   * score distribution tiers, and top-ranked candidates preview.
   */
  getDashboardStats(offerId?: string, period?: string): Observable<ReportingDashboardResponse> {
    const params = this.buildQueryParams(offerId, period);
    return this.http.get<ReportingDashboardResponse>(`${this.apiUrl}/stats`, { params });
  }

  /**
   * Retrieves the ranked candidate dataset for the specified campaign and period filter.
   */
  getRankedCandidates(
    offerId?: string,
    period?: string,
    limit?: number,
  ): Observable<CandidateReportRow[]> {
    let params = this.buildQueryParams(offerId, period);
    if (limit && limit > 0) {
      params = params.set('limit', limit.toString());
    }
    return this.http.get<CandidateReportRow[]>(`${this.apiUrl}/candidates`, { params });
  }

  /**
   * Streams a formatted Excel (.xlsx) workbook for download.
   */
  downloadExcel(offerId?: string, period?: string, limit?: number): Observable<Blob> {
    let params = this.buildQueryParams(offerId, period);
    if (limit && limit > 0) {
      params = params.set('limit', limit.toString());
    }
    const timeZone = Intl?.DateTimeFormat?.().resolvedOptions?.().timeZone;
    if (timeZone) {
      params = params.set('timezone', timeZone);
    }
    return this.http.get(`${this.apiUrl}/export/excel`, {
      params,
      headers: this.buildHeaders(),
      responseType: 'blob',
    });
  }

  /**
   * Streams a formatted executive synthesis PDF report for download.
   */
  downloadPdf(offerId?: string, period?: string): Observable<Blob> {
    let params = this.buildQueryParams(offerId, period);
    const timeZone = Intl?.DateTimeFormat?.().resolvedOptions?.().timeZone;
    if (timeZone) {
      params = params.set('timezone', timeZone);
    }
    return this.http.get(`${this.apiUrl}/export/pdf`, {
      params,
      headers: this.buildHeaders(),
      responseType: 'blob',
    });
  }

  /**
   * Browser file download trigger using a temporary in-memory Object URL.
   * Automatically revokes the Object URL after triggering download to prevent memory leaks.
   */
  triggerFileDownload(blob: Blob, fileName: string): void {
    const url = window.URL.createObjectURL(blob);
    const anchor = document.createElement('a');
    anchor.href = url;
    anchor.download = fileName;
    document.body.appendChild(anchor);
    anchor.click();
    document.body.removeChild(anchor);
    window.URL.revokeObjectURL(url);
  }

  private buildQueryParams(offerId?: string, period?: string): HttpParams {
    let params = new HttpParams();
    if (offerId && offerId !== 'all') {
      params = params.set('offerId', offerId);
    }
    if (period && period !== 'all') {
      params = params.set('period', period);
    }
    return params;
  }

  private buildHeaders(): Record<string, string> {
    const timeZone = Intl?.DateTimeFormat?.().resolvedOptions?.().timeZone;
    return timeZone ? { 'X-Timezone': timeZone } : {};
  }
}
