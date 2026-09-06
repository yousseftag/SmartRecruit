import {
  Component,
  inject,
  OnInit,
  OnDestroy,
  signal,
  computed,
  HostListener,
  ElementRef,
  DestroyRef,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { Subscription } from 'rxjs';
import { ApplicationService } from '../../../core/services/application.service';
import {
  ApplicationResponse,
  ExtractedData,
  ExtractedMatching,
  UserRole,
} from '../../../core/models';
import { AuthService } from '../../../core/auth/auth.service';
import { ScoreGauge } from '../../../shared/components/score-gauge/score-gauge';
import { ExperienceFormatPipe } from '../../../shared/pipes/experience-format.pipe';
import { SendEmailModal } from '../../../shared/components/send-email-modal/send-email-modal';
import { LucideDynamicIcon, LucideMail } from '@lucide/angular';
import {
  WORKFLOW_STATUSES,
  WORKFLOW_STATUS_LIST,
  WorkflowStatusDefinition,
} from '../../../core/constants/status.constants';
import { WorkflowStatus } from '../../../core/models/application.model';

@Component({
  selector: 'app-candidate-profile',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    ScoreGauge,
    ExperienceFormatPipe,
    SendEmailModal,
    LucideDynamicIcon,
  ],
  templateUrl: './candidate-profile.html',
})
export class CandidateProfile implements OnInit, OnDestroy {
  private route = inject(ActivatedRoute);
  private applicationService = inject(ApplicationService);
  private authService = inject(AuthService);
  private elementRef = inject(ElementRef);
  private sanitizer = inject(DomSanitizer);
  private destroyRef = inject(DestroyRef);

  readonly LucideMail = LucideMail;

  private activePoller: Subscription | null = null;

  readonly canManageStatus = computed(() =>
    this.authService.hasAnyRole([UserRole.HR_ADMIN, UserRole.RECRUITER]),
  );

  readonly application = signal<ApplicationResponse | null>(null);
  readonly isLoading = signal(true);
  readonly isError = signal(false);

  readonly isUpdatingStatus = signal(false);
  readonly isStatusDropdownOpen = signal(false);
  readonly isEmailModalOpen = signal(false);

  readonly isReExtracting = signal(false);

  // --- PDF Preview Modal State ---
  readonly isPreviewModalOpen = signal(false);
  readonly isLoadingCv = signal(false);
  readonly cvPreviewUrl = signal<SafeResourceUrl | null>(null);
  private rawBlobUrl: string | null = null;

  readonly toastMessage = signal<string | null>(null);
  readonly toastType = signal<'success' | 'error'>('success');

  // --- Official 7 Workflow Status Options Matching Backend Enum ---
  readonly statusOptions: WorkflowStatusDefinition[] = WORKFLOW_STATUS_LIST;

  // --- Computed Extracted Properties ---
  readonly cvData = computed<ExtractedData | null>(
    () => this.application()?.cvExtractedData || null,
  );
  readonly candidateInfo = computed(() => this.cvData()?.candidate_info || null);
  readonly candidateFullName = computed(() => {
    const candidate = this.application()?.candidate;
    const info = this.candidateInfo();
    const first = candidate?.firstName || info?.first_name || '';
    const last = candidate?.lastName || info?.last_name || '';
    const full = `${first} ${last}`.trim();
    return full || 'Candidat';
  });
  readonly matchingData = computed<ExtractedMatching | null>(
    () => this.application()?.extractedMatching || null,
  );
  readonly matchedCriteria = computed(() => this.matchingData()?.matched_criteria || null);

  readonly offerRequiredSkills = computed<string[]>(
    () => this.application()?.offerRequiredSkills || [],
  );
  readonly matchedSkills = computed<string[]>(() => this.matchedCriteria()?.skills || []);
  readonly allSkills = computed<string[]>(() => this.cvData()?.skills || []);

  // Skills required by offer but missing from candidate CV
  readonly missingSkills = computed<string[]>(() => {
    const matched = new Set(this.matchedSkills().map((s) => s.toLowerCase().trim()));
    return this.offerRequiredSkills().filter((s) => !matched.has(s.toLowerCase().trim()));
  });

  // Extra candidate skills not explicitly required by the offer
  readonly additionalSkills = computed<string[]>(() => {
    const required = new Set(this.offerRequiredSkills().map((s) => s.toLowerCase().trim()));
    const matched = new Set(this.matchedSkills().map((s) => s.toLowerCase().trim()));
    return this.allSkills().filter(
      (s) => !required.has(s.toLowerCase().trim()) && !matched.has(s.toLowerCase().trim()),
    );
  });

