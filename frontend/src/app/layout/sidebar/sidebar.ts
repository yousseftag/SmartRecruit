import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import {
  LucideDynamicIcon,
  LucideLayoutDashboard,
  LucideBriefcase,
  LucideUsers,
  LucideGitMerge,
  LucideChartColumnBig,
  LucideShieldCheck,
  LucideSettings,
  LucideUpload,
} from '@lucide/angular';
import { NavItem } from './models/nav-item.model';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule, LucideDynamicIcon],
  templateUrl: './sidebar.html',
})
export class Sidebar {
  private authService = inject(AuthService);
  private router = inject(Router);

  hasAccess(roles?: string[]): boolean {
    if (!roles || roles.length === 0) return true;
    return roles.some((role) => this.authService.hasRole(role));
  }

  navItems: NavItem[] = [
    { label: 'Tableau de bord', route: '/hr/dashboard', icon: LucideLayoutDashboard },
    { label: 'Offres', route: '/hr/offers', icon: LucideBriefcase },
    {
      label: 'Importer des CVs',
      route: '/hr/candidates/import',
      icon: LucideUpload,
      roles: ['HR_ADMIN', 'RECRUITER'],
    },
    { label: 'Candidats', route: '/hr/candidates', icon: LucideUsers },
    { label: 'Workflow', route: '/hr/workflow', icon: LucideGitMerge },
    { label: 'Reporting', route: '/hr/reporting', icon: LucideChartColumnBig },
    {
      label: 'Administration',
      route: '/hr/administration',
      icon: LucideShieldCheck,
      roles: ['HR_ADMIN'],
    },
    {
      label: 'Paramètres',
      route: '/hr/settings/general',
      icon: LucideSettings,
      roles: ['HR_ADMIN'],
    },
  ];

  isRouteActive(route: string): boolean {
    const current = this.router.url.split('?')[0];
    if (route === '/hr/candidates/import') {
      return current.startsWith('/hr/candidates/import');
    }
    if (route === '/hr/candidates') {
      return (
        current.startsWith('/hr/candidates') &&
        !current.startsWith('/hr/candidates/import')
      );
    }
    if (route === '/hr/dashboard') {
      return current === '/hr/dashboard' || current === '/hr';
    }
    return current.startsWith(route);
  }
}

