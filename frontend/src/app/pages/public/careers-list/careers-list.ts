import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { OfferService } from '../../../core/services/offer.service';
import { OfferPublicResponse } from '../../../core/models/offer.model';

@Component({
  selector: 'app-careers-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './careers-list.html',
})
export class CareersList implements OnInit {
  private offerService = inject(OfferService);

  readonly offers = signal<OfferPublicResponse[]>([]);
  readonly isLoading = signal(true);
  readonly error = signal(false);
  readonly errorMessage = signal('');

  ngOnInit() {
    this.offerService.getPublicOffers().subscribe({
      next: (data) => {
        this.offers.set(data);
        this.isLoading.set(false);
      },
      error: (err) => {
        this.error.set(true);
        this.errorMessage.set(err.message || JSON.stringify(err));
        this.isLoading.set(false);
      },
    });
  }
}


