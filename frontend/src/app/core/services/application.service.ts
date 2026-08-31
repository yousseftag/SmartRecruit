import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, defer, timer, switchMap, expand, takeWhile, map, catchError, of } from 'rxjs';
import {
  ApplicationResponse,
  ApplicationExtractionStatusResponse,
  ApplicationSummaryResponse,
  ImportResponse,
  UpdateApplicationStatusRequest,
} from '../models/application.model';
import { environment } from '../../../environments/environment';

// ── Polling Event Types ───────────────────────────────────────────────────────
export type PollingEventKind =
  'PENDING' | 'STALLED' | 'BACKEND_STALLED' | 'SUCCESS' | 'FAILED' | 'TIMEOUT' | 'ERROR';

export interface PollingEvent {
  kind: PollingEventKind;
  /** Attempt index (1-based) */
  attempt: number;
  /** Next interval that will be used (ms) */
  nextIntervalMs: number;
}

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

  /** Single-shot lightweight HTTP check — used by the reactive stream and restore probe. */
  pollExtractionStatus(id: string): Observable<ApplicationExtractionStatusResponse> {
    return this.http.get<ApplicationExtractionStatusResponse>(
      `${this.apiUrl}/${id}/extraction-status`,
    );
  }

  /**
   * Reactive, non-overlapping polling stream with exponential backoff.
   *
   * Emits a `PollingEvent` after each HTTP response. The stream completes
   * (or emits a terminal event) when one of the following is reached:
   *   - `SUCCESS` / `FAILED` — AI worker returned a terminal state
   *   - `BACKEND_STALLED` — Backend detected timeout (>5 min pending)
   *   - `STALLED` — `stalledAfterAttempts` (10 polls) reached without terminal state (warning shown, keeps polling)
   *   - `TIMEOUT` — `maxAttempts` (30 polls) exceeded, stream completes
   *   - `ERROR` — HTTP error, stream completes
   */
  watchExtractionStatus$(id: string): Observable<PollingEvent> {
    const cfg = environment.polling;

    interface State {
      attempt: number;
      intervalMs: number;
      _status?: string;
      _terminal?: 'TIMEOUT';
      _error?: boolean;
    }

    const initialState: State = { attempt: 0, intervalMs: cfg.intervalMs };

    return defer(() =>
      of(initialState).pipe(
        expand((state: State) => {
          if (state.attempt >= cfg.maxAttempts) {
            return of({ ...state, _terminal: 'TIMEOUT' as const });
          }

          const nextInterval = Math.min(
            state.intervalMs * cfg.backoffMultiplier,
            cfg.maxIntervalMs,
          );

          return timer(state.attempt === 0 ? 0 : state.intervalMs).pipe(
            switchMap(() =>
              this.pollExtractionStatus(id).pipe(
                map((res) => ({
                  attempt: state.attempt + 1,
                  intervalMs: nextInterval,
                  _status: res.extractionStatus,
                })),
                catchError(() =>
                  of({
                    attempt: state.attempt + 1,
                    intervalMs: nextInterval,
                    _error: true,
                  }),
                ),
              ),
            ),
          );
        }),
        takeWhile(
          (state: State) =>
            !state._terminal &&
            !state._error &&
            state._status !== 'SUCCESS' &&
            state._status !== 'FAILED' &&
            state._status !== 'STALLED',
          /* inclusive */ true,
        ),
        map((state: State): PollingEvent => {
          const attempt = state.attempt;
          const nextIntervalMs = Math.min(
            state.intervalMs * cfg.backoffMultiplier,
            cfg.maxIntervalMs,
          );

          if (state._terminal === 'TIMEOUT') return { kind: 'TIMEOUT', attempt, nextIntervalMs: 0 };
          if (state._error) return { kind: 'ERROR', attempt, nextIntervalMs: 0 };
          if (state._status === 'SUCCESS') return { kind: 'SUCCESS', attempt, nextIntervalMs: 0 };
          if (state._status === 'FAILED') return { kind: 'FAILED', attempt, nextIntervalMs: 0 };
          if (state._status === 'STALLED') {
            return {
              kind: 'BACKEND_STALLED',
              attempt,
              nextIntervalMs: 0,
            };
          }
          if (attempt >= cfg.stalledAfterAttempts) {
            return { kind: 'STALLED', attempt, nextIntervalMs };
          }
          return { kind: 'PENDING', attempt, nextIntervalMs };
        }),
      ),
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
