import { Component, OnInit, signal, computed, HostListener, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule, NavigationEnd, Event as RouterEvent } from '@angular/router';
import { filter } from 'rxjs/operators';
import {
  LucideUser,
  LucideMoon,
  LucideLogOut,
  LucideKey,
  LucideCircleCheck,
  LucideGlobe,
  LucideExternalLink,
} from '@lucide/angular';
import { AuthService } from '../../core/auth/auth.service';
import { ThemeService } from '../../core/services/theme.service';
import { EditProfile } from '../../pages/edit-profile/edit-profile';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    EditProfile,
    LucideUser,
    LucideMoon,
    LucideLogOut,
    LucideKey,
    LucideCircleCheck,
    LucideGlobe,
    LucideExternalLink,
  ],
  templateUrl: './header.html',
})
export class Header implements OnInit {
  private authService = inject(AuthService);
  private themeService = inject(ThemeService);
  private router = inject(Router);

  // User Profile Reactive Signals
  readonly userName = computed(() => {
    const user = this.authService.currentUser();
    return user ? user.fullName || user.preferredUsername || 'Utilisateur' : 'Utilisateur';
  });

  readonly userEmail = computed(
    () => this.authService.currentUser()?.email || 'Aucun email renseigné',
  );

  readonly userInitials = computed(() => {
    const user = this.authService.currentUser();
    if (!user) return 'U';
    if (user.firstName && user.lastName) {
      return (user.firstName.charAt(0) + user.lastName.charAt(0)).toUpperCase();
    }
    if (user.fullName) {
      return user.fullName.substring(0, 2).toUpperCase();
    }
    return 'U';
  });

  readonly userRole = computed(() => {
    if (this.authService.isAdmin()) return 'Admin RH';
    if (this.authService.isRecruiter()) return 'Recruteur';
    return 'Consultation';
  });

  // UI State
  readonly isDropdownOpen = signal<boolean>(false);
  readonly isDarkMode = this.themeService.isDarkMode;
  readonly pageTitle = signal<string>('Tableau de bord');

  // Profile Modal State
  readonly isProfileModalOpen = signal<boolean>(false);
  readonly profileSuccessMessage = signal<string>('');

  ngOnInit() {
    this.setupRouterListener();
    this.authService.profileUpdated.subscribe(() => {
      this.authService.syncAuthState();
    });
  }

  // Subscribes to router events to dynamically update the page title based on the current active route.
  private setupRouterListener() {
    this.updateTitle(this.router.url);

    this.router.events
      .pipe(filter((event: RouterEvent): event is NavigationEnd => event instanceof NavigationEnd))
      .subscribe((event: NavigationEnd) => {
        this.updateTitle(event.urlAfterRedirects);
      });
  }

  private updateTitle(url: string) {
    const cleanUrl = url.split('?')[0].split('#')[0];

    if (cleanUrl === '/hr/offers/new') {
      this.pageTitle.set("Créer une offre d'emploi");
      return;
    }
    if (/^\/hr\/offers\/[^/]+\/edit$/.test(cleanUrl)) {
      this.pageTitle.set("Modifier l'offre d'emploi");
      return;
    }
    if (/^\/hr\/offers\/[^/]+$/.test(cleanUrl)) {
      this.pageTitle.set("Détails de l'offre d'emploi");
      return;
    }
    if (cleanUrl === '/hr/offers') {
      this.pageTitle.set("Offres d'emploi");
      return;
    }
    if (cleanUrl === '/hr/candidates/import') {
      this.pageTitle.set('Importer des CVs');
      return;
    }
    if (/^\/hr\/candidates\/[^/]+$/.test(cleanUrl)) {
      this.pageTitle.set('Profil du candidat');
      return;
    }
    if (cleanUrl === '/hr/candidates') {
      this.pageTitle.set('Gestion des candidatures');
      return;
    }
    if (cleanUrl === '/hr/workflow') {
      this.pageTitle.set('Workflow de recrutement');
      return;
    }
    if (cleanUrl === '/hr/reporting') {
      this.pageTitle.set('Rapports & Statistiques');
      return;
    }
    if (cleanUrl === '/hr/settings/templates') {
      this.pageTitle.set("Modèles d'emails");
      return;
    }
    if (cleanUrl === '/hr/administration') {
      this.pageTitle.set('Gestion des utilisateurs');
      return;
    }
    if (cleanUrl === '/hr/dashboard' || cleanUrl === '/hr' || cleanUrl === '') {
      this.pageTitle.set('Tableau de bord');
      return;
    }

    this.pageTitle.set('Tableau de bord');
  }

  toggleDropdown(event: Event) {
    event.stopPropagation();
    this.isDropdownOpen.update((val) => !val);
  }

  @HostListener('document:click')
  closeDropdown() {
    if (this.isDropdownOpen()) {
      this.isDropdownOpen.set(false);
    }
  }

  toggleDarkMode(event: Event) {
    event.stopPropagation();
    this.themeService.toggleDarkMode();
  }

  manageAccount() {
    this.isDropdownOpen.set(false);
    this.isProfileModalOpen.set(true);
  }

  onProfileModalClose(success: boolean) {
    this.isProfileModalOpen.set(false);
    if (success) {
      this.profileSuccessMessage.set('Profil mis à jour avec succès');

      setTimeout(() => {
        this.profileSuccessMessage.set('');
      }, 5000);
    }
  }

  changePassword() {
    this.authService.changePassword();
  }

  logout() {
    this.authService.logout().subscribe();
  }
}
