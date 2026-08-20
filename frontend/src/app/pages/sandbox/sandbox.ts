import { Component, inject, signal } from '@angular/core';
import { CommonModule, JsonPipe } from '@angular/common';
import { ScoreGauge } from '../../shared/components/score-gauge/score-gauge';
import { FileDropzone } from '../../shared/components/file-dropzone/file-dropzone';
import { StatusBadge } from '../../shared/components/status-badge/status-badge';
import { OfferService } from '../../core/services/offer.service';
import { ApplicationService } from '../../core/services/application.service';
import { AuthService } from '../../core/auth/auth.service';
import {
  ApplicationSummaryResponse,
  WorkflowStatus,
  ExtractionStatus,
} from '../../core/models/application.model';
import { OfferTitleResponse, OfferPublicSummaryResponse } from '../../core/models/offer.model';

@Component({
  selector: 'app-sandbox',
  standalone: true,
  imports: [CommonModule, ScoreGauge, FileDropzone, StatusBadge, JsonPipe],
  templateUrl: './sandbox.html',
})
export class Sandbox {
  private offerService = inject(OfferService);
  private applicationService = inject(ApplicationService);
  public authService = inject(AuthService);

  // Status Badge test values
  workflowStatuses: WorkflowStatus[] = [
    'NEW',
    'SHORTLISTED',
    'INTERVIEWING',
    'FOLLOW_UP',
    'HIRED',
    'REJECTED',
    'ARCHIVED',
  ];

  extractionStatuses: ExtractionStatus[] = ['PENDING', 'SUCCESS', 'FAILED'];

  // Dropped files state for Dropzone test
  droppedFiles = signal<{ source: string; files: { name: string; size: number }[] } | null>(null);

  // API Testing States
  apiResults = signal<{ action: string; data: any; error?: string; loading: boolean } | null>(null);

  onFilesDropped(files: File[], source: string) {
    this.droppedFiles.set({
      source,
      files: files.map((f) => ({ name: f.name, size: f.size })),
    });
  }

  // --- API Testing Triggers ---

  testPublicOffers() {
    this.apiResults.set({ action: 'GET /api/v1/public/offers', data: null, loading: true });
    this.offerService.getPublicOffers().subscribe({
      next: (offers: OfferPublicSummaryResponse[]) => {
        this.apiResults.set({
          action: 'GET /api/v1/public/offers (Public Endpoint)',
          data: offers,
          loading: false,
        });
      },
      error: (err) => {
        this.apiResults.set({
          action: 'GET /api/v1/public/offers (Public Endpoint)',
          data: null,
          error: err?.message || 'Erreur lors de la requête',
          loading: false,
        });
      },
    });
  }

  testOfferTitles() {
    this.apiResults.set({ action: 'GET /api/v1/offers/titles', data: null, loading: true });
    this.offerService.getOfferTitles().subscribe({
      next: (titles: OfferTitleResponse[]) => {
        this.apiResults.set({
          action: 'GET /api/v1/offers/titles (Secured Endpoint - Keycloak Bearer)',
          data: titles,
          loading: false,
        });
      },
      error: (err) => {
        this.apiResults.set({
          action: 'GET /api/v1/offers/titles (Secured Endpoint)',
          data: null,
          error: err?.message || 'Erreur 401 / 403 (Non authentifié ou non autorisé)',
          loading: false,
        });
      },
    });
  }

  testAllApplications() {
    this.apiResults.set({ action: 'GET /api/v1/applications', data: null, loading: true });
    this.applicationService.getAllApplications().subscribe({
      next: (apps: ApplicationSummaryResponse[]) => {
        this.apiResults.set({
          action: 'GET /api/v1/applications (Secured Endpoint - Keycloak Bearer)',
          data: apps,
          loading: false,
        });
      },
      error: (err) => {
        this.apiResults.set({
          action: 'GET /api/v1/applications (Secured Endpoint)',
          data: null,
          error: err?.message || 'Erreur 401 / 403 (Non authentifié ou non autorisé)',
          loading: false,
        });
      },
    });
  }
}
