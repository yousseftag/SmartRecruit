import { Component, OnInit, signal, computed, HostListener, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, NavigationEnd, Event as RouterEvent } from '@angular/router';
import { filter } from 'rxjs/operators';
import {
  LucideUser,
  LucideMoon,
  LucideLogOut,
  LucideKey,
  LucideCircleCheck,
  LucideCircleHelp,
} from '@lucide/angular';
import { AuthService } from '../../core/auth/auth.service';
import { EditProfile } from '../../pages/edit-profile/edit-profile';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [
    CommonModule,
    EditProfile,
    LucideUser,
    LucideMoon,
    LucideLogOut,
    LucideKey,
    LucideCircleCheck,
    LucideCircleHelp,
  ],
  templateUrl: './header.html',
})
export class Header implements OnInit {
  private authService = inject(AuthService);
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
  readonly isDarkMode = signal<boolean>(false);
  readonly pageTitle = signal<string>('Tableau de bord');

  // Profile Modal State
  readonly isProfileModalOpen = signal<boolean>(false);
  readonly profileSuccessMessage = signal<string>('');

  ngOnInit() {
    this.setupRouterListener();
    this.authService.profileUpdated.subscribe(() => {
      this.authService.syncAuthState();
    });

    const savedMode = localStorage.getItem('darkMode');
    if (savedMode === 'true') {
      this.isDarkMode.set(true);
      document.documentElement.classList.add('dark');
    }
  }

  // Subscribes to router events to dynamically update the page title based on the current active route.
  private setupRouterListener() {
    const routeTitles: Record<string, string> = {
      '/hr/dashboard': 'Tableau de bord',
      '/hr/offers': 'Offres',
      '/hr/candidates/import': 'Importation de CVs',
      '/hr/candidates': 'Candidats',
      '/hr/workflow': 'Workflow',
      '/hr/reporting': 'Reporting',
      '/hr/administration': 'Administration',
      '/hr/settings': 'Paramètres',
    };

    this.updateTitle(this.router.url, routeTitles);

    this.router.events
      .pipe(filter((event: RouterEvent): event is NavigationEnd => event instanceof NavigationEnd))
      .subscribe((event: NavigationEnd) => {
        this.updateTitle(event.urlAfterRedirects, routeTitles);
      });
  }

  private updateTitle(url: string, routeTitles: Record<string, string>) {
    const matchingRoute = Object.keys(routeTitles).find((route) => url.startsWith(route));
    this.pageTitle.set(matchingRoute ? routeTitles[matchingRoute] : 'Tableau de bord');
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
    this.isDarkMode.update((val) => !val);

    if (this.isDarkMode()) {
      document.documentElement.classList.add('dark');
      localStorage.setItem('darkMode', 'true');
    } else {
      document.documentElement.classList.remove('dark');
      localStorage.setItem('darkMode', 'false');
    }
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
