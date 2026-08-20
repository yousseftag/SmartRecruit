import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterModule } from '@angular/router';
import { OfferService } from '../../../core/services/offer.service';
import {
  OfferPublicSummaryResponse,
  ContractType,
  CONTRACT_TYPES,
} from '../../../core/models/offer.model';
import { ExperienceFormatPipe } from '../../../shared/pipes/experience-format.pipe';

@Component({
  selector: 'app-careers-list',
  standalone: true,
  imports: [CommonModule, RouterModule, DatePipe, ExperienceFormatPipe],
  templateUrl: './careers-list.html',
})
export class CareersList implements OnInit {
  private offerService = inject(OfferService);

  readonly contractTypes = CONTRACT_TYPES;
  readonly offers = signal<OfferPublicSummaryResponse[]>([]);
  readonly isLoading = signal(true);
  readonly error = signal(false);
  readonly errorMessage = signal('');
  readonly searchQuery = signal('');
  readonly selectedContract = signal<ContractType | 'ALL'>('ALL');

  readonly filteredOffers = computed(() => {
    let list = this.offers();
    const contract = this.selectedContract();
    const query = this.searchQuery().toLowerCase().trim();

    if (contract !== 'ALL') {
      list = list.filter(
        (o) => o.contractType?.toLowerCase() === contract.toLowerCase()
      );
    }

    if (query) {
      list = list.filter(
        (o) =>
          o.title.toLowerCase().includes(query) ||
          (o.contractType && o.contractType.toLowerCase().includes(query)) ||
          (o.localization && o.localization.toLowerCase().includes(query))
      );
    }

    return list;
  });

  ngOnInit() {
    this.offerService.getPublicOffers().subscribe({
      next: (data) => {
        this.offers.set(data);
        this.isLoading.set(false);
      },
      error: (err) => {
        this.error.set(true);
        this.errorMessage.set(err.message || 'Erreur lors du chargement des offres.');
        this.isLoading.set(false);
      },
    });
  }

  setSearchQuery(event: Event) {
    const target = event.target as HTMLInputElement;
    this.searchQuery.set(target.value);
  }
}
