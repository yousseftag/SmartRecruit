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
   *
   * @param offerId Optional offer UUID filter (omitted or 'all' for consolidated).
   * @param period Analytical period window ('all', '30d', '90d', '1y').
   */
  getDashboardStats(offerId?: string, period?: string): Observable<ReportingDashboardResponse> {
    const params = this.buildQueryParams(offerId, period);
    return this.http.get<ReportingDashboardResponse>(`${this.apiUrl}/stats`, { params });
  }

  /**
   * Retrieves the ranked candidate dataset for the specified campaign and period filter.
   *
   * @param offerId Optional offer UUID filter.
   * @param period Analytical period window.
   * @param limit Maximum rows to retrieve (0 or undefined for default/unpaged).
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
   *
   * @param offerId Optional offer UUID filter.
   * @param period Analytical period window.
   * @param limit Maximum candidates to include (0 for all).
   */
  downloadExcel(offerId?: string, period?: string, limit?: number): Observable<Blob> {
    let params = this.buildQueryParams(offerId, period);
    if (limit && limit > 0) {
      params = params.set('limit', limit.toString());
    }
    return this.http.get(`${this.apiUrl}/export/excel`, {
      params,
      responseType: 'blob',
    });
  }

  /**
   * Streams a formatted executive synthesis PDF report for download.
   *
   * @param offerId Optional offer UUID filter.
   * @param period Analytical period window.
   */
  downloadPdf(offerId?: string, period?: string): Observable<Blob> {
    const params = this.buildQueryParams(offerId, period);
    return this.http.get(`${this.apiUrl}/export/pdf`, {
      params,
      responseType: 'blob',
    });
  }

  /**
   * Browser file download trigger using a temporary in-memory Object URL.
   * Automatically revokes the Object URL after triggering download to prevent memory leaks.
   *
   * @param blob Binary file data.
   * @param fileName Target filename for browser save dialog.
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
}
