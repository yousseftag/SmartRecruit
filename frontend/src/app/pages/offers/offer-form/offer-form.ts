import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import {
  LucideArrowLeft,
  LucideSave,
  LucideAlertTriangle,
  LucideBriefcase,
  LucideCheck,
  LucideFileText,
} from '@lucide/angular';
import { OfferService } from '../../../core/services/offer.service';
import {
  CategoryCriteria,
  CategoryWeights,
  ContractType,
  CONTRACT_TYPES,
  CreateOfferRequest,
  OfferInternalResponse,
} from '../../../core/models/offer.model';
import { WeightedCriteriaGrid } from '../../../shared/components/weighted-criteria-grid/weighted-criteria-grid';

@Component({
  selector: 'app-offer-form',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    WeightedCriteriaGrid,
    LucideArrowLeft,
    LucideSave,
    LucideAlertTriangle,
    LucideBriefcase,
    LucideCheck,
    LucideFileText,
  ],
  templateUrl: './offer-form.html',
})
export class OfferForm implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private offerService = inject(OfferService);

  readonly contractTypeOptions = CONTRACT_TYPES;

  // Edit Mode state
  readonly isEditMode = signal(false);
  readonly offerId = signal<string | null>(null);
  readonly existingOffer = signal<OfferInternalResponse | null>(null);
  readonly isLoading = signal(false);
  readonly isSaving = signal(false);
  readonly isWeightValid = signal(true);

  // Form Fields
  title = signal('');
  contractType = signal<ContractType>('CDI');
  durationMonths = signal<number | null>(null);
  minScore = signal<number>(70);
  descriptionMarkdown = signal('');

  // Category Weights & Criteria
  weights = signal<CategoryWeights>({
    skills: 40,
    experience: 25,
    coursework: 15,
    languages: 10,
    localization: 10,
  });

  criteria = signal<CategoryCriteria>({
    skills: [],
    skill_weights: {},
    experience: 0,
    coursework: [],
    languages: [],
    localization: '',
  });

  // Feedback notifications
  readonly toastMessage = signal<string | null>(null);
  readonly toastType = signal<'success' | 'error'>('success');

  readonly canSubmit = computed(() => {
    return (
      this.title().trim().length > 0 &&
      this.isWeightValid() &&
      !this.isSaving() &&
      !this.isLoading()
    );
  });

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode.set(true);
      this.offerId.set(id);
      this.loadOffer(id);
    }
  }

  loadOffer(id: string) {
    this.isLoading.set(true);
    this.offerService.getOfferById(id).subscribe({
      next: (offer) => {
        this.existingOffer.set(offer);
        this.title.set(offer.title);
        this.contractType.set((offer.contractType as ContractType) || 'CDI');
        this.durationMonths.set(offer.durationMonths ?? null);
        this.minScore.set(offer.minScore ?? 70);
        this.descriptionMarkdown.set(offer.descriptionMarkdown || '');
        if (offer.categoryWeights) {
          this.weights.set({ ...offer.categoryWeights });
        }
        if (offer.categoryCriteria) {
          this.criteria.set({
            skills: [...(offer.categoryCriteria.skills || [])],
            skill_weights: { ...(offer.categoryCriteria.skill_weights || {}) },
            experience: offer.categoryCriteria.experience ?? 0,
            coursework: [...(offer.categoryCriteria.coursework || [])],
            languages: [...(offer.categoryCriteria.languages || [])],
            localization: offer.categoryCriteria.localization || '',
          });
        }
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        this.showToast("Impossible de charger l'offre.", 'error');
      },
    });
  }

  onSumValidChange(valid: boolean) {
    this.isWeightValid.set(valid);
  }

  submit() {
    if (!this.canSubmit()) return;

    const req: CreateOfferRequest = {
      title: this.title().trim(),
      contractType: this.contractType(),
      durationMonths: this.durationMonths() ? Number(this.durationMonths()) : null,
      minScore: Number(this.minScore()) || 70,
      descriptionMarkdown: this.descriptionMarkdown().trim() || null,
      categoryWeights: this.weights(),
      categoryCriteria: this.criteria(),
    };

    this.isSaving.set(true);

    if (this.isEditMode() && this.offerId()) {
      this.offerService.updateOffer(this.offerId()!, req).subscribe({
        next: (updated) => {
          this.isSaving.set(false);
          this.showToast("L'offre a été mise à jour avec succès.", 'success');
          setTimeout(() => {
            this.router.navigate(['/hr/offers', updated.id]);
          }, 600);
        },
        error: (err) => {
          this.isSaving.set(false);
          const msg =
            err.error?.message || "Une erreur est survenue lors de la mise à jour de l'offre.";
          this.showToast(msg, 'error');
        },
      });
    } else {
      this.offerService.createOffer(req).subscribe({
        next: (created) => {
          this.isSaving.set(false);
          this.showToast("L'offre a été créée avec succès en brouillon.", 'success');
          setTimeout(() => {
            this.router.navigate(['/hr/offers', created.id]);
          }, 600);
        },
        error: (err) => {
          this.isSaving.set(false);
          const msg =
            err.error?.message || "Une erreur est survenue lors de la création de l'offre.";
          this.showToast(msg, 'error');
        },
      });
    }
  }

  cancel() {
    if (this.isEditMode() && this.offerId()) {
      this.router.navigate(['/hr/offers', this.offerId()]);
    } else {
      this.router.navigate(['/hr/offers']);
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
