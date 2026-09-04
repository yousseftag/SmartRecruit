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
import { RouterModule, Router } from '@angular/router';
import {
  LucidePlus,
  LucideSearch,
  LucideFilter,
  LucideChevronDown,
  LucideChevronLeft,
  LucideChevronRight,
  LucideMoreVertical,
  LucideEye,
  LucidePencil,
  LucideRefreshCw,
  LucideCheckCircle2,
  LucideXCircle,
  LucideBriefcase,
  LucideRotateCcw,
  LucideCheck,
  LucideAlertTriangle,
} from '@lucide/angular';
import { Subscription, interval } from 'rxjs';
import { OfferService } from '../../../core/services/offer.service';
import { AuthService } from '../../../core/auth/auth.service';
import {
  OfferInternalResponse,
  OfferStatus,
  OfferAiStatus,
  CONTRACT_TYPES,
} from '../../../core/models/offer.model';
import { StatusBadge } from '../../../shared/components/status-badge/status-badge';

export type OfferSortOption = 'NEWEST' | 'OLDEST' | 'TITLE_ASC' | 'SCORE_DESC';

@Component({
  selector: 'app-offers-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    DatePipe,
    StatusBadge,
    LucidePlus,
    LucideSearch,
    LucideFilter,
    LucideChevronDown,
    LucideChevronLeft,
    LucideChevronRight,
    LucideMoreVertical,
    LucideEye,
    LucidePencil,
    LucideRefreshCw,
    LucideCheckCircle2,
    LucideXCircle,
    LucideBriefcase,
    LucideRotateCcw,
    LucideCheck,
    LucideAlertTriangle,
  ],
  templateUrl: './offers-list.html',
})
export class OffersList implements OnInit, OnDestroy {
  private offerService = inject(OfferService);
  private authService = inject(AuthService);
  private router = inject(Router);
  private elementRef = inject(ElementRef);

  // Offers State
  readonly offers = signal<OfferInternalResponse[]>([]);
  readonly isLoading = signal(true);
  readonly isActionInProgress = signal(false);

  // Search & Filters
  readonly searchQuery = signal('');
  readonly selectedStatus = signal<'ALL' | OfferStatus>('ALL');
  readonly selectedAiStatus = signal<'ALL' | OfferAiStatus>('ALL');
  readonly selectedContractType = signal<string>('ALL');
  readonly sortBy = signal<OfferSortOption>('NEWEST');

  // Pagination
  readonly currentPage = signal(1);
  readonly pageSizes = [10, 25, 50];
  readonly pageSize = signal(10);

  // Dropdowns state
  readonly isStatusDropdownOpen = signal(false);
  readonly isAiStatusDropdownOpen = signal(false);
  readonly isContractDropdownOpen = signal(false);
  readonly isSortDropdownOpen = signal(false);
  readonly activeActionOfferId = signal<string | null>(null);

  // Toast
  readonly toastMessage = signal<string | null>(null);
  readonly toastType = signal<'success' | 'error'>('success');

  // Auto-refresh subscription for PENDING AI statuses
  private pollSub?: Subscription;

  readonly canCreate = computed(
    () => this.authService.hasRole('HR_ADMIN') || this.authService.hasRole('RECRUITER'),
  );

  readonly canManage = computed(
    () => this.authService.hasRole('HR_ADMIN') || this.authService.hasRole('RECRUITER'),
  );

