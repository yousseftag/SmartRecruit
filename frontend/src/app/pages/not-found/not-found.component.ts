import { Component, inject } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { RouterModule } from '@angular/router';
import {
  LucideLayoutDashboard,
  LucideBriefcase,
  LucideArrowLeft,
  LucideLock,
  LucideFileQuestion,
} from '@lucide/angular';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-not-found',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    LucideLayoutDashboard,
    LucideBriefcase,
    LucideArrowLeft,
    LucideLock,
    LucideFileQuestion,
  ],
  template: `
    <div class="min-h-screen flex flex-col items-center justify-center bg-bg font-sans px-6 py-12">
      <!-- Card Container -->
      <div
        class="card max-w-lg w-full p-8 md:p-10 text-center shadow-lg border border-line bg-white flex flex-col items-center"
      >
        <!-- Icon Badge -->
        <div
          class="w-16 h-16 rounded-2xl bg-blue-100/50 flex items-center justify-center text-blue mb-5"
        >
          <svg lucideFileQuestion style="width: 32px; height: 32px;"></svg>
        </div>

        <h1 class="text-4xl md:text-5xl font-extrabold text-ink tracking-tight mb-2">404</h1>
        <h2 class="text-lg font-bold text-ink mb-3">Page introuvable</h2>

        <p class="text-xs md:text-sm text-slate mb-8 leading-relaxed max-w-sm">
          @if (isAuthenticated()) {
            L'élément ou la page demandée n'existe pas ou n'est plus accessible dans votre espace de
            travail.
          } @else {
            Oups ! L'adresse demandée est introuvable ou l'offre d'emploi recherchée a été clôturée.
          }
        </p>

        <!-- Context-Aware Actions -->
        <div class="flex flex-col sm:flex-row items-center gap-3 w-full justify-center">
          @if (isAuthenticated()) {
            <!-- Recruiter Actions -->
            <a
              routerLink="/hr/dashboard"
              class="btn btn-primary w-full sm:w-auto text-xs font-bold py-2.5 px-5 flex items-center justify-center gap-2"
            >
              <svg lucideLayoutDashboard style="width: 14px; height: 14px;"></svg>
              <span>Tableau de bord RH</span>
            </a>
            <a
              routerLink="/careers"
              class="btn btn-outline w-full sm:w-auto text-xs font-semibold py-2.5 px-4 flex items-center justify-center gap-2"
            >
              <svg lucideBriefcase style="width: 14px; height: 14px;"></svg>
              <span>Portail carrières</span>
            </a>
          } @else {
            <!-- Public Candidate Actions -->
            <a
              routerLink="/careers"
              class="btn btn-primary w-full sm:w-auto text-xs font-bold py-2.5 px-5 flex items-center justify-center gap-2"
            >
              <svg lucideBriefcase style="width: 14px; height: 14px;"></svg>
              <span>Découvrir nos offres d'emploi</span>
            </a>
            <button
              (click)="goBack()"
              type="button"
              class="btn btn-outline w-full sm:w-auto text-xs font-semibold py-2.5 px-4 flex items-center justify-center gap-1.5"
            >
              <svg lucideArrowLeft style="width: 14px; height: 14px;"></svg>
              <span>Page précédente</span>
            </button>
          }
        </div>

        @if (!isAuthenticated()) {
          <!-- Discreet RH access link for unauthenticated visitors -->
          <div class="mt-8 pt-6 border-t border-line w-full text-center">
            <a
              routerLink="/hr/dashboard"
              class="inline-flex items-center gap-1.5 text-[11px] text-faint hover:text-slate transition-colors"
            >
              <svg lucideLock style="width: 12px; height: 12px;"></svg>
              <span>Vous faites partie de l'équipe RH ? Accéder à l'espace de gestion</span>
            </a>
          </div>
        }
      </div>
    </div>
  `,
})
export class NotFoundComponent {
  private authService = inject(AuthService);
  private location = inject(Location);

  readonly isAuthenticated = this.authService.isAuthenticated;

  goBack(): void {
    this.location.back();
  }
}
