import {
  Component,
  inject,
  OnInit,
  OnDestroy,
  signal,
  computed,
  HostListener,
  ElementRef,
} from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { ApplicationService } from '../../../core/services/application.service';
import { OfferService } from '../../../core/services/offer.service';
import {
  ApplicationSummaryResponse,
  WorkflowStatus,
  OfferTitleResponse,
  UserRole,
} from '../../../core/models';
import { AuthService } from '../../../core/auth/auth.service';
import { StatusBadge } from '../../../shared/components/status-badge/status-badge';
import { Subscription, interval, switchMap, takeWhile } from 'rxjs';
import { environment } from '../../../../environments/environment';

export type DatePeriod = 'ALL' | '24H' | '7D' | '30D';
export type SortOption = 'NEWEST' | 'OLDEST' | 'SCORE_DESC' | 'SCORE_ASC';

@Component({
  selector: 'app-candidates-list',
  standalone: true,
  imports: [CommonModule, RouterModule, DatePipe, StatusBadge],
  templateUrl: './candidates-list.html',
})
export class CandidatesList implements OnInit, OnDestroy {
  private applicationService = inject(ApplicationService);
  private offerService = inject(OfferService);
  private authService = inject(AuthService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private elementRef = inject(ElementRef);

  readonly canImport = computed(() =>
    this.authService.hasAnyRole([UserRole.HR_ADMIN, UserRole.RECRUITER]),
  );

  readonly offers = signal<OfferTitleResponse[]>([]);
  readonly applications = signal<ApplicationSummaryResponse[]>([]);
  readonly isLoading = signal(true);
  readonly searchQuery = signal('');
  readonly selectedOfferId = signal<string>('ALL');
  readonly currentTab = signal<WorkflowStatus | 'ALL'>('ALL');
  readonly selectedPeriod = signal<DatePeriod>('ALL');
  readonly sortBy = signal<SortOption>('NEWEST');

  // Pagination Signals
  readonly currentPage = signal(1);
  readonly pageSize = signal(10);
  readonly pageSizes = [10, 25, 50];

  // Custom Dropdown Open States
  readonly isOfferDropdownOpen = signal(false);
  readonly isStatusDropdownOpen = signal(false);
  readonly isPeriodDropdownOpen = signal(false);
  readonly isSortDropdownOpen = signal(false);

  private pollingSub?: Subscription;

  // Status filter dropdown options with dot colors
  readonly statusOptions: {
    label: string;
    value: WorkflowStatus | 'ALL';
    color: string;
  }[] = [
    { label: 'Tous les statuts', value: 'ALL', color: 'bg-slate-400' },
    { label: 'Nouveau', value: 'NEW', color: 'bg-blue' },
    { label: 'Présélectionné', value: 'SHORTLISTED', color: 'bg-green' },
    { label: 'En entretien', value: 'INTERVIEWING', color: 'bg-purple-500' },
    { label: 'Relancé / Suivi', value: 'FOLLOW_UP', color: 'bg-amber-500' },
    { label: 'Embauché', value: 'HIRED', color: 'bg-emerald-600' },
    { label: 'Refusé', value: 'REJECTED', color: 'bg-red' },
    { label: 'Archivé', value: 'ARCHIVED', color: 'bg-slate-500' },
  ];

  // Period options
  readonly periodOptions: { label: string; value: DatePeriod }[] = [
    { label: 'Toutes les dates', value: 'ALL' },
    { label: 'Dernières 24h', value: '24H' },
    { label: '7 derniers jours', value: '7D' },
    { label: '30 derniers jours', value: '30D' },
  ];

  // Sort options
  readonly sortOptions: { label: string; value: SortOption }[] = [
    { label: "Plus récents d'abord", value: 'NEWEST' },
    { label: "Plus anciens d'abord", value: 'OLDEST' },
    { label: 'Score IA (Décroissant)', value: 'SCORE_DESC' },
    { label: 'Score IA (Croissant)', value: 'SCORE_ASC' },
  ];

  // Computed selected display labels
  readonly selectedOfferLabel = computed(() => {
    const id = this.selectedOfferId();
    if (id === 'ALL') return 'Toutes les offres';
    const found = this.offers().find((o) => o.id === id);
    return found ? found.title : 'Offre sélectionnée';
  });

  readonly selectedStatusOption = computed(() => {
    const status = this.currentTab();
    return this.statusOptions.find((s) => s.value === status) || this.statusOptions[0];
  });

  readonly selectedPeriodLabel = computed(() => {
    const p = this.selectedPeriod();
    const found = this.periodOptions.find((opt) => opt.value === p);
    return found ? found.label : 'Période';
  });

  readonly selectedSortLabel = computed(() => {
    const s = this.sortBy();
    const found = this.sortOptions.find((opt) => opt.value === s);
    return found ? found.label : 'Tri';
  });

  // Filtered and Sorted Applications
  readonly filteredApplications = computed(() => {
    let list = [...this.applications()];

    // 1. Filter by workflow status tab
    const tab = this.currentTab();
    if (tab !== 'ALL') {
      list = list.filter((app) => app.status === tab);
    }

    // 2. Filter by search query (Candidate name, email, offer title, or current title)
    const query = this.searchQuery().toLowerCase().trim();
    if (query) {
      list = list.filter((app) => {
        const name =
          `${app.candidate?.firstName || ''} ${app.candidate?.lastName || ''}`.toLowerCase();
        const email = (app.candidate?.email || '').toLowerCase();
        const offer = (app.offerTitle || '').toLowerCase();
        const jobTitle = (app.candidate?.currentJobTitle || '').toLowerCase();
        return (
          name.includes(query) ||
          email.includes(query) ||
          offer.includes(query) ||
          jobTitle.includes(query)
        );
      });
    }

    // 3. Filter by Date Period
    const period = this.selectedPeriod();
    if (period !== 'ALL') {
      const now = new Date().getTime();
      const cutoffMap: Record<DatePeriod, number> = {
        ALL: 0,
        '24H': 24 * 60 * 60 * 1000,
        '7D': 7 * 24 * 60 * 60 * 1000,
        '30D': 30 * 24 * 60 * 60 * 1000,
      };
      const cutoff = now - cutoffMap[period];
      list = list.filter((app) => new Date(app.appliedAt).getTime() >= cutoff);
    }

    // 4. Sorting
    const sort = this.sortBy();
    list.sort((a, b) => {
      if (sort === 'NEWEST') {
        return new Date(b.appliedAt).getTime() - new Date(a.appliedAt).getTime();
      }
      if (sort === 'OLDEST') {
        return new Date(a.appliedAt).getTime() - new Date(b.appliedAt).getTime();
      }
      if (sort === 'SCORE_DESC') {
        const sa = a.totalScore ?? -1;
        const sb = b.totalScore ?? -1;
        return sb - sa;
      }
      if (sort === 'SCORE_ASC') {
        const sa = a.totalScore ?? 999;
        const sb = b.totalScore ?? 999;
        return sa - sb;
      }
      return 0;
    });

    return list;
  });

  // Pagination Computations
  readonly totalCount = computed(() => this.filteredApplications().length);
  readonly totalPages = computed(() => Math.max(1, Math.ceil(this.totalCount() / this.pageSize())));

  readonly paginatedApplications = computed(() => {
    const list = this.filteredApplications();
    const page = Math.min(this.currentPage(), this.totalPages());
    const size = this.pageSize();
    const start = (page - 1) * size;
    return list.slice(start, start + size);
  });

  readonly paginationSummary = computed(() => {
    const total = this.totalCount();
    if (total === 0) return '0 candidature';
    const page = Math.min(this.currentPage(), this.totalPages());
    const size = this.pageSize();
    const start = (page - 1) * size + 1;
    const end = Math.min(page * size, total);
    return `Affichage de ${start} à ${end} sur ${total} candidature(s)`;
  });

  ngOnInit() {
    // 1. Fetch Offer titles for the filter dropdown
    this.offerService.getOfferTitles().subscribe({
      next: (titles) => this.offers.set(titles),
      error: (err) => console.error('Failed to load offer titles', err),
    });

    // 2. Subscribe to queryParams (e.g. ?offerId=xxx)
    this.route.queryParams.subscribe((params) => {
      const offerId = params['offerId'] || 'ALL';
      this.selectedOfferId.set(offerId);
      this.fetchApplications(offerId === 'ALL' ? undefined : offerId);
    });
  }

  ngOnDestroy() {
    this.stopPolling();
  }

  // Close dropdowns on outside click
  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    if (!this.elementRef.nativeElement.contains(event.target)) {
      this.closeAllDropdowns();
    }
  }

