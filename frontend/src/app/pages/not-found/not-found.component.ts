import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-not-found',
  standalone: true,
  imports: [RouterModule],
  template: `
    <div class="min-h-screen flex flex-col items-center justify-center bg-bg font-sans p-4">
      <h1 class="text-5xl font-extrabold text-ink mb-2">404</h1>
      <p class="text-slate mb-8">Page introuvable.</p>
      
      <div class="flex items-center gap-6 text-sm font-medium">
        <a routerLink="/careers" class="text-blue hover:underline">Voir les offres &rarr;</a>
        <a routerLink="/hr/dashboard" class="text-slate hover:text-ink">Espace RH</a>
      </div>
    </div>
  `,
})
export class NotFoundComponent {}

