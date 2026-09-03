import { Component, OnInit, inject, signal, computed, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import {
  LucideDynamicIcon,
  LucideGitMerge,
  LucideSearch,
  LucideFilter,
  LucideMail,
  LucideUser,
  LucideMoreVertical,
  LucideExternalLink,
  LucideClock,
  LucideSparkles,
  LucideCheckCircle2,
  LucideXCircle,
  LucideHelpCircle,
  LucideBriefcase,
  LucideRotateCw,
} from '@lucide/angular';
import { ApplicationService } from '../../../core/services/application.service';
import { OfferService } from '../../../core/services/offer.service';
import { ApplicationSummaryResponse, WorkflowStatus } from '../../../core/models/application.model';
import { OfferTitleResponse } from '../../../core/models/offer.model';
import { SendEmailModal } from '../../../shared/components/send-email-modal/send-email-modal';
import { CustomSelect, SelectOption } from '../../../shared/components/custom-select/custom-select';
import {
  WORKFLOW_STATUSES,
  WORKFLOW_STATUS_LIST,
  WorkflowStatusDefinition,
} from '../../../core/constants/status.constants';

export interface WorkflowColumn {
  key: WorkflowStatus;
  label: string;
  description: string;
  badgeClass: string;
  borderClass: string;
  dotColor: string;
  promptEmail: boolean;
}

@Component({
  selector: 'app-workflow-board',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    LucideDynamicIcon,
    SendEmailModal,
    CustomSelect,
  ],
  templateUrl: './workflow-board.html',
})
export class WorkflowBoard implements OnInit {
  private applicationService = inject(ApplicationService);
  private offerService = inject(OfferService);
  private router = inject(Router);

  // Icons
  readonly LucideGitMerge = LucideGitMerge;
  readonly LucideSearch = LucideSearch;
  readonly LucideFilter = LucideFilter;
  readonly LucideMail = LucideMail;
  readonly LucideUser = LucideUser;
  readonly LucideMoreVertical = LucideMoreVertical;
  readonly LucideExternalLink = LucideExternalLink;
  readonly LucideClock = LucideClock;
  readonly LucideSparkles = LucideSparkles;
  readonly LucideCheckCircle2 = LucideCheckCircle2;
  readonly LucideXCircle = LucideXCircle;
  readonly LucideHelpCircle = LucideHelpCircle;
  readonly LucideBriefcase = LucideBriefcase;
  readonly LucideRotateCw = LucideRotateCw;

  // 7 Workflow Columns Configuration (Single Source of Truth)
  readonly columns: WorkflowColumn[] = WORKFLOW_STATUS_LIST.map((s) => ({
    key: s.value,
    label: s.label,
    description: s.description,
    badgeClass: s.badgeClass,
    borderClass: s.borderClass,
    dotColor: s.dotClass,
    promptEmail: s.promptEmail,
  }));

  // State Signals
  applications = signal<ApplicationSummaryResponse[]>([]);
  offers = signal<OfferTitleResponse[]>([]);
  selectedOfferId = signal<string>('');
  searchQuery = signal<string>('');
  minScoreFilter = signal<number | null>(null);
  isLoading = signal<boolean>(false);

  // Dropdown options
  readonly scoreOptions: SelectOption[] = [
    { value: null, label: 'Tous les scores' },
    {
      value: 80,
      label: 'Score ≥ 80%',
      badge: 'Excellent',
      badgeClass: 'bg-emerald-50 text-emerald-700 border-emerald-200',
    },
    {
      value: 70,
      label: 'Score ≥ 70%',
      badge: 'Bon',
      badgeClass: 'bg-blue-50 text-blue-700 border-blue-200',
    },
    {
      value: 50,
      label: 'Score ≥ 50%',
      badge: 'Moyen',
      badgeClass: 'bg-amber-50 text-amber-700 border-amber-200',
    },
  ];

