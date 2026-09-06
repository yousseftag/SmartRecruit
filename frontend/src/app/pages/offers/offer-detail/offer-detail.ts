import { Component, OnInit, OnDestroy, inject, signal, computed } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import {
  LucideArrowLeft,
  LucideMapPin,
  LucideClock,
  LucideCalendar,
  LucidePencil,
  LucideCheckCircle2,
  LucideXCircle,
  LucideRotateCcw,
  LucideRefreshCw,
  LucideUsers,
  LucideSparkles,
  LucideAlertTriangle,
  LucideCheck,
  LucideFileText,
} from '@lucide/angular';
import { Subscription, interval } from 'rxjs';
import { OfferService } from '../../../core/services/offer.service';
import { AuthService } from '../../../core/auth/auth.service';
import { OfferInternalResponse } from '../../../core/models/offer.model';
import { StatusBadge } from '../../../shared/components/status-badge/status-badge';
import { ScoreGauge } from '../../../shared/components/score-gauge/score-gauge';
import { WeightedCriteriaGrid } from '../../../shared/components/weighted-criteria-grid/weighted-criteria-grid';
import { MarkdownPipe } from '../../../shared/pipes/markdown.pipe';

@Component({
  selector: 'app-offer-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    DatePipe,
    MarkdownPipe,
    StatusBadge,
    ScoreGauge,
    WeightedCriteriaGrid,
    LucideArrowLeft,
    LucideMapPin,
    LucideClock,
    LucideCalendar,
    LucidePencil,
    LucideCheckCircle2,
    LucideXCircle,
    LucideRotateCcw,
    LucideRefreshCw,
    LucideUsers,
    LucideSparkles,
    LucideAlertTriangle,
    LucideCheck,
    LucideFileText,
  ],
  templateUrl: './offer-detail.html',
})
export class OfferDetail implements OnInit, OnDestroy {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private offerService = inject(OfferService);
  private authService = inject(AuthService);

  readonly offerId = signal<string | null>(null);
  readonly offer = signal<OfferInternalResponse | null>(null);
  readonly isLoading = signal(true);
  readonly isError = signal(false);
  readonly isActionInProgress = signal(false);

  // Notifications
  readonly toastMessage = signal<string | null>(null);
  readonly toastType = signal<'success' | 'error'>('success');

  private pollSub?: Subscription;

  readonly canManage = computed(
    () => this.authService.hasRole('HR_ADMIN') || this.authService.hasRole('RECRUITER'),
  );

  readonly isAiPending = computed(() => this.offer()?.offerAiStatus === 'PENDING');

  readonly missingSkills = computed<string[]>(() => {
    const list = this.offer()?.extractedRequirements?.['missing_from_criteria'];
    return Array.isArray(list) ? list : [];
  });

  readonly aiInsights = computed<string | null>(() => {
    return this.offer()?.extractedRequirements?.['insights'] || null;
  });

