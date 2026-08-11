import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import {
  LucideDynamicIcon,
  LucideLayoutDashboard,
  LucideBriefcase,
  LucideUsers,
  LucideGitMerge,
  LucideChartColumnBig,
  LucideShieldCheck,
  LucideSettings,
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

  isAdmin = this.authService.isAdmin;

  navItems: NavItem[] = [
    {
      label: 'Tableau de bord',
      route: '/dashboard',
      icon: LucideLayoutDashboard,
      requiresAdmin: false,
    },
    { label: 'Offres', route: '/offers', icon: LucideBriefcase, requiresAdmin: false },
    { label: 'Candidats', route: '/candidates', icon: LucideUsers, requiresAdmin: false },
    { label: 'Workflow', route: '/workflow', icon: LucideGitMerge, requiresAdmin: false },
    { label: 'Reporting', route: '/reporting', icon: LucideChartColumnBig, requiresAdmin: false },
    {
      label: 'Administration',
      route: '/administration',
      icon: LucideShieldCheck,
      requiresAdmin: true,
    },
    { label: 'Paramètres', route: '/settings/general', icon: LucideSettings, requiresAdmin: true },
  ];
}