  readonly offerOptions = computed<SelectOption[]>(() => {
    const all = this.offers().map((o) => ({
      value: o.id,
      label: o.title,
    }));
    return [{ value: '', label: 'Toutes les offres' }, ...all];
  });

  // Drag and drop state
  draggedApplicationId = signal<string | null>(null);
  dragOverColumn = signal<string | null>(null);

  // Card menu popover
  openMenuCardId = signal<string | null>(null);

  // Email modal state
  emailModalState = signal<{
    isOpen: boolean;
    applicationId: string | null;
    candidateName: string;
    candidateEmail: string;
    offerTitle: string;
    targetStatus: string | null;
  }>({
    isOpen: false,
    applicationId: null,
    candidateName: '',
    candidateEmail: '',
    offerTitle: '',
    targetStatus: null,
  });

  // Toast
  toastMessage = signal<string | null>(null);
  toastType = signal<'success' | 'error'>('success');

  // Filtered applications
  filteredApplications = computed(() => {
    let list = this.applications();
    const query = this.searchQuery().toLowerCase().trim();
    const minScore = this.minScoreFilter();

    if (query) {
      list = list.filter((app) => {
        const name =
          `${app.candidate?.firstName || ''} ${app.candidate?.lastName || ''}`.toLowerCase();
        const email = (app.candidate?.email || '').toLowerCase();
        const offer = (app.offerTitle || '').toLowerCase();
        return name.includes(query) || email.includes(query) || offer.includes(query);
      });
    }

    if (minScore !== null && minScore > 0) {
      list = list.filter((app) => (app.totalScore ?? 0) >= minScore);
    }

    return list;
  });

  // Group applications by status column
  columnMap = computed(() => {
    const map = new Map<string, ApplicationSummaryResponse[]>();
    for (const col of this.columns) {
      map.set(col.key, []);
    }

    for (const app of this.filteredApplications()) {
      const statusKey = (app.status || 'NEW').toUpperCase();
      const list = map.get(statusKey);
      if (list) {
        list.push(app);
      } else {
        map.get('NEW')?.push(app);
      }
    }

    return map;
  });

  @HostListener('document:click')
  onDocumentClick(): void {
    if (this.openMenuCardId()) {
      this.openMenuCardId.set(null);
    }
  }

  ngOnInit(): void {
    this.loadOffers();
    this.loadApplications();
  }

  loadOffers(): void {
    this.offerService.getOfferTitles().subscribe({
      next: (data) => this.offers.set(data),
      error: (err) => console.error('Failed to load offer titles', err),
    });
  }

  loadApplications(): void {
    this.isLoading.set(true);
    const offerId = this.selectedOfferId() || undefined;
    this.applicationService.getAllApplications(offerId).subscribe({
      next: (data) => {
        this.applications.set(data);
        this.isLoading.set(false);
      },
      error: (err) => {
        this.isLoading.set(false);
        this.showToast('Erreur lors du chargement des candidatures.', 'error');
        console.error('Failed to load applications', err);
      },
    });
  }

  onOfferFilterChange(offerId: string): void {
    this.selectedOfferId.set(offerId);
    this.loadApplications();
  }

  onScoreFilterChange(score: number | null): void {
    this.minScoreFilter.set(score);
  }

  // --- Drag and Drop Handlers ---

  onDragStart(event: DragEvent, app: ApplicationSummaryResponse): void {
    this.draggedApplicationId.set(app.id);
    if (event.dataTransfer) {
      event.dataTransfer.effectAllowed = 'move';
      event.dataTransfer.setData('text/plain', app.id);
    }
  }

  onDragEnd(): void {
    this.draggedApplicationId.set(null);
    this.dragOverColumn.set(null);
  }

  onDragOver(event: DragEvent, columnKey: string): void {
    event.preventDefault();
    if (event.dataTransfer) {
      event.dataTransfer.dropEffect = 'move';
    }
    this.dragOverColumn.set(columnKey);
  }

