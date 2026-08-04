import { Component, AfterViewInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { createIcons, icons } from 'lucide';
import { NavItem } from './models/nav-item.model';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar.html',
})
export class Sidebar implements AfterViewInit {
  private authService = inject(AuthService);

  isAdmin = this.authService.isAdmin;

  navItems: NavItem[] = [
    {
      label: 'Tableau de bord',
      route: '/dashboard',
      icon: 'layout-dashboard',
      requiresAdmin: false,
    },
    { label: 'Offres', route: '/offers', icon: 'briefcase', requiresAdmin: false },
    { label: 'Candidats', route: '/candidates', icon: 'users', requiresAdmin: false },
    { label: 'Workflow', route: '/workflow', icon: 'git-merge', requiresAdmin: false },
    { label: 'Reporting', route: '/reporting', icon: 'bar-chart-2', requiresAdmin: false },
    {
      label: 'Administration',
      route: '/administration',
      icon: 'shield-check',
      requiresAdmin: true,
    },
    { label: 'Paramètres', route: '/settings/general', icon: 'settings', requiresAdmin: true },
  ];

  ngAfterViewInit() {
    createIcons({ icons: icons as any });
  }
}