  readonly experienceLabel = computed(() => {
    const months = this.offer()?.categoryCriteria?.experience ?? 0;
    if (!months || months <= 0) return 'Débutant / Non spécifié';
    const years = Math.floor(months / 12);
    const remMonths = months % 12;
    if (years > 0 && remMonths > 0) {
      return `${months} mois (${years} an${years > 1 ? 's' : ''} et ${remMonths} mois)`;
    } else if (years > 0) {
      return `${months} mois (${years} an${years > 1 ? 's' : ''})`;
    } else {
      return `${months} mois`;
    }
  });

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.offerId.set(id);
      this.loadOffer(id);
    } else {
      this.isError.set(true);
      this.isLoading.set(false);
    }
  }

  ngOnDestroy() {
    this.stopPolling();
  }

  loadOffer(id: string) {
    this.isLoading.set(true);
    this.offerService.getOfferById(id).subscribe({
      next: (data) => {
        this.offer.set(data);
        this.isLoading.set(false);
        if (data.offerAiStatus === 'PENDING') {
          this.startPolling(id);
        }
      },
      error: () => {
        this.isLoading.set(false);
        this.isError.set(true);
      },
    });
  }

  private startPolling(id: string) {
    this.stopPolling();
    this.pollSub = interval(2500).subscribe(() => {
      this.offerService.getOfferById(id).subscribe({
        next: (fresh) => {
          this.offer.set(fresh);
          if (fresh.offerAiStatus !== 'PENDING') {
            this.stopPolling();
            if (fresh.offerAiStatus === 'SUCCESS') {
              this.showToast('Analyse IA terminée avec succès !', 'success');
            } else if (fresh.offerAiStatus === 'FAILED') {
              this.showToast("L'analyse IA a échoué. Vous pouvez la relancer.", 'error');
            }
          }
        },
        error: () => {},
      });
    });
  }

  private stopPolling() {
    this.pollSub?.unsubscribe();
    this.pollSub = undefined;
  }

  // --- State Machine Lifecycle Actions ---

  publishOffer() {
    const o = this.offer();
    if (!o) return;

    if (o.status !== 'DRAFT') {
      this.showToast('Seule une offre en brouillon peut être publiée.', 'error');
      return;
    }
    if (o.offerAiStatus === 'PENDING') {
      this.showToast("L'offre est en cours d'analyse par l'IA. Veuillez patienter.", 'error');
      return;
    }
    if (o.offerAiStatus !== 'SUCCESS') {
      this.showToast("L'analyse IA doit être en succès pour pouvoir publier.", 'error');
      return;
    }

    this.isActionInProgress.set(true);
    this.offerService.publishOffer(o.id).subscribe({
      next: (updated) => {
        this.offer.set(updated);
        this.isActionInProgress.set(false);
        this.showToast(`L'offre "${updated.title}" est maintenant active et publiée !`, 'success');
      },
      error: (err) => {
        this.isActionInProgress.set(false);
        this.showToast(err.error?.message || 'Erreur lors de la publication.', 'error');
      },
    });
  }

  closeOffer() {
    const o = this.offer();
    if (!o) return;

    this.isActionInProgress.set(true);
    this.offerService.closeOffer(o.id).subscribe({
      next: (updated) => {
        this.offer.set(updated);
        this.isActionInProgress.set(false);
        this.showToast(`L'offre "${updated.title}" a été clôturée.`, 'success');
      },
      error: (err) => {
        this.isActionInProgress.set(false);
        this.showToast(err.error?.message || 'Erreur lors de la clôture.', 'error');
      },
    });
  }

  reopenOffer() {
    const o = this.offer();
    if (!o) return;

    this.isActionInProgress.set(true);
    this.offerService.reopenOffer(o.id).subscribe({
      next: (updated) => {
        this.offer.set(updated);
        this.isActionInProgress.set(false);
        this.showToast(`L'offre "${updated.title}" a été réouverte avec succès.`, 'success');
      },
      error: (err) => {
        this.isActionInProgress.set(false);
        this.showToast(err.error?.message || 'Erreur lors de la réouverture.', 'error');
      },
    });
  }

  reprocessOffer() {
    const o = this.offer();
    if (!o) return;

    this.isActionInProgress.set(true);
    this.offerService.reprocessOffer(o.id).subscribe({
      next: (updated) => {
        this.offer.set(updated);
        this.isActionInProgress.set(false);
        this.showToast("Re-vectorisation IA lancée. L'analyse est en cours...", 'success');
        this.startPolling(o.id);
      },
      error: (err) => {
        this.isActionInProgress.set(false);
        this.showToast(err.error?.message || "Erreur lors du relancement de l'analyse.", 'error');
      },
    });
  }

  viewCandidates() {
    const id = this.offerId();
    if (id) {
      this.router.navigate(['/hr/candidates'], { queryParams: { offerId: id } });
    }
  }

  showToast(message: string, type: 'success' | 'error' = 'success') {
    this.toastMessage.set(message);
    this.toastType.set(type);
    setTimeout(() => {
      this.toastMessage.set(null);
    }, 4000);
  }
}