  onDragLeave(columnKey: string): void {
    if (this.dragOverColumn() === columnKey) {
      this.dragOverColumn.set(null);
    }
  }

  onDrop(event: DragEvent, targetStatus: WorkflowStatus): void {
    event.preventDefault();
    this.dragOverColumn.set(null);

    const appId = this.draggedApplicationId() || event.dataTransfer?.getData('text/plain');
    this.draggedApplicationId.set(null);

    if (!appId) return;

    const currentApp = this.applications().find((a) => a.id === appId);
    if (!currentApp || currentApp.status === targetStatus) return;

    const previousStatus = currentApp.status;

    // Optimistic UI Update
    this.applications.update((apps) =>
      apps.map((a) => (a.id === appId ? { ...a, status: targetStatus } : a)),
    );

    // Call backend API
    this.applicationService.updateApplicationStatus(appId, targetStatus).subscribe({
      next: () => {
        const colConfig = this.columns.find((c) => c.key === targetStatus);
        const colLabel = colConfig ? colConfig.label : targetStatus;
        this.showToast(`Statut mis à jour : ${colLabel}`, 'success');

        // Check if this status triggers the Send Email Modal prompt
        if (colConfig?.promptEmail) {
          this.openEmailModalFor(currentApp, targetStatus);
        }
      },
      error: (err) => {
        // Revert on error
        this.applications.update((apps) =>
          apps.map((a) => (a.id === appId ? { ...a, status: previousStatus } : a)),
        );
        this.showToast('Erreur lors du changement de statut.', 'error');
        console.error('Failed to update status', err);
      },
    });
  }

  // --- Modal & Menu Actions ---

  toggleCardMenu(appId: string, event: MouseEvent): void {
    event.stopPropagation();
    if (this.openMenuCardId() === appId) {
      this.openMenuCardId.set(null);
    } else {
      this.openMenuCardId.set(appId);
    }
  }

  openEmailModalFor(app: ApplicationSummaryResponse, targetStatus?: string): void {
    const candidateName = app.candidate
      ? `${app.candidate.firstName || ''} ${app.candidate.lastName || ''}`.trim() || 'Candidat'
      : 'Candidat';

    this.emailModalState.set({
      isOpen: true,
      applicationId: app.id,
      candidateName,
      candidateEmail: app.candidate?.email || '',
      offerTitle: app.offerTitle || 'Offre',
      targetStatus: targetStatus || (app.status as string) || null,
    });
  }

  closeEmailModal(): void {
    this.emailModalState.update((s) => ({ ...s, isOpen: false }));
  }

  onEmailSent(): void {
    this.showToast('Email envoyé avec succès !', 'success');
  }

  navigateToCandidate(appId: string, event: MouseEvent): void {
    const target = event.target as HTMLElement;
    if (target.closest('button') || target.closest('.popover-menu')) {
      return;
    }
    this.router.navigate(['/hr/candidates', appId]);
  }

  // --- Helpers ---

  getCandidateInitials(app: ApplicationSummaryResponse): string {
    const first = app.candidate?.firstName?.charAt(0) || '';
    const last = app.candidate?.lastName?.charAt(0) || '';
    return (first + last).toUpperCase() || 'C';
  }

  getScoreBadgeClass(score: number | null): string {
    if (score === null || score === undefined)
      return 'bg-slate-100 text-slate-600 border-slate-200';
    if (score >= 80) return 'bg-emerald-50 text-emerald-700 border-emerald-200';
    if (score >= 60) return 'bg-amber-50 text-amber-700 border-amber-200';
    return 'bg-rose-50 text-rose-700 border-rose-200';
  }

  private showToast(msg: string, type: 'success' | 'error' = 'success'): void {
    this.toastMessage.set(msg);
    this.toastType.set(type);
    setTimeout(() => {
      this.toastMessage.set(null);
    }, 4000);
  }
}
