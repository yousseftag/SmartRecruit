import { Component, OnInit, signal, HostListener, inject, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, NavigationEnd, Event as RouterEvent } from '@angular/router';
import { filter } from 'rxjs/operators';
import { createIcons, User, Moon, HelpCircle, LogOut, Key } from 'lucide';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './header.html',
})
export class Header implements OnInit, AfterViewInit {
  private authService = inject(AuthService);
  private router = inject(Router);

  // User Profile State
  userName = signal<string>('');
  userInitials = signal<string>('U');
  userEmail = signal<string>('');
  userRole = signal<string>('');

  // UI State
  isDropdownOpen = signal<boolean>(false);
  isDarkMode = signal<boolean>(false);
  pageTitle = signal<string>('Tableau de bord');

  ngOnInit() {
    this.setupRouterListener();
    this.loadUserData();
  }

  ngAfterViewInit() {
    this.initIcons();
  }

  // Subscribes to router events to dynamically update the page title based on the current active route.
  private setupRouterListener() {
    const routeTitles: Record<string, string> = {
      '/dashboard': 'Tableau de bord',
      '/offers': 'Offres',
      '/candidates': 'Candidats',
      '/workflow': 'Workflow',
      '/reporting': 'Reporting',
      '/administration': 'Administration',
      '/settings': 'Paramètres',
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

  // Extracts user information from the parsed Keycloak token to populate the UI.
  private loadUserData() {
    const profile = this.authService.getUserProfile();

    if (profile) {
      this.userName.set(profile.fullName || profile.preferredUsername || 'Utilisateur');
      this.userEmail.set(profile.email || 'Aucun email renseigné');

      let initials = 'U';
      if (profile.firstName && profile.lastName) {
        initials =
          profile.firstName.charAt(0).toUpperCase() + profile.lastName.charAt(0).toUpperCase();
      } else if (profile.fullName) {
        initials = profile.fullName.substring(0, 2).toUpperCase();
      }
      this.userInitials.set(initials);

      if (this.authService.hasRole('HR_ADMIN')) {
        this.userRole.set('Admin RH');
      } else if (this.authService.hasRole('RECRUITER')) {
        this.userRole.set('Recruteur');
      } else {
        this.userRole.set('Consultation');
      }
    }
  }

  // Initializes Lucide icons used within the component template.

  private initIcons() {
    createIcons({
      icons: { User, Moon, HelpCircle, LogOut, Key },
    });
  }

  toggleDropdown(event: Event) {
    event.stopPropagation();
    this.isDropdownOpen.update((val) => !val);

    if (this.isDropdownOpen()) {
      setTimeout(() => this.initIcons(), 0);
    }
  }

  @HostListener('document:click')
  closeDropdown() {
    if (this.isDropdownOpen()) {
      this.isDropdownOpen.set(false);
    }
  }

  // Toggles the global dark mode class on the HTML document element.
  toggleDarkMode(event: Event) {
    event.stopPropagation();
    this.isDarkMode.update((val) => !val);

    if (this.isDarkMode()) {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  }

  manageAccount() {
    this.authService.manageAccount();
  }

  changePassword() {
    this.authService.changePassword();
  }

  async logout() {
    await this.authService.logout();
  }
}