  readonly isExtracting = computed(() => {
    return this.isReExtracting() || this.application()?.extractionStatus === 'PENDING';
  });

  readonly hasExtractionSuccess = computed(() => {
    return this.application()?.extractionStatus === 'SUCCESS';
  });

  readonly isExtractionFailed = computed(() => {
    return this.application()?.extractionStatus === 'FAILED';
  });

  readonly isExtractionStalled = computed(() => {
    return this.application()?.extractionStatus === 'STALLED';
  });

  readonly totalScore = computed(() => this.application()?.totalScore ?? null);
  readonly minScore = computed(() => this.application()?.offerMinScore ?? null);
  readonly passedMinScore = computed(() => this.application()?.passedMinScore ?? null);

  readonly isScoreAdmissible = computed(() => {
    const passed = this.passedMinScore();
    if (passed !== null && passed !== undefined) return passed;
    const score = this.totalScore();
    const min = this.minScore();
    if (score === null) return false;
    if (min === null) return true;
    return score >= min;
  });

  readonly currentStatusOption = computed<WorkflowStatusDefinition>(() => {
    const current = this.application()?.status as WorkflowStatus | undefined;
    if (current && current in WORKFLOW_STATUSES) {
      return WORKFLOW_STATUSES[current];
    }
    return WORKFLOW_STATUSES.NEW;
  });

  readonly categoryScoresList = computed(() => {
    const scores = this.application()?.categoryScores;
    if (!scores) return [];

    const order = ['skills', 'experience', 'coursework', 'languages', 'localization'];

    return order
      .filter((key) => scores[key] !== undefined && scores[key] !== null)
      .map((key) => ({
        key,
        label: this.getCategoryName(key),
        value: Number(scores[key]),
      }));
  });

  constructor() {
    this.destroyRef.onDestroy(() => {
      this.stopPolling();
    });
  }

  getCategoryName(key: string): string {
    const translations: Record<string, string> = {
      skills: 'Compétences',
      experience: 'Expérience',
      coursework: 'Formation',
      languages: 'Langues',
      localization: 'Localisation',
    };
    return translations[key] || key;
  }

