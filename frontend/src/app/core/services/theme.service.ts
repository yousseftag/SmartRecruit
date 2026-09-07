import { Injectable, computed, inject, signal } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class ThemeService {
  private router = inject(Router);

  private readonly isDarkPreference = signal<boolean>(false);
  readonly isDarkMode = computed(() => this.isDarkPreference());

  constructor() {
    const saved = localStorage.getItem('darkMode') === 'true';
    this.isDarkPreference.set(saved);

    // Synchronize theme on route changes
    this.router.events
      .pipe(filter((event): event is NavigationEnd => event instanceof NavigationEnd))
      .subscribe((event: NavigationEnd) => {
        this.applyTheme(event.urlAfterRedirects);
      });

    // Initial theme application based on current window location
    this.applyTheme(window.location.pathname);
  }

  toggleDarkMode(): void {
    const nextState = !this.isDarkPreference();
    this.isDarkPreference.set(nextState);
    localStorage.setItem('darkMode', String(nextState));
    this.applyTheme(this.router.url || window.location.pathname);
  }

  applyTheme(currentUrl: string): void {
    const cleanUrl = (currentUrl || '').split('?')[0].split('#')[0];
    const isPublicCareers = cleanUrl.startsWith('/careers');

    if (isPublicCareers) {
      // The public careers portal always remains in crisp corporate light mode
      document.documentElement.classList.remove('dark');
    } else if (this.isDarkPreference()) {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  }
}