  // --- Filtered Offers ---
  readonly filteredOffers = computed(() => {
    let list = [...this.offers()];

    // Search query
    const q = this.searchQuery().trim().toLowerCase();
    if (q) {
      list = list.filter(
        (o) =>
          o.title.toLowerCase().includes(q) ||
          (o.contractType && o.contractType.toLowerCase().includes(q)) ||
          (o.categoryCriteria?.localization &&
            o.categoryCriteria.localization.toLowerCase().includes(q)),
      );
    }

    // Status filter
    const st = this.selectedStatus();
    if (st !== 'ALL') {
      list = list.filter((o) => o.status === st);
    }

    // AI Status filter
    const aiSt = this.selectedAiStatus();
    if (aiSt !== 'ALL') {
      list = list.filter((o) => o.offerAiStatus === aiSt);
    }

    // Contract filter
    const ct = this.selectedContractType();
    if (ct !== 'ALL') {
      list = list.filter((o) => o.contractType === ct);
    }

    // Sorting
    switch (this.sortBy()) {
      case 'OLDEST':
        list.sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime());
        break;
      case 'TITLE_ASC':
        list.sort((a, b) => a.title.localeCompare(b.title));
        break;
      case 'SCORE_DESC':
        list.sort((a, b) => (b.minScore ?? 0) - (a.minScore ?? 0));
        break;
      case 'NEWEST':
      default:
        list.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
        break;
    }

    return list;
  });

  readonly totalCount = computed(() => this.filteredOffers().length);

  readonly totalPages = computed(() => Math.max(1, Math.ceil(this.totalCount() / this.pageSize())));

  readonly paginatedOffers = computed(() => {
    const start = (this.currentPage() - 1) * this.pageSize();
    return this.filteredOffers().slice(start, start + this.pageSize());
  });

  readonly startIndex = computed(() => {
    if (this.totalCount() === 0) return 0;
    return (this.currentPage() - 1) * this.pageSize() + 1;
  });

  readonly endIndex = computed(() =>
    Math.min(this.currentPage() * this.pageSize(), this.totalCount()),
  );

  ngOnInit() {
    this.loadOffers();
    // Start polling every 3 seconds if any offer has PENDING AI status
    this.pollSub = interval(3000).subscribe(() => {
      const hasPending = this.offers().some((o) => o.offerAiStatus === 'PENDING');
      if (hasPending) {
        this.silentRefreshOffers();
      }
    });
  }

  ngOnDestroy() {
    this.pollSub?.unsubscribe();
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    if (!this.elementRef.nativeElement.contains(event.target)) {
      this.closeAllDropdowns();
    }
  }

  closeAllDropdowns() {
    this.isStatusDropdownOpen.set(false);
    this.isAiStatusDropdownOpen.set(false);
    this.isContractDropdownOpen.set(false);
    this.isSortDropdownOpen.set(false);
    this.activeActionOfferId.set(null);
  }

  loadOffers() {
    this.isLoading.set(true);
    this.offerService.getAllOffers().subscribe({
      next: (data) => {
        this.offers.set(data);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        this.showToast('Erreur lors du chargement des offres.', 'error');
      },
    });
  }

  silentRefreshOffers() {
    this.offerService.getAllOffers().subscribe({
      next: (data) => this.offers.set(data),
      error: () => {},
    });
  }

  // --- Filter handlers ---

  setSearchQuery(event: Event) {
    const val = (event.target as HTMLInputElement).value;
    this.searchQuery.set(val);
    this.currentPage.set(1);
  }

  selectStatus(status: 'ALL' | OfferStatus) {
    this.selectedStatus.set(status);
    this.currentPage.set(1);
    this.isStatusDropdownOpen.set(false);
  }

  selectAiStatus(status: 'ALL' | OfferAiStatus) {
    this.selectedAiStatus.set(status);
    this.currentPage.set(1);
    this.isAiStatusDropdownOpen.set(false);
  }

  selectContractType(type: string) {
    this.selectedContractType.set(type);
    this.currentPage.set(1);
    this.isContractDropdownOpen.set(false);
  }

  selectSort(option: OfferSortOption) {
    this.sortBy.set(option);
    this.currentPage.set(1);
    this.isSortDropdownOpen.set(false);
  }

  setPageSize(size: number) {
    this.pageSize.set(size);
    this.currentPage.set(1);
  }

  goToPage(page: number) {
    if (page >= 1 && page <= this.totalPages()) {
      this.currentPage.set(page);
    }
  }

  // --- Toggle Dropdowns ---

  toggleStatusDropdown(e: Event) {
    e.stopPropagation();
    const cur = this.isStatusDropdownOpen();
    this.closeAllDropdowns();
    this.isStatusDropdownOpen.set(!cur);
  }

  toggleAiStatusDropdown(e: Event) {
    e.stopPropagation();
    const cur = this.isAiStatusDropdownOpen();
    this.closeAllDropdowns();
    this.isAiStatusDropdownOpen.set(!cur);
  }

  toggleContractDropdown(e: Event) {
    e.stopPropagation();
    const cur = this.isContractDropdownOpen();
    this.closeAllDropdowns();
    this.isContractDropdownOpen.set(!cur);
  }

  toggleSortDropdown(e: Event) {
    e.stopPropagation();
    const cur = this.isSortDropdownOpen();
    this.closeAllDropdowns();
    this.isSortDropdownOpen.set(!cur);
  }

  toggleActionMenu(offerId: string, e: Event) {
    e.stopPropagation();
    if (this.activeActionOfferId() === offerId) {
      this.activeActionOfferId.set(null);
    } else {
      this.closeAllDropdowns();
      this.activeActionOfferId.set(offerId);
    }
  }

  // --- Lifecycle Action Handlers ---

  publishOffer(offer: OfferInternalResponse, e: Event) {
    e.stopPropagation();
    this.closeAllDropdowns();

    if (offer.status !== 'DRAFT') {
      this.showToast('Seule une offre en brouillon peut être publiée.', 'error');
      return;
    }
    if (offer.offerAiStatus === 'PENDING') {
      this.showToast("L'analyse IA est en cours. Veuillez patienter.", 'error');
      return;
    }
    if (offer.offerAiStatus !== 'SUCCESS') {
      this.showToast("L'analyse IA doit réussir avant de publier l'offre.", 'error');
      return;
    }

    this.isActionInProgress.set(true);
    this.offerService.publishOffer(offer.id).subscribe({
      next: (updated) => {
        this.isActionInProgress.set(false);
        this.updateOfferInList(updated);
        this.showToast(`L'offre "${updated.title}" est maintenant active et publiée.`, 'success');
      },
      error: (err) => {
        this.isActionInProgress.set(false);
        this.showToast(err.error?.message || 'Erreur lors de la publication.', 'error');
      },
    });
  }

  closeOffer(offer: OfferInternalResponse, e: Event) {
    e.stopPropagation();
    this.closeAllDropdowns();

    this.isActionInProgress.set(true);
    this.offerService.closeOffer(offer.id).subscribe({
      next: (updated) => {
        this.isActionInProgress.set(false);
        this.updateOfferInList(updated);
        this.showToast(`L'offre "${updated.title}" a été clôturée.`, 'success');
      },
      error: (err) => {
        this.isActionInProgress.set(false);
        this.showToast(err.error?.message || 'Erreur lors de la clôture.', 'error');
      },
    });
  }

  reopenOffer(offer: OfferInternalResponse, e: Event) {
    e.stopPropagation();
    this.closeAllDropdowns();

    this.isActionInProgress.set(true);
    this.offerService.reopenOffer(offer.id).subscribe({
      next: (updated) => {
        this.isActionInProgress.set(false);
        this.updateOfferInList(updated);
        this.showToast(`L'offre "${updated.title}" a été réouverte.`, 'success');
      },
      error: (err) => {
        this.isActionInProgress.set(false);
        this.showToast(err.error?.message || 'Erreur lors de la réouverture.', 'error');
      },
    });
  }

  reprocessOffer(offer: OfferInternalResponse, e: Event) {
    e.stopPropagation();
    this.closeAllDropdowns();

    this.isActionInProgress.set(true);
    this.offerService.reprocessOffer(offer.id).subscribe({
      next: (updated) => {
        this.isActionInProgress.set(false);
        this.updateOfferInList(updated);
        this.showToast(`Re-vectorisation IA lancée pour "${updated.title}".`, 'success');
      },
      error: (err) => {
        this.isActionInProgress.set(false);
        this.showToast(err.error?.message || 'Erreur lors du relancement IA.', 'error');
      },
    });
  }

  private updateOfferInList(updated: OfferInternalResponse) {
    const current = this.offers().map((o) => (o.id === updated.id ? updated : o));
    this.offers.set(current);
  }

  showToast(message: string, type: 'success' | 'error' = 'success') {
    this.toastMessage.set(message);
    this.toastType.set(type);
    setTimeout(() => {
      this.toastMessage.set(null);
    }, 4000);
  }
}