  showToast(message: string, type: 'success' | 'error' = 'success') {
    this.toastMessage.set(message);
    this.toastType.set(type);
    setTimeout(() => {
      this.toastMessage.set(null);
    }, 3500);
  }

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.fetchApplication(id);
    } else {
      this.isError.set(true);
      this.isLoading.set(false);
    }
  }

  // Close dropdown on click outside
  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    if (!this.elementRef.nativeElement.contains(event.target)) {
      this.isStatusDropdownOpen.set(false);
    }
  }

  toggleStatusDropdown(event: Event) {
    if (!this.canManageStatus() || this.isUpdatingStatus()) return;
    event.stopPropagation();
    this.isStatusDropdownOpen.update((v) => !v);
  }

  private fetchApplication(id: string) {
    this.applicationService.getApplicationDetails(id).subscribe({
      next: (data) => {
        this.application.set(data);
        this.isLoading.set(false);

        // If it's still being parsed on initial load, poll for completion
        if (data.extractionStatus === 'PENDING') {
          this.pollStatus(id);
        }
      },
      error: () => {
        this.isError.set(true);
        this.isLoading.set(false);
      },
    });
  }

  updateStatus(newStatus: string) {
    const app = this.application();
    if (!app || this.isUpdatingStatus() || app.status === newStatus) return;

    this.isUpdatingStatus.set(true);
    this.isStatusDropdownOpen.set(false);

    this.applicationService.updateApplicationStatus(app.id, newStatus).subscribe({
      next: () => {
        this.application.update((curr) =>
          curr ? ({ ...curr, status: newStatus } as ApplicationResponse) : null,
        );
        this.isUpdatingStatus.set(false);
        this.showToast('Statut mis à jour avec succès !', 'success');
      },
      error: (err) => {
        console.error('Failed to update status', err);
        this.isUpdatingStatus.set(false);
        this.showToast('Erreur lors de la mise à jour du statut.', 'error');
      },
    });
  }

  // --- On-Demand AI Re-Analysis ---
  reAnalyze() {
    const app = this.application();
    if (!app || this.isExtracting()) return;

    this.isReExtracting.set(true);

    // Optimistically enter pending state in UI and clear previous extracted data
    this.application.update((curr) =>
      curr
        ? ({
            ...curr,
            extractionStatus: 'PENDING',
            totalScore: null,
            categoryScores: null,
            extractedMatching: null,
            cvExtractedData: null,
          } as ApplicationResponse)
        : null,
    );

    this.applicationService.reExtractCv(app.id).subscribe({
      next: () => {
        this.showToast('Nouvelle analyse IA initiée...', 'success');
        this.pollStatus(app.id);
      },
      error: (err) => {
        this.isReExtracting.set(false);
        this.fetchApplication(app.id);
        const errMsg = err?.error?.message || "Impossible de relancer l'analyse IA.";
        this.showToast(errMsg, 'error');
      },
    });
  }

  private pollStatus(applicationId: string) {
    this.stopPolling();

    this.activePoller = this.applicationService.watchExtractionStatus$(applicationId).subscribe({
      next: (event) => {
        if (event.kind === 'SUCCESS') {
          this.stopPolling();
          this.isReExtracting.set(false);
          // Re-fetch entire application to get fresh scores and match breakdowns
          this.fetchApplication(applicationId);
          this.showToast('Analyse IA terminée avec succès !', 'success');
        } else if (event.kind === 'FAILED') {
          this.stopPolling();
          this.isReExtracting.set(false);
          this.fetchApplication(applicationId);
          this.showToast("Échec de l'analyse IA du CV.", 'error');
        } else if (event.kind === 'BACKEND_STALLED') {
          this.stopPolling();
          this.isReExtracting.set(false);
          this.fetchApplication(applicationId);
          this.showToast("L'analyse IA a dépassé le délai limite (bloquée).", 'error');
        } else if (event.kind === 'TIMEOUT') {
          this.stopPolling();
          this.isReExtracting.set(false);
          this.showToast("Délai d'attente d'extraction dépassé.", 'error');
        } else if (event.kind === 'ERROR') {
          this.stopPolling();
          this.isReExtracting.set(false);
          this.showToast("Erreur lors du suivi de l'analyse IA.", 'error');
        }
      },
      error: () => {
        this.stopPolling();
        this.isReExtracting.set(false);
        this.showToast("Erreur lors du suivi de l'analyse IA.", 'error');
      },
    });
  }

  private stopPolling() {
    if (this.activePoller) {
      this.activePoller.unsubscribe();
      this.activePoller = null;
    }
  }

  // --- PDF Preview Modal Logic ---

  openCvModal() {
    const app = this.application();
    if (!app?.id) return;

    this.isPreviewModalOpen.set(true);

    if (!this.cvPreviewUrl()) {
      this.isLoadingCv.set(true);
      this.applicationService.downloadCv(app.id).subscribe({
        next: (blob) => {
          this.isLoadingCv.set(false);
          this.rawBlobUrl = URL.createObjectURL(blob);
          this.cvPreviewUrl.set(this.sanitizer.bypassSecurityTrustResourceUrl(this.rawBlobUrl));
        },
        error: (err) => {
          this.isLoadingCv.set(false);
          console.error('Erreur lors du chargement du CV :', err);
          this.showToast('Impossible de charger le document CV.', 'error');
          this.closeCvModal();
        },
      });
    }
  }

  closeCvModal() {
    this.isPreviewModalOpen.set(false);
  }

  openInNewTab() {
    if (this.rawBlobUrl) {
      window.open(this.rawBlobUrl, '_blank');
    }
  }

  downloadCvFile() {
    if (!this.rawBlobUrl) return;
    const a = document.createElement('a');
    a.href = this.rawBlobUrl;
    a.download = this.application()?.cvOriginalFilename || 'cv.pdf';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
  }

  openEmailModal(): void {
    this.isEmailModalOpen.set(true);
  }

  closeEmailModal(): void {
    this.isEmailModalOpen.set(false);
  }

  onEmailSent(): void {
    this.showToast('Email envoyé au candidat avec succès !', 'success');
  }

  @HostListener('document:keydown.escape')
  onEscapePress() {
    if (this.isPreviewModalOpen()) {
      this.closeCvModal();
    }
    if (this.isEmailModalOpen()) {
      this.closeEmailModal();
    }
  }

  ngOnDestroy() {
    this.stopPolling();
    if (this.rawBlobUrl) {
      URL.revokeObjectURL(this.rawBlobUrl);
      this.rawBlobUrl = null;
    }
  }
}