  private closeAllDropdowns() {
    this.isOfferDropdownOpen.set(false);
    this.isStatusDropdownOpen.set(false);
    this.isPeriodDropdownOpen.set(false);
    this.isSortDropdownOpen.set(false);
  }

  toggleOfferDropdown(event: Event) {
    event.stopPropagation();
    const next = !this.isOfferDropdownOpen();
    this.closeAllDropdowns();
    this.isOfferDropdownOpen.set(next);
  }

  toggleStatusDropdown(event: Event) {
    event.stopPropagation();
    const next = !this.isStatusDropdownOpen();
    this.closeAllDropdowns();
    this.isStatusDropdownOpen.set(next);
  }

  togglePeriodDropdown(event: Event) {
    event.stopPropagation();
    const next = !this.isPeriodDropdownOpen();
    this.closeAllDropdowns();
    this.isPeriodDropdownOpen.set(next);
  }

  toggleSortDropdown(event: Event) {
    event.stopPropagation();
    const next = !this.isSortDropdownOpen();
    this.closeAllDropdowns();
    this.isSortDropdownOpen.set(next);
  }

  selectOffer(offerId: string) {
    this.selectedOfferId.set(offerId);
    this.currentPage.set(1);
    this.isOfferDropdownOpen.set(false);

    // Update query params in URL
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: offerId === 'ALL' ? {} : { offerId },
      queryParamsHandling: '',
    });
  }

  selectStatus(status: WorkflowStatus | 'ALL') {
    this.currentTab.set(status);
    this.currentPage.set(1);
    this.isStatusDropdownOpen.set(false);
  }

  selectPeriod(period: DatePeriod) {
    this.selectedPeriod.set(period);
    this.currentPage.set(1);
    this.isPeriodDropdownOpen.set(false);
  }

  selectSort(sort: SortOption) {
    this.sortBy.set(sort);
    this.isSortDropdownOpen.set(false);
  }

  setPage(page: number) {
    if (page >= 1 && page <= this.totalPages()) {
      this.currentPage.set(page);
    }
  }

  setPageSize(size: number) {
    this.pageSize.set(size);
    this.currentPage.set(1);
  }

  fetchApplications(offerId?: string) {
    this.isLoading.set(true);
    this.applicationService.getAllApplications(offerId).subscribe({
      next: (apps) => {
        this.applications.set(apps);
        this.isLoading.set(false);
        this.startPollingIfPending(apps);
      },
      error: () => {
        this.isLoading.set(false);
      },
    });
  }

  setSearchQuery(event: Event) {
    const target = event.target as HTMLInputElement;
    this.searchQuery.set(target.value);
    this.currentPage.set(1);
  }

  // --- Progressive Status Polling for Pending AI Extractions ---
  private isExtractionPending(app: ApplicationSummaryResponse): boolean {
    return (
      app.extractionStatus === 'PENDING' ||
      (app.totalScore === null && app.extractionStatus !== 'FAILED')
    );
  }

  private startPollingIfPending(apps: ApplicationSummaryResponse[]) {
    this.stopPolling();

    const hasPending = apps.some((app) => this.isExtractionPending(app));
    if (!hasPending) return;

    const intervalMs = environment.pollingIntervalMs;
    const maxPolls = environment.pollingMaxAttempts;
    let pollCount = 0;

    this.pollingSub = interval(intervalMs)
      .pipe(
        takeWhile(() => pollCount < maxPolls),
        switchMap(() => {
          pollCount++;
          const offerId = this.selectedOfferId();
          return this.applicationService.getAllApplications(
            offerId === 'ALL' ? undefined : offerId,
          );
        }),
      )
      .subscribe({
        next: (latestApps) => {
          this.applications.set(latestApps);
          if (!latestApps.some((app) => this.isExtractionPending(app))) {
            this.stopPolling();
          }
        },
        error: (err) => console.error('Polling error', err),
      });
  }

  private stopPolling() {
    if (this.pollingSub) {
      this.pollingSub.unsubscribe();
      this.pollingSub = undefined;
    }
  }

  // Helper for Candidate Name display (Ghost Candidate support)
  getCandidateDisplayName(app: ApplicationSummaryResponse): {
    text: string;
    isGhost: boolean;
    isPending: boolean;
  } {
    if (app.candidate?.firstName || app.candidate?.lastName) {
      return {
        text: `${app.candidate.firstName || ''} ${app.candidate.lastName || ''}`.trim(),
        isGhost: false,
        isPending: false,
      };
    }

    if (app.totalScore === null) {
      return {
        text: 'Extraction en cours...',
        isGhost: true,
        isPending: true,
      };
    }

    return {
      text: 'Candidat Inconnu',
      isGhost: true,
      isPending: false,
    };
  }

  getInitials(app: ApplicationSummaryResponse): string {
    const fn = app.candidate?.firstName?.charAt(0) || '';
    const ln = app.candidate?.lastName?.charAt(0) || '';
    return (fn + ln).toUpperCase() || '?';
  }

  getScoreColorClass(
    score: number | null | undefined,
    minScore: number | null | undefined,
  ): string {
    if (score === null || score === undefined) return 'text-slate';
    if (minScore !== null && minScore !== undefined) {
      return score >= minScore ? 'text-green' : 'text-red';
    }
    if (score >= 75) return 'text-green';
    if (score >= 50) return 'text-amber-700 dark:text-amber-400';
    return 'text-red';
  }
}
